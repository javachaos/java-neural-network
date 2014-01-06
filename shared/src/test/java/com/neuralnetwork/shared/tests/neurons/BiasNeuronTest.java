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

import com.neuralnetwork.shared.neurons.BiasNeuron;
import com.neuralnetwork.shared.values.OneValue;

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
				new BiasNeuron().getValue(), new OneValue());
	}

}
