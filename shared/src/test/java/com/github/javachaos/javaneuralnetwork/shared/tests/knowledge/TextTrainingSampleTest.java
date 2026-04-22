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

import com.github.javachaos.javaneuralnetwork.shared.knowledge.TextTrainingSample;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextTrainingSampleTest {

    @Test
    final void testCreatesTextTrainingSample() {
        TextTrainingSample sample = TextTrainingSample.of(
                "question unknown",
                "answer",
                "answer known",
                1.0);

        assertEquals("question unknown", sample.beforeText());
        assertEquals("answer", sample.phrase());
        assertEquals("answer known", sample.afterText());
        assertEquals(1.0, sample.reward());
        assertFalse(sample.terminal());

        TextTrainingSample terminal = new TextTrainingSample("a", "b", "c", -1.0, true);
        assertTrue(terminal.terminal());
    }

    @Test
    final void testRejectsInvalidTextTrainingSamples() {
        assertThrows(IllegalArgumentException.class,
                () -> TextTrainingSample.of(" ", "answer", "after", 0.0));
        assertThrows(IllegalArgumentException.class,
                () -> TextTrainingSample.of("before", " ", "after", 0.0));
        assertThrows(IllegalArgumentException.class,
                () -> TextTrainingSample.of("before", "answer", " ", 0.0));
        assertThrows(IllegalArgumentException.class,
                () -> TextTrainingSample.of("before", "answer", "after", Double.NaN));
        assertThrows(NullPointerException.class,
                () -> TextTrainingSample.of(null, "answer", "after", 0.0));
    }
}
