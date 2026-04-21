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

import com.github.javachaos.javaneuralnetwork.shared.hilbert.Basis;
import com.github.javachaos.javaneuralnetwork.shared.hilbert.HilbertVector;
import com.github.javachaos.javaneuralnetwork.shared.hilbert.LinearOperator;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.ConceptSubspace;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.WorldState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorldStateTest {

    private static final double EPSILON = 1.0e-10;

    @Test
    final void testWorldStateStoresNamedComponentsImmutably() {
        WorldState world = WorldState.of("room", HilbertVector.of(3.0, 4.0));
        WorldState withAgent = world.withComponent("agent", HilbertVector.of(1.0, 0.0));

        assertEquals("room", world.name());
        assertEquals(2, world.dimension());
        assertEquals(0L, world.revision());
        assertFalse(world.hasComponent("agent"));
        assertTrue(withAgent.hasComponent("agent"));
        assertEquals(1L, withAgent.revision());
        assertArrayEquals(new double[]{1.0, 0.0}, withAgent.component("agent").orElseThrow().toArray(), EPSILON);
        assertEquals(withAgent, withAgent.withoutComponent("missing"));
        assertFalse(withAgent.withoutComponent("agent").hasComponent("agent"));
    }

    @Test
    final void testWorldStateTransformsGlobalAndComponentState() {
        WorldState world = WorldState.of("before", HilbertVector.of(1.0, 2.0))
                .withComponent("object", HilbertVector.of(0.5, 1.5));
        LinearOperator swapAndScale = LinearOperator.of(new double[][]{
                {0.0, 2.0},
                {3.0, 0.0}
        });

        WorldState after = world.transform("after", swapAndScale);

        assertEquals("after", after.name());
        assertEquals(2L, after.revision());
        assertArrayEquals(new double[]{4.0, 3.0}, after.state().toArray(), EPSILON);
        assertArrayEquals(new double[]{3.0, 1.5}, after.component("object").orElseThrow().toArray(), EPSILON);
    }

    @Test
    final void testWorldStateProjectsMeasuresAndCompares() {
        WorldState world = WorldState.of("state", HilbertVector.of(3.0, 4.0));
        WorldState previous = WorldState.of("previous", HilbertVector.of(1.0, 1.0));
        ConceptSubspace xAxis = ConceptSubspace.of("x", HilbertVector.basis(2, 0));

        assertArrayEquals(new double[]{3.0, 0.0}, world.project(xAxis).toArray(), EPSILON);
        assertEquals(9.0 / 25.0, world.relevance(xAxis), EPSILON);
        assertEquals(16.0 / 25.0, world.measure(Basis.standard(2)).probability(1), EPSILON);
        assertArrayEquals(new double[]{2.0, 3.0}, world.differenceFrom(previous).toArray(), EPSILON);
        assertEquals(13.0, world.squaredDistanceTo(previous), EPSILON);
        assertEquals(Math.sqrt(13.0), world.distanceTo(previous), EPSILON);
    }

    @Test
    final void testWorldStateSuperposesCompatibleStates() {
        WorldState left = WorldState.of("left", HilbertVector.of(1.0, 0.0));
        WorldState right = WorldState.of("right", HilbertVector.of(0.0, 1.0));

        assertArrayEquals(
                new double[]{0.25, 0.75},
                left.superpose("mixed", right, 0.25, 0.75).state().toArray(),
                EPSILON);
    }

    @Test
    final void testWorldStateRejectsInvalidInputs() {
        WorldState world = WorldState.of("state", HilbertVector.of(1.0, 2.0));

        assertThrows(IllegalArgumentException.class, () -> WorldState.of(" ", HilbertVector.of(1.0)));
        assertThrows(IllegalArgumentException.class,
                () -> world.withComponent("bad", HilbertVector.of(1.0, 2.0, 3.0)));
        assertThrows(IllegalArgumentException.class,
                () -> world.distanceTo(WorldState.of("other", HilbertVector.of(1.0))));
        assertThrows(IllegalArgumentException.class,
                () -> world.transform("bad", LinearOperator.identity(3)));
    }
}
