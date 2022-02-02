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
package com.neuralnetwork.shared.tests.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import com.neuralnetwork.shared.util.ErrorFunctions;

class ErrorFunctionsTest {

	private static final double D_0_029203605461901722 = 0.029203605461901722;
	private static final double D_0_00342 = 0.00342;
	private static final double D_0_00002313 = 0.00002313;
	private static final double D_0_0002131 = 0.0002131;
	private static final double D_0_01231 = 0.01231;
	private static final double D_0_034321 = 0.034321;
	private static final double D_0_340 = 0.340;
	private static final double D_0_03242 = 0.03242;

	@Test
	final void testMeanSquaredError() {
		ArrayList<Double> v1 = new ArrayList<>();
		v1.add(0.0);
		v1.add(0.0);
		v1.add(0.0);
		v1.add(0.0);
		ArrayList<Double> v2 = new ArrayList<>();
		v2.add(0.0);
		v2.add(0.0);
		v2.add(0.0);
		v2.add(0.0);
		assertEquals(0.0,
				ErrorFunctions.getInstance().meanSquaredError(v1, v2), 10 * Math.ulp(0.0));
		
		v1 = new ArrayList<>();
		v1.add(D_0_03242);
		v1.add(D_0_340);
		v1.add(0.0);
		v1.add(D_0_034321);
		v2 = new ArrayList<>();
		v2.add(D_0_01231);
		v2.add(D_0_0002131);
		v2.add(D_0_00002313);
		v2.add(D_0_00342);
		assertEquals(D_0_029203605461901722,
				ErrorFunctions.getInstance().meanSquaredError(v1, v2),
				10 * Math.ulp(D_0_029203605461901722));
	}

}
