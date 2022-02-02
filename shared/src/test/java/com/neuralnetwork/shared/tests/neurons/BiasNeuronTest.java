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
package com.neuralnetwork.shared.tests.neurons;

import com.neuralnetwork.shared.neurons.BiasNeuron;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BiasNeuronTest {

	@Test
	final void testBiasNeuron() {
		assertEquals(
				1.0, new BiasNeuron().getValue(),
				10 * Math.ulp(1.0));
	}

	@Test
	final void testFeedForward() {
		BiasNeuron bn = new BiasNeuron();
		double v = bn.feedforward(null);
		double u = bn.feedforward(1.0, null);
		assertEquals(v, u, Math.ulp(1));
		assertEquals(1, bn.getOutputValue(), Math.ulp(1));
	}

	@Test
	final void testToString() {
		BiasNeuron bn = new BiasNeuron();
		assertEquals("BN(1.0)", bn.toString());
	}

}
