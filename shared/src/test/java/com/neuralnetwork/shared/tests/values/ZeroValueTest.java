/**
 * 
 */
package com.neuralnetwork.shared.tests.values;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.neuralnetwork.shared.values.ZeroValue;

/**
 * @author Fred
 *
 */
public class ZeroValueTest {

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.values.ZeroValue#ZeroValue()}.
	 */
	@Test
	public final void testZeroValue() {
		assertEquals(new ZeroValue().getValue(), 
				0.0, 10 * Math.ulp(0.0));
	}

}
