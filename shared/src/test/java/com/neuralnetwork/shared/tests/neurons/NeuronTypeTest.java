/**
 * 
 */
package com.neuralnetwork.shared.tests.neurons;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.neuralnetwork.shared.neurons.NeuronType;

/**
 * @author Fred
 *
 */
public class NeuronTypeTest {

	@Test
	public final void test() {
		NeuronType[] t = NeuronType.values();
		
		for (NeuronType n : t) {
			assertNotNull(n);
		}
		
		assertEquals(NeuronType.valueOf("HIDDEN"),
				NeuronType.HIDDEN);
		assertEquals(NeuronType.valueOf("INPUT"),
				NeuronType.INPUT);
		assertEquals(NeuronType.valueOf("OUTPUT"),
				NeuronType.OUTPUT);
		assertEquals(NeuronType.valueOf("SOM"),
				NeuronType.SOM);
	}

}
