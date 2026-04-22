package com.github.javachaos.javaneuralnetwork.test;

import java.util.Arrays;

import com.github.javachaos.javaneuralnetwork.core.BackpropagationNetwork;
import com.github.javachaos.javaneuralnetwork.core.BackpropagationStateTransitionModel;
import com.github.javachaos.javaneuralnetwork.core.TransferFunctions;
import com.github.javachaos.javaneuralnetwork.shared.hilbert.HilbertVector;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.HilbertImitationLearner;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.NeuralImitationLearner;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.StatePrediction;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.WorldState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BackpropagationStateTransitionModelTest {

    @Test
    final void testPredictsWorldStateWithCoreNetwork() {
        BackpropagationStateTransitionModel model = model();
        WorldState input = WorldState.of("cue", HilbertVector.of(1.0, 0.0));

        WorldState prediction = model.predict(input);

        assertEquals("core-transition", model.name());
        assertEquals(2, model.inputDimension());
        assertEquals(2, model.outputDimension());
        assertEquals("cue -> core-transition", prediction.name());
        assertEquals(2, prediction.dimension());
        assertTrue(Arrays.stream(prediction.state().toArray()).allMatch(Double::isFinite));
    }

    @Test
    final void testObserveComparesThenTrains() {
        BackpropagationStateTransitionModel model = model();
        WorldState before = WorldState.of("before", HilbertVector.of(1.0, 0.0));
        WorldState observed = WorldState.of("observed", HilbertVector.of(0.0, 1.0));

        StatePrediction prediction = model.observe("step", before, observed, 0.1, 0.0);
        double error = model.train(before, observed, 0.1, 0.0);

        assertTrue(prediction.hasObserved());
        assertEquals(2, prediction.predicted().dimension());
        assertTrue(Double.isFinite(prediction.errorNorm()));
        assertTrue(Double.isFinite(error));
    }

    @Test
    final void testBackpropagationModelCanTrainInsideNeuralImitationLoop() {
        BackpropagationStateTransitionModel model = model();
        HilbertImitationLearner memory = HilbertImitationLearner.of("prototype-memory", 2, 1.0, 0.1);
        NeuralImitationLearner learner = NeuralImitationLearner.of(
                "neural-imitation",
                memory,
                model,
                (predictionName, before, observed) -> model.observe(predictionName, before, observed, 0.2, 0.0),
                0.5);
        WorldState before = WorldState.of("before", HilbertVector.of(1.0, 0.0));
        WorldState observed = WorldState.of("observed", HilbertVector.of(0.0, 1.0));

        NeuralImitationLearner.LearningStep step = learner.observe(before, observed);
        NeuralImitationLearner.HybridPrediction prediction = learner.forecastDetailed(before);

        assertTrue(step.neuralLearning().isPresent());
        assertTrue(step.prediction().isPresent());
        assertTrue(prediction.imitation().isPresent());
        assertTrue(prediction.modelPrediction().isPresent());
        assertEquals(2, prediction.predicted().dimension());
        assertTrue(Arrays.stream(prediction.predicted().state().toArray()).allMatch(Double::isFinite));
    }

    @Test
    final void testRejectsInvalidStateTransitions() {
        BackpropagationStateTransitionModel model = model();
        WorldState before = WorldState.of("before", HilbertVector.of(1.0, 0.0));
        WorldState wrongInput = WorldState.of("wrong-input", HilbertVector.of(1.0, 0.0, 0.0));
        WorldState wrongOutput = WorldState.of("wrong-output", HilbertVector.of(1.0, 0.0, 0.0));

        assertThrows(NullPointerException.class,
                () -> BackpropagationStateTransitionModel.of("bad", null, 1, 1));
        assertThrows(IllegalArgumentException.class,
                () -> BackpropagationStateTransitionModel.of(" ", network(), 1, 1));
        assertThrows(IllegalArgumentException.class,
                () -> BackpropagationStateTransitionModel.of("bad", network(), 0, 1));
        assertThrows(IllegalArgumentException.class, () -> model.predict(wrongInput));
        assertThrows(IllegalArgumentException.class, () -> model.train(before, wrongOutput, 0.1, 0.0));
        assertThrows(IllegalArgumentException.class, () -> model.train(before, before, Double.NaN, 0.0));
    }

    private BackpropagationStateTransitionModel model() {
        return BackpropagationStateTransitionModel.of("core-transition", network(), 2, 2);
    }

    private BackpropagationNetwork network() {
        return new BackpropagationNetwork(
                new int[]{2, 2},
                new TransferFunctions.TransferFunction[]{
                        TransferFunctions.TransferFunction.NONE,
                        TransferFunctions.TransferFunction.SIGMOID
                });
    }
}
