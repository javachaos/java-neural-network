package com.github.javachaos.javaneuralnetwork.examples.tests;

import com.github.javachaos.javaneuralnetwork.examples.HilbertLanguageLearningExample;
import com.github.javachaos.javaneuralnetwork.examples.HilbertLanguageLearningExample.LanguageDemoResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HilbertLanguageLearningExampleTest {

    @Test
    final void testLanguageExampleLearnsPhraseTransitions() {
        LanguageDemoResult result = HilbertLanguageLearningExample.run();

        assertEquals(4, result.learner().updates());
        assertEquals("it is warm", result.rankedPhrases().get(0).phrase());
        assertTrue(result.answerAlignmentAfter() > result.answerAlignmentBefore());
        assertTrue(result.semanticSpace().coordinate(result.predictedAnswer(), "warm") > 0.0);
    }

    @Test
    final void testLanguageExampleLearnsNegationOperator() {
        LanguageDemoResult result = HilbertLanguageLearningExample.run();

        assertTrue(result.warmAlignmentBefore() > 0.99);
        assertTrue(result.warmAlignmentAfter() < -0.99);
    }
}
