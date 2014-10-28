package com.neuralnetwork.shared.util;

import java.util.Collection;

/**
 * Math tools.
 * @author alfred
 *
 */
public class MathTools {
	
	/**
	 * Incarnation prohibited.
	 */
	private MathTools() {
	}

	/**
	 * sum.
	 * @param col
	 * @return
	 * 		the sum.
	 */
	public static final Double sum(final Collection<? extends Number> col) {
		double sum = 0;
		for (Number n : col) {
			sum += (Double) n;
		}
		return sum;
	}
}
