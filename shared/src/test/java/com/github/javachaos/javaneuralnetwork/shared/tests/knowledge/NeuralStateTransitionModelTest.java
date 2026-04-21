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

import java.util.List;
import java.util.function.Function;

import com.github.javachaos.javaneuralnetwork.shared.hilbert.HilbertVector;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.NeuralStateTransitionModel;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.StatePrediction;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.WorldState;
import com.github.javachaos.javaneuralnetwork.shared.layers.HiddenLayer;
import com.github.javachaos.javaneuralnetwork.shared.layers.InputLayer;
import com.github.javachaos.javaneuralnetwork.shared.layers.OutputLayer;
import com.github.javachaos.javaneuralnetwork.shared.network.Network;
import com.github.javachaos.javaneuralnetwork.shared.neurons.InputNeuron;
import com.github.javachaos.javaneuralnetwork.shared.neurons.Neuron;
import com.github.javachaos.javaneuralnetwork.shared.neurons.OutputNeuron;
import com.github.javachaos.javaneuralnetwork.shared.training.TrainType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NeuralStateTransitionModelTest {

    private static final double EPSILON = 1.0e-10;

    @Test
    final void testPredictsWorldStateFromNetwork() {
        NeuralStateTransitionModel model = NeuralStateTransitionModel.of(
                "blend",
                new StubNetwork(input -> List.of(input.get(0) + input.get(1), input.get(0) - input.get(1))),
                2,
                2);

        WorldState prediction = model.predict(WorldState.of("cue", HilbertVector.of(3.0, 1.0)));

        assertEquals("blend", model.name());
        assertEquals(2, model.inputDimension());
        assertEquals(2, model.outputDimension());
        assertEquals("cue -> blend", prediction.name());
        assertArrayEquals(new double[]{4.0, 2.0}, prediction.state().toArray(), EPSILON);
    }

    @Test
    final void testForecastAndCompareUseNetworkPrediction() {
        NeuralStateTransitionModel model = NeuralStateTransitionModel.of(
                "double",
                new StubNetwork(input -> List.of(input.get(0) * 2.0, input.get(1) * 2.0)),
                2,
                2);
        WorldState before = WorldState.of("before", HilbertVector.of(1.0, 2.0));
        WorldState observed = WorldState.of("observed", HilbertVector.of(2.0, 5.0));

        StatePrediction forecast = model.forecast("next", before);
        StatePrediction compared = model.compare("observed-next", before, observed);

        assertTrue(model.canPredict(before));
        assertArrayEquals(new double[]{2.0, 4.0}, forecast.predicted().state().toArray(), EPSILON);
        assertArrayEquals(new double[]{0.0, 1.0}, compared.residual().orElseThrow().toArray(), EPSILON);
        assertEquals(1.0, compared.errorSquared(), EPSILON);
    }

    @Test
    final void testRejectsInvalidNetworkPredictions() {
        NeuralStateTransitionModel model = NeuralStateTransitionModel.of(
                "bad-output",
                new StubNetwork(input -> List.of(input.get(0))),
                2,
                2);
        WorldState input = WorldState.of("input", HilbertVector.of(1.0, 2.0));

        assertThrows(NullPointerException.class,
                () -> NeuralStateTransitionModel.of("bad", null, 1, 1));
        assertThrows(IllegalArgumentException.class,
                () -> NeuralStateTransitionModel.of(" ", new StubNetwork(List::copyOf), 1, 1));
        assertThrows(IllegalArgumentException.class,
                () -> NeuralStateTransitionModel.of("bad", new StubNetwork(List::copyOf), 0, 1));
        assertThrows(IllegalArgumentException.class,
                () -> model.predict(WorldState.of("wrong", HilbertVector.of(1.0))));
        assertThrows(IllegalStateException.class, () -> model.predict(input));
    }

    private static final class StubNetwork implements Network {

        private final Function<List<Double>, List<Double>> function;

        private StubNetwork(final Function<List<Double>, List<Double>> function) {
            this.function = function;
        }

        @Override
        public List<Double> runInputs(final List<Double> inputLayer) {
            return function.apply(inputLayer);
        }

        @Override
        public void reset() {
        }

        @Override
        public void addHiddenLayer(final HiddenLayer l) {
        }

        @Override
        public HiddenLayer getHiddenLayer(final int i) {
            return null;
        }

        @Override
        public OutputLayer getOutputLayer() {
            return null;
        }

        @Override
        public void setOutputLayer(final OutputLayer l) {
        }

        @Override
        public Neuron getNeuron(final int x, final int y) {
            return null;
        }

        @Override
        public int getHeight() {
            return 0;
        }

        @Override
        public OutputNeuron getOutputNeuron(final int x) {
            return null;
        }

        @Override
        public InputNeuron getInputNeuron(final int x) {
            return null;
        }

        @Override
        public InputLayer getInputLayer() {
            return null;
        }

        @Override
        public void setInputLayer(final InputLayer l) {
        }

        @Override
        public void setTrainingAlgorithm(final TrainType t) {
        }

        @Override
        public void build() {
        }
    }
}
