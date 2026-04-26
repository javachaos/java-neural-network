package com.github.javachaos.javaneuralnetwork.examples.tests;

import com.github.javachaos.javaneuralnetwork.examples.HilbertTextTrainingExample;
import com.github.javachaos.javaneuralnetwork.examples.HilbertTextTrainingExample.TextTrainingDemoResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HilbertTextTrainingExampleTest {

    @Test
    final void testTextTrainingExampleLearnsFromPlainTextRows() {
        TextTrainingDemoResult result = HilbertTextTrainingExample.run();

        assertEquals(4, result.training().samples());
        assertEquals(4, result.training().learner().updates());
        assertEquals("it is warm", result.rankedPhrases().get(0).phrase());
        assertTrue(result.answerCoordinate() > 0.0);
        assertTrue(result.warmCoordinate() > 0.0);
        assertTrue(result.knownCoordinate() > 0.0);
    }
}
