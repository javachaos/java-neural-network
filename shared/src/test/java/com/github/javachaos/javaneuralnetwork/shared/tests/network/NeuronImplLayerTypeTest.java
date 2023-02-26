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
package com.github.javachaos.javaneuralnetwork.shared.tests.network;

import org.junit.jupiter.api.Test;

import com.github.javachaos.javaneuralnetwork.shared.network.LayerType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class NeuronImplLayerTypeTest {

	@Test
	final void test() {
		LayerType[] t = LayerType.values();
		
		for (LayerType l : t) {
			assertNotNull(l);
		}
		assertEquals(LayerType.HIDDEN, LayerType.valueOf("HIDDEN"));
		assertEquals(LayerType.INPUT, LayerType.valueOf("INPUT"));
		assertEquals(LayerType.OUTPUT, LayerType.valueOf("OUTPUT"));
	}

}
