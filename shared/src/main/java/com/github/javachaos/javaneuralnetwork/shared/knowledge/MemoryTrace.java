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

import com.github.javachaos.javaneuralnetwork.shared.hilbert.HilbertVector;
import com.github.javachaos.javaneuralnetwork.shared.hilbert.LinearOperator;

/**
 * Immutable memory of a world-state transition.
 */
public final class MemoryTrace {

    private final String name;
    private final WorldState before;
    private final LinearOperator action;
    private final WorldState after;
    private final HilbertVector predictedAfter;
    private final HilbertVector residual;

    private MemoryTrace(
            final String name,
            final WorldState before,
            final LinearOperator action,
            final WorldState after) {
        this.name = requireName(name);
        this.before = Objects.requireNonNull(before, "Before state cannot be null.");
        this.action = Objects.requireNonNull(action, "Action operator cannot be null.");
        this.after = Objects.requireNonNull(after, "After state cannot be null.");
        requireActionDimensions();
        this.predictedAfter = action.apply(before.state());
        this.residual = after.state().subtract(predictedAfter);
    }

    /**
     * Creates a transition memory from an explicit action operator.
     *
     * @param name the trace name
     * @param before the state before the transition
     * @param action the transition operator
     * @param after the observed state after the transition
     * @return a memory trace
     */
    public static MemoryTrace of(
            final String name,
            final WorldState before,
            final LinearOperator action,
            final WorldState after) {
        return new MemoryTrace(name, before, action, after);
    }

    /**
     * Creates an observation trace using identity as the prediction operator.
     *
     * @param name the trace name
     * @param before the state before the transition
     * @param after the observed state after the transition
     * @return a memory trace
     */
    public static MemoryTrace observe(
            final String name,
            final WorldState before,
            final WorldState after) {
        Objects.requireNonNull(before, "Before state cannot be null.");
        Objects.requireNonNull(after, "After state cannot be null.");
        if (before.dimension() != after.dimension()) {
            throw new IllegalArgumentException("Observed states must have equal dimensions.");
        }
        return new MemoryTrace(name, before, LinearOperator.identity(before.dimension()), after);
    }

    /**
     * Replays the remembered action from another starting state.
     *
     * @param start the starting world state
     * @return the predicted next world state
     */
    public WorldState replay(final WorldState start) {
        Objects.requireNonNull(start, "Start state cannot be null.");
        return start.transform(name + " replay", action);
    }

    /**
     * Scores how much of the residual error lives in a concept subspace.
     *
     * @param concept the concept subspace
     * @return a value in the interval [0, 1]
     */
    public double residualRelevance(final ConceptSubspace concept) {
        Objects.requireNonNull(concept, "Concept cannot be null.");
        if (concept.dimension() != residual.dimension()) {
            throw new IllegalArgumentException("Concept dimension does not match residual dimension.");
        }
        if (residual.normSquared() == 0.0) {
            return 0.0;
        }
        return concept.relevance(residual);
    }

    /**
     * @return true when the residual norm is within tolerance
     */
    public boolean isWellPredicted(final double tolerance) {
        if (!Double.isFinite(tolerance) || tolerance < 0.0) {
            throw new IllegalArgumentException("Tolerance must be finite and non-negative.");
        }
        return errorNorm() <= tolerance;
    }

    /**
     * @return the state change, actual after minus before
     */
    public HilbertVector change() {
        if (before.dimension() != after.dimension()) {
            throw new IllegalArgumentException("Cannot compute direct change across different dimensions.");
        }
        return after.differenceFrom(before);
    }

    /**
     * @return squared prediction error
     */
    public double errorSquared() {
        return residual.normSquared();
    }

    /**
     * @return prediction error norm
     */
    public double errorNorm() {
        return residual.norm();
    }

    /**
     * @return prediction error normalized by the actual after-state norm
     */
    public double normalizedError() {
        double denominator = after.state().norm();
        if (denominator == 0.0) {
            return errorNorm();
        }
        return errorNorm() / denominator;
    }

    /**
     * @return the trace name
     */
    public String name() {
        return name;
    }

    /**
     * @return the state before the transition
     */
    public WorldState before() {
        return before;
    }

    /**
     * @return the transition operator
     */
    public LinearOperator action() {
        return action;
    }

    /**
     * @return the observed state after the transition
     */
    public WorldState after() {
        return after;
    }

    /**
     * @return the action-applied prediction of the after state
     */
    public HilbertVector predictedAfter() {
        return predictedAfter;
    }

    /**
     * @return the actual after state minus the predicted after state
     */
    public HilbertVector residual() {
        return residual;
    }

    private void requireActionDimensions() {
        if (action.columns() != before.dimension()) {
            throw new IllegalArgumentException("Action input dimension does not match before state.");
        }
        if (action.rows() != after.dimension()) {
            throw new IllegalArgumentException("Action output dimension does not match after state.");
        }
    }

    private static String requireName(final String name) {
        Objects.requireNonNull(name, "Name cannot be null.");
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be blank.");
        }
        return trimmed;
    }
}
