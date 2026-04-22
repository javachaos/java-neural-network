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

/**
 * One action-conditioned observation of how a world changes.
 *
 * @param name transition name
 * @param before state before the action
 * @param action symbolic action label
 * @param after state after the action
 * @param reward scalar outcome attached to the transition
 * @param terminal true when the transition ended an episode
 */
public record TemporalTransition(
        String name,
        WorldState before,
        String action,
        WorldState after,
        double reward,
        boolean terminal) {

    public TemporalTransition {
        name = requireName(name, "Transition name");
        before = Objects.requireNonNull(before, "Before state cannot be null.");
        action = requireName(action, "Action");
        after = Objects.requireNonNull(after, "After state cannot be null.");
        requireFinite(reward, "Reward");
        if (before.dimension() != after.dimension()) {
            throw new IllegalArgumentException("Temporal transitions require equal before/after dimensions.");
        }
    }

    /**
     * Creates a non-terminal transition.
     *
     * @param name transition name
     * @param before state before the action
     * @param action symbolic action label
     * @param after state after the action
     * @param reward scalar outcome attached to the transition
     * @return a temporal transition
     */
    public static TemporalTransition of(
            final String name,
            final WorldState before,
            final String action,
            final WorldState after,
            final double reward) {
        return new TemporalTransition(name, before, action, after, reward, false);
    }

    /**
     * @return actual state change, after minus before
     */
    public HilbertVector change() {
        return after.differenceFrom(before);
    }

    /**
     * @return a memory trace view of this state transition
     */
    public MemoryTrace asMemoryTrace() {
        return MemoryTrace.observe(name, before, after);
    }

    private static String requireName(final String value, final String label) {
        Objects.requireNonNull(value, label + " cannot be null.");
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(label + " cannot be blank.");
        }
        return trimmed;
    }

    private static void requireFinite(final double value, final String label) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(label + " must be finite.");
        }
    }
}
