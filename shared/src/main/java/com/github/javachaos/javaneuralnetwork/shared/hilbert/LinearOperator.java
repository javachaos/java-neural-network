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
 * Immutable finite-dimensional linear operator.
 */
public final class LinearOperator {

    private final double[][] values;
    private final int rows;
    private final int columns;

    private LinearOperator(final double[][] matrix) {
        this.values = matrix;
        this.rows = matrix.length;
        this.columns = matrix[0].length;
    }

    /**
     * Creates a linear operator from matrix rows.
     *
     * @param matrix the matrix values
     * @return a new linear operator
     */
    public static LinearOperator of(final double[][] matrix) {
        Objects.requireNonNull(matrix, "Matrix cannot be null.");
        if (matrix.length == 0) {
            throw new IllegalArgumentException("Operator must have at least one row.");
        }
        Objects.requireNonNull(matrix[0], "Matrix row cannot be null.");
        if (matrix[0].length == 0) {
            throw new IllegalArgumentException("Operator must have at least one column.");
        }
        int columns = matrix[0].length;
        double[][] copy = new double[matrix.length][columns];
        for (int i = 0; i < matrix.length; i++) {
            Objects.requireNonNull(matrix[i], "Matrix row cannot be null.");
            if (matrix[i].length != columns) {
                throw new IllegalArgumentException("Operator rows must have equal length.");
            }
            for (int j = 0; j < columns; j++) {
                requireFinite(matrix[i][j]);
                copy[i][j] = matrix[i][j];
            }
        }
        return new LinearOperator(copy);
    }

    /**
     * Creates an identity operator.
     *
     * @param dimension the space dimension
     * @return an identity operator
     */
    public static LinearOperator identity(final int dimension) {
        requirePositiveDimension(dimension);
        double[][] matrix = new double[dimension][dimension];
        for (int i = 0; i < dimension; i++) {
            matrix[i][i] = 1.0;
        }
        return new LinearOperator(matrix);
    }

    /**
     * Creates a zero operator.
     *
     * @param rows the number of rows
     * @param columns the number of columns
     * @return a zero operator
     */
    public static LinearOperator zero(final int rows, final int columns) {
        requirePositiveDimension(rows);
        requirePositiveDimension(columns);
        return new LinearOperator(new double[rows][columns]);
    }

    /**
     * Creates the projector onto the subspace spanned by one vector.
     *
     * @param vector the vector spanning the subspace
     * @return a projection operator
     */
    public static LinearOperator projector(final HilbertVector vector) {
        Objects.requireNonNull(vector, "Vector cannot be null.");
        HilbertVector unit = vector.normalized();
        double[][] matrix = new double[unit.dimension()][unit.dimension()];
        for (int i = 0; i < unit.dimension(); i++) {
            for (int j = 0; j < unit.dimension(); j++) {
                matrix[i][j] = unit.get(i) * unit.get(j);
            }
        }
        return new LinearOperator(matrix);
    }

    /**
     * @return the number of output dimensions
     */
    public int rows() {
        return rows;
    }

    /**
     * @return the number of input dimensions
     */
    public int columns() {
        return columns;
    }

    /**
     * Reads one matrix value.
     *
     * @param row the row
     * @param column the column
     * @return the matrix value
     */
    public double get(final int row, final int column) {
        return values[row][column];
    }

    /**
     * Applies this operator to a vector.
     *
     * @param vector the input vector
     * @return the transformed vector
     */
    public HilbertVector apply(final HilbertVector vector) {
        Objects.requireNonNull(vector, "Vector cannot be null.");
        if (columns != vector.dimension()) {
            throw new IllegalArgumentException("Operator and vector dimensions do not match.");
        }
        double[] result = new double[rows];
        for (int i = 0; i < rows; i++) {
            double sum = 0.0;
            for (int j = 0; j < columns; j++) {
                sum += values[i][j] * vector.get(j);
            }
            result[i] = sum;
        }
        return HilbertVector.of(result);
    }

    /**
     * Returns the composition of this operator after another operator.
     *
     * @param before the operator applied first
     * @return the composed operator
     */
    public LinearOperator compose(final LinearOperator before) {
        Objects.requireNonNull(before, "Operator cannot be null.");
        if (columns != before.rows) {
            throw new IllegalArgumentException("Operator dimensions do not compose.");
        }
        double[][] result = new double[rows][before.columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < before.columns; j++) {
                double sum = 0.0;
                for (int k = 0; k < columns; k++) {
                    sum += values[i][k] * before.values[k][j];
                }
                result[i][j] = sum;
            }
        }
        return new LinearOperator(result);
    }

    /**
     * Adds two operators.
     *
     * @param other the other operator
     * @return the operator sum
     */
    public LinearOperator add(final LinearOperator other) {
        requireSameShape(other);
        double[][] result = new double[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                result[i][j] = values[i][j] + other.values[i][j];
            }
        }
        return new LinearOperator(result);
    }

    /**
     * Subtracts two operators.
     *
     * @param other the other operator
     * @return the operator difference
     */
    public LinearOperator subtract(final LinearOperator other) {
        requireSameShape(other);
        return add(other.scale(-1.0));
    }

    /**
     * Multiplies an operator by a scalar.
     *
     * @param scalar the scalar value
     * @return the scaled operator
     */
    public LinearOperator scale(final double scalar) {
        requireFinite(scalar);
        double[][] result = new double[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                result[i][j] = values[i][j] * scalar;
            }
        }
        return new LinearOperator(result);
    }

    /**
     * @return the transpose of this operator
     */
    public LinearOperator transpose() {
        double[][] result = new double[columns][rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                result[j][i] = values[i][j];
            }
        }
        return new LinearOperator(result);
    }

    /**
     * Computes the tensor product of two operators.
     *
     * @param other the other operator
     * @return the tensor product operator
     */
    public LinearOperator tensorProduct(final LinearOperator other) {
        Objects.requireNonNull(other, "Operator cannot be null.");
        double[][] result = new double[rows * other.rows][columns * other.columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                for (int k = 0; k < other.rows; k++) {
                    for (int l = 0; l < other.columns; l++) {
                        result[i * other.rows + k][j * other.columns + l] =
                                values[i][j] * other.values[k][l];
                    }
                }
            }
        }
        return new LinearOperator(result);
    }

    /**
     * Tests whether this operator is self-adjoint within a tolerance.
     *
     * @param tolerance comparison tolerance
     * @return true when this operator equals its transpose
     */
    public boolean isSelfAdjoint(final double tolerance) {
        if (rows != columns) {
            return false;
        }
        requireNonNegativeTolerance(tolerance);
        for (int i = 0; i < rows; i++) {
            for (int j = i + 1; j < columns; j++) {
                if (Math.abs(values[i][j] - values[j][i]) > tolerance) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Tests whether this operator is idempotent within a tolerance.
     *
     * @param tolerance comparison tolerance
     * @return true when applying this operator twice is the same as once
     */
    public boolean isIdempotent(final double tolerance) {
        if (rows != columns) {
            return false;
        }
        requireNonNegativeTolerance(tolerance);
        LinearOperator squared = compose(this);
        return closeTo(squared, tolerance);
    }

    /**
     * Compares two operators within a tolerance.
     *
     * @param other the other operator
     * @param tolerance comparison tolerance
     * @return true when every coordinate is within tolerance
     */
    public boolean closeTo(final LinearOperator other, final double tolerance) {
        requireSameShape(other);
        requireNonNegativeTolerance(tolerance);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                if (Math.abs(values[i][j] - other.values[i][j]) > tolerance) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * @return a defensive copy of the matrix values
     */
    public double[][] toArray() {
        double[][] copy = new double[rows][columns];
        for (int i = 0; i < rows; i++) {
            copy[i] = Arrays.copyOf(values[i], columns);
        }
        return copy;
    }

    private void requireSameShape(final LinearOperator other) {
        Objects.requireNonNull(other, "Operator cannot be null.");
        if (rows != other.rows || columns != other.columns) {
            throw new IllegalArgumentException("Operator dimensions do not match.");
        }
    }

    private static void requirePositiveDimension(final int dimension) {
        if (dimension <= 0) {
            throw new IllegalArgumentException("Dimension must be positive.");
        }
    }

    private static void requireNonNegativeTolerance(final double tolerance) {
        requireFinite(tolerance);
        if (tolerance < 0.0) {
            throw new IllegalArgumentException("Tolerance cannot be negative.");
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
        if (!(obj instanceof LinearOperator other)) {
            return false;
        }
        return Arrays.deepEquals(values, other.values);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(values);
    }

    @Override
    public String toString() {
        return "LinearOperator" + Arrays.deepToString(values);
    }
}
