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
package com.github.javachaos.javaneuralnetwork.shared.tests.util;

import org.junit.jupiter.api.Test;

import com.github.javachaos.javaneuralnetwork.shared.util.NetworkConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NeuralNetworkConfigTest {

	@Test
	final void testNetworkConfig() {
		int numIn;
		NetworkConfig conf = new NetworkConfig(5, 5,
				new int[]{3, 2, 3});
		numIn = conf.getNumInputs();
		assertEquals(5, numIn);
		try {
			conf = new NetworkConfig(
					-5, 5, new int[]{3, 2, 3});
			numIn = conf.getNumInputs();
			assertEquals(5, numIn);
		} catch (IllegalArgumentException e) {
			assertEquals("Error bad configuration.", e.getMessage());
		}
		
		try {
			conf = new NetworkConfig(
					5, -5, new int[]{3, 2, 3});
			numIn = conf.getNumInputs();
			assertEquals(5, numIn);
		} catch (IllegalArgumentException e1) {
			assertEquals("Error bad configuration.", e1.getMessage());
		}
		
	}

	@Test
	final void testGetNumInputs() {
		NetworkConfig conf = new NetworkConfig(5, 5,
				new int[]{3, 2, 3});
		assertEquals(5, conf.getNumInputs());
	}

	@Test
	final void testGetLayerSizes() {
		NetworkConfig conf = new NetworkConfig(5, 5,
				new int[]{3, 2, 3});
		assertEquals(3, conf.getLayerSizes()[0]);
		assertEquals(2, conf.getLayerSizes()[1]);
		assertEquals(3, conf.getLayerSizes()[2]);
	}

	@Test
	final void testSetLayerSizes() {
		NetworkConfig conf = new NetworkConfig(5, 5,
				new int[]{3, 2, 3});
		conf.setLayerSizes(new int[]{0, 1, 0});
		assertEquals(0, conf.getLayerSizes()[0]);
		assertEquals(1, conf.getLayerSizes()[1]);
		assertEquals(0, conf.getLayerSizes()[2]);
	}

	@Test
	final void testGetNumHiddenLayers() {
		NetworkConfig conf = new NetworkConfig(5, 5,
				new int[]{3, 2, 3});
		assertEquals(3, conf.getNumHiddenLayers());
	}

	@Test
	final void testGetNumOuputs() {
		NetworkConfig conf = new NetworkConfig(5, 5,
				new int[]{3, 2, 3});
		assertEquals(5, conf.getNumOuputs());
	}

}
