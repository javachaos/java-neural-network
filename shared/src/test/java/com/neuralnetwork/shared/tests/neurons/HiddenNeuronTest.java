/*******************************************************************************
 * Copyright (c) 2013 Fred Laderoute.
 * All rights reserved. This program and the accompanying
 * materials are made available under the terms of the GNU 
 * Public License v3.0 which accompanies this distribution, 
 * and is available at http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *      Fred Laderoute - initial API and implementation
 *******************************************************************************/
/**
 * 
 */
package com.neuralnetwork.shared.tests.neurons;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.neuralnetwork.shared.neurons.HiddenNeuron;
import com.neuralnetwork.shared.values.OneValue;

/**
 * @author Fred
 *
 */
public class HiddenNeuronTest {

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.HiddenNeuron#toString()}.
	 */
	@Test
	public final void testToString() {
		HiddenNeuron h = new HiddenNeuron();
		h.setValue(new OneValue());
		assertEquals(h.toString(), "HN(1.0) ");
	}

}
