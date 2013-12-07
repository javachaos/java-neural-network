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
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.neuralnetwork.shared.neurons.NeuronType;

/**
 * @author Fred
 *
 */
public class NeuronTypeTest {

    /**
     * Test the NeuronType class.
     */
	@Test
	public final void test() {
		NeuronType[] t = NeuronType.values();
		
		for (NeuronType n : t) {
			assertNotNull(n);
		}
		
		assertEquals(NeuronType.valueOf("HIDDEN"),
				NeuronType.HIDDEN);
		assertEquals(NeuronType.valueOf("INPUT"),
				NeuronType.INPUT);
		assertEquals(NeuronType.valueOf("OUTPUT"),
				NeuronType.OUTPUT);
		assertEquals(NeuronType.valueOf("SOM"),
				NeuronType.SOM);
	}

}
