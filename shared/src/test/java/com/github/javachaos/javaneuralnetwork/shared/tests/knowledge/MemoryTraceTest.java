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
import com.github.javachaos.javaneuralnetwork.shared.hilbert.LinearOperator;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.ConceptSubspace;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.MemoryTrace;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.WorldState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MemoryTraceTest {

    private static final double EPSILON = 1.0e-10;

    @Test
    final void testPerfectPredictionHasNoResidual() {
        WorldState before = WorldState.of("before", HilbertVector.of(1.0, 2.0));
        WorldState after = WorldState.of("after", HilbertVector.of(2.0, 1.0));
        LinearOperator swap = LinearOperator.of(new double[][]{
                {0.0, 1.0},
                {1.0, 0.0}
        });

        MemoryTrace trace = MemoryTrace.of("swap", before, swap, after);

        assertEquals("swap", trace.name());
        assertArrayEquals(new double[]{2.0, 1.0}, trace.predictedAfter().toArray(), EPSILON);
        assertArrayEquals(new double[]{0.0, 0.0}, trace.residual().toArray(), EPSILON);
        assertEquals(0.0, trace.errorSquared(), EPSILON);
        assertEquals(0.0, trace.residualRelevance(ConceptSubspace.of("x", HilbertVector.basis(2, 0))), EPSILON);
        assertTrue(trace.isWellPredicted(EPSILON));
        assertArrayEquals(new double[]{1.0, -1.0}, trace.change().toArray(), EPSILON);
    }

    @Test
    final void testObservationTraceStoresUnexpectedChange() {
        WorldState before = WorldState.of("before", HilbertVector.of(1.0, 0.0));
        WorldState after = WorldState.of("after", HilbertVector.of(0.0, 1.0));
        ConceptSubspace yAxis = ConceptSubspace.of("y", HilbertVector.basis(2, 1));

        MemoryTrace trace = MemoryTrace.observe("turn", before, after);

        assertArrayEquals(new double[]{1.0, 0.0}, trace.predictedAfter().toArray(), EPSILON);
        assertArrayEquals(new double[]{-1.0, 1.0}, trace.residual().toArray(), EPSILON);
        assertEquals(2.0, trace.errorSquared(), EPSILON);
        assertEquals(Math.sqrt(2.0), trace.errorNorm(), EPSILON);
        assertEquals(Math.sqrt(2.0), trace.normalizedError(), EPSILON);
        assertEquals(0.5, trace.residualRelevance(yAxis), EPSILON);
    }

    @Test
    final void testReplayAppliesRememberedActionToNewWorldState() {
        WorldState before = WorldState.of("before", HilbertVector.of(1.0, 2.0));
        WorldState after = WorldState.of("after", HilbertVector.of(2.0, 1.0));
        LinearOperator swap = LinearOperator.of(new double[][]{
                {0.0, 1.0},
                {1.0, 0.0}
        });
        MemoryTrace trace = MemoryTrace.of("swap", before, swap, after);
        WorldState start = WorldState.of("start", HilbertVector.of(3.0, 5.0))
                .withComponent("object", HilbertVector.of(7.0, 11.0));

        WorldState replayed = trace.replay(start);

        assertEquals("swap replay", replayed.name());
        assertArrayEquals(new double[]{5.0, 3.0}, replayed.state().toArray(), EPSILON);
        assertArrayEquals(new double[]{11.0, 7.0}, replayed.component("object").orElseThrow().toArray(), EPSILON);
    }

    @Test
    final void testRejectsInvalidTraceInputs() {
        WorldState before = WorldState.of("before", HilbertVector.of(1.0, 2.0));
        WorldState after = WorldState.of("after", HilbertVector.of(2.0, 1.0));

        assertThrows(IllegalArgumentException.class,
                () -> MemoryTrace.of(" ", before, LinearOperator.identity(2), after));
        assertThrows(IllegalArgumentException.class,
                () -> MemoryTrace.of("bad", before, LinearOperator.identity(3), after));
        assertThrows(IllegalArgumentException.class,
                () -> MemoryTrace.observe("bad", before, WorldState.of("other", HilbertVector.of(1.0))));
        assertThrows(IllegalArgumentException.class,
                () -> MemoryTrace.observe("trace", before, after).isWellPredicted(-1.0));
    }
}
