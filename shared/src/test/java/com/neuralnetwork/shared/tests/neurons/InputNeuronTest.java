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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import com.neuralnetwork.shared.neurons.IInputNeuron;
import com.neuralnetwork.shared.neurons.InputNeuron;
import com.neuralnetwork.shared.neurons.NeuronType;
import com.neuralnetwork.shared.values.OneValue;
import com.neuralnetwork.shared.values.ZeroValue;

/**
 * @author Fred
 *
 */
public class InputNeuronTest {

	/**
	 * Test method for {@link com.neuralnetwork.shared
	 * .neurons.InputNeuron
	 * #InputNeuron(com.neuralnetwork.shared.values.IValue)}.
	 */
	@Test
	public final void testInputNeuronIValueOfQ() {
		IInputNeuron n = new InputNeuron(new OneValue());
		assertEquals(n.getType(), NeuronType.INPUT);
		assertEquals(n.getValue(), new OneValue());
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.InputNeuron
	 * #InputNeuron()}.
	 */
	@Test
	public final void testInputNeuron() {
		IInputNeuron n = new InputNeuron();
		assertEquals(n.getType(), NeuronType.INPUT);
		assertFalse(n.getValue().equals(new ZeroValue()));
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.InputNeuron
	 * #toString()}.
	 */
	@Test
	public final void testToString() {
		IInputNeuron n = new InputNeuron();
		assertFalse(n.toString().equals("IN(0.0) "));
	}

}
