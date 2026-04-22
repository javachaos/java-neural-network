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
import com.github.javachaos.javaneuralnetwork.shared.knowledge.ConceptSubspace;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.GoalSubspace;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.TemporalOperatorLearner;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.TemporalTransition;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.WorldState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TemporalOperatorLearnerTest {

    private static final double EPSILON = 1.0e-9;

    @Test
    final void testLearnsSeparateActionConditionedOperators() {
        WorldState x = WorldState.of("x", HilbertVector.basis(2, 0));
        WorldState y = WorldState.of("y", HilbertVector.basis(2, 1));
        TemporalOperatorLearner learner = TemporalOperatorLearner.of("time", 2, 1.0);

        learner.learn(TemporalTransition.of("swap-x", x, "swap", y, 1.0));
        learner.learn(TemporalTransition.of("swap-y", y, "swap", x, 1.0));
        learner.learn(TemporalTransition.of("stay-x", x, "stay", x, 0.0));
        learner.learn(TemporalTransition.of("stay-y", y, "stay", y, 0.0));

        assertArrayEquals(y.state().toArray(), learner.predict(x, "swap").state().toArray(), EPSILON);
        assertArrayEquals(x.state().toArray(), learner.predict(x, "stay").state().toArray(), EPSILON);
        assertEquals(2, learner.actionCount());
        assertEquals(4, learner.updates());
        assertEquals(4, learner.memorySize());
    }

    @Test
    final void testActionRankingUsesRewardAndGoalProgress() {
        WorldState x = WorldState.of("x", HilbertVector.basis(2, 0));
        WorldState y = WorldState.of("y", HilbertVector.basis(2, 1));
        GoalSubspace seekY = GoalSubspace.approach("seek-y",
                ConceptSubspace.of("y-axis", HilbertVector.basis(2, 1)));
        TemporalOperatorLearner learner = TemporalOperatorLearner.of("time", 2, 1.0);

        learner.learn(TemporalTransition.of("advance", x, "advance", y, 1.0));
        learner.learn(TemporalTransition.of("wait", x, "wait", x, -0.25));

        List<TemporalOperatorLearner.ActionScore> ranked =
                learner.rankActions(x, List.of("wait", "advance"), seekY);

        assertEquals("advance", ranked.get(0).action());
        assertTrue(ranked.get(0).expectedReward() > ranked.get(1).expectedReward());
        assertTrue(ranked.get(0).goalProgress() > ranked.get(1).goalProgress());
        assertTrue(ranked.get(0).score() > ranked.get(1).score());
    }

    @Test
    final void testBoundedMemoryKeepsRecentTraces() {
        WorldState x = WorldState.of("x", HilbertVector.basis(2, 0));
        WorldState y = WorldState.of("y", HilbertVector.basis(2, 1));
        TemporalOperatorLearner learner = TemporalOperatorLearner.of("time", 2, 0.5, 2);

        learner.learn(TemporalTransition.of("one", x, "swap", y, 0.0));
        learner.learn(TemporalTransition.of("two", y, "swap", x, 0.0));
        learner.learn(TemporalTransition.of("three", x, "swap", y, 0.0));

        assertEquals(3, learner.updates());
        assertEquals(2, learner.memorySize());
        assertEquals(3, learner.visits("swap"));
    }

    @Test
    final void testRejectsInvalidLearningRequests() {
        WorldState valid = WorldState.of("valid", HilbertVector.of(1.0, 0.0));
        WorldState wrong = WorldState.of("wrong", HilbertVector.of(1.0, 0.0, 0.0));
        TemporalOperatorLearner learner = TemporalOperatorLearner.of("time", 2, 1.0);

        assertThrows(IllegalArgumentException.class,
                () -> TemporalOperatorLearner.of(" ", 2, 1.0));
        assertThrows(IllegalArgumentException.class,
                () -> TemporalOperatorLearner.of("bad", 0, 1.0));
        assertThrows(IllegalArgumentException.class,
                () -> TemporalOperatorLearner.of("bad", 2, 0.0));
        assertThrows(IllegalArgumentException.class,
                () -> TemporalOperatorLearner.of("bad", 2, 1.0, 0));
        assertThrows(IllegalArgumentException.class,
                () -> learner.learn(TemporalTransition.of("bad", wrong, "move", wrong, 0.0)));
        assertThrows(IllegalArgumentException.class,
                () -> learner.predict(valid, "missing"));
        assertThrows(IllegalArgumentException.class,
                () -> learner.rankActions(wrong, List.of("move")));
        assertThrows(NullPointerException.class,
                () -> learner.learn(null));
    }
}
