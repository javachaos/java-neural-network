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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

import com.github.javachaos.javaneuralnetwork.shared.neurons.InputNeuron;
import com.github.javachaos.javaneuralnetwork.shared.neurons.InputNeuronImpl;
import com.github.javachaos.javaneuralnetwork.shared.neurons.NeuronType;
import com.github.javachaos.javaneuralnetwork.shared.tests.values.TestConstants;

class InputNeuronImplTest {

	@Test
	final void testInputNeuronIValueOfQ() {
		InputNeuron n = new InputNeuronImpl(1.0);
		assertEquals(NeuronType.INPUT, n.getType());
		assertEquals(1.0, n.getValue(), TestConstants.DELTA);
	}

	@Test
	final void testInputNeuron() {
		InputNeuron n = new InputNeuronImpl();
		assertEquals(NeuronType.INPUT, n.getType());
		assertNotEquals(0.0, n.getValue());
	}

	@Test
	final void testToString() {
		InputNeuron n = new InputNeuronImpl();
		assertNotEquals("IN(0.0) ", n.toString());
	}

}
