/**
 * 
 */
package com.neuralnetwork.shared.tests.neurons;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.neuralnetwork.shared.neurons.AbstractOutputNeuron;
import com.neuralnetwork.shared.neurons.NeuronType;
import com.neuralnetwork.shared.neurons.OutputNeuron;
import com.neuralnetwork.shared.values.OneValue;
import com.neuralnetwork.shared.values.ZeroValue;

/**
 * @author Fred
 *
 */
public class AbstractOutputNeuronTest {

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.AbstractOutputNeuron
	 * #AbstractOutputNeuron()}.
	 */
	@Test
	public final void testAbstractOutputNeuron() {
		AbstractOutputNeuron ai = new OutputNeuron();
		ai.setValue(new ZeroValue());
		assertEquals(ai.getType(), NeuronType.OUTPUT);
		assertEquals(ai.getValue(), new ZeroValue());
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.AbstractOutputNeuron
	 * #getOutputValue()}.
	 */
	@Test
	public final void testGetOutputValue() {
		AbstractOutputNeuron ai = new OutputNeuron();
		ai.setValue(new OneValue());
		assertEquals(ai.getType(), NeuronType.OUTPUT);
		assertEquals(ai.getOutputValue(), new OneValue().getValue(), 10 * Math.ulp(1.0));
	}

}
