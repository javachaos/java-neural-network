/**
 * 
 */
package com.neuralnetwork.shared.tests.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.neuralnetwork.shared.network.LayerType;

/**
 * @author Fred
 *
 */
public class LayerTypeTest {

	@Test
	public final void test() {
		LayerType[] t = LayerType.values();
		
		for (LayerType l : t) {
			assertNotNull(l);
		}
		
		assertEquals(LayerType.HIDDEN, LayerType.valueOf("HIDDEN"));
		assertEquals(LayerType.INPUT, LayerType.valueOf("INPUT"));
		assertEquals(LayerType.OUTPUT, LayerType.valueOf("OUTPUT"));
	}

}
