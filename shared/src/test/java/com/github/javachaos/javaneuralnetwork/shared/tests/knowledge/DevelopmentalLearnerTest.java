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
import com.github.javachaos.javaneuralnetwork.shared.knowledge.DevelopmentalLearner;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.WorldState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DevelopmentalLearnerTest {

    private static final double EPSILON = 1.0e-9;

    @Test
    final void testFirstObservationCreatesPrototypeWithoutPrediction() {
        DevelopmentalLearner learner =
                DevelopmentalLearner.zeroInitialized("baby", 2, 0.5, 0.2, 0.25);

        DevelopmentalLearner.ObservationStep step = learner.observe(state("x", 1.0, 0.0));

        assertFalse(step.hasPrediction());
        assertTrue(step.novel());
        assertEquals(0.0, step.surprise(), EPSILON);
        assertEquals(1, step.observations());
        assertEquals(0, step.updates());
        assertEquals(1, step.prototypeCount());
        assertEquals(1, learner.prototypes().size());
        assertEquals(1, learner.worldModel().stateCount());
    }

    @Test
    final void testRepeatedExperienceReducesSurprise() {
        DevelopmentalLearner learner =
                DevelopmentalLearner.zeroInitialized("alternating", 2, 0.5, 0.2, 0.25);
        List<DevelopmentalLearner.ObservationStep> steps = learner.observeAll(List.of(
                state("x", 1.0, 0.0),
                state("y", 0.0, 1.0),
                state("x", 1.0, 0.0),
                state("y", 0.0, 1.0),
                state("x", 1.0, 0.0),
                state("y", 0.0, 1.0),
                state("x", 1.0, 0.0),
                state("y", 0.0, 1.0)));

        assertEquals(1.0, steps.get(1).normalizedSurprise(), EPSILON);
        assertTrue(steps.get(steps.size() - 1).normalizedSurprise() < 0.13);
        assertEquals(7, learner.transitionLearner().updates());
        assertEquals(7, learner.worldModel().traceCount());
        assertArrayEquals(new double[]{1.0, 0.0},
                learner.predictNext().orElseThrow().state().toArray(), 0.13);
    }

    @Test
    final void testBmuPrototypesCompressRecurringStates() {
        DevelopmentalLearner learner =
                DevelopmentalLearner.zeroInitialized("compress", 2, 1.0, 0.2, 0.25);

        learner.observeAll(List.of(
                state("x", 1.0, 0.0),
                state("y", 0.0, 1.0),
                state("x again", 1.0, 0.0),
                state("y again", 0.0, 1.0),
                state("x third", 1.0, 0.0),
                state("y third", 0.0, 1.0)));

        assertEquals(6, learner.observations());
        assertEquals(2, learner.prototypes().size());
        assertEquals(2.0 / 6.0, learner.prototypeCompressionRatio(), EPSILON);
    }

    @Test
    final void testRejectsInvalidLearnersAndObservations() {
        DevelopmentalLearner learner = DevelopmentalLearner.zeroInitialized("valid", 2, 0.5);

        assertThrows(IllegalArgumentException.class,
                () -> DevelopmentalLearner.zeroInitialized(" ", 2, 0.5));
        assertThrows(IllegalArgumentException.class,
                () -> DevelopmentalLearner.zeroInitialized("bad", 2, 0.5, -0.1, 0.2));
        assertThrows(IllegalArgumentException.class,
                () -> DevelopmentalLearner.zeroInitialized("bad", 2, 0.5, 0.1, 1.1));
        assertThrows(IllegalArgumentException.class,
                () -> DevelopmentalLearner.of("bad", LinearOperator.zero(2, 3), 0.5, 0.1, 0.2));
        assertThrows(NullPointerException.class, () -> learner.observe(null));
        assertThrows(IllegalArgumentException.class,
                () -> learner.observe(WorldState.of("wrong", HilbertVector.of(1.0, 0.0, 0.0))));
    }

    private WorldState state(final String name, final double x, final double y) {
        return WorldState.of(name, HilbertVector.of(x, y));
    }
}
