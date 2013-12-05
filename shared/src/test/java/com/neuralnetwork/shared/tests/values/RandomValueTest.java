/**
 * 
 */
package com.neuralnetwork.shared.tests.values;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.neuralnetwork.shared.values.RandomValue;

/**
 * @author Fred
 *
 */
public class RandomValueTest {

	/**
	 * Number of times to run the test.
	 */
	private static final int NUM_ITER = 1000000;
	
	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.values.RandomValue#RandomValue()}.
	 */
	@Test
	public final void testRandomValue() {
		for (int i = 0; i < NUM_ITER; i++) {
			Double v = new RandomValue().getValue();
			assertTrue(v >= 0 && v < 1);
		}
	}

}
