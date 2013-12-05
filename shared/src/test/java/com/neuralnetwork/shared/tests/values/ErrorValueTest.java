/**
 * 
 */
package com.neuralnetwork.shared.tests.values;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.neuralnetwork.shared.values.ErrorValue;

/**
 * @author Fred
 *
 */
public class ErrorValueTest {

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.values.ErrorValue#ErrorValue(double)}.
	 */
	@Test
	public void testErrorValue() {
		assertEquals(new ErrorValue(0.0023).getValue(),
				0.0023, 10 * Math.ulp(0.0023));
	}

}
