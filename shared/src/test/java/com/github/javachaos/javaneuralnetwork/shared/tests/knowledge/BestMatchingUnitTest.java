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

import com.github.javachaos.javaneuralnetwork.shared.hilbert.HilbertVector;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.BestMatchingUnit;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.WorldState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BestMatchingUnitTest {

    private static final double EPSILON = 1.0e-10;

    @Test
    final void testFindsClosestPrototypeAndResidual() {
        WorldState input = WorldState.of("input", HilbertVector.of(0.9, 0.2));
        WorldState x = WorldState.of("x", HilbertVector.of(1.0, 0.0));
        WorldState y = WorldState.of("y", HilbertVector.of(0.0, 1.0));

        BestMatchingUnit.Match match = BestMatchingUnit.find(input, List.of(y, x)).orElseThrow();

        assertEquals("x", match.prototype().name());
        assertArrayEquals(new double[]{-0.1, 0.2}, match.residual().toArray(), EPSILON);
        assertEquals(0.05, match.distanceSquared(), EPSILON);
        assertEquals(Math.sqrt(0.05), match.distance(), EPSILON);
        assertEquals(1.0 / (1.0 + Math.sqrt(0.05)), match.similarity(), EPSILON);
    }

    @Test
    final void testRanksCompatiblePrototypesByDistance() {
        WorldState input = WorldState.of("input", HilbertVector.of(0.2, 0.8));
        WorldState x = WorldState.of("x", HilbertVector.of(1.0, 0.0));
        WorldState y = WorldState.of("y", HilbertVector.of(0.0, 1.0));
        WorldState incompatible = WorldState.of("z", HilbertVector.of(0.0, 0.0, 1.0));

        List<BestMatchingUnit.Match> ranked =
                BestMatchingUnit.rank(input, List.of(x, incompatible, y));

        assertEquals(2, ranked.size());
        assertEquals("y", ranked.get(0).prototype().name());
        assertEquals("x", ranked.get(1).prototype().name());
    }

    @Test
    final void testNoveltyComesFromQuantizationDistance() {
        WorldState input = WorldState.of("input", HilbertVector.of(0.0, 0.0));
        WorldState prototype = WorldState.of("prototype", HilbertVector.of(0.6, 0.8));

        BestMatchingUnit.Match match = BestMatchingUnit.match(input, prototype);

        assertFalse(match.isNovel(1.0));
        assertTrue(match.isNovel(0.99));
    }

    @Test
    final void testRejectsInvalidBmuRequests() {
        WorldState input = WorldState.of("input", HilbertVector.of(1.0, 0.0));
        WorldState incompatible = WorldState.of("bad", HilbertVector.of(1.0, 0.0, 0.0));

        assertTrue(BestMatchingUnit.find(input, List.of()).isEmpty());
        assertTrue(BestMatchingUnit.find(input, List.of(incompatible)).isEmpty());
        assertThrows(NullPointerException.class, () -> BestMatchingUnit.find(null, List.of()));
        assertThrows(NullPointerException.class, () -> BestMatchingUnit.find(input, null));
        assertThrows(NullPointerException.class, () -> BestMatchingUnit.rank(input, List.of((WorldState) null)));
        assertThrows(IllegalArgumentException.class, () -> BestMatchingUnit.match(input, incompatible));
        assertThrows(IllegalArgumentException.class,
                () -> BestMatchingUnit.match(input, input).isNovel(Double.NaN));
    }
}
