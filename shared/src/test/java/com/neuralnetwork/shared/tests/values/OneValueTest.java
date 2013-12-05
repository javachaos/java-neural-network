/**
 * 
 */
package com.neuralnetwork.shared.tests.values;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.neuralnetwork.shared.values.OneValue;

/**
 * @author Fred
 *
 */
public class OneValueTest {

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.values.OneValue#OneValue()}.
	 */
	@Test
	public final void testOneValue() {
		assertEquals(new OneValue().getValue(), 
				1.0, 10 * Math.ulp(1.0));
	}

}
