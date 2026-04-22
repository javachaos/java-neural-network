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

import com.github.javachaos.javaneuralnetwork.shared.knowledge.LanguageState;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.LanguageTemporalLearner;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.LexicalSemanticSpace;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.TextLanguageTrainer;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.TextTrainingSample;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextLanguageTrainerTest {

    @Test
    final void testBuildsSemanticSpaceFromTextCorpus() {
        LexicalSemanticSpace space = TextLanguageTrainer.semanticSpace("text", samples());

        assertTrue(space.dimension() >= 8);
        assertTrue(space.vocabulary().contains("question"));
        assertTrue(space.vocabulary().contains("warm"));
        assertTrue(space.vocabulary().contains("know"));
        assertEquals(1.0, space.coordinate(space.encode("state", "answer warm known"), "answer"));
    }

    @Test
    final void testTrainsAndRanksCandidatePhrasesFromPlainText() {
        TextLanguageTrainer.TrainingResult result =
                TextLanguageTrainer.train("text", samples(), 1.0);

        List<LanguageTemporalLearner.PhraseScore> ranked = result.rankToward(
                "weather question unknown",
                List.of("i do not know", "it is cold", "it is warm"),
                "answer");

        assertEquals(samples().size(), result.samples());
        assertEquals(samples().size(), result.learner().updates());
        assertEquals("it is warm", ranked.get(0).phrase());
        assertTrue(ranked.get(0).expectedReward() > ranked.get(1).expectedReward());
        assertTrue(ranked.get(0).goalProgress() >= ranked.get(1).goalProgress());
    }

    @Test
    final void testPredictsFromPlainTextContext() {
        TextLanguageTrainer.TrainingResult result =
                TextLanguageTrainer.train("text", samples(), 1.0);

        LanguageState predicted = result.predict("weather question unknown", "it is warm");

        assertTrue(result.semanticSpace().coordinate(predicted, "answer") > 0.0);
        assertTrue(result.semanticSpace().coordinate(predicted, "warm") > 0.0);
        assertTrue(result.semanticSpace().coordinate(predicted, "known") > 0.0);
    }

    @Test
    final void testCanContinueTrainingOnlineFromText() {
        TextLanguageTrainer.TrainingResult result =
                TextLanguageTrainer.train("text", samples(), 1.0);
        int updatesBefore = result.learner().updates();

        result.observe(TextTrainingSample.of(
                "weather question unknown",
                "it is cold",
                "weather answer cold known",
                2.0));

        List<LanguageTemporalLearner.PhraseScore> ranked = result.rankToward(
                "weather question unknown",
                List.of("it is warm", "it is cold"),
                "answer");

        assertEquals(updatesBefore + 1, result.learner().updates());
        assertEquals("it is cold", ranked.get(0).phrase());
    }

    @Test
    final void testRejectsInvalidTrainingInputs() {
        assertThrows(IllegalArgumentException.class,
                () -> TextLanguageTrainer.train(" ", samples()));
        assertThrows(IllegalArgumentException.class,
                () -> TextLanguageTrainer.train("text", List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> TextLanguageTrainer.train("text", samples(), 0.0));
        assertThrows(IllegalArgumentException.class,
                () -> TextLanguageTrainer.train("text", samples())
                        .rankToward("question", List.of("answer"), "missing"));
        assertThrows(NullPointerException.class,
                () -> TextLanguageTrainer.train("text", null));
    }

    private List<TextTrainingSample> samples() {
        return List.of(
                TextTrainingSample.of(
                        "weather question unknown",
                        "it is warm",
                        "weather answer warm known",
                        1.0),
                TextTrainingSample.of(
                        "weather question unknown",
                        "it is cold",
                        "weather answer cold known",
                        0.25),
                TextTrainingSample.of(
                        "weather question unknown",
                        "i do not know",
                        "weather question unknown",
                        -0.5));
    }
}
