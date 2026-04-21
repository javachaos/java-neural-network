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
import com.github.javachaos.javaneuralnetwork.shared.hilbert.LinearOperator;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.ConceptSubspace;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.GoalSubspace;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.MemoryTrace;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.PredictiveMemory;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.WorldState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PredictiveMemoryTest {

    private static final double EPSILON = 1.0e-10;

    @Test
    final void testMemoryRemembersTracesImmutably() {
        MemoryTrace trace = identityTrace("stay", 1.0, 0.0);
        PredictiveMemory empty = PredictiveMemory.empty();
        PredictiveMemory memory = empty.remember(trace);

        assertTrue(empty.isEmpty());
        assertFalse(memory.isEmpty());
        assertEquals(1, memory.size());
        assertEquals(trace, memory.traces().get(0));
    }

    @Test
    final void testRankByRecallPrefersClosestBeforeState() {
        MemoryTrace near = identityTrace("near", 1.0, 2.0);
        MemoryTrace far = identityTrace("far", 10.0, 10.0);
        PredictiveMemory memory = PredictiveMemory.empty()
                .remember(far)
                .remember(near);

        List<PredictiveMemory.TraceScore> ranked =
                memory.rankByRecall(WorldState.of("cue", HilbertVector.of(1.0, 2.0)));

        assertEquals("near", ranked.get(0).trace().name());
        assertEquals(1.0, ranked.get(0).recallScore(), EPSILON);
        assertTrue(ranked.get(0).score() > ranked.get(1).score());
    }

    @Test
    final void testRankByReliabilityPrefersLowerPredictionError() {
        MemoryTrace perfect = identityTrace("perfect", 1.0, 0.0);
        MemoryTrace noisy = MemoryTrace.of(
                "noisy",
                WorldState.of("before", HilbertVector.of(1.0, 0.0)),
                LinearOperator.identity(2),
                WorldState.of("after", HilbertVector.of(0.0, 1.0)));
        PredictiveMemory memory = PredictiveMemory.empty()
                .remember(noisy)
                .remember(perfect);

        List<PredictiveMemory.TraceScore> ranked = memory.rankByReliability();

        assertEquals("perfect", ranked.get(0).trace().name());
        assertEquals(1.0, ranked.get(0).reliabilityScore(), EPSILON);
        assertTrue(ranked.get(0).score() > ranked.get(1).score());
    }

    @Test
    final void testRankByResidualRelevanceFindsConceptSpecificSurprise() {
        ConceptSubspace yAxis = ConceptSubspace.of("y", HilbertVector.basis(2, 1));
        MemoryTrace ySurprise = MemoryTrace.of(
                "y-surprise",
                WorldState.of("before", HilbertVector.of(1.0, 0.0)),
                LinearOperator.identity(2),
                WorldState.of("after", HilbertVector.of(1.0, 2.0)));
        MemoryTrace perfect = identityTrace("perfect", 1.0, 0.0);
        PredictiveMemory memory = PredictiveMemory.empty()
                .remember(perfect)
                .remember(ySurprise);

        List<PredictiveMemory.TraceScore> ranked = memory.rankByResidualRelevance(yAxis);

        assertEquals("y-surprise", ranked.get(0).trace().name());
        assertEquals(1.0, ranked.get(0).residualRelevance(), EPSILON);
        assertEquals(0.0, ranked.get(1).residualRelevance(), EPSILON);
    }

    @Test
    final void testRankForGoalPrefersTraceThatMovesTowardGoal() {
        ConceptSubspace xAxis = ConceptSubspace.of("x", HilbertVector.basis(2, 0));
        GoalSubspace goal = GoalSubspace.approach("seek-x", xAxis);
        WorldState cue = WorldState.of("cue", HilbertVector.of(0.0, 1.0));
        LinearOperator swap = LinearOperator.of(new double[][]{
                {0.0, 1.0},
                {1.0, 0.0}
        });
        MemoryTrace turnToX = MemoryTrace.of("turn-to-x", cue, swap, WorldState.of("x", HilbertVector.of(1.0, 0.0)));
        MemoryTrace stayAtY = MemoryTrace.of("stay-at-y", cue, LinearOperator.identity(2), cue);
        PredictiveMemory memory = PredictiveMemory.empty()
                .remember(stayAtY)
                .remember(turnToX);

        List<PredictiveMemory.TraceScore> ranked = memory.rankForGoal(cue, goal);

        assertEquals("turn-to-x", ranked.get(0).trace().name());
        assertEquals(1.0, ranked.get(0).goalProgress(), EPSILON);
        assertEquals("turn-to-x", memory.bestForGoal(cue, goal).orElseThrow().trace().name());
        assertTrue(ranked.get(0).score() > ranked.get(1).score());
    }

    @Test
    final void testRejectsInvalidMemoryRequests() {
        PredictiveMemory memory = PredictiveMemory.empty();
        ConceptSubspace xAxis = ConceptSubspace.of("x", HilbertVector.basis(2, 0));
        GoalSubspace goal = GoalSubspace.approach("x", xAxis);

        assertThrows(NullPointerException.class, () -> memory.remember(null));
        assertThrows(NullPointerException.class, () -> PredictiveMemory.of(null));
        assertThrows(IllegalArgumentException.class,
                () -> memory.rankForGoal(WorldState.of(HilbertVector.of(1.0, 2.0, 3.0)), goal));
    }

    private MemoryTrace identityTrace(final String name, final double x, final double y) {
        WorldState state = WorldState.of(name + "-state", HilbertVector.of(x, y));
        return MemoryTrace.of(name, state, LinearOperator.identity(2), state);
    }
}
