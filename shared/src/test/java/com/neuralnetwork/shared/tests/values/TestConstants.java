package com.neuralnetwork.shared.tests.values;

import com.neuralnetwork.shared.values.Constants;

/**
 * Test Utilities.
 * @author fred
 *
 */
public final class TestConstants {

	/**
	 * Delta constant for floating-point comparison. 
	 */
	public static final Double DELTA = Constants.TEN_D * Math.ulp(1.0);
	
	/**
	 * Unused.
	 */
	private TestConstants() {
	}
}
