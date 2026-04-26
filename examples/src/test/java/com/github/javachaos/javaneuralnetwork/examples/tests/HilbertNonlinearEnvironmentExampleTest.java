package com.github.javachaos.javaneuralnetwork.examples.tests;

import com.github.javachaos.javaneuralnetwork.examples.HilbertNonlinearEnvironmentExample;
import com.github.javachaos.javaneuralnetwork.examples.HilbertNonlinearEnvironmentExample.NonlinearSimulationResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HilbertNonlinearEnvironmentExampleTest {

    private static final double EPSILON = 1.0e-7;

    @Test
    final void testQuadraticLiftLearnsNonlinearEnvironmentBetterThanRawLinearState() {
        NonlinearSimulationResult result = HilbertNonlinearEnvironmentExample.runSimulation();

        assertEquals(10, result.trainingSamples());
        assertEquals(4, result.validationSamples());
        assertEquals(4, result.rawDimension());
        assertEquals(10, result.liftedDimension());
        assertTrue(result.rawInitialTrainingMse() > result.rawFinalTrainingMse());
        assertTrue(result.rawInitialValidationMse() > result.rawFinalValidationMse());
        assertTrue(result.rawFinalValidationMse() > 0.005);
        assertTrue(result.rawMixedErrorSquared() > 0.02);
        assertTrue(result.liftedInitialTrainingMse() > result.liftedFinalTrainingMse());
        assertTrue(result.liftedInitialValidationMse() > result.liftedFinalValidationMse());
        assertEquals(0.0, result.liftedFinalTrainingMse(), EPSILON);
        assertEquals(0.0, result.liftedFinalValidationMse(), EPSILON);
        assertEquals(0.0, result.liftedMixedErrorSquared(), EPSILON);
        assertTrue(result.liftedFinalValidationMse() * 1_000.0 < result.rawFinalValidationMse());
        assertArrayEquals(result.mixedExpected().state().toArray(),
                result.liftedMixedPrediction().state().toArray(), EPSILON);
    }

    @Test
    final void testSimulationMainRuns() {
        HilbertNonlinearEnvironmentExample.main(new String[0]);
    }
}
