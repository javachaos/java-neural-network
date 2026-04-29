package com.github.javachaos.javaneuralnetwork.shared.util;

import java.util.Collection;

/**
 * Math tools.
 * @author alfred
 *
 */
public final class MathTools {
	
	/**
	 * Incarnation prohibited.
	 */
	private MathTools() {
	}

	/**
	 * sum.
	 * @param col
	 * 		collection parameter.
	 * @return
	 * 		the sum.
	 */
	public static Double sum(final Collection<? extends Number> col) {
		double sum = 0;
		for (Number n : col) {
			sum += (Double) n;
		}
		return sum;
	}

	public static double[][] transpose(final double[][] matrix) {
		int m = matrix.length;
		int n = matrix[0].length;
		double[][] transposed = new double[n][m];
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				transposed[j][i] = matrix[i][j];
			}
		}
		return transposed;
	}

	public static double[][] matrixMultiply(final double[][] a, final double[][] b) {
		int m = a.length;
		int n = a[0].length;
		int p = b[0].length;
		double[][] result = new double[m][p];
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < p; j++) {
				for (int k = 0; k < n; k++) {
					result[i][j] += a[i][k] * b[k][j];
				}
			}
		}
		return result;
	}

	public static double[][] hadamardProduct(final double[][] a, final double[][] b) {
		int m = a.length;
		int n = a[0].length;
		double[][] result = new double[m][n];
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				result[i][j] = a[i][j] * b[i][j];
			}
		}
		return result;
	}

	private static final byte encodeLOG8(double value, double maxAbs) {
	    double minAbs = maxAbs / 4096.0;
	    double logRange = Math.log10(maxAbs / minAbs);
		if (Math.abs(value) < minAbs) {
			return 0;
		}

		double ax = Math.min(Math.abs(value), maxAbs);
		int mag = 1 + (int) Math.round(126.0 * Math.log(ax / minAbs) / logRange);
		mag = Math.max(1, Math.min(127, mag));
		return (byte) (value < 0 ? -mag : mag);
	}

	/**
	 * Convert a double matrix to an int8 matrix by multiplying each element by 1000 and rounding to the nearest integer.
	 * Expected input matrix values are normalized to the range [0, 1].
	 * @param matrix
	 * @return
	 */
	public static byte[][] toINT8(final double[][] matrix) {
		int m = matrix.length;
		int n = matrix[0].length;
		byte[][] result = new byte[m][n];
		for (int i = 0; i < m; i++) {
			double max = Double.MIN_VALUE;
			for (int j = 0; j < n; j++) {
				max = Math.max(max, matrix[i][j]);
			}
			for (int j = 0; j < n; j++) {
				result[i][j] = encodeLOG8(matrix[i][j], max);

			}
		}
		return result;
	}

}
