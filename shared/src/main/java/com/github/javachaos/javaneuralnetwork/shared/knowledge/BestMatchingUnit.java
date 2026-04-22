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

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.github.javachaos.javaneuralnetwork.shared.hilbert.HilbertVector;

/**
 * Best-matching-unit queries over Hilbert world-state prototypes.
 */
public final class BestMatchingUnit {

    private BestMatchingUnit() {
    }

    /**
     * Match between an input state and one prototype state.
     *
     * @param input the queried state
     * @param prototype the closest known state
     * @param residual input minus prototype
     * @param distanceSquared squared Hilbert-space distance
     * @param distance Hilbert-space distance
     * @param similarity inverse-distance similarity in {@code (0, 1]}
     */
    public record Match(
            WorldState input,
            WorldState prototype,
            HilbertVector residual,
            double distanceSquared,
            double distance,
            double similarity) {
        public Match {
            Objects.requireNonNull(input, "Input state cannot be null.");
            Objects.requireNonNull(prototype, "Prototype state cannot be null.");
            Objects.requireNonNull(residual, "Residual cannot be null.");
            requireNonNegativeFinite(distanceSquared, "Distance squared");
            requireNonNegativeFinite(distance, "Distance");
            requirePositiveFinite(similarity, "Similarity");
            if (input.dimension() != prototype.dimension()
                    || input.dimension() != residual.dimension()) {
                throw new IllegalArgumentException("Match dimensions must agree.");
            }
        }

        /**
         * @param threshold maximum accepted distance
         * @return true when the input is farther than the accepted distance
         */
        public boolean isNovel(final double threshold) {
            requireNonNegativeFinite(threshold, "Threshold");
            return distance > threshold;
        }
    }

    /**
     * Finds the closest compatible prototype to an input state.
     *
     * @param input the input state
     * @param prototypes candidate prototype states
     * @return the best match, if any compatible prototype exists
     */
    public static Optional<Match> find(
            final WorldState input,
            final Collection<WorldState> prototypes) {
        return rank(input, prototypes).stream().findFirst();
    }

    /**
     * Ranks compatible prototypes by BMU distance.
     *
     * @param input the input state
     * @param prototypes candidate prototype states
     * @return matches in ascending distance order
     */
    public static List<Match> rank(
            final WorldState input,
            final Collection<WorldState> prototypes) {
        Objects.requireNonNull(input, "Input state cannot be null.");
        Objects.requireNonNull(prototypes, "Prototypes cannot be null.");
        return prototypes.stream()
                .map(prototype -> Objects.requireNonNull(prototype, "Prototype state cannot be null."))
                .filter(prototype -> prototype.dimension() == input.dimension())
                .map(prototype -> match(input, prototype))
                .sorted(Comparator.comparingDouble(Match::distanceSquared)
                        .thenComparing(match -> match.prototype().name())
                        .thenComparingLong(match -> match.prototype().revision()))
                .toList();
    }

    /**
     * Scores one compatible prototype.
     *
     * @param input the input state
     * @param prototype the prototype state
     * @return match score
     */
    public static Match match(final WorldState input, final WorldState prototype) {
        Objects.requireNonNull(input, "Input state cannot be null.");
        Objects.requireNonNull(prototype, "Prototype state cannot be null.");
        if (input.dimension() != prototype.dimension()) {
            throw new IllegalArgumentException("Input and prototype dimensions do not match.");
        }
        HilbertVector residual = input.differenceFrom(prototype);
        double distanceSquared = residual.normSquared();
        double distance = Math.sqrt(distanceSquared);
        double similarity = 1.0 / (1.0 + distance);
        return new Match(input, prototype, residual, distanceSquared, distance, similarity);
    }

    private static void requirePositiveFinite(final double value, final String label) {
        if (!Double.isFinite(value) || value <= 0.0) {
            throw new IllegalArgumentException(label + " must be finite and positive.");
        }
    }

    private static void requireNonNegativeFinite(final double value, final String label) {
        if (!Double.isFinite(value) || value < 0.0) {
            throw new IllegalArgumentException(label + " must be finite and non-negative.");
        }
    }
}
