package com.neuralnetwork.shared.util;

import java.util.Vector;

/**
 * Vector Utils class.
 * @author alfred
 *
 */
public final class VectorUtil {

	/**
	 * Incarnation prohibited.
	 */
	private VectorUtil() {
	}
	
	/**
	 * Get random vector of size featureSize.
	 * @param featureSize
	 * 		the size of the random vector.
	 * @return
	 * 		vector of random values.
	 */
	public static Vector<Double> getRandomVector(final int featureSize) {
		Vector<Double> randVector = new Vector<Double>(featureSize);
		for (int i = 0; i < featureSize; i++) {
			randVector.add(Math.random());
		}
		
		return randVector;
	}
	
}
