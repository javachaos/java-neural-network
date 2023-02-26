/*******************************************************************************
 * Copyright (c) 2014 Fred .
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *     Fred  - initial API and implementation
 ******************************************************************************/
package com.github.javachaos.javaneuralnetwork.shared.tests.neurons;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.github.javachaos.javaneuralnetwork.shared.neurons.NeuronType;

class NeuronImplTypeTest {

	@Test
	final void test() {
		NeuronType[] t = NeuronType.values();
		
		for (NeuronType n : t) {
			assertNotNull(n);
		}
		
		assertEquals(NeuronType.HIDDEN,
				NeuronType.valueOf("HIDDEN"));
		assertEquals(NeuronType.INPUT,
				NeuronType.valueOf("INPUT"));
		assertEquals(NeuronType.OUTPUT,
				NeuronType.valueOf("OUTPUT"));
		assertEquals(NeuronType.SOM,
				NeuronType.valueOf("SOM"));
	}

}
