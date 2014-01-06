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
