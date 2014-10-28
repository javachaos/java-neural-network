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

import org.junit.Test;

import com.neuralnetwork.shared.util.NetworkConfig;
import com.neuralnetwork.shared.util.SimpleNetworkConfigs;

/**
 * @author Fred
 *
 */
public class SimpleNetworkConfigTest {

	/**
	 * Test simple network config.
	 */
	@Test
	public final void testSimpleNetwork() {
		NetworkConfig snc = SimpleNetworkConfigs.CONFIG_5_4_3_4_5;
		snc.getLayerSizes();
		snc = null;
	}

}
