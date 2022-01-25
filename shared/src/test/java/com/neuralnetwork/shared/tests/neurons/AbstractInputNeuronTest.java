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

import com.neuralnetwork.shared.neurons.AbstractInputNeuron;
import com.neuralnetwork.shared.neurons.InputNeuron;
import com.neuralnetwork.shared.neurons.NeuronType;
import com.neuralnetwork.shared.tests.util.TestConstants;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @author Fred
 *
 */
public class AbstractInputNeuronTest {

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.AbstractInputNeuron
	 * #AbstractInputNeuron()}.
	 */
	@Test
	public final void testAbstractInputNeuron() {
		AbstractInputNeuron ai = new InputNeuron();
		assertEquals(ai.getType(), NeuronType.INPUT);
		assertFalse(ai.getValue().equals(0.0));
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.AbstractInputNeuron
	 * #AbstractInputNeuron(com.neuralnetwork
	 * .shared.values.IValue)}.
	 */
	@Test
	public final void testAbstractInputNeuronIValueOfQ() {
		AbstractInputNeuron ai = new InputNeuron(1.0);
		assertEquals(ai.getType(), NeuronType.INPUT);
		assertEquals(1.0, ai.getValue(), TestConstants.DELTA);
	}

}
