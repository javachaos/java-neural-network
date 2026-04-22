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
import com.github.javachaos.javaneuralnetwork.shared.knowledge.BestMatchingUnit;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.ConceptSubspace;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.GoalSubspace;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.MemoryTrace;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.PredictiveMemory;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.WorldModel;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.WorldState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorldModelTest {

    private static final double EPSILON = 1.0e-10;

    @Test
    final void testRememberAddsTraceAndEndpointStates() {
        WorldState x = WorldState.of("x", HilbertVector.basis(2, 0));
        WorldState y = WorldState.of("y", HilbertVector.basis(2, 1));
        MemoryTrace trace = MemoryTrace.of("x-to-y", x, swap(), y);

        WorldModel model = WorldModel.empty().remember(trace);

        assertEquals(2, model.stateCount());
        assertEquals(1, model.traceCount());
        assertEquals(x, model.states().get(0));
        assertEquals(y, model.states().get(1));
        assertEquals(trace, model.traces().get(0));
        assertEquals("x-to-y", model.rankTracesByRecall(x).get(0).trace().name());
    }

    @Test
    final void testRankStatesBySimilarityAndConceptRelevance() {
        WorldState x = WorldState.of("x", HilbertVector.basis(2, 0));
        WorldState y = WorldState.of("y", HilbertVector.basis(2, 1));
        WorldState diagonal = WorldState.of("diagonal", HilbertVector.of(1.0, 1.0).normalized());
        ConceptSubspace xAxis = ConceptSubspace.of("x-axis", HilbertVector.basis(2, 0));
        WorldModel model = WorldModel.empty()
                .observe(y)
                .observe(diagonal)
                .observe(x)
                .withConcept(xAxis);

        List<WorldModel.StateScore> bySimilarity = model.rankStatesBySimilarity(x);
        List<WorldModel.StateScore> byRelevance = model.rankStatesByRelevance(xAxis);

        assertEquals("x", bySimilarity.get(0).state().name());
        assertEquals(0.0, bySimilarity.get(0).distance(), EPSILON);
        assertEquals("x", byRelevance.get(0).state().name());
        assertEquals(1.0, byRelevance.get(0).relevance(), EPSILON);
        assertEquals("diagonal", byRelevance.get(1).state().name());
        assertEquals(0.5, byRelevance.get(1).relevance(), EPSILON);
        assertEquals("y", byRelevance.get(2).state().name());
        assertEquals(0.0, byRelevance.get(2).relevance(), EPSILON);
    }

    @Test
    final void testRankGoalsAndGoalDirectedTraces() {
        WorldState x = WorldState.of("x", HilbertVector.basis(2, 0));
        WorldState y = WorldState.of("y", HilbertVector.basis(2, 1));
        ConceptSubspace xAxis = ConceptSubspace.of("x-axis", HilbertVector.basis(2, 0));
        ConceptSubspace yAxis = ConceptSubspace.of("y-axis", HilbertVector.basis(2, 1));
        GoalSubspace seekX = GoalSubspace.approach("seek-x", xAxis);
        GoalSubspace seekY = GoalSubspace.approach("seek-y", yAxis);
        MemoryTrace stayAtY = MemoryTrace.of("stay-at-y", y, LinearOperator.identity(2), y);
        MemoryTrace turnToX = MemoryTrace.of("turn-to-x", y, swap(), x);
        WorldModel model = WorldModel.empty()
                .remember(stayAtY)
                .remember(turnToX)
                .withGoal(seekY)
                .withGoal(seekX);

        List<WorldModel.GoalScore> goalScores = model.rankGoals(x);
        List<PredictiveMemory.TraceScore> traceScores = model.rankTracesForGoal(y, seekX);

        assertEquals("seek-x", goalScores.get(0).goal().name());
        assertEquals(1.0, goalScores.get(0).satisfaction(), EPSILON);
        assertEquals("turn-to-x", traceScores.get(0).trace().name());
        assertEquals(1.0, traceScores.get(0).goalProgress(), EPSILON);
        assertEquals("turn-to-x", model.bestTraceForGoal(y, seekX).orElseThrow().trace().name());
    }

    @Test
    final void testFactoryBuildsImmutableGraph() {
        WorldState x = WorldState.of("x", HilbertVector.basis(2, 0));
        WorldState y = WorldState.of("y", HilbertVector.basis(2, 1));
        MemoryTrace trace = MemoryTrace.of("x-to-y", x, swap(), y);
        ConceptSubspace yAxis = ConceptSubspace.of("y-axis", HilbertVector.basis(2, 1));
        GoalSubspace seekY = GoalSubspace.approach("seek-y", yAxis);

        WorldModel model = WorldModel.of(List.of(x), List.of(trace), List.of(yAxis), List.of(seekY));

        assertEquals(2, model.stateCount());
        assertEquals(1, model.traceCount());
        assertEquals(1, model.concepts().size());
        assertEquals(1, model.goals().size());
        assertThrows(UnsupportedOperationException.class, () -> model.states().add(y));
    }

    @Test
    final void testRejectsInvalidWorldModelRequests() {
        WorldModel model = WorldModel.empty();
        WorldState x = WorldState.of("x", HilbertVector.basis(2, 0));
        ConceptSubspace xAxis = ConceptSubspace.of("x-axis", HilbertVector.basis(2, 0));
        GoalSubspace seekX = GoalSubspace.approach("seek-x", xAxis);

        assertThrows(NullPointerException.class, () -> model.observe(null));
        assertThrows(NullPointerException.class, () -> model.remember(null));
        assertThrows(NullPointerException.class, () -> model.withConcept(null));
        assertThrows(NullPointerException.class, () -> model.withGoal(null));
        assertThrows(NullPointerException.class, () -> model.rankStatesBySimilarity(null));
        assertThrows(NullPointerException.class, () -> model.rankStatesByRelevance(null));
        assertThrows(NullPointerException.class, () -> model.rankGoals(null));
        assertThrows(NullPointerException.class, () -> WorldModel.of(null, List.of(), List.of(), List.of()));
        assertTrue(WorldModel.empty().withGoal(seekX)
                .rankGoals(WorldState.of("zero", HilbertVector.zero(2))).isEmpty());
        assertEquals("x", model.observe(x).rankStatesByRelevance(xAxis).get(0).state().name());
    }

    @Test
    final void testWorldModelFindsBestMatchingStatePrototype() {
        WorldState x = WorldState.of("x", HilbertVector.basis(2, 0));
        WorldState y = WorldState.of("y", HilbertVector.basis(2, 1));
        WorldState cue = WorldState.of("cue", HilbertVector.of(0.8, 0.2));
        WorldModel model = WorldModel.empty()
                .observe(y)
                .observe(x);

        BestMatchingUnit.Match match = model.bestMatchingState(cue).orElseThrow();

        assertEquals("x", match.prototype().name());
        assertEquals("x", model.rankBestMatchingStates(cue).get(0).prototype().name());
        assertEquals(0.08, match.distanceSquared(), EPSILON);
    }

    private LinearOperator swap() {
        return LinearOperator.of(new double[][]{
                {0.0, 1.0},
                {1.0, 0.0}
        });
    }
}
