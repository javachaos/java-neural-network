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
import com.neuralnetwork.shared.values.GenericValue;

/**
 * @author Fred
 *
 */
public class GenericValueTest {

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.values.GenericValue#hashCode()}.
	 */
	@Test
	public final void testHashCode() {
		GenericValue<?> v = new DoubleValue();
		GenericValue<?> v1 = new DoubleValue();
		assertEquals(v.hashCode(), v1.hashCode());
		v.setValue(null);
		v1.setValue(null);
		assertEquals(v.hashCode(), v1.hashCode());
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.values.GenericValue#setValue(
	 * java.lang.Number)}.
	 */
	@Test
	public final void testSetValue() {
		GenericValue<Double> v = new DoubleValue();
		GenericValue<Double> v1 = new DoubleValue();
		assertEquals(v.hashCode(), v1.hashCode());
		v.setValue(0.1);
		v1.setValue(0.1);
		assertEquals(v.getValue(), v1.getValue(),
				10 * Math.ulp(v.getValue()));
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.values.GenericValue
	 * #getValue()}.
	 */
	@Test
	public final void testGetValue() {
		GenericValue<Double> v = new DoubleValue();
		GenericValue<Double> v1 = new DoubleValue();
		assertEquals(v.hashCode(), v1.hashCode());
		v.setValue(0.1);
		v1.setValue(0.1);
		assertEquals(v.getValue(), v1.getValue(),
				10 * Math.ulp(v.getValue()));
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.values.GenericValue
	 * #equals(java.lang.Object)}.
	 */
	@Test
	public final void testEqualsObject() {
		GenericValue<Double> v = new DoubleValue();
		GenericValue<Double> v1 = new DoubleValue();
		assertEquals(v.hashCode(), v1.hashCode());
		v.setValue(0.1);
		v1.setValue(0.1);
		assertTrue(v.equals(v1));
		assertTrue(v.equals(v));
		v.setValue(null);
		assertFalse(v.equals(v1));
		assertFalse(v1.equals(null));
		v.setValue(0.001);
		assertFalse(v1.equals(v));
		v.setValue(null);
		v1.setValue(null);
		assertTrue(v1.equals(v));
	}

}
