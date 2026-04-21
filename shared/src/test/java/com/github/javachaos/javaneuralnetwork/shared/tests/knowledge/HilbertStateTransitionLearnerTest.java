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

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

import com.github.javachaos.javaneuralnetwork.shared.hilbert.HilbertVector;
import com.github.javachaos.javaneuralnetwork.shared.hilbert.LinearOperator;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.ConceptSubspace;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.GoalSubspace;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.HilbertStateTransitionLearner;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.MemoryTrace;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.WorldState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HilbertStateTransitionLearnerTest {

    private static final double EPSILON = 1.0e-9;

    @Test
    final void testLearnsSwapOperatorFromBasisTransitions() {
        WorldState x = WorldState.of("x", HilbertVector.basis(2, 0));
        WorldState y = WorldState.of("y", HilbertVector.basis(2, 1));
        List<MemoryTrace> traces = List.of(
                MemoryTrace.observe("x-to-y", x, y),
                MemoryTrace.observe("y-to-x", y, x));
        HilbertStateTransitionLearner learner =
                HilbertStateTransitionLearner.zeroInitialized("swap", 2, 2, 1.0);

        double initialError = learner.meanSquaredError(traces);
        HilbertStateTransitionLearner.TrainingReport report = learner.train(traces, 1);

        assertEquals(0.5, initialError, EPSILON);
        assertEquals(1, report.epochs());
        assertEquals(2, report.samples());
        assertEquals(2, report.updates());
        assertEquals(0.0, report.finalMeanSquaredError(), EPSILON);
        assertTrue(learner.operator().closeTo(LinearOperator.of(new double[][]{
                {0.0, 1.0},
                {1.0, 0.0}
        }), EPSILON));
        assertArrayEquals(y.state().toArray(), learner.predict(x).state().toArray(), EPSILON);
        assertArrayEquals(x.state().toArray(), learner.predict(y).state().toArray(), EPSILON);
    }

    @Test
    final void testBasisTrainingGeneralizesToUnseenSuperpositions() {
        LinearOperator swap = swapOperator();
        List<MemoryTrace> training = basisTraces("swap-basis", swap);
        List<MemoryTrace> holdout = List.of(
                traceFor("plus-superposition", swap, normalized(1.0, 1.0)),
                traceFor("minus-superposition", swap, normalized(1.0, -1.0)),
                traceFor("weighted-superposition", swap, normalized(0.6, 0.8)));
        HilbertStateTransitionLearner learner =
                HilbertStateTransitionLearner.zeroInitialized("swap", 2, 2, 1.0);

        learner.train(training, 1);

        assertEquals(0.0, learner.meanSquaredError(training), EPSILON);
        assertEquals(0.0, learner.meanSquaredError(holdout), EPSILON);
        assertArrayEquals(new double[]{0.8, 0.6},
                learner.predict(WorldState.of("weighted", HilbertVector.of(0.6, 0.8)))
                        .state().toArray(), EPSILON);
    }

    @Test
    final void testBasisTrainingGeneralizesToRandomNormalizedHoldoutStates() {
        LinearOperator rotate = LinearOperator.of(new double[][]{
                {0.0, 0.0, 1.0},
                {1.0, 0.0, 0.0},
                {0.0, 1.0, 0.0}
        });
        List<MemoryTrace> training = basisTraces("rotate-basis", rotate);
        List<MemoryTrace> holdout = randomHoldouts("rotate-holdout", rotate, 32);
        HilbertStateTransitionLearner learner =
                HilbertStateTransitionLearner.zeroInitialized("rotate", 3, 3, 1.0);

        learner.train(training, 1);

        assertEquals(0.0, learner.meanSquaredError(training), EPSILON);
        assertEquals(0.0, learner.meanSquaredError(holdout), EPSILON);
    }

    @Test
    final void testValidationExposesUnderdeterminedTrainingData() {
        LinearOperator swap = swapOperator();
        MemoryTrace trainOnlyX = traceFor("x-to-y", swap, HilbertVector.basis(2, 0));
        MemoryTrace holdoutY = traceFor("y-to-x", swap, HilbertVector.basis(2, 1));
        HilbertStateTransitionLearner learner =
                HilbertStateTransitionLearner.zeroInitialized("partial-swap", 2, 2, 1.0);

        learner.learn(trainOnlyX);

        assertEquals(0.0, learner.meanSquaredError(List.of(trainOnlyX)), EPSILON);
        assertTrue(learner.meanSquaredError(List.of(holdoutY)) > 0.49);
    }

    @Test
    final void testValidationExposesNoisyTrainingLabels() {
        LinearOperator cleanSwap = swapOperator();
        List<MemoryTrace> noisyTraining = List.of(
                MemoryTrace.observe(
                        "noisy-x-to-y",
                        WorldState.of("x", HilbertVector.basis(2, 0)),
                        WorldState.of("noisy-y", HilbertVector.of(0.2, 1.0))),
                MemoryTrace.observe(
                        "noisy-y-to-x",
                        WorldState.of("y", HilbertVector.basis(2, 1)),
                        WorldState.of("noisy-x", HilbertVector.of(1.0, -0.2))));
        List<MemoryTrace> cleanHoldout = List.of(
                traceFor("clean-x", cleanSwap, HilbertVector.basis(2, 0)),
                traceFor("clean-y", cleanSwap, HilbertVector.basis(2, 1)),
                traceFor("clean-plus", cleanSwap, normalized(1.0, 1.0)));
        HilbertStateTransitionLearner learner =
                HilbertStateTransitionLearner.zeroInitialized("noisy-swap", 2, 2, 1.0);

        learner.train(noisyTraining, 1);

        assertEquals(0.0, learner.meanSquaredError(noisyTraining), EPSILON);
        assertTrue(learner.meanSquaredError(cleanHoldout) > 0.01);
    }

    @Test
    final void testLearningStepReportsPreUpdateResidual() {
        WorldState before = WorldState.of("before", HilbertVector.of(1.0, 0.0));
        WorldState after = WorldState.of("after", HilbertVector.of(0.0, 1.0));
        HilbertStateTransitionLearner learner =
                HilbertStateTransitionLearner.zeroInitialized("step", 2, 2, 1.0);

        HilbertStateTransitionLearner.LearningStep step = learner.learn(before, after);

        assertEquals(1.0, step.errorSquared(), EPSILON);
        assertEquals(1.0, step.normalizedError(), EPSILON);
        assertTrue(step.previousOperator().closeTo(LinearOperator.zero(2, 2), EPSILON));
        assertArrayEquals(after.state().toArray(), learner.predict(before).state().toArray(), EPSILON);
    }

    @Test
    final void testLearnedTransitionCanBeScoredAgainstGoalSubspace() {
        WorldState x = WorldState.of("x", HilbertVector.basis(2, 0));
        WorldState y = WorldState.of("y", HilbertVector.basis(2, 1));
        GoalSubspace seekY = GoalSubspace.approach("seek-y",
                ConceptSubspace.of("y-axis", HilbertVector.basis(2, 1)));
        HilbertStateTransitionLearner learner =
                HilbertStateTransitionLearner.zeroInitialized("turn-toward-y", 2, 2, 1.0);

        learner.learn(x, y);
        WorldState predicted = learner.predict(x);

        assertEquals(1.0, seekY.satisfaction(predicted), EPSILON);
        assertEquals(1.0, seekY.progress(x, predicted), EPSILON);
    }

    @Test
    final void testOnlineUpdatesReduceErrorForRepeatedTrace() {
        WorldState before = WorldState.of("before", HilbertVector.of(2.0, 0.0));
        WorldState after = WorldState.of("after", HilbertVector.of(0.0, 1.0));
        MemoryTrace trace = MemoryTrace.observe("scale-and-rotate", before, after);
        HilbertStateTransitionLearner learner =
                HilbertStateTransitionLearner.zeroInitialized("slow", 2, 2, 0.5);

        double initialError = learner.meanSquaredError(List.of(trace));
        learner.train(List.of(trace), 6);
        double finalError = learner.meanSquaredError(List.of(trace));

        assertTrue(finalError < initialError);
        assertTrue(finalError < 0.001);
    }

    @Test
    final void testRejectsInvalidLearningRequests() {
        HilbertStateTransitionLearner learner =
                HilbertStateTransitionLearner.zeroInitialized("valid", 2, 2, 1.0);
        WorldState valid = WorldState.of("valid", HilbertVector.of(1.0, 0.0));
        WorldState wrong = WorldState.of("wrong", HilbertVector.of(1.0, 0.0, 0.0));

        assertThrows(IllegalArgumentException.class,
                () -> HilbertStateTransitionLearner.zeroInitialized("bad", 0, 2, 1.0));
        assertThrows(IllegalArgumentException.class,
                () -> HilbertStateTransitionLearner.identityInitialized("bad", 2, 0.0));
        assertThrows(IllegalArgumentException.class,
                () -> HilbertStateTransitionLearner.of(" ", LinearOperator.identity(2), 1.0));
        assertThrows(NullPointerException.class,
                () -> HilbertStateTransitionLearner.of("bad", null, 1.0));
        assertThrows(IllegalArgumentException.class, () -> learner.learn(wrong, valid));
        assertThrows(IllegalArgumentException.class, () -> learner.learn(valid, wrong));
        assertThrows(IllegalArgumentException.class, () -> learner.train(List.of(), 1));
        assertThrows(IllegalArgumentException.class, () -> learner.train(List.of(MemoryTrace.observe("v", valid, valid)), 0));
    }

    private LinearOperator swapOperator() {
        return LinearOperator.of(new double[][]{
                {0.0, 1.0},
                {1.0, 0.0}
        });
    }

    private List<MemoryTrace> basisTraces(final String name, final LinearOperator operator) {
        List<MemoryTrace> traces = new ArrayList<>();
        for (int i = 0; i < operator.columns(); i++) {
            traces.add(traceFor(name + "-" + i, operator, HilbertVector.basis(operator.columns(), i)));
        }
        return traces;
    }

    private List<MemoryTrace> randomHoldouts(
            final String name,
            final LinearOperator operator,
            final int count) {
        SplittableRandom random = new SplittableRandom(8675309L);
        List<MemoryTrace> traces = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            double[] values = new double[operator.columns()];
            for (int j = 0; j < values.length; j++) {
                values[j] = random.nextDouble(-1.0, 1.0);
            }
            traces.add(traceFor(name + "-" + i, operator, HilbertVector.of(values).normalized()));
        }
        return traces;
    }

    private MemoryTrace traceFor(
            final String name,
            final LinearOperator operator,
            final HilbertVector input) {
        WorldState before = WorldState.of(name + "-before", input);
        return MemoryTrace.of(name, before, operator,
                WorldState.of(name + "-after", operator.apply(input)));
    }

    private HilbertVector normalized(final double... values) {
        return HilbertVector.of(values).normalized();
    }
}
