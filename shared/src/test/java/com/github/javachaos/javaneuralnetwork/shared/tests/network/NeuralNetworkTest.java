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
package com.github.javachaos.javaneuralnetwork.shared.tests.network;

import java.util.ArrayList;
import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javachaos.javaneuralnetwork.shared.layers.HiddenLayer;
import com.github.javachaos.javaneuralnetwork.shared.layers.HiddenNeuronLayer;
import com.github.javachaos.javaneuralnetwork.shared.layers.InputNeuronLayer;
import com.github.javachaos.javaneuralnetwork.shared.layers.OutputNeuronLayer;
import com.github.javachaos.javaneuralnetwork.shared.network.LayerType;
import com.github.javachaos.javaneuralnetwork.shared.network.Network;
import com.github.javachaos.javaneuralnetwork.shared.network.NeuralNetwork;
import com.github.javachaos.javaneuralnetwork.shared.neurons.NeuronType;
import com.github.javachaos.javaneuralnetwork.shared.training.TrainType;

import static org.junit.jupiter.api.Assertions.*;

class NeuralNetworkTest {

    private static final Logger LOGGER = 
            LoggerFactory.getLogger(NeuralNetworkTest.class);
    private static final double NN_INPUT_VALUE = 0.0321;

	@Test
	final void testCreate() {
		NeuralNetwork neuralNetwork = new NeuralNetwork(
				3, 3,2, new int[] {3, 3},
		        TrainType.BACKPROP);
		assertNotNull(neuralNetwork);
		NeuralNetwork n = new NeuralNetwork(3, 3, TrainType.BACKPROP);
		assertNotNull(n);
	}

	@Test
    final void testNetworkIntIntIntIntArray() {
		Network n = new NeuralNetwork(2, 3, 1, new int[] {3});
		n.build();
		assertNotNull(n);
		Exception ex = assertThrows(IllegalArgumentException.class,
				() -> new NeuralNetwork(2, 3, -1, new int[] {3}));
		assertEquals("Error cannot have negative amount of hidden layers.", ex.getMessage());
		ex = assertThrows(IllegalArgumentException.class,
				() -> new NeuralNetwork(2, -3, 1, new int[] {3}));
		assertEquals("Error cannot have negative amount of output layers.", ex.getMessage());
		ex = assertThrows(IllegalArgumentException.class,
				() -> new NeuralNetwork(-2, 3, 1, new int[] {3}));
		assertEquals("Error cannot have negative amount of input layers.", ex.getMessage());
	}

	@Test
    final void testNetworkIntInt() {
		Network n = new NeuralNetwork(5, 5);
		n.build();
		assertNotNull(n);
	}

	@Test
    final void testGetNeuron() {
		Network n = new NeuralNetwork(5, 5, 3,
				new int[] {4, 2, 4});
		n.build();
		assertNotNull(n);
		assertNotNull(n.getNeuron(0, 0));
		assertNotNull(n.getNeuron(1, 0));
		assertNotNull(n.getNeuron(2, 0));
		assertNotNull(n.getNeuron(3, 0));
		assertNotNull(n.getNeuron(4, 0));
		assertNotNull(n.getNeuron(0, 1));
		assertNotNull(n.getNeuron(1, 1));
		assertNotNull(n.getNeuron(2, 1));
		assertNotNull(n.getNeuron(3, 1));
		assertNotNull(n.getNeuron(0, 2));
		assertNotNull(n.getNeuron(1, 2));
		assertNotNull(n.getNeuron(0, 3));
		assertNotNull(n.getNeuron(1, 3));
		assertNotNull(n.getNeuron(2, 3));
		assertNotNull(n.getNeuron(3, 3));
		assertNotNull(n.getNeuron(0, 4));
		assertNotNull(n.getNeuron(1, 4));
		assertNotNull(n.getNeuron(2, 4));
		assertNotNull(n.getNeuron(3, 4));
		assertNotNull(n.getNeuron(4, 4));

		assertNull(n.getNeuron(Integer.MAX_VALUE, 0));
		
	}

	@Test
    final void testGetOutputNeuron() {
		Network n = new NeuralNetwork(5, 5, 3,
				new int[] {4, 2, 4});
		n.build();
		assertEquals(NeuronType.OUTPUT, Objects.requireNonNull(n.getOutputNeuron(0)).getType());
		assertEquals(NeuronType.OUTPUT, Objects.requireNonNull(n.getOutputNeuron(1)).getType());
		assertEquals(NeuronType.OUTPUT, Objects.requireNonNull(n.getOutputNeuron(2)).getType());
		assertEquals(NeuronType.OUTPUT, Objects.requireNonNull(n.getOutputNeuron(3)).getType());
		assertEquals(NeuronType.OUTPUT, Objects.requireNonNull(n.getOutputNeuron(4)).getType());
		assertNull(n.getOutputNeuron(-2));
	}

	@Test
    final void testGetInputNeuron() {
		Network n = new NeuralNetwork(5, 5, 3,
				new int[] {4, 2, 4});
		n.build();
		assertEquals(NeuronType.INPUT, Objects.requireNonNull(n.getInputNeuron(0)).getType());
		assertEquals(NeuronType.INPUT, Objects.requireNonNull(n.getInputNeuron(1)).getType());
		assertEquals(NeuronType.INPUT, Objects.requireNonNull(n.getInputNeuron(2)).getType());
		assertEquals(NeuronType.INPUT, Objects.requireNonNull(n.getInputNeuron(3)).getType());
		assertEquals(NeuronType.INPUT, Objects.requireNonNull(n.getInputNeuron(4)).getType());
		assertNull(n.getInputNeuron(-2));
	}

	@Test
    final void testGetHeight() {
		Network n = new NeuralNetwork(5, 5, 3,
				new int[] {4, 2, 4});
		n.build();
		assertEquals(5, n.getHeight());
	}

	@Test
    final void testRunInputs() {
		Network n = new NeuralNetwork(5, 5, 3,
				new int[] {4, 2, 4});
		n.build();
		assertEquals(n.getHeight(), 3 + 2);
		ArrayList<Double> values = new ArrayList<>();
		values.add(NN_INPUT_VALUE);
        values.add(NN_INPUT_VALUE);
        values.add(NN_INPUT_VALUE);
        values.add(NN_INPUT_VALUE);
        values.add(NN_INPUT_VALUE);
		LOGGER.debug(n.runInputs(values).toString());
		
	}

	@Test
    final void testReset() {
		Network n = new NeuralNetwork(5, 5, 3,
				new int[] {4, 2, 4});
		assertEquals(n.getHeight(), 3 + 2);
		n.build();
		n.reset();
	}

	@Test
    final void testTrainVectorOfDouble() {
		Network n = new NeuralNetwork(5, 5, 3,
				new int[] {4, 2, 4});
		assertEquals(n.getHeight(), 3 + 2);
		n.build();
	}

	@Test
    final void testTrainTrainingStackErrorValue() {
		Network n = new NeuralNetwork(5, 5, 3,
				new int[] {4, 2, 4});
		assertEquals(n.getHeight(), 3 + 2);
		n.build();
	}

	@Test
    final void testGetOutputLayer() {
		Network n = new NeuralNetwork(5, 5, 3,
				new int[] {4, 2, 4});
		assertEquals(n.getHeight(), 3 + 2);
		n.build();
		assertEquals(LayerType.OUTPUT, n.getOutputLayer().getLayerType());
	}

	@Test
    final void testSetOutputLayer() {
		Network n = new NeuralNetwork(5, 5, 3,
				new int[] {4, 2, 4});
		n.setOutputLayer(new OutputNeuronLayer(3));
		n.build();
		assertEquals(LayerType.OUTPUT, n.getOutputLayer().getLayerType());
		assertEquals(3, n.getOutputLayer().getSize());
	}

	@Test
    final void testAddHiddenLayer() {
		Network n = new NeuralNetwork(5, 5, 3,
				new int[] {4, 2, 4});
		n.addHiddenLayer(new HiddenNeuronLayer(3 * 2, 3));
		n.build();
		HiddenLayer h = n.getHiddenLayer(3);
		assertEquals(LayerType.HIDDEN, h.getLayerType());
		assertEquals(3 * 2, h.getSize());
	}

	@Test
    final void testGetHiddenLayer() {
		Network n = new NeuralNetwork(5, 5, 3,
				new int[] {4, 2, 4});
		n.build();
		assertEquals(LayerType.HIDDEN, n.getHiddenLayer(0).getLayerType());
		assertEquals(LayerType.HIDDEN, n.getHiddenLayer(1).getLayerType());
		assertEquals(LayerType.HIDDEN, n.getHiddenLayer(2).getLayerType());
	}

	@Test
    final void testGetInputLayer() {
		Network n = new NeuralNetwork(5, 5, 3,
				new int[] {4, 2, 4});
		n.build();
		assertEquals(LayerType.INPUT, n.getInputLayer().getLayerType());
	}

	@Test
    final void testSetInputLayer() {
		Network n = new NeuralNetwork(5, 5, 3,
				new int[] {4, 2, 4});
		n.setInputLayer(new InputNeuronLayer(3 * 2));
		n.build();
		assertEquals(LayerType.INPUT, n.getInputLayer().getLayerType());
		assertEquals(3 * 2, n.getInputLayer().getSize());
	}

	@Test
    final void testBuild() {
		Network n = new NeuralNetwork(5, 5, 3,
				new int[] {4, 2, 4});
		n.build();
		assertNotNull(n);
	}

	@Test
    final void testToString() {
		Network n = new NeuralNetwork(5, 5, 3,
				new int[] {4, 2, 4});
		n.build();
		LOGGER.debug(n.toString());
		assertNotEquals("IN(0.0) IN(0.0) IN(0.0) IN(0.0) IN(0.0)",
		           n.toString());
	}

}
