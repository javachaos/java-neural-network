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

import org.junit.Test;

import com.neuralnetwork.shared.neurons.AbstractOutputNeuron;
import com.neuralnetwork.shared.neurons.NeuronType;
import com.neuralnetwork.shared.neurons.OutputNeuron;
import com.neuralnetwork.shared.tests.util.TestConstants;

/**
 * @author Fred
 *
 */
public class AbstractOutputNeuronTest {
    
	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.AbstractOutputNeuron
	 * #AbstractOutputNeuron()}.
	 */
	@Test
	public final void testAbstractOutputNeuron() {
		AbstractOutputNeuron ai = new OutputNeuron();
		ai.setValue(0.0);
		assertEquals(ai.getType(), NeuronType.OUTPUT);
		assertEquals(0.0, ai.getValue(), TestConstants.DELTA);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.AbstractOutputNeuron
	 * #getOutputValue()}.
	 */
	@Test
	public final void testGetOutputValue() {
		AbstractOutputNeuron ai = new OutputNeuron();
		ai.setValue(1.0);
		assertEquals(ai.getType(), NeuronType.OUTPUT);
		assertEquals(ai.getOutputValue(), 
		             1.0, TestConstants.DELTA);
	}

}
