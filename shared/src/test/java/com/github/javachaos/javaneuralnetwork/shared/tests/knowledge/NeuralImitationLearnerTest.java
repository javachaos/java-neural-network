/*******************************************************************************
 * Copyright (c) 2014 Fred .
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *     Fred  - initial API and implementation
 ******************************************************************************/
package com.github.javachaos.javaneuralnetwork.shared.tests.knowledge;

import com.github.javachaos.javaneuralnetwork.shared.hilbert.HilbertVector;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.HilbertImitationLearner;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.NeuralImitationLearner;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.StatePrediction;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.StateTransitionModel;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.WorldState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NeuralImitationLearnerTest {

    private static final double EPSILON = 1.0e-10;

    @Test
    final void testBlendsNeuralPredictionWithImitationMemory() {
        HilbertImitationLearner memory = HilbertImitationLearner.of("memory", 2, 1.0, 0.1);
        memory.observe(state("before", 0.0, 0.0), state("after", 1.0, 0.0));
        NeuralImitationLearner learner = NeuralImitationLearner.predictOnly(
                "hybrid", memory, new OffsetModel("offset", 0.0, 2.0), 1.0);

        NeuralImitationLearner.HybridPrediction prediction =
                learner.forecastDetailed(state("cue", 0.0, 0.0));

        assertTrue(prediction.imitation().isPresent());
        assertTrue(prediction.modelPrediction().isPresent());
        assertEquals(1.0, prediction.imitationWeight(), EPSILON);
        assertEquals(1.0, prediction.neuralWeight(), EPSILON);
        assertArrayEquals(new double[]{0.5, 1.0}, prediction.predicted().state().toArray(), EPSILON);
        assertArrayEquals(prediction.predicted().state().toArray(),
                learner.predict(state("cue", 0.0, 0.0)).state().toArray(), EPSILON);
    }

    @Test
    final void testObserveUpdatesMemoryAndTrainer() {
        RecordingModel model = new RecordingModel("recorder");
        HilbertImitationLearner memory = HilbertImitationLearner.of("memory", 2, 1.0, 0.1);
        NeuralImitationLearner learner = NeuralImitationLearner.of(
                "hybrid", memory, model, model::observe, 0.5);
        WorldState before = state("before", 0.0, 0.0);
        WorldState after = state("after", 1.0, 1.0);

        NeuralImitationLearner.LearningStep step = learner.observe(before, after);

        assertTrue(step.prediction().isPresent());
        assertTrue(step.neuralLearning().isPresent());
        assertEquals(1, model.trainingCalls());
        assertEquals(1, learner.observations());
        assertEquals(2, memory.prototypeCount());
        assertTrue(learner.canPredict(before));
    }

    @Test
    final void testUsesNeuralModelUntilMemoryHasTransitions() {
        HilbertImitationLearner memory = HilbertImitationLearner.of("memory", 2, 1.0, 0.1);
        NeuralImitationLearner learner = NeuralImitationLearner.predictOnly(
                "hybrid", memory, new OffsetModel("offset", 0.5, -0.5), 0.25);

        NeuralImitationLearner.HybridPrediction prediction =
                learner.forecastDetailed(state("cue", 1.0, 1.0));

        assertFalse(prediction.imitation().isPresent());
        assertTrue(prediction.modelPrediction().isPresent());
        assertEquals(0.0, prediction.imitationWeight(), EPSILON);
        assertEquals(0.25, prediction.neuralWeight(), EPSILON);
        assertArrayEquals(new double[]{1.5, 0.5}, prediction.predicted().state().toArray(), EPSILON);
    }

    @Test
    final void testRejectsInvalidHybridRequests() {
        HilbertImitationLearner memory = HilbertImitationLearner.of("memory", 2, 1.0, 0.1);
        StateTransitionModel model = new OffsetModel("offset", 0.0, 0.0);
        WorldState valid = state("valid", 0.0, 0.0);
        WorldState wrong = WorldState.of("wrong", HilbertVector.of(1.0, 0.0, 0.0));

        assertThrows(NullPointerException.class,
                () -> NeuralImitationLearner.of("bad", memory, model, null, 1.0));
        assertThrows(IllegalArgumentException.class,
                () -> NeuralImitationLearner.predictOnly(" ", memory, model, 1.0));
        assertThrows(IllegalArgumentException.class,
                () -> NeuralImitationLearner.predictOnly("bad", memory, model, 0.0));
        assertThrows(IllegalArgumentException.class,
                () -> NeuralImitationLearner.predictOnly("bad", memory,
                        new FixedDimensionModel("wide", 2, 3), 1.0));
        NeuralImitationLearner learner =
                NeuralImitationLearner.predictOnly("hybrid", memory, model, 1.0);
        assertThrows(IllegalArgumentException.class, () -> learner.predict(wrong));
        assertThrows(IllegalArgumentException.class, () -> learner.observe(valid, wrong));
    }

    private WorldState state(final String name, final double x, final double y) {
        return WorldState.of(name, HilbertVector.of(x, y));
    }

    private static final class OffsetModel implements StateTransitionModel {
        private final String name;
        private final double xOffset;
        private final double yOffset;

        private OffsetModel(final String name, final double xOffset, final double yOffset) {
            this.name = name;
            this.xOffset = xOffset;
            this.yOffset = yOffset;
        }

        @Override
        public WorldState predict(final WorldState state) {
            return WorldState.of(state.name() + " -> " + name,
                    HilbertVector.of(state.state().get(0) + xOffset, state.state().get(1) + yOffset));
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public int inputDimension() {
            return 2;
        }

        @Override
        public int outputDimension() {
            return 2;
        }
    }

    private static final class RecordingModel implements StateTransitionModel {
        private final String name;
        private int trainingCalls;

        private RecordingModel(final String name) {
            this.name = name;
        }

        private StatePrediction observe(
                final String predictionName,
                final WorldState before,
                final WorldState observed) {
            StatePrediction prediction = compare(predictionName, before, observed);
            trainingCalls++;
            return prediction;
        }

        private int trainingCalls() {
            return trainingCalls;
        }

        @Override
        public WorldState predict(final WorldState state) {
            return WorldState.of(state.name() + " -> " + name, state.state());
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public int inputDimension() {
            return 2;
        }

        @Override
        public int outputDimension() {
            return 2;
        }
    }

    private static final class FixedDimensionModel implements StateTransitionModel {
        private final String name;
        private final int inputDimension;
        private final int outputDimension;

        private FixedDimensionModel(final String name, final int inputDimension, final int outputDimension) {
            this.name = name;
            this.inputDimension = inputDimension;
            this.outputDimension = outputDimension;
        }

        @Override
        public WorldState predict(final WorldState state) {
            return WorldState.of(state.state());
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public int inputDimension() {
            return inputDimension;
        }

        @Override
        public int outputDimension() {
            return outputDimension;
        }
    }
}
