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
package com.neuralnetwork.shared.tests.util;

import static org.junit.Assert.assertEquals;

import java.util.Vector;

import org.junit.Test;

import com.neuralnetwork.shared.util.ErrorFunctions;
import com.neuralnetwork.shared.values.Constants;

/**
 * @author Fred
 *
 */
public class ErrorFunctionsTest {

	/**
	 * Testing constant.
	 */
	private static final double D_0_029203605461901722 = 0.029203605461901722;
	
	/**
	 * Testing constant.
	 */
	private static final double D_0_00342 = 0.00342;
	
	/**
	 * Testing constant.
	 */
	private static final double D_0_00002313 = 0.00002313;
	
	/**
	 * Testing constant.
	 */
	private static final double D_0_0002131 = 0.0002131;
	
	/**
	 * Testing constant.
	 */
	private static final double D_0_01231 = 0.01231;
	
	/**
	 * Testing constant.
	 */
	private static final double D_0_034321 = 0.034321;
	
	/**
	 * Testing constant.
	 */
	private static final double D_0_340 = 0.340;
	
	/**
	 * Testing constant.
	 */
	private static final double D_0_03242 = 0.03242;

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
				0.0, Constants.TEN * Math.ulp(0.0));
		
		v1 = new Vector<Double>();
		v1.add(D_0_03242);
		v1.add(D_0_340);
		v1.add(0.0);
		v1.add(D_0_034321);
		v2 = new Vector<Double>();
		v2.add(D_0_01231);
		v2.add(D_0_0002131);
		v2.add(D_0_00002313);
		v2.add(D_0_00342);
		assertEquals(ErrorFunctions.getInstance().meanSquaredError(v1, v2),
				D_0_029203605461901722,
				Constants.TEN * Math.ulp(D_0_029203605461901722));
	}

}
