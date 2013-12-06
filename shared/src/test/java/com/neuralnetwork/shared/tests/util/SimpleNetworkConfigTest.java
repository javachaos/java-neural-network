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

	@Test
	public final void test() {
		NetworkConfig snc = SimpleNetworkConfigs.CONFIG_5_4_3_4_5;
		snc.getLayerSizes();
		snc = null;
	}

}
