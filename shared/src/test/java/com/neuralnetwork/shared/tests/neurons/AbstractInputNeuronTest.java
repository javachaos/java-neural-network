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

import com.neuralnetwork.shared.neurons.AbstractInputNeuron;
import com.neuralnetwork.shared.neurons.InputNeuron;
import com.neuralnetwork.shared.neurons.NeuronType;
import com.neuralnetwork.shared.tests.values.TestConstants;

/**
 * @author Fred
 *
 */
class AbstractInputNeuronTest {

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.AbstractInputNeuron
	 * #AbstractInputNeuron()}.
	 */
	@Test
	final void testAbstractInputNeuron() {
		AbstractInputNeuron ai = new InputNeuron();
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
		AbstractInputNeuron ai = new InputNeuron(1.0);
		assertEquals(NeuronType.INPUT, ai.getType());
		assertEquals(1.0, ai.getValue(), TestConstants.DELTA);
	}

}
