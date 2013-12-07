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
package com.neuralnetwork.shared.tests.util;

import static org.junit.Assert.*;

import java.util.Vector;

import org.junit.Test;

import com.neuralnetwork.shared.util.ErrorFunctions;

/**
 * @author Fred
 *
 */
public class ErrorFunctionsTest {

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.util.ErrorFunctions#meanSquaredError(
	 * com.neuralnetwork.shared.neurons.SOMLayer, 
	 * com.neuralnetwork.shared.neurons.SOMLayer)}.
	 */
	@Test
	public final void testMeanSquaredError() {
		Vector<Double> v1 = new Vector<Double>();
		v1.add(0.0);
		v1.add(0.0);
		v1.add(0.0);
		v1.add(0.0);
		Vector<Double> v2 = new Vector<Double>();
		v2.add(0.0);
		v2.add(0.0);
		v2.add(0.0);
		v2.add(0.0);
		assertEquals(ErrorFunctions.getInstance().meanSquaredError(v1, v2),
				0.0, 10 * Math.ulp(0.0));
		
		v1 = new Vector<Double>();
		v1.add(0.03242);
		v1.add(0.340);
		v1.add(0.0);
		v1.add(0.034321);
		v2 = new Vector<Double>();
		v2.add(0.01231);
		v2.add(0.0002131);
		v2.add(0.00002313);
		v2.add(0.00342);
		assertEquals(ErrorFunctions.getInstance().meanSquaredError(v1, v2),
				0.029203605461901722, 10 * Math.ulp(0.029203605461901722));
	}

}
