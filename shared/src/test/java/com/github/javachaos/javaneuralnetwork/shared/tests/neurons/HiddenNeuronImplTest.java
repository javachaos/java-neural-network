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

import org.junit.jupiter.api.Test;

import com.github.javachaos.javaneuralnetwork.shared.neurons.HiddenNeuronImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Fred
 *
 */
class HiddenNeuronImplTest {

	@Test
	final void testToString() {
		HiddenNeuronImpl h = new HiddenNeuronImpl();
		h.setValue(1.0);
		assertEquals("HN(1.0) ", h.toString());
	}

}
