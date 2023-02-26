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
package com.github.javachaos.javaneuralnetwork.shared.tests.neurons;

import org.junit.jupiter.api.Test;

import com.github.javachaos.javaneuralnetwork.shared.neurons.OutputNeuronImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OutputNeuronImplTest {

	private static final int NUM_ITER = 100000;

	@Test
	final void testOutputNeuron() {
		for (int i = 0; i < NUM_ITER; i++) {
			OutputNeuronImpl o = new OutputNeuronImpl();
			double v = o.getOutputValue();
			assertTrue(v > -1);
			assertTrue(v < 1);
		}
	}

	@Test
	final void testToString() {
		OutputNeuronImpl o = new OutputNeuronImpl();
		o.setValue(0.0);
		assertEquals("ON(0.0) ", o.toString());
	}

}
