package com.github.javachaos.javaneuralnetwork.test;

import com.github.javachaos.javaneuralnetwork.core.BabyWorldDevelopmentExample;
import com.github.javachaos.javaneuralnetwork.core.BabyWorldDevelopmentExample.BabyWorldResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BabyWorldDevelopmentExampleTest {

    @Test
    final void testDevelopmentalLearnerReducesSurpriseAndPredictsCycle() {
        BabyWorldResult result = BabyWorldDevelopmentExample.runSimulation();

        assertEquals(192, result.observations());
        assertEquals(191, result.traces());
        assertEquals(4, result.prototypes());
        assertTrue(result.firstPredictedSurprise() > result.finalMeanSurprise());
        assertTrue(result.finalMeanSurprise() < 0.20);
        assertTrue(result.nextErrorSquared() < 0.08);
        assertTrue(result.prototypeCompressionRatio() < 0.03);
        assertTrue(result.predictedVisibleRelevance() > result.predictedReturnedRelevance());
    }

    @Test
    final void testSimulationMainRuns() {
        BabyWorldDevelopmentExample.main(new String[0]);
    }
}
