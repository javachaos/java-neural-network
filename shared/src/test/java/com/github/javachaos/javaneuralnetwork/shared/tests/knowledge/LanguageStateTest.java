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

import java.util.ArrayList;
import java.util.List;

import com.github.javachaos.javaneuralnetwork.shared.hilbert.HilbertVector;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.LanguageState;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.WorldState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LanguageStateTest {

    private static final double EPSILON = 1.0e-10;

    @Test
    final void testLanguageStateStoresSemanticStateAndTokensImmutably() {
        List<String> tokens = new ArrayList<>(List.of("hello", "world"));
        LanguageState state = LanguageState.of(
                "hello world",
                WorldState.of("semantic", HilbertVector.of(1.0, 2.0)),
                tokens);

        tokens.add("changed");

        assertEquals("hello world", state.text());
        assertEquals(2, state.dimension());
        assertEquals(List.of("hello", "world"), state.tokens());
        assertArrayEquals(new double[]{1.0, 2.0}, state.vector().toArray(), EPSILON);
    }

    @Test
    final void testRejectsInvalidLanguageStateInputs() {
        WorldState valid = WorldState.of("valid", HilbertVector.of(1.0));

        assertThrows(IllegalArgumentException.class, () -> LanguageState.of(" ", valid, List.of()));
        assertThrows(NullPointerException.class, () -> LanguageState.of("valid", null, List.of()));
        assertThrows(NullPointerException.class, () -> LanguageState.of("valid", valid, null));
    }
}
