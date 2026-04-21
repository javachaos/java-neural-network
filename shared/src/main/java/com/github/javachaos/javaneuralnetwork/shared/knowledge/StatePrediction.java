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

import java.util.Objects;
import java.util.Optional;

import com.github.javachaos.javaneuralnetwork.shared.hilbert.HilbertVector;

/**
 * Immutable result of asking a state-transition model to predict a world state.
 */
public final class StatePrediction {

    private final String name;
    private final WorldState before;
    private final WorldState predicted;
    private final WorldState observed;
    private final HilbertVector residual;

    private StatePrediction(
            final String name,
            final WorldState before,
            final WorldState predicted,
            final WorldState observed) {
        this.name = requireName(name);
        this.before = Objects.requireNonNull(before, "Before state cannot be null.");
        this.predicted = Objects.requireNonNull(predicted, "Predicted state cannot be null.");
        this.observed = observed;
        if (observed != null) {
            if (observed.dimension() != predicted.dimension()) {
                throw new IllegalArgumentException("Observed state dimension must match predicted state.");
            }
            this.residual = observed.state().subtract(predicted.state());
        } else {
            this.residual = null;
        }
    }

    /**
     * Creates a prediction without an observed outcome.
     *
     * @param name the prediction name
     * @param before the input world state
     * @param predicted the predicted output world state
     * @return a prediction record
     */
    public static StatePrediction forecast(
            final String name,
            final WorldState before,
            final WorldState predicted) {
        return new StatePrediction(name, before, predicted, null);
    }

    /**
     * Creates a prediction with an observed outcome.
     *
     * @param name the prediction name
     * @param before the input world state
     * @param predicted the predicted output world state
     * @param observed the observed output world state
     * @return an observed prediction record
     */
    public static StatePrediction observed(
            final String name,
            final WorldState before,
            final WorldState predicted,
            final WorldState observed) {
        return new StatePrediction(name, before, predicted,
                Objects.requireNonNull(observed, "Observed state cannot be null."));
    }

    /**
     * Scores the predicted state against a goal subspace.
     *
     * @param goal the goal to score
     * @return predicted-state satisfaction
     */
    public double predictedSatisfaction(final GoalSubspace goal) {
        Objects.requireNonNull(goal, "Goal cannot be null.");
        return goal.satisfaction(predicted);
    }

    /**
     * Scores the observed state against a goal subspace.
     *
     * @param goal the goal to score
     * @return observed-state satisfaction
     */
    public double observedSatisfaction(final GoalSubspace goal) {
        Objects.requireNonNull(goal, "Goal cannot be null.");
        return goal.satisfaction(requireObserved());
    }

    /**
     * Computes predicted progress from the before-state.
     *
     * @param goal the goal to score
     * @return predicted progress
     */
    public double predictedProgress(final GoalSubspace goal) {
        Objects.requireNonNull(goal, "Goal cannot be null.");
        return goal.progress(before, predicted);
    }

    /**
     * Computes observed progress from the before-state.
     *
     * @param goal the goal to score
     * @return observed progress
     */
    public double observedProgress(final GoalSubspace goal) {
        Objects.requireNonNull(goal, "Goal cannot be null.");
        return goal.progress(before, requireObserved());
    }

    /**
     * @return true when an observed state is present
     */
    public boolean hasObserved() {
        return observed != null;
    }

    /**
     * @return squared prediction error
     */
    public double errorSquared() {
        return requireResidual().normSquared();
    }

    /**
     * @return prediction error norm
     */
    public double errorNorm() {
        return requireResidual().norm();
    }

    /**
     * @return prediction error normalized by observed-state norm
     */
    public double normalizedError() {
        WorldState actual = requireObserved();
        double denominator = actual.state().norm();
        if (denominator == 0.0) {
            return errorNorm();
        }
        return errorNorm() / denominator;
    }

    /**
     * @return the prediction name
     */
    public String name() {
        return name;
    }

    /**
     * @return the input world state
     */
    public WorldState before() {
        return before;
    }

    /**
     * @return the predicted output world state
     */
    public WorldState predicted() {
        return predicted;
    }

    /**
     * @return the observed output world state, when known
     */
    public Optional<WorldState> observed() {
        return Optional.ofNullable(observed);
    }

    /**
     * @return observed minus predicted, when an observation is known
     */
    public Optional<HilbertVector> residual() {
        return Optional.ofNullable(residual);
    }

    private WorldState requireObserved() {
        if (observed == null) {
            throw new IllegalStateException("Prediction has no observed state.");
        }
        return observed;
    }

    private HilbertVector requireResidual() {
        if (residual == null) {
            throw new IllegalStateException("Prediction has no observed residual.");
        }
        return residual;
    }

    private static String requireName(final String name) {
        Objects.requireNonNull(name, "Prediction name cannot be null.");
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Prediction name cannot be blank.");
        }
        return trimmed;
    }
}
