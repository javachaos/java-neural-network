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
import com.github.javachaos.javaneuralnetwork.shared.util.SimpleNetworkConfigs;

import static org.junit.jupiter.api.Assertions.*;

class SimpleNeuralNetworkConfigTest {

	@Test
	final void testSimpleNetwork() {
		NetworkConfig snc = SimpleNetworkConfigs.CONFIG_5_4_3_4_5;
		assertEquals(3, snc.getNumHiddenLayers());
		int[] sizes = snc.getLayerSizes();
		assertNotNull(sizes);
		assertArrayEquals(new int[]{4, 3, 4}, sizes);
	}

}
