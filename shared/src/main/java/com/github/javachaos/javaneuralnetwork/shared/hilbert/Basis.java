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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Orthonormal basis for a finite real Hilbert space or subspace.
 */
public final class Basis {

    public static final double DEFAULT_TOLERANCE = 1.0e-10;

    private final List<HilbertVector> vectors;
    private final int dimension;

    private Basis(final List<HilbertVector> vectors) {
        this.vectors = List.copyOf(vectors);
        this.dimension = vectors.get(0).dimension();
    }

    /**
     * Creates a standard basis for a finite-dimensional real Hilbert space.
     *
     * @param dimension the space dimension
     * @return the standard basis
     */
    public static Basis standard(final int dimension) {
        if (dimension <= 0) {
            throw new IllegalArgumentException("Dimension must be positive.");
        }
        List<HilbertVector> vectors = new ArrayList<>(dimension);
        for (int i = 0; i < dimension; i++) {
            vectors.add(HilbertVector.basis(dimension, i));
        }
        return new Basis(vectors);
    }

    /**
     * Creates an orthonormal basis using the default tolerance.
     *
     * @param vectors the basis vectors
     * @return an orthonormal basis
     */
    public static Basis orthonormal(final HilbertVector... vectors) {
        return orthonormal(DEFAULT_TOLERANCE, vectors);
    }

    /**
     * Creates an orthonormal basis with a caller-supplied tolerance.
     *
     * @param tolerance comparison tolerance
     * @param vectors the basis vectors
     * @return an orthonormal basis
     */
    public static Basis orthonormal(final double tolerance, final HilbertVector... vectors) {
        requireTolerance(tolerance);
        Objects.requireNonNull(vectors, "Basis vectors cannot be null.");
        if (vectors.length == 0) {
            throw new IllegalArgumentException("Basis must contain at least one vector.");
        }
        int dimension = requireVector(vectors[0]).dimension();
        List<HilbertVector> copy = new ArrayList<>(vectors.length);
        for (int i = 0; i < vectors.length; i++) {
            HilbertVector vector = requireVector(vectors[i]);
            if (vector.dimension() != dimension) {
                throw new IllegalArgumentException("Basis vector dimensions do not match.");
            }
            if (Math.abs(vector.norm() - 1.0) > tolerance) {
                throw new IllegalArgumentException("Basis vectors must have unit norm.");
            }
            for (int j = 0; j < i; j++) {
                if (Math.abs(vector.innerProduct(vectors[j])) > tolerance) {
                    throw new IllegalArgumentException("Basis vectors must be orthogonal.");
                }
            }
            copy.add(vector);
        }
        return new Basis(copy);
    }

    /**
     * @return the ambient space dimension
     */
    public int dimension() {
        return dimension;
    }

    /**
     * @return the number of basis vectors
     */
    public int size() {
        return vectors.size();
    }

    /**
     * @return true when this basis spans the whole ambient space
     */
    public boolean isComplete() {
        return size() == dimension;
    }

    /**
     * Reads one basis vector.
     *
     * @param index the basis vector index
     * @return the basis vector
     */
    public HilbertVector vector(final int index) {
        return vectors.get(index);
    }

    /**
     * Computes the coordinates of a state in this basis.
     *
     * @param state the state to express in this basis
     * @return the coordinate vector
     */
    public HilbertVector coordinatesOf(final HilbertVector state) {
        requireState(state);
        double[] coordinates = new double[size()];
        for (int i = 0; i < size(); i++) {
            coordinates[i] = vectors.get(i).innerProduct(state);
        }
        return HilbertVector.of(coordinates);
    }

    /**
     * Creates the projection operator onto the subspace spanned by this basis.
     *
     * @return the projection operator
     */
    public LinearOperator projector() {
        double[][] matrix = new double[dimension][dimension];
        for (HilbertVector vector : vectors) {
            for (int i = 0; i < dimension; i++) {
                for (int j = 0; j < dimension; j++) {
                    matrix[i][j] += vector.get(i) * vector.get(j);
                }
            }
        }
        return LinearOperator.of(matrix);
    }

    /**
     * Projects a state onto this basis' subspace.
     *
     * @param state the state to project
     * @return the projected state
     */
    public HilbertVector project(final HilbertVector state) {
        requireState(state);
        return projector().apply(state);
    }

    /**
     * Measures a state in this basis.
     *
     * @param state the state to measure
     * @return the measurement
     */
    public Measurement measure(final HilbertVector state) {
        return Measurement.of(this, state);
    }

    /**
     * @return immutable basis vectors
     */
    public List<HilbertVector> vectors() {
        return vectors;
    }

    private void requireState(final HilbertVector state) {
        Objects.requireNonNull(state, "State cannot be null.");
        if (state.dimension() != dimension) {
            throw new IllegalArgumentException("State dimension does not match basis dimension.");
        }
    }

    private static HilbertVector requireVector(final HilbertVector vector) {
        return Objects.requireNonNull(vector, "Basis vector cannot be null.");
    }

    private static void requireTolerance(final double tolerance) {
        if (!Double.isFinite(tolerance) || tolerance < 0.0) {
            throw new IllegalArgumentException("Tolerance must be finite and non-negative.");
        }
    }
}
