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
