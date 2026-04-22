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

import com.github.javachaos.javaneuralnetwork.shared.hilbert.HilbertVector;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.MemoryTrace;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.TemporalTransition;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.WorldState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TemporalTransitionTest {

    private static final double EPSILON = 1.0e-10;

    @Test
    final void testTransitionStoresActionRewardAndChange() {
        WorldState before = WorldState.of("before", HilbertVector.of(1.0, 0.0));
        WorldState after = WorldState.of("after", HilbertVector.of(0.0, 1.0));

        TemporalTransition transition = TemporalTransition.of("turn", before, "swap", after, 0.5);

        assertEquals("turn", transition.name());
        assertEquals("swap", transition.action());
        assertEquals(0.5, transition.reward(), EPSILON);
        assertFalse(transition.terminal());
        assertArrayEquals(new double[]{-1.0, 1.0}, transition.change().toArray(), EPSILON);
    }

    @Test
    final void testTransitionConvertsToMemoryTrace() {
        WorldState before = WorldState.of("before", HilbertVector.of(1.0, 0.0));
        WorldState after = WorldState.of("after", HilbertVector.of(0.0, 1.0));
        TemporalTransition transition = new TemporalTransition("terminal", before, "swap", after, 1.0, true);

        MemoryTrace trace = transition.asMemoryTrace();

        assertEquals("terminal", trace.name());
        assertEquals(before, trace.before());
        assertEquals(after, trace.after());
        assertTrue(transition.terminal());
    }

    @Test
    final void testRejectsInvalidTransitions() {
        WorldState valid = WorldState.of("valid", HilbertVector.of(1.0, 0.0));
        WorldState wrong = WorldState.of("wrong", HilbertVector.of(1.0, 0.0, 0.0));

        assertThrows(IllegalArgumentException.class,
                () -> TemporalTransition.of(" ", valid, "move", valid, 0.0));
        assertThrows(IllegalArgumentException.class,
                () -> TemporalTransition.of("valid", valid, " ", valid, 0.0));
        assertThrows(IllegalArgumentException.class,
                () -> TemporalTransition.of("bad", valid, "move", wrong, 0.0));
        assertThrows(IllegalArgumentException.class,
                () -> TemporalTransition.of("bad", valid, "move", valid, Double.NaN));
        assertThrows(NullPointerException.class,
                () -> TemporalTransition.of("bad", null, "move", valid, 0.0));
    }
}
