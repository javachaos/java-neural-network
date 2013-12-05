/**
 * 
 */
package com.neuralnetwork.shared.tests.neurons;

import static org.junit.Assert.*;

import org.junit.Test;

import com.neuralnetwork.shared.neurons.BiasNeuron;
import com.neuralnetwork.shared.values.OneValue;

/**
 * @author Fred
 *
 */
public class BiasNeuronTest {

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.BiasNeuron
	 * #BiasNeuron()}.
	 */
	@Test
	public final void testBiasNeuron() {
		assertEquals(
				new BiasNeuron().getValue(), new OneValue());
	}

}
