/*******************************************************************************
 * Copyright (c) 2014 Fred Laderoute.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *     Fred Laderoute - initial API and implementation
 ******************************************************************************/
/**
 * 
 */
package com.neuralnetwork.shared.tests.values;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.neuralnetwork.shared.values.Constants;
import com.neuralnetwork.shared.values.DoubleValue;
import com.neuralnetwork.shared.values.OneValue;

/**
 * @author Fred
 *
 */
public class DoubleValueTest {

	/**
	 * Test Const.
	 */
	private static final double D_123_123234442 = 123.123234442;
	
	/**
	 * Test Const.
	 */
	private static final double D_0_32432 = 0.32432;
	
	/**
	 * Test Const.
	 */
	private static final double INITIAL_VALUE = -11.00000;
	
	/**
	 * Test Const.
	 */
	private static final double D_11_32432 = 11.32432;
	
	/**
	 * Testing Constant.
	 */
	private static final double D_10_32432 = 10.32432;

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.values.DoubleValue#DoubleValue()}.
	 */
	@Test
	public final void testDoubleValue() {
		DoubleValue v = new DoubleValue();
		assertEquals(v.getValue(), 0.0, Constants.TEN * Math.ulp(v.getValue()));
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.values.DoubleValue#DoubleValue(double)}.
	 */
	@Test
	public final void testDoubleValueDouble() {
		DoubleValue v = new DoubleValue(D_10_32432);
		assertEquals(v.getValue(), 
				D_10_32432, Constants.TEN * Math.ulp(v.getValue()));
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.values.DoubleValue#setSign(boolean)}.
	 */
	@Test
	public final void testSetSign() {
		DoubleValue v = new DoubleValue(D_10_32432);
		assertEquals(v.getValue(), D_10_32432, Constants.TEN 
				* Math.ulp(v.getValue()));
		v.setSign(true);
		assertTrue(v.getSign());
		v.setSign(false);
		assertFalse(v.getSign());
		v = new DoubleValue(-1.0);
		v.setSign(true);
		assertTrue(v.getSign());
		v.setSign(false);
		assertFalse(v.getSign());
		
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.values.DoubleValue#getSign()}.
	 */
	@Test
	public final void testGetSign() {
		DoubleValue v = new DoubleValue(D_10_32432);
		assertEquals(v.getValue(), D_10_32432, Constants.TEN
				* Math.ulp(v.getValue()));
		v.setSign(true);
		assertTrue(v.getSign());
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.values.DoubleValue#updateValue(
	 * com.neuralnetwork.shared.values.IValue)}.
	 */
	@Test
	public final void testUpdateValue() {		
		DoubleValue v = new DoubleValue(D_10_32432);
		assertEquals(v.getValue(), D_10_32432, Constants.TEN
				* Math.ulp(v.getValue()));
		v.updateValue(new OneValue());
		assertEquals(v.getValue(), D_11_32432, Constants.TEN
				* Math.ulp(v.getValue()));
		v.updateValue(new DoubleValue(INITIAL_VALUE));
		assertEquals(v.getValue(), D_0_32432, Constants.TEN
				* Math.ulp(v.getValue()));
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.values.DoubleValue#toString()}.
	 */
	@Test
	public final void testToString() {
		assertEquals(new DoubleValue(D_123_123234442).toString(), 
				"123.123234442");
	}

}
