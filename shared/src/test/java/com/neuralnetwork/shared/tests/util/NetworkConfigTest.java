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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.neuralnetwork.shared.util.NetworkConfig;

/**
 * @author Fred
 *
 */
public class NetworkConfigTest {

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
	public final void testNetworkConfig() {
		NetworkConfig conf = new NetworkConfig(FIVE, FIVE, 
				new int[]{THREE, 2, THREE});
		conf.getNumInputs();
		try {
			conf = new NetworkConfig(
					-FIVE, FIVE, new int[]{THREE, 2, THREE});
			conf.getNumInputs();
		} catch (IllegalArgumentException e) {
			assertEquals(e.getMessage(), "Error bad configuration.");
		}
		
		try {
			conf = new NetworkConfig(
					FIVE, -FIVE, new int[]{THREE, 2, THREE});
			conf.getNumInputs();
		} catch (IllegalArgumentException e1) {
			assertEquals(e1.getMessage(), "Error bad configuration.");
		}
		
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.util.NetworkConfig#getNumInputs()}.
	 */
	@Test
	public final void testGetNumInputs() {
		NetworkConfig conf = new NetworkConfig(FIVE, FIVE,
				new int[]{THREE, 2, THREE});
		assertEquals(conf.getNumInputs(), FIVE);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.util.NetworkConfig#getLayerSizes()}.
	 */
	@Test
	public final void testGetLayerSizes() {
		NetworkConfig conf = new NetworkConfig(FIVE, FIVE,
				new int[]{THREE, 2, THREE});
		assertEquals(conf.getLayerSizes()[0], THREE);
		assertEquals(conf.getLayerSizes()[1], 2);
		assertEquals(conf.getLayerSizes()[2], THREE);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.util.NetworkConfig#setLayerSizes(int[])}.
	 */
	@Test
	public final void testSetLayerSizes() {
		NetworkConfig conf = new NetworkConfig(FIVE, FIVE,
				new int[]{THREE, 2, THREE});
		conf.setLayerSizes(new int[]{0, 1, 0});
		assertEquals(conf.getLayerSizes()[0], 0);
		assertEquals(conf.getLayerSizes()[1], 1);
		assertEquals(conf.getLayerSizes()[2], 0);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.util.NetworkConfig#getNumHiddenLayers()}.
	 */
	@Test
	public final void testGetNumHiddenLayers() {
		NetworkConfig conf = new NetworkConfig(FIVE, FIVE,
				new int[]{THREE, 2, THREE});
		assertEquals(conf.getNumHiddenLayers(), THREE);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.util.NetworkConfig#getNumOuputs()}.
	 */
	@Test
	public final void testGetNumOuputs() {
		NetworkConfig conf = new NetworkConfig(FIVE, FIVE,
				new int[]{THREE, 2, THREE});
		assertEquals(conf.getNumOuputs(), FIVE);
	}

}
