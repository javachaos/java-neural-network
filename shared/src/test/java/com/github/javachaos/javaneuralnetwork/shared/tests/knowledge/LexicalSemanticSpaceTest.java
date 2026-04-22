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

import com.github.javachaos.javaneuralnetwork.shared.knowledge.ConceptSubspace;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.LanguageState;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.LexicalSemanticSpace;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LexicalSemanticSpaceTest {

    private static final double EPSILON = 1.0e-10;

    @Test
    final void testEncodesKnownTokensIntoSemanticAxes() {
        LexicalSemanticSpace space = testSpace();

        LanguageState state = space.encode("state", "weather question unknown");

        assertEquals(List.of("weather", "question", "unknown"), state.tokens());
        assertEquals(1.0, space.coordinate(state, "question"), EPSILON);
        assertEquals(1.0, space.coordinate(state, "unknown"), EPSILON);
        assertEquals(0.0, space.coordinate(state, "answer"), EPSILON);
    }

    @Test
    final void testNegationActsAsCheapSemanticOperator() {
        LexicalSemanticSpace space = testSpace();

        LanguageState warm = space.encode("warm", "warm");
        LanguageState notWarm = space.encode("not-warm", "not warm");

        assertTrue(space.alignment(warm, "warm") > 0.99);
        assertTrue(space.alignment(notWarm, "warm") < -0.99);
        assertTrue(space.coordinate(notWarm, "warm") < space.coordinate(warm, "warm"));
    }

    @Test
    final void testConceptAndVocabularyAccessors() {
        LexicalSemanticSpace space = testSpace();
        ConceptSubspace answer = space.concept("answer");
        LanguageState state = space.encode("state", "answer known");

        assertEquals(6, space.dimension());
        assertEquals(1.0 / 2.0, answer.relevance(state.vector()), EPSILON);
        assertTrue(space.vocabulary().contains("warm"));
    }

    @Test
    final void testRejectsInvalidSemanticSpaceInputs() {
        LexicalSemanticSpace space = testSpace();

        assertThrows(IllegalArgumentException.class,
                () -> LexicalSemanticSpace.of("bad", List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> LexicalSemanticSpace.of("bad", List.of("x", "x")));
        assertThrows(IllegalArgumentException.class,
                () -> space.withToken("bad", 1.0, 2.0));
        assertThrows(IllegalArgumentException.class,
                () -> space.concept("missing"));
        assertThrows(NullPointerException.class,
                () -> space.tokenize(null));
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
