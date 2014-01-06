/*******************************************************************************
 * Copyright (c) 2014 Fred Laderoute.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *     Fred Laderoute - initial API and implementation
 *******************************************************************************/
/**
 * 
 */
package com.neuralnetwork.shared.tests.neurons;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.neuralnetwork.shared.neurons.AbstractOutputNeuron;
import com.neuralnetwork.shared.neurons.NeuronType;
import com.neuralnetwork.shared.neurons.OutputNeuron;
import com.neuralnetwork.shared.values.OneValue;
import com.neuralnetwork.shared.values.ZeroValue;

/**
 * @author Fred
 *
 */
public class AbstractOutputNeuronTest {

    /**
     * Ten value used for testing.
     */
    private static final int TEN = 10;
    
	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.AbstractOutputNeuron
	 * #AbstractOutputNeuron()}.
	 */
	@Test
	public final void testAbstractOutputNeuron() {
		AbstractOutputNeuron ai = new OutputNeuron();
		ai.setValue(new ZeroValue());
		assertEquals(ai.getType(), NeuronType.OUTPUT);
		assertEquals(ai.getValue(), new ZeroValue());
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.AbstractOutputNeuron
	 * #getOutputValue()}.
	 */
	@Test
	public final void testGetOutputValue() {
		AbstractOutputNeuron ai = new OutputNeuron();
		ai.setValue(new OneValue());
		assertEquals(ai.getType(), NeuronType.OUTPUT);
		assertEquals(ai.getOutputValue(), 
		             new OneValue().getValue(), TEN * Math.ulp(1.0));
	}

}
