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
package com.github.javachaos.javaneuralnetwork.shared.knowledge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import com.github.javachaos.javaneuralnetwork.shared.hilbert.HilbertVector;
import com.github.javachaos.javaneuralnetwork.shared.hilbert.LinearOperator;

/**
 * Online learner for Hilbert-space state transitions.
 *
 * <p>The update rule is normalized least-mean-squares over state vectors:
 * {@code A' = A + rate * (target - A input) input^T / <input,input>}.
 */
public final class HilbertStateTransitionLearner implements StateTransitionModel {

    private static final double DEFAULT_STABILITY = 1.0e-12;

    private final String name;
    private final double learningRate;
    private final double stability;
    private LinearOperator operator;
    private int updates;

    private HilbertStateTransitionLearner(
            final String name,
            final LinearOperator initialOperator,
            final double learningRate,
            final double stability) {
        this.name = requireName(name);
        this.operator = Objects.requireNonNull(initialOperator, "Initial operator cannot be null.");
        this.learningRate = requirePositiveFinite(learningRate, "Learning rate");
        this.stability = requireNonNegativeFinite(stability, "Stability");
    }

    /**
     * Result of one online learning update.
     *
     * @param prediction the pre-update prediction
     * @param previousOperator the operator before learning
     * @param updatedOperator the operator after learning
     * @param errorSquared the pre-update squared prediction error
     * @param normalizedError the pre-update normalized prediction error
     */
    public record LearningStep(
            StatePrediction prediction,
            LinearOperator previousOperator,
            LinearOperator updatedOperator,
            double errorSquared,
            double normalizedError) {
        public LearningStep {
            Objects.requireNonNull(prediction, "Prediction cannot be null.");
            Objects.requireNonNull(previousOperator, "Previous operator cannot be null.");
            Objects.requireNonNull(updatedOperator, "Updated operator cannot be null.");
            requireNonNegativeFinite(errorSquared, "Error squared");
            requireNonNegativeFinite(normalizedError, "Normalized error");
        }
    }

    /**
     * Summary of a multi-epoch training run.
     *
     * @param epochs number of training passes
     * @param samples number of traces per epoch
     * @param initialMeanSquaredError mean squared error before training
     * @param finalMeanSquaredError mean squared error after training
     * @param updates total number of learner updates after training
     */
    public record TrainingReport(
            int epochs,
            int samples,
            double initialMeanSquaredError,
            double finalMeanSquaredError,
            int updates) {
        public TrainingReport {
            if (epochs <= 0) {
                throw new IllegalArgumentException("Epochs must be positive.");
            }
            if (samples <= 0) {
                throw new IllegalArgumentException("Samples must be positive.");
            }
            requireNonNegativeFinite(initialMeanSquaredError, "Initial mean squared error");
            requireNonNegativeFinite(finalMeanSquaredError, "Final mean squared error");
            if (updates < 0) {
                throw new IllegalArgumentException("Updates cannot be negative.");
            }
        }
    }

    /**
     * Creates a learner with a zero initial operator.
     *
     * @param name the learner name
     * @param inputDimension the input state dimension
     * @param outputDimension the output state dimension
     * @param learningRate the update rate
     * @return a transition learner
     */
    public static HilbertStateTransitionLearner zeroInitialized(
            final String name,
            final int inputDimension,
            final int outputDimension,
            final double learningRate) {
        return of(name, LinearOperator.zero(outputDimension, inputDimension), learningRate);
    }

    /**
     * Creates a square learner with identity as the initial operator.
     *
     * @param name the learner name
     * @param dimension the state dimension
     * @param learningRate the update rate
     * @return a transition learner
     */
    public static HilbertStateTransitionLearner identityInitialized(
            final String name,
            final int dimension,
            final double learningRate) {
        return of(name, LinearOperator.identity(dimension), learningRate);
    }

    /**
     * Creates a learner from an initial operator.
     *
     * @param name the learner name
     * @param initialOperator the starting transition operator
     * @param learningRate the update rate
     * @return a transition learner
     */
    public static HilbertStateTransitionLearner of(
            final String name,
            final LinearOperator initialOperator,
            final double learningRate) {
        return withStability(name, initialOperator, learningRate, DEFAULT_STABILITY);
    }

    /**
     * Creates a learner with an explicit normalization stability constant.
     *
     * @param name the learner name
     * @param initialOperator the starting transition operator
     * @param learningRate the update rate
     * @param stability non-negative value added to input norm squared
     * @return a transition learner
     */
    public static HilbertStateTransitionLearner withStability(
            final String name,
            final LinearOperator initialOperator,
            final double learningRate,
            final double stability) {
        return new HilbertStateTransitionLearner(name, initialOperator, learningRate, stability);
    }

    @Override
    public WorldState predict(final WorldState state) {
        Objects.requireNonNull(state, "State cannot be null.");
        if (state.dimension() != inputDimension()) {
            throw new IllegalArgumentException("State dimension does not match learner input dimension.");
        }
        return WorldState.of(state.name() + " -> " + name, operator.apply(state.state()));
    }

    /**
     * Learns from one observed world-state transition.
     *
     * @param before the input state
     * @param after the target output state
     * @return the pre-update prediction and update metadata
     */
    public LearningStep learn(final WorldState before, final WorldState after) {
        requireTrainingPair(before, after);
        StatePrediction prediction = compare(name + " update " + (updates + 1), before, after);
        HilbertVector residual = prediction.residual().orElseThrow();
        LinearOperator previous = operator;
        LinearOperator delta = outerProduct(residual, before.state())
                .scale(learningRate / (before.state().normSquared() + stability));
        operator = operator.add(delta);
        updates++;
        return new LearningStep(prediction, previous, operator,
                prediction.errorSquared(), prediction.normalizedError());
    }

    /**
     * Learns from one memory trace.
     *
     * @param trace the trace to learn from
     * @return the pre-update prediction and update metadata
     */
    public LearningStep learn(final MemoryTrace trace) {
        Objects.requireNonNull(trace, "Trace cannot be null.");
        return learn(trace.before(), trace.after());
    }

    /**
     * Trains over a collection of memory traces for several epochs.
     *
     * @param traces the transition traces
     * @param epochs the number of passes over the traces
     * @return a training summary
     */
    public TrainingReport train(final Collection<MemoryTrace> traces, final int epochs) {
        List<MemoryTrace> samples = requireTraceSamples(traces);
        if (epochs <= 0) {
            throw new IllegalArgumentException("Epochs must be positive.");
        }
        double initialError = meanSquaredError(samples);
        for (int epoch = 0; epoch < epochs; epoch++) {
            for (MemoryTrace trace : samples) {
                learn(trace);
            }
        }
        return new TrainingReport(epochs, samples.size(), initialError, meanSquaredError(samples), updates);
    }

    /**
     * Computes mean squared prediction error over traces.
     *
     * @param traces the traces to score
     * @return mean squared error per output coordinate
     */
    public double meanSquaredError(final Collection<MemoryTrace> traces) {
        List<MemoryTrace> samples = requireTraceSamples(traces);
        double sum = 0.0;
        for (MemoryTrace trace : samples) {
            requireTrainingPair(trace.before(), trace.after());
            sum += compare("mse", trace.before(), trace.after()).errorSquared() / outputDimension();
        }
        return sum / samples.size();
    }

    /**
     * @return the current learned transition operator
     */
    public LinearOperator operator() {
        return operator;
    }

    /**
     * Replaces the current learned operator.
     *
     * @param nextOperator the next operator
     */
    public void setOperator(final LinearOperator nextOperator) {
        Objects.requireNonNull(nextOperator, "Operator cannot be null.");
        if (nextOperator.rows() != outputDimension() || nextOperator.columns() != inputDimension()) {
            throw new IllegalArgumentException("Operator shape does not match learner dimensions.");
        }
        this.operator = nextOperator;
    }

    /**
     * @return the learning rate
     */
    public double learningRate() {
        return learningRate;
    }

    /**
     * @return the normalization stability constant
     */
    public double stability() {
        return stability;
    }

    /**
     * @return the number of online updates applied
     */
    public int updates() {
        return updates;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public int inputDimension() {
        return operator.columns();
    }

    @Override
    public int outputDimension() {
        return operator.rows();
    }

    private LinearOperator outerProduct(final HilbertVector left, final HilbertVector right) {
        double[][] matrix = new double[left.dimension()][right.dimension()];
        for (int i = 0; i < left.dimension(); i++) {
            for (int j = 0; j < right.dimension(); j++) {
                matrix[i][j] = left.get(i) * right.get(j);
            }
        }
        return LinearOperator.of(matrix);
    }

    private void requireTrainingPair(final WorldState before, final WorldState after) {
        Objects.requireNonNull(before, "Before state cannot be null.");
        Objects.requireNonNull(after, "After state cannot be null.");
        if (before.dimension() != inputDimension()) {
            throw new IllegalArgumentException("Before state dimension does not match learner input dimension.");
        }
        if (after.dimension() != outputDimension()) {
            throw new IllegalArgumentException("After state dimension does not match learner output dimension.");
        }
    }

    private List<MemoryTrace> requireTraceSamples(final Collection<MemoryTrace> traces) {
        Objects.requireNonNull(traces, "Traces cannot be null.");
        if (traces.isEmpty()) {
            throw new IllegalArgumentException("Traces cannot be empty.");
        }
        List<MemoryTrace> samples = new ArrayList<>(traces.size());
        for (MemoryTrace trace : traces) {
            samples.add(Objects.requireNonNull(trace, "Trace cannot be null."));
        }
        return samples;
    }

    private static String requireName(final String name) {
        Objects.requireNonNull(name, "Learner name cannot be null.");
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Learner name cannot be blank.");
        }
        return trimmed;
    }

    private static double requirePositiveFinite(final double value, final String label) {
        if (!Double.isFinite(value) || value <= 0.0) {
            throw new IllegalArgumentException(label + " must be finite and positive.");
        }
        return value;
    }

    private static double requireNonNegativeFinite(final double value, final String label) {
        if (!Double.isFinite(value) || value < 0.0) {
            throw new IllegalArgumentException(label + " must be finite and non-negative.");
        }
        return value;
    }
}
