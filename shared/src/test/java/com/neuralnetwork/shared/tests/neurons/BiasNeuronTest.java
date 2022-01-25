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

import com.neuralnetwork.shared.neurons.BiasNeuron;
import com.neuralnetwork.shared.values.Constants;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Fred
 *
 */
public class BiasNeuronTest {

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.BiasNeuron
	 * #BiasNeuron()}.
	 */
	@Test
	public final void testBiasNeuron() {
		assertEquals(
				1.0, new BiasNeuron().getValue(),
				Constants.TEN * Math.ulp(1.0));
	}
	
	/**
	 * Test feedforward methods.
	 */
	@Test
	public final void testFeedForward() {
		BiasNeuron bn = new BiasNeuron();
		bn.feedforward(null);
		bn.feedforward(1.0, null);
		assertEquals(1, bn.getOutputValue(), Math.ulp(1));
	}
	
	/**
	 * Test toString.
	 */
	@Test
	public final void testToString() {
		BiasNeuron bn = new BiasNeuron();
		assertEquals(bn.toString(), "BN(1.0)");
	}

}
