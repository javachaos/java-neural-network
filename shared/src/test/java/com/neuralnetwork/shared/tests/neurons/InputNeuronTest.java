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
package com.neuralnetwork.shared.tests.neurons;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

import com.neuralnetwork.shared.neurons.IInputNeuron;
import com.neuralnetwork.shared.neurons.InputNeuron;
import com.neuralnetwork.shared.neurons.NeuronType;
import com.neuralnetwork.shared.tests.values.TestConstants;

/**
 * @author Fred
 *
 */
class InputNeuronTest {

	/**
	 * Test method for {@link com.neuralnetwork.shared
	 * .neurons.InputNeuron
	 * #InputNeuron(com.neuralnetwork.shared.values.IValue)}.
	 */
	@Test
	final void testInputNeuronIValueOfQ() {
		IInputNeuron n = new InputNeuron(1.0);
		assertEquals(NeuronType.INPUT, n.getType());
		assertEquals(1.0, n.getValue(), TestConstants.DELTA);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.InputNeuron
	 * #InputNeuron()}.
	 */
	@Test
	final void testInputNeuron() {
		IInputNeuron n = new InputNeuron();
		assertEquals(NeuronType.INPUT, n.getType());
		assertNotEquals(0.0, n.getValue());
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.InputNeuron
	 * #toString()}.
	 */
	@Test
	final void testToString() {
		IInputNeuron n = new InputNeuron();
		assertNotEquals("IN(0.0) ", n.toString());
	}

}
