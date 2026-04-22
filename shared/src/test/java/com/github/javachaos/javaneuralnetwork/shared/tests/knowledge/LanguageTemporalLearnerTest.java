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

import com.github.javachaos.javaneuralnetwork.shared.knowledge.GoalSubspace;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.LanguageState;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.LanguageTemporalLearner;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.LexicalSemanticSpace;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LanguageTemporalLearnerTest {

    private static final double EPSILON = 1.0e-9;

    @Test
    final void testLearnsPhraseAsSemanticStateTransition() {
        LexicalSemanticSpace space = testSpace();
        LanguageTemporalLearner learner = LanguageTemporalLearner.of("language", space, 1.0);
        LanguageState question = space.encode("question", "question unknown");
        LanguageState answer = space.encode("answer", "answer warm known");

        learner.observe(question, "it is warm", answer, 1.0);
        LanguageState prediction = learner.predict(question, "it is warm");

        assertEquals(1, learner.updates());
        assertTrue(space.alignment(prediction, "answer") > space.alignment(question, "answer"));
        assertEquals(space.coordinate(answer, "warm"), space.coordinate(prediction, "warm"), EPSILON);
        assertEquals(space.coordinate(answer, "known"), space.coordinate(prediction, "known"), EPSILON);
    }

    @Test
    final void testRanksPhrasesByRewardAndGoalProgress() {
        LexicalSemanticSpace space = testSpace();
        LanguageTemporalLearner learner = LanguageTemporalLearner.of("language", space, 1.0);
        LanguageState question = space.encode("question", "question unknown");
        LanguageState warmAnswer = space.encode("warm-answer", "answer warm known");
        LanguageState uncertain = space.encode("uncertain", "question unknown");
        GoalSubspace answerGoal = GoalSubspace.approach("seek-answer", space.concept("answer"));

        learner.observe(question, "it is warm", warmAnswer, 1.0);
        learner.observe(question, "i do not know", uncertain, -0.5);

        List<LanguageTemporalLearner.PhraseScore> ranked = learner.rankPhrases(
                question,
                List.of("i do not know", "it is warm"),
                answerGoal);

        assertEquals("it is warm", ranked.get(0).phrase());
        assertTrue(ranked.get(0).expectedReward() > ranked.get(1).expectedReward());
        assertTrue(ranked.get(0).goalProgress() > ranked.get(1).goalProgress());
        assertTrue(ranked.get(0).score() > ranked.get(1).score());
    }

    @Test
    final void testLearnsNegationAsAStateOperator() {
        LexicalSemanticSpace space = testSpace();
        LanguageTemporalLearner learner = LanguageTemporalLearner.of("language", space, 1.0);
        LanguageState warm = space.encode("warm", "warm");
        LanguageState notWarm = space.encode("not-warm", "not warm");

        learner.observe(warm, "not", notWarm, 0.25);
        LanguageState prediction = learner.predict(warm, "not");

        assertTrue(space.alignment(warm, "warm") > 0.99);
        assertTrue(space.alignment(prediction, "warm") < -0.99);
    }

    @Test
    final void testRejectsInvalidLanguageLearningRequests() {
        LexicalSemanticSpace space = testSpace();
        LexicalSemanticSpace otherSpace = LexicalSemanticSpace.of("other", List.of("only"))
                .withToken("only", 1.0);
        LanguageTemporalLearner learner = LanguageTemporalLearner.of("language", space, 1.0);
        LanguageState valid = space.encode("valid", "question");
        LanguageState wrongDimension = otherSpace.encode("wrong", "only");

        assertThrows(IllegalArgumentException.class,
                () -> LanguageTemporalLearner.of("bad", space, 0.0));
        assertThrows(IllegalArgumentException.class,
                () -> learner.observe(wrongDimension, "move", valid, 0.0));
        assertThrows(IllegalArgumentException.class,
                () -> learner.observe(valid, " ", valid, 0.0));
        assertThrows(IllegalArgumentException.class,
                () -> learner.predict(valid, "missing"));
        assertThrows(NullPointerException.class,
                () -> learner.rankPhrases(valid, null));
    }

    private LexicalSemanticSpace testSpace() {
        return LexicalSemanticSpace.of("language", List.of(
                "question",
                "answer",
                "warm",
                "cold",
                "known",
                "unknown"))
                .withToken("question", 1.0, 0.0, 0.0, 0.0, 0.0, 0.0)
                .withToken("answer", 0.0, 1.0, 0.0, 0.0, 0.0, 0.0)
                .withToken("warm", 0.0, 0.0, 1.0, 0.0, 0.0, 0.0)
                .withToken("cold", 0.0, 0.0, 0.0, 1.0, 0.0, 0.0)
                .withToken("known", 0.0, 0.0, 0.0, 0.0, 1.0, 0.0)
                .withToken("unknown", 0.0, 0.0, 0.0, 0.0, 0.0, 1.0);
    }
}
