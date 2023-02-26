/*******************************************************************************
 * Copyright (c) 2014 Fred .
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *     Fred  - initial API and implementation
 ******************************************************************************/
package com.github.javachaos.javaneuralnetwork.shared.tests.functions;

import org.junit.jupiter.api.Test;

import com.github.javachaos.javaneuralnetwork.shared.functions.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


class LinearFunctionTest {

	private static final int NUM_ITER = 1000;
	private static final int ACCURACY = 10;

	@Test
	final void testLinearFunction() {
		BaseFunction f = new LinearFunction();
		assertNotNull(f);
		for (int i = 0; i < NUM_ITER; i++) {
			assertEquals(f.activate(i), i, ACCURACY * Math.ulp(i));
			assertEquals(1, f.derivative(i), ACCURACY * Math.ulp(i));
		}
		f = new NullFunction();
		
		for (int i = 0; i < NUM_ITER; i++) {
			assertEquals(0, f.activate(i), ACCURACY * Math.ulp(i));
			assertEquals(0, f.derivative(i), ACCURACY * Math.ulp(i));
		}
	}

}
