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
package com.github.javachaos.javaneuralnetwork.shared.hilbert;

import java.util.Arrays;
import java.util.Objects;

/**
 * Result of measuring a state in an orthonormal basis.
 */
public final class Measurement {

    private final HilbertVector amplitudes;
    private final double[] probabilities;

    private Measurement(final HilbertVector amplitudes, final double[] probabilities) {
        this.amplitudes = amplitudes;
        this.probabilities = probabilities;
    }

    /**
     * Measures a state in a basis.
     *
     * @param basis the basis to measure against
     * @param state the state being measured
     * @return a measurement result
     */
    public static Measurement of(final Basis basis, final HilbertVector state) {
        Objects.requireNonNull(basis, "Basis cannot be null.");
        Objects.requireNonNull(state, "State cannot be null.");
        double normSquared = state.normSquared();
        if (normSquared == 0.0) {
            throw new IllegalArgumentException("Cannot measure the zero state.");
        }
        HilbertVector amplitudes = basis.coordinatesOf(state);
        double[] probabilities = new double[amplitudes.dimension()];
        for (int i = 0; i < probabilities.length; i++) {
            double amplitude = amplitudes.get(i);
            probabilities[i] = amplitude * amplitude / normSquared;
        }
        return new Measurement(amplitudes, probabilities);
    }

    /**
     * @return the measurement amplitudes
     */
    public HilbertVector amplitudes() {
        return amplitudes;
    }

    /**
     * Reads one measurement probability.
     *
     * @param index the measurement index
     * @return the probability
     */
    public double probability(final int index) {
        return probabilities[index];
    }

    /**
     * @return all measurement probabilities
     */
    public double[] probabilities() {
        return Arrays.copyOf(probabilities, probabilities.length);
    }

    /**
     * @return the total probability captured by the basis
     */
    public double totalProbability() {
        double sum = 0.0;
        for (double probability : probabilities) {
            sum += probability;
        }
        return sum;
    }

    /**
     * @return the index with highest probability
     */
    public int mostLikelyIndex() {
        int index = 0;
        double best = probabilities[0];
        for (int i = 1; i < probabilities.length; i++) {
            if (probabilities[i] > best) {
                best = probabilities[i];
                index = i;
            }
        }
        return index;
    }
}
