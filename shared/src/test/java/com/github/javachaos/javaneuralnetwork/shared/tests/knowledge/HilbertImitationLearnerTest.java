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
import com.github.javachaos.javaneuralnetwork.shared.knowledge.HilbertImitationLearner;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.MemoryTrace;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.WorldModel;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.WorldState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HilbertImitationLearnerTest {

    private static final double EPSILON = 1.0e-10;

    @Test
    final void testLearnsPrototypeTransitionFromOneDemonstration() {
        HilbertImitationLearner learner = HilbertImitationLearner.of("imitator", 2, 1.0, 0.1);
        WorldState before = state("before", 0.0, 0.0);
        WorldState after = state("after", 1.0, 1.0);

        HilbertImitationLearner.Observation observation = learner.observe(before, after);
        HilbertImitationLearner.Imitation imitation = learner.imitate(before);

        assertEquals(2, learner.prototypeCount());
        assertEquals(1, learner.transitionCount());
        assertEquals(1, learner.observations());
        assertTrue(observation.before().created());
        assertTrue(observation.after().created());
        assertEquals(1, observation.transition().support());
        assertArrayEquals(after.state().toArray(), imitation.predicted().state().toArray(), EPSILON);
        assertEquals(1.0, imitation.confidence(), EPSILON);
        assertTrue(learner.canPredict(before));
    }

    @Test
    final void testCarriesBmuResidualThroughRememberedTransition() {
        HilbertImitationLearner learner = HilbertImitationLearner.of("copy-motion", 2, 1.0, 0.1);
        learner.observe(state("start", 0.0, 0.0), state("finish", 1.0, 1.0));
        WorldState cue = state("near-start", 0.2, -0.1);

        HilbertImitationLearner.Imitation imitation = learner.imitate(cue);

        assertArrayEquals(new double[]{0.2, -0.1},
                imitation.carriedResidual().toArray(), EPSILON);
        assertArrayEquals(new double[]{1.2, 0.9},
                imitation.predicted().state().toArray(), EPSILON);
        assertTrue(imitation.confidence() < 1.0);
        assertTrue(imitation.confidence() > 0.8);
    }

    @Test
    final void testCloseObservationsAdaptPrototypesInsteadOfStoringRawSamples() {
        HilbertImitationLearner learner = HilbertImitationLearner.of("adapter", 2, 0.5, 0.25);
        learner.observe(state("a0", 0.0, 0.0), state("b0", 1.0, 0.0));

        HilbertImitationLearner.Observation observation =
                learner.observe(state("a1", 0.2, 0.0), state("b1", 1.2, 0.0));

        assertEquals(2, learner.prototypeCount());
        assertEquals(1, learner.transitionCount());
        assertFalse(observation.before().created());
        assertFalse(observation.after().created());
        assertEquals(2, observation.transition().support());
        assertArrayEquals(new double[]{0.1, 0.0},
                learner.prototypes().get(0).state().toArray(), EPSILON);
        assertArrayEquals(new double[]{1.1, 0.0},
                learner.prototypes().get(1).state().toArray(), EPSILON);
        assertArrayEquals(new double[]{1.2, 0.0},
                learner.predict(state("near-a", 0.2, 0.0)).state().toArray(), EPSILON);
    }

    @Test
    final void testNovelObservationsCreateSeparatePrototypeTransitions() {
        HilbertImitationLearner learner = HilbertImitationLearner.of("novelty", 2, 1.0, 0.25);
        learner.observe(state("a0", 0.0, 0.0), state("b0", 1.0, 0.0));
        learner.observe(state("a1", 0.0, 1.0), state("b1", 1.0, 1.0));

        assertEquals(4, learner.prototypeCount());
        assertEquals(2, learner.transitionCount());
        assertArrayEquals(new double[]{1.0, 0.0},
                learner.predict(state("cue-0", 0.0, 0.0)).state().toArray(), EPSILON);
        assertArrayEquals(new double[]{1.0, 1.0},
                learner.predict(state("cue-1", 0.0, 1.0)).state().toArray(), EPSILON);
    }

    @Test
    final void testMostSupportedTransitionWinsWhenSourceHasMultipleOutcomes() {
        HilbertImitationLearner learner = HilbertImitationLearner.of("support", 2, 1.0, 0.1);
        WorldState source = state("source", 0.0, 0.0);
        WorldState b = state("b", 1.0, 0.0);
        WorldState c = state("c", 0.0, 1.0);

        learner.observe(source, c);
        learner.observe(source, b);
        learner.observe(source, b);

        assertEquals(2, learner.transitionCount());
        assertArrayEquals(b.state().toArray(), learner.predict(source).state().toArray(), EPSILON);
        assertEquals(3, learner.observations());
        assertTrue(learner.imitate(source).confidence() > 0.65);
        assertTrue(learner.imitate(source).confidence() < 0.67);
    }

    @Test
    final void testWorldModelViewContainsPrototypeMemoryOnly() {
        HilbertImitationLearner learner = HilbertImitationLearner.of("world", 2, 0.5, 0.25);
        learner.observe(state("a0", 0.0, 0.0), state("b0", 1.0, 0.0));
        learner.observe(state("a1", 0.2, 0.0), state("b1", 1.2, 0.0));

        WorldModel model = learner.worldModel();

        assertEquals(2, model.stateCount());
        assertEquals(1, model.traceCount());
        assertEquals("world-transition-0-1", model.traces().get(0).name());
        assertEquals("world-prototype-0",
                model.bestMatchingState(state("cue", 0.2, 0.0)).orElseThrow().prototype().name());
    }

    @Test
    final void testWorldModelViewCanReplayNonZeroPrototypeTransition() {
        HilbertImitationLearner learner = HilbertImitationLearner.of("replay", 2, 1.0, 0.1);
        WorldState source = state("source", 1.0, 0.0);
        WorldState target = state("target", 0.0, 1.0);
        learner.observe(source, target);

        MemoryTrace trace = learner.worldModel().traces().get(0);

        assertTrue(trace.isWellPredicted(EPSILON));
        assertArrayEquals(target.state().toArray(), trace.replay(source).state().toArray(), EPSILON);
    }

    @Test
    final void testMeanSquaredErrorScoresImitationPredictions() {
        HilbertImitationLearner learner = HilbertImitationLearner.of("mse", 2, 1.0, 0.1);
        MemoryTrace first = MemoryTrace.observe("first", state("a", 0.0, 0.0), state("b", 1.0, 1.0));
        MemoryTrace second = MemoryTrace.observe("second", state("near-a", 0.2, -0.1), state("near-b", 1.2, 0.9));

        learner.learn(first);

        assertEquals(0.0, learner.meanSquaredError(List.of(first, second)), EPSILON);
    }

    @Test
    final void testRejectsInvalidImitationRequests() {
        HilbertImitationLearner learner = HilbertImitationLearner.of("valid", 2, 1.0, 0.1);
        WorldState valid = state("valid", 1.0, 0.0);
        WorldState wrong = WorldState.of("wrong", HilbertVector.of(1.0, 0.0, 0.0));

        assertThrows(IllegalArgumentException.class,
                () -> HilbertImitationLearner.of("bad", 0, 1.0, 0.1));
        assertThrows(IllegalArgumentException.class,
                () -> HilbertImitationLearner.of("bad", 2, -0.1, 0.1));
        assertThrows(IllegalArgumentException.class,
                () -> HilbertImitationLearner.of("bad", 2, 1.0, -0.1));
        assertThrows(IllegalStateException.class, () -> learner.predict(valid));
        assertThrows(IllegalArgumentException.class, () -> learner.observe(valid, wrong));
        assertThrows(NullPointerException.class, () -> learner.observe(null, valid));
        learner.observe(valid, valid);
        assertThrows(IllegalArgumentException.class, () -> learner.predict(wrong));
        assertThrows(IllegalArgumentException.class, () -> learner.meanSquaredError(List.of()));
        assertThrows(NullPointerException.class, () -> learner.learnAll(null));
    }

    private WorldState state(final String name, final double x, final double y) {
        return WorldState.of(name, HilbertVector.of(x, y));
    }
}
