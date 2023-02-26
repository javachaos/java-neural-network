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
}
