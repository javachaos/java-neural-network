/**
 * 
 */
package com.neuralnetwork.shared.tests.neurons;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.neuralnetwork.shared.neurons.AbstractInputNeuron;
import com.neuralnetwork.shared.neurons.InputNeuron;
import com.neuralnetwork.shared.neurons.NeuronType;
import com.neuralnetwork.shared.values.OneValue;
import com.neuralnetwork.shared.values.ZeroValue;

/**
 * @author Fred
 *
 */
public class AbstractInputNeuronTest {

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.AbstractInputNeuron
	 * #AbstractInputNeuron()}.
	 */
	@Test
	public final void testAbstractInputNeuron() {
		AbstractInputNeuron ai = new InputNeuron();
		assertEquals(ai.getType(), NeuronType.INPUT);
		assertEquals(ai.getValue(), new ZeroValue());
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.AbstractInputNeuron
	 * #AbstractInputNeuron(com.neuralnetwork
	 * .shared.values.IValue)}.
	 */
	@Test
	public final void testAbstractInputNeuronIValueOfQ() {
		AbstractInputNeuron ai = new InputNeuron(new OneValue());
		assertEquals(ai.getType(), NeuronType.INPUT);
		assertEquals(ai.getValue(), new OneValue());
	}

}