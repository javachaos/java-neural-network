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
import com.github.javachaos.javaneuralnetwork.shared.knowledge.ConceptSubspace;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.GoalSubspace;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.WorldState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GoalSubspaceTest {

    private static final double EPSILON = 1.0e-10;

    @Test
    final void testApproachGoalScoresDesiredConcept() {
        ConceptSubspace xAxis = ConceptSubspace.of("x", HilbertVector.basis(2, 0));
        GoalSubspace goal = GoalSubspace.approach("move-x", xAxis);
        WorldState state = WorldState.of("state", HilbertVector.of(3.0, 4.0));

        assertEquals("move-x", goal.name());
        assertEquals(2, goal.dimension());
        assertFalse(goal.avoided().isPresent());
        assertEquals(9.0 / 25.0, goal.satisfaction(state), EPSILON);
        assertTrue(goal.isSatisfied(WorldState.of(HilbertVector.of(1.0, 0.0)), 1.0));
    }

    @Test
    final void testApproachAvoidingGoalBalancesDesiredAndAvoidedConcepts() {
        ConceptSubspace xAxis = ConceptSubspace.of("x", HilbertVector.basis(2, 0));
        ConceptSubspace yAxis = ConceptSubspace.of("y", HilbertVector.basis(2, 1));
        GoalSubspace goal = GoalSubspace.approachAvoiding("x-not-y", xAxis, yAxis);
        WorldState mostlyY = WorldState.of("mostly-y", HilbertVector.of(3.0, 4.0));
        WorldState mostlyX = WorldState.of("mostly-x", HilbertVector.of(4.0, 3.0));

        assertTrue(goal.avoided().isPresent());
        assertEquals((9.0 / 25.0 - 16.0 / 25.0) / 2.0, goal.satisfaction(mostlyY), EPSILON);
        assertEquals(7.0 / 25.0, goal.progress(mostlyY, mostlyX), EPSILON);
    }

    @Test
    final void testWeightedGoalUsesRelativeWeights() {
        ConceptSubspace xAxis = ConceptSubspace.of("x", HilbertVector.basis(2, 0));
        ConceptSubspace yAxis = ConceptSubspace.of("y", HilbertVector.basis(2, 1));
        GoalSubspace goal = GoalSubspace.weighted("weighted", xAxis, yAxis, 3.0, 1.0);

        assertEquals((3.0 * 9.0 / 25.0 - 16.0 / 25.0) / 4.0,
                goal.satisfaction(WorldState.of(HilbertVector.of(3.0, 4.0))),
                EPSILON);
    }

    @Test
    final void testRejectsInvalidGoals() {
        ConceptSubspace xAxis = ConceptSubspace.of("x", HilbertVector.basis(2, 0));
        ConceptSubspace zAxis = ConceptSubspace.of("z", HilbertVector.basis(3, 2));

        assertThrows(IllegalArgumentException.class, () -> GoalSubspace.approach(" ", xAxis));
        assertThrows(IllegalArgumentException.class, () -> GoalSubspace.approachAvoiding("bad", xAxis, zAxis));
        assertThrows(IllegalArgumentException.class, () -> GoalSubspace.weighted("bad", xAxis, null, 1.0, 1.0));
        assertThrows(IllegalArgumentException.class, () -> GoalSubspace.weighted("bad", xAxis, null, 0.0, 0.0));
        assertThrows(IllegalArgumentException.class,
                () -> GoalSubspace.approach("x", xAxis)
                        .satisfaction(WorldState.of(HilbertVector.of(1.0, 2.0, 3.0))));
    }
}
