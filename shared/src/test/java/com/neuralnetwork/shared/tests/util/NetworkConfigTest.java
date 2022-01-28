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
package com.neuralnetwork.shared.tests.util;

import com.neuralnetwork.shared.util.NetworkConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Fred
 *
 */
class NetworkConfigTest {

	/**
	 * Testing constant.
	 */
	private static final int THREE = 3;
	
	/**
	 * Testing constant.
	 */
	private static final int FIVE = 5;

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.util.NetworkConfig#NetworkConfig(int, int, int[])}.
	 */
	@Test
	final void testNetworkConfig() {
		NetworkConfig conf = new NetworkConfig(FIVE, FIVE, 
				new int[]{THREE, 2, THREE});
		conf.getNumInputs();
		try {
			conf = new NetworkConfig(
					-FIVE, FIVE, new int[]{THREE, 2, THREE});
			conf.getNumInputs();
		} catch (IllegalArgumentException e) {
			assertEquals("Error bad configuration.", e.getMessage());
		}
		
		try {
			conf = new NetworkConfig(
					FIVE, -FIVE, new int[]{THREE, 2, THREE});
			conf.getNumInputs();
		} catch (IllegalArgumentException e1) {
			assertEquals("Error bad configuration.", e1.getMessage());
		}
		
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.util.NetworkConfig#getNumInputs()}.
	 */
	@Test
	final void testGetNumInputs() {
		NetworkConfig conf = new NetworkConfig(FIVE, FIVE,
				new int[]{THREE, 2, THREE});
		assertEquals(FIVE, conf.getNumInputs());
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.util.NetworkConfig#getLayerSizes()}.
	 */
	@Test
	final void testGetLayerSizes() {
		NetworkConfig conf = new NetworkConfig(FIVE, FIVE,
				new int[]{THREE, 2, THREE});
		assertEquals(THREE, conf.getLayerSizes()[0]);
		assertEquals(2, conf.getLayerSizes()[1]);
		assertEquals(THREE, conf.getLayerSizes()[2]);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.util.NetworkConfig#setLayerSizes(int[])}.
	 */
	@Test
	final void testSetLayerSizes() {
		NetworkConfig conf = new NetworkConfig(FIVE, FIVE,
				new int[]{THREE, 2, THREE});
		conf.setLayerSizes(new int[]{0, 1, 0});
		assertEquals(0, conf.getLayerSizes()[0]);
		assertEquals(1, conf.getLayerSizes()[1]);
		assertEquals(0, conf.getLayerSizes()[2]);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.util.NetworkConfig#getNumHiddenLayers()}.
	 */
	@Test
	final void testGetNumHiddenLayers() {
		NetworkConfig conf = new NetworkConfig(FIVE, FIVE,
				new int[]{THREE, 2, THREE});
		assertEquals(THREE, conf.getNumHiddenLayers());
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.util.NetworkConfig#getNumOuputs()}.
	 */
	@Test
	final void testGetNumOuputs() {
		NetworkConfig conf = new NetworkConfig(FIVE, FIVE,
				new int[]{THREE, 2, THREE});
		assertEquals(FIVE, conf.getNumOuputs());
	}

}
