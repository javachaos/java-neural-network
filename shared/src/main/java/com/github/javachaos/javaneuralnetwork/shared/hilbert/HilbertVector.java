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
 * Immutable coordinate representation of a vector in a finite real Hilbert
 * space.
 */
public final class HilbertVector {

    private final double[] coordinates;

    private HilbertVector(final double[] values) {
        this.coordinates = values;
    }

    /**
     * Creates a vector from real coordinates.
     *
     * @param values the coordinates of the vector
     * @return a new vector
     */
    public static HilbertVector of(final double... values) {
        Objects.requireNonNull(values, "Coordinates cannot be null.");
        if (values.length == 0) {
            throw new IllegalArgumentException("Vector dimension must be positive.");
        }
        double[] copy = Arrays.copyOf(values, values.length);
        for (double value : copy) {
            requireFinite(value);
        }
        return new HilbertVector(copy);
    }

    /**
     * Creates the zero vector for a finite-dimensional space.
     *
     * @param dimension the space dimension
     * @return a zero vector
     */
    public static HilbertVector zero(final int dimension) {
        requirePositiveDimension(dimension);
        return new HilbertVector(new double[dimension]);
    }

    /**
     * Creates a standard basis vector.
     *
     * @param dimension the space dimension
     * @param index the coordinate to set to one
     * @return a standard basis vector
     */
    public static HilbertVector basis(final int dimension, final int index) {
        requirePositiveDimension(dimension);
        if (index < 0 || index >= dimension) {
            throw new IllegalArgumentException("Basis index is outside the vector dimension.");
        }
        double[] values = new double[dimension];
        values[index] = 1.0;
        return new HilbertVector(values);
    }

    /**
     * @return the dimension of this vector
     */
    public int dimension() {
        return coordinates.length;
    }

    /**
     * Returns one coordinate.
     *
     * @param index the coordinate index
     * @return the coordinate value
     */
    public double get(final int index) {
        return coordinates[index];
    }

    /**
     * Adds two vectors.
     *
     * @param other the vector to add
     * @return the vector sum
     */
    public HilbertVector add(final HilbertVector other) {
        requireSameDimension(other);
        double[] values = new double[dimension()];
        for (int i = 0; i < values.length; i++) {
            values[i] = coordinates[i] + other.coordinates[i];
        }
        return new HilbertVector(values);
    }

    /**
     * Subtracts two vectors.
     *
     * @param other the vector to subtract
     * @return the vector difference
     */
    public HilbertVector subtract(final HilbertVector other) {
        requireSameDimension(other);
        double[] values = new double[dimension()];
        for (int i = 0; i < values.length; i++) {
            values[i] = coordinates[i] - other.coordinates[i];
        }
        return new HilbertVector(values);
    }

    /**
     * Multiplies a vector by a scalar.
     *
     * @param scalar the scalar value
     * @return the scaled vector
     */
    public HilbertVector scale(final double scalar) {
        requireFinite(scalar);
        double[] values = new double[dimension()];
        for (int i = 0; i < values.length; i++) {
            values[i] = coordinates[i] * scalar;
        }
        return new HilbertVector(values);
    }

    /**
     * Computes the real inner product.
     *
     * @param other the other vector
     * @return the inner product
     */
    public double innerProduct(final HilbertVector other) {
        requireSameDimension(other);
        double sum = 0.0;
        for (int i = 0; i < dimension(); i++) {
            sum += coordinates[i] * other.coordinates[i];
        }
        return sum;
    }

    /**
     * @return the squared norm induced by the inner product
     */
    public double normSquared() {
        return innerProduct(this);
    }

    /**
     * @return the norm induced by the inner product
     */
    public double norm() {
        return Math.sqrt(normSquared());
    }

    /**
     * @return a unit vector pointing in the same direction
     */
    public HilbertVector normalized() {
        double n = norm();
        if (n == 0.0) {
            throw new IllegalArgumentException("Cannot normalize the zero vector.");
        }
        return scale(1.0 / n);
    }

    /**
     * Projects this vector onto another vector.
     *
     * @param basisVector the vector spanning the target subspace
     * @return the projection of this vector
     */
    public HilbertVector projectOnto(final HilbertVector basisVector) {
        Objects.requireNonNull(basisVector, "Basis vector cannot be null.");
        requireSameDimension(basisVector);
        double basisNormSquared = basisVector.normSquared();
        if (basisNormSquared == 0.0) {
            throw new IllegalArgumentException("Cannot project onto the zero vector.");
        }
        return basisVector.scale(innerProduct(basisVector) / basisNormSquared);
    }

    /**
     * Computes the tensor product of this vector and another vector.
     *
     * @param other the other vector
     * @return the tensor product coordinates
     */
    public HilbertVector tensorProduct(final HilbertVector other) {
        Objects.requireNonNull(other, "Other vector cannot be null.");
        double[] values = new double[dimension() * other.dimension()];
        int index = 0;
        for (double left : coordinates) {
            for (double right : other.coordinates) {
                values[index++] = left * right;
            }
        }
        return new HilbertVector(values);
    }

    /**
     * @return a defensive copy of the coordinate array
     */
    public double[] toArray() {
        return Arrays.copyOf(coordinates, coordinates.length);
    }

    private void requireSameDimension(final HilbertVector other) {
        Objects.requireNonNull(other, "Other vector cannot be null.");
        if (dimension() != other.dimension()) {
            throw new IllegalArgumentException("Vector dimensions do not match.");
        }
    }

    private static void requirePositiveDimension(final int dimension) {
        if (dimension <= 0) {
            throw new IllegalArgumentException("Dimension must be positive.");
        }
    }

    private static void requireFinite(final double value) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("Values must be finite.");
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof HilbertVector other)) {
            return false;
        }
        return Arrays.equals(coordinates, other.coordinates);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(coordinates);
    }

    @Override
    public String toString() {
        return "HilbertVector" + Arrays.toString(coordinates);
    }
}
