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

import com.github.javachaos.javaneuralnetwork.shared.neurons.AbstractInputNeuronImpl;
import com.github.javachaos.javaneuralnetwork.shared.neurons.InputNeuronImpl;
import com.github.javachaos.javaneuralnetwork.shared.neurons.NeuronType;
import com.github.javachaos.javaneuralnetwork.shared.tests.values.TestConstants;

/**
 * @author Fred
 *
 */
class AbstractInputNeuronImplTest {

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.AbstractInputNeuron
	 * #AbstractInputNeuron()}.
	 */
	@Test
	final void testAbstractInputNeuron() {
		AbstractInputNeuronImpl ai = new InputNeuronImpl();
		assertEquals(NeuronType.INPUT, ai.getType());
		assertNotEquals(0.0, ai.getValue());
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.AbstractInputNeuron
	 * #AbstractInputNeuron(com.neuralnetwork
	 * .shared.values.IValue)}.
	 */
	@Test
	final void testAbstractInputNeuronIValueOfQ() {
		AbstractInputNeuronImpl ai = new InputNeuronImpl(1.0);
		assertEquals(NeuronType.INPUT, ai.getType());
		assertEquals(1.0, ai.getValue(), TestConstants.DELTA);
	}

}
