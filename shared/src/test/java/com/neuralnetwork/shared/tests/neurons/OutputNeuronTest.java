/**
 * 
 */
package com.neuralnetwork.shared.tests.neurons;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.neuralnetwork.shared.neurons.OutputNeuron;
import com.neuralnetwork.shared.values.DoubleValue;

/**
 * @author Fred
 *
 */
public class OutputNeuronTest {

	/**
	 * Number of times to run the test.
	 */
	private static final int NUM_ITER = 100000;
	
	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.OutputNeuron#OutputNeuron()}.
	 */
	@Test
	public final void testOutputNeuron() {
		for (int i = 0; i < NUM_ITER; i++) {
			OutputNeuron o = new OutputNeuron();
			Double v = o.getOutputValue();
			assertTrue(v >= 0);
			assertTrue(v < 1);
		}
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.OutputNeuron#toString()}.
	 */
	@Test
	public final void testToString() {
		OutputNeuron o = new OutputNeuron();
		o.setValue(new DoubleValue(0.0));
		assertEquals(o.toString(), "ON(0.0) ");
	}

}
