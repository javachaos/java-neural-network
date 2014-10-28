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
				0.0, Constants.TEN * Math.ulp(0.0));
	}

}
