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

/**
 * A goal represented as a desired concept subspace and an optional avoided
 * concept subspace.
 */
public final class GoalSubspace {

    private final String name;
    private final ConceptSubspace desired;
    private final ConceptSubspace avoided;
    private final double desiredWeight;
    private final double avoidedWeight;

    private GoalSubspace(
            final String name,
            final ConceptSubspace desired,
            final ConceptSubspace avoided,
            final double desiredWeight,
            final double avoidedWeight) {
        this.name = requireName(name);
        this.desired = Objects.requireNonNull(desired, "Desired concept cannot be null.");
        this.avoided = avoided;
        this.desiredWeight = requirePositiveWeight(desiredWeight, "Desired weight");
        this.avoidedWeight = requireNonNegativeWeight(avoidedWeight, "Avoided weight");
        requireCompatibleConcepts();
    }

    /**
     * Creates a goal that approaches one concept.
     *
     * @param name the goal name
     * @param desired the desired concept
     * @return a goal
     */
    public static GoalSubspace approach(final String name, final ConceptSubspace desired) {
        return new GoalSubspace(name, desired, null, 1.0, 0.0);
    }

    /**
     * Creates a goal that approaches one concept and avoids another.
     *
     * @param name the goal name
     * @param desired the desired concept
     * @param avoided the avoided concept
     * @return a goal
     */
    public static GoalSubspace approachAvoiding(
            final String name,
            final ConceptSubspace desired,
            final ConceptSubspace avoided) {
        return weighted(name, desired, avoided, 1.0, 1.0);
    }

    /**
     * Creates a weighted approach/avoidance goal.
     *
     * @param name the goal name
     * @param desired the desired concept
     * @param avoided the avoided concept
     * @param desiredWeight the desired concept weight
     * @param avoidedWeight the avoided concept weight
     * @return a goal
     */
    public static GoalSubspace weighted(
            final String name,
            final ConceptSubspace desired,
            final ConceptSubspace avoided,
            final double desiredWeight,
            final double avoidedWeight) {
        return new GoalSubspace(name, desired, avoided, desiredWeight, avoidedWeight);
    }

    /**
     * Scores how well a state satisfies this goal.
     *
     * @param state the state to score
     * @return satisfaction in a weighted approach/avoidance interval
     */
    public double satisfaction(final WorldState state) {
        requireCompatibleState(state);
        double desiredScore = desired.relevance(state.state());
        double avoidedScore = avoided == null ? 0.0 : avoided.relevance(state.state());
        double denominator = desiredWeight + avoidedWeight;
        return (desiredWeight * desiredScore - avoidedWeight * avoidedScore) / denominator;
    }

    /**
     * Computes goal progress between two world states.
     *
     * @param before the earlier state
     * @param after the later state
     * @return after satisfaction minus before satisfaction
     */
    public double progress(final WorldState before, final WorldState after) {
        return satisfaction(after) - satisfaction(before);
    }

    /**
     * @param state the state to test
     * @param threshold the satisfaction threshold
     * @return true when this goal is satisfied at or above threshold
     */
    public boolean isSatisfied(final WorldState state, final double threshold) {
        requireFinite(threshold, "Threshold");
        return satisfaction(state) >= threshold;
    }

    /**
     * @return the goal name
     */
    public String name() {
        return name;
    }

    /**
     * @return the desired concept
     */
    public ConceptSubspace desired() {
        return desired;
    }

    /**
     * @return the optional avoided concept
     */
    public Optional<ConceptSubspace> avoided() {
        return Optional.ofNullable(avoided);
    }

    /**
     * @return the desired concept weight
     */
    public double desiredWeight() {
        return desiredWeight;
    }

    /**
     * @return the avoided concept weight
     */
    public double avoidedWeight() {
        return avoidedWeight;
    }

    /**
     * @return the ambient Hilbert-space dimension
     */
    public int dimension() {
        return desired.dimension();
    }

    private void requireCompatibleConcepts() {
        if (avoided == null && avoidedWeight > 0.0) {
            throw new IllegalArgumentException("Avoided concept is required when avoided weight is positive.");
        }
        if (avoided != null && desired.dimension() != avoided.dimension()) {
            throw new IllegalArgumentException("Goal concepts must share the same dimension.");
        }
    }

    private void requireCompatibleState(final WorldState state) {
        Objects.requireNonNull(state, "World state cannot be null.");
        if (state.dimension() != dimension()) {
            throw new IllegalArgumentException("World state dimension does not match goal dimension.");
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

    private static double requirePositiveWeight(final double value, final String label) {
        requireFinite(value, label);
        if (value <= 0.0) {
            throw new IllegalArgumentException(label + " must be positive.");
        }
        return value;
    }

    private static double requireNonNegativeWeight(final double value, final String label) {
        requireFinite(value, label);
        if (value < 0.0) {
            throw new IllegalArgumentException(label + " cannot be negative.");
        }
        return value;
    }

    private static void requireFinite(final double value, final String label) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(label + " must be finite.");
        }
    }
}
