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

import org.junit.Test;

import com.neuralnetwork.shared.values.Constants;
import com.neuralnetwork.shared.values.ErrorValue;

/**
 * @author Fred
 *
 */
public class ErrorValueTest {

	/**
	 * Testing value.
	 */
	private static final double D_0_0023 = 0.0023;

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.values.ErrorValue#ErrorValue(double)}.
	 */
	@Test
	public final void testErrorValue() {
		assertEquals(new ErrorValue(D_0_0023).getValue(),
				D_0_0023, Constants.TEN * Math.ulp(D_0_0023));
	}

}
