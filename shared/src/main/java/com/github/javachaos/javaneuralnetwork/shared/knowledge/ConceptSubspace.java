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

import com.github.javachaos.javaneuralnetwork.shared.hilbert.Basis;
import com.github.javachaos.javaneuralnetwork.shared.hilbert.HilbertVector;
import com.github.javachaos.javaneuralnetwork.shared.hilbert.LinearOperator;
import com.github.javachaos.javaneuralnetwork.shared.hilbert.Measurement;

/**
 * A named concept represented as an orthonormal subspace.
 */
public final class ConceptSubspace {

    private final String name;
    private final Basis basis;
    private final LinearOperator projector;

    private ConceptSubspace(final String name, final Basis basis) {
        this.name = name;
        this.basis = basis;
        this.projector = basis.projector();
    }

    /**
     * Creates a concept from an orthonormal spanning set.
     *
     * @param name the concept name
     * @param basisVectors the orthonormal vectors spanning the concept
     * @return a concept subspace
     */
    public static ConceptSubspace of(final String name, final HilbertVector... basisVectors) {
        return new ConceptSubspace(requireName(name), Basis.orthonormal(basisVectors));
    }

    /**
     * Creates a concept from an existing basis.
     *
     * @param name the concept name
     * @param basis the concept basis
     * @return a concept subspace
     */
    public static ConceptSubspace of(final String name, final Basis basis) {
        return new ConceptSubspace(requireName(name), Objects.requireNonNull(basis, "Basis cannot be null."));
    }

    /**
     * Projects a state onto this concept.
     *
     * @param state the state to project
     * @return the concept component of the state
     */
    public HilbertVector project(final HilbertVector state) {
        return projector.apply(state);
    }

    /**
     * Scores how much of a state lies in this concept subspace.
     *
     * @param state the state to score
     * @return a value in the interval [0, 1]
     */
    public double relevance(final HilbertVector state) {
        Objects.requireNonNull(state, "State cannot be null.");
        double normSquared = state.normSquared();
        if (normSquared == 0.0) {
            throw new IllegalArgumentException("Cannot score the zero state.");
        }
        return project(state).normSquared() / normSquared;
    }

    /**
     * Measures a state in this concept's basis.
     *
     * @param state the state to measure
     * @return the measurement result
     */
    public Measurement measure(final HilbertVector state) {
        return basis.measure(state);
    }

    /**
     * @return the ambient dimension of this concept
     */
    public int dimension() {
        return basis.dimension();
    }

    /**
     * @return the concept name
     */
    public String name() {
        return name;
    }

    /**
     * @return the basis spanning this concept
     */
    public Basis basis() {
        return basis;
    }

    /**
     * @return the projector onto this concept
     */
    public LinearOperator projector() {
        return projector;
    }

    private static String requireName(final String name) {
        Objects.requireNonNull(name, "Concept name cannot be null.");
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Concept name cannot be blank.");
        }
        return trimmed;
    }
}
