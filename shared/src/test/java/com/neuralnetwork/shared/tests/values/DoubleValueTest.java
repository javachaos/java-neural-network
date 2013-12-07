/*******************************************************************************
 * Copyright (c) 2013 Fred Laderoute.
 * All rights reserved. This program and the accompanying
 * materials are made available under the terms of the GNU 
 * Public License v3.0 which accompanies this distribution, 
 * and is available at http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *      Fred Laderoute - initial API and implementation
 *******************************************************************************/
/**
 * 
 */
package com.neuralnetwork.shared.tests.values;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.neuralnetwork.shared.values.DoubleValue;
import com.neuralnetwork.shared.values.OneValue;

/**
 * @author Fred
 *
 */
public class DoubleValueTest {

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.values.DoubleValue#DoubleValue()}.
	 */
	@Test
	public void testDoubleValue() {
		DoubleValue v = new DoubleValue();
		assertEquals(v.getValue(), 0.0, 10 * Math.ulp(v.getValue()));
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.values.DoubleValue#DoubleValue(double)}.
	 */
	@Test
	public void testDoubleValueDouble() {
		DoubleValue v = new DoubleValue(10.32432);
		assertEquals(v.getValue(), 10.32432, 10 * Math.ulp(v.getValue()));
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.values.DoubleValue#setSign(boolean)}.
	 */
	@Test
	public void testSetSign() {
		DoubleValue v = new DoubleValue(10.32432);
		assertEquals(v.getValue(), 10.32432, 10 * Math.ulp(v.getValue()));
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
	public void testGetSign() {
		DoubleValue v = new DoubleValue(10.32432);
		assertEquals(v.getValue(), 10.32432, 10 * Math.ulp(v.getValue()));
		v.setSign(true);
		assertTrue(v.getSign());
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.values.DoubleValue#updateValue(
	 * com.neuralnetwork.shared.values.IValue)}.
	 */
	@Test
	public void testUpdateValue() {		
		DoubleValue v = new DoubleValue(10.32432);
		assertEquals(v.getValue(), 10.32432, 10 * Math.ulp(v.getValue()));
		v.updateValue(new OneValue());
		assertEquals(v.getValue(), 11.32432, 10 * Math.ulp(v.getValue()));
		v.updateValue(new DoubleValue(-11.00000));
		assertEquals(v.getValue(), 0.32432, 10 * Math.ulp(v.getValue()));
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.values.DoubleValue#toString()}.
	 */
	@Test
	public void testToString() {
		assertEquals(new DoubleValue(123.123234442).toString(), "123.123234442");
	}

}
