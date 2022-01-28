package com.neuralnetwork.shared.util;

import java.util.ArrayList;
import java.util.List;

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
	public static List<Double> getRandomVector(final int featureSize) {
		ArrayList<Double> randVector = new ArrayList<>(featureSize);
		for (int i = 0; i < featureSize; i++) {
			randVector.add(Math.random());
		}
		
		return randVector;
	}
	
}
