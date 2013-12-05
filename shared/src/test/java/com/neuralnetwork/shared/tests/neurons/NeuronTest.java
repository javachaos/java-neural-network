/**
 * 
 */
package com.neuralnetwork.shared.tests.neurons;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.neuralnetwork.shared.neurons.HiddenNeuron;
import com.neuralnetwork.shared.neurons.InputNeuron;
import com.neuralnetwork.shared.neurons.Neuron;
import com.neuralnetwork.shared.neurons.NeuronType;
import com.neuralnetwork.shared.neurons.OutputNeuron;
import com.neuralnetwork.shared.values.ZeroValue;

/**
 * @author Fred
 *
 */
public class NeuronTest {

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.Neuron#hashCode()}.
	 */
	@Test
	public final void testHashCode() {
		Neuron n = new HiddenNeuron();
		assertEquals(n.hashCode(), n.hashCode());
	
		n.setValue(null);
		n.setType(null);
		n.addOutputLink(new HiddenNeuron());
		assertEquals(n.hashCode(), n.hashCode());
		Neuron v = new OutputNeuron();
		v.setValue(null);
		v.setType(null);
		v.setActivationFunction(null);
		assertEquals(v.hashCode(), v.hashCode());
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.Neuron#Neuron(
	 * com.neuralnetwork.shared.neurons.NeuronType)}.
	 */
	@Test
	public final void testNeuronNeuronType() {
		Neuron n = new InputNeuron();
		Neuron m = new OutputNeuron();
		Neuron o = new HiddenNeuron();
		
		assertEquals(n.getType(), NeuronType.INPUT);
		assertEquals(m.getType(), NeuronType.OUTPUT);
		assertEquals(o.getType(), NeuronType.HIDDEN);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.Neuron#Neuron()}.
	 */
	@Test
	public final void testNeuron() {
		Neuron n = new InputNeuron();
		Neuron m = new InputNeuron(new ZeroValue());
		
		assertEquals(n, m);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.Neuron#addInputLink(
	 * com.neuralnetwork.shared.neurons.INeuron,
	 * com.neuralnetwork.shared.values.IValue)}.
	 */
	@Test
	public final void testAddInputLinkINeuronIValueOfQ() {
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.Neuron#addInputLink(
	 * com.neuralnetwork.shared.neurons.INeuron)}.
	 */
	@Test
	public final void testAddInputLinkINeuron() {
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.Neuron#getInputLink(int)}.
	 */
	@Test
	public final void testGetInputLink() {
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.Neuron#getInputLinks(int[])}.
	 */
	@Test
	public final void testGetInputLinks() {
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.Neuron#getType()}.
	 */
	@Test
	public final void testGetType() {
		Neuron n = new InputNeuron();
		Neuron m = new OutputNeuron();
		Neuron o = new HiddenNeuron();
		
		assertEquals(n.getType(), NeuronType.INPUT);
		assertEquals(m.getType(), NeuronType.OUTPUT);
		assertEquals(o.getType(), NeuronType.HIDDEN);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.Neuron#getNextParent()}.
	 */
	@Test
	public final void testGetNextParent() {
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.Neuron#getNextChild()}.
	 */
	@Test
	public final void testGetNextChild() {
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.Neuron#addOutputLink(
	 * com.neuralnetwork.shared.neurons.INeuron, 
	 * com.neuralnetwork.shared.values.IValue)}.
	 */
	@Test
	public final void testAddOutputLinkINeuronIValueOfQ() {
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.Neuron#addOutputLink(
	 * com.neuralnetwork.shared.neurons.INeuron)}.
	 */
	@Test
	public final void testAddOutputLinkINeuron() {
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.Neuron#getOutputLink(int)}.
	 */
	@Test
	public final void testGetOutputLink() {
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.Neuron#getOutputLinks(int[])}.
	 */
	@Test
	public final void testGetOutputLinks() {
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.Neuron#reset()}.
	 */
	@Test
	public final void testReset() {
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.Neuron#setActivationFunction(
	 * com.neuralnetwork.shared.functions.IActivationFunction)}.
	 */
	@Test
	public final void testSetActivationFunction() {
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.Neuron#getActivationFunction()}.
	 */
	@Test
	public final void testGetActivationFunction() {
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.Neuron#getValue()}.
	 */
	@Test
	public final void testGetValue() {
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.Neuron#setValue(
	 * com.neuralnetwork.shared.values.IValue)}.
	 */
	@Test
	public final void testSetValue() {
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.Neuron
	 * #equals(java.lang.Object)}.
	 */
	@Test
	public final void testEqualsObject() {
	}

}
