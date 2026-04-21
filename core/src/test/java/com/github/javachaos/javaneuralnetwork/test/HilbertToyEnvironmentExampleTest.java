package com.github.javachaos.javaneuralnetwork.test;

import com.github.javachaos.javaneuralnetwork.core.HilbertToyEnvironmentExample;
import com.github.javachaos.javaneuralnetwork.core.HilbertToyEnvironmentExample.SimulationResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HilbertToyEnvironmentExampleTest {

    private static final double EPSILON = 1.0e-9;

    @Test
    final void testSimulationLearnsEnvironmentAndGeneralizesToHoldoutMixtures() {
        SimulationResult result = HilbertToyEnvironmentExample.runSimulation();

        assertEquals(4, result.trainingSamples());
        assertEquals(5, result.validationSamples());
        assertTrue(result.initialTrainingMse() > result.finalTrainingMse());
        assertTrue(result.initialValidationMse() > result.finalValidationMse());
        assertEquals(0.0, result.finalTrainingMse(), EPSILON);
        assertEquals(0.0, result.finalValidationMse(), EPSILON);
        assertArrayEquals(result.mixedExpected().state().toArray(),
                result.mixedPrediction().state().toArray(), EPSILON);
        assertEquals(0.5, result.mixedDangerRelevance(), EPSILON);
        assertEquals(0.5, result.mixedCuriosityRelevance(), EPSILON);
    }

    @Test
    final void testSimulationMainRuns() {
        HilbertToyEnvironmentExample.main(new String[0]);
    }
}
