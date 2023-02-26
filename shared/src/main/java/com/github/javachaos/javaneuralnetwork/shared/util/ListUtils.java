package com.github.javachaos.javaneuralnetwork.shared.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Vector Utils class.
 */
public final class ListUtils {

	private ListUtils() {
	}

	public static List<Double> getRandomVector(final int featureSize) {
		ArrayList<Double> randVector = new ArrayList<>(featureSize);
		for (int i = 0; i < featureSize; i++) {
			randVector.add(Math.random());
		}
		
		return randVector;
	}
	
}
