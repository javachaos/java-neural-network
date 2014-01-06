/*******************************************************************************
 * Copyright (c) 2014 Fred Laderoute.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *     Fred Laderoute - initial API and implementation
 *******************************************************************************/
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
