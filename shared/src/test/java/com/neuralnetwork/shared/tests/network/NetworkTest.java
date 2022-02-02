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
package com.neuralnetwork.shared.tests.network;

import java.util.ArrayList;
import java.util.Objects;

import com.neuralnetwork.shared.training.TrainType;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neuralnetwork.shared.layers.HiddenLayer;
import com.neuralnetwork.shared.layers.IHiddenLayer;
import com.neuralnetwork.shared.layers.InputLayer;
import com.neuralnetwork.shared.layers.OutputLayer;
import com.neuralnetwork.shared.network.INetwork;
import com.neuralnetwork.shared.network.LayerType;
import com.neuralnetwork.shared.network.Network;
import com.neuralnetwork.shared.neurons.NeuronType;

import static org.junit.jupiter.api.Assertions.*;

class NetworkTest {

    private static final Logger LOGGER = 
            LoggerFactory.getLogger(NetworkTest.class);
    private static final double NN_INPUT_VALUE = 0.0321;

	@Test
	final void testCreate() {
		Network network = new Network(
				3, 3,2, new int[] {3, 3},
		        TrainType.BACKPROP);
		assertNotNull(network);
		Network n = new Network(3, 3, TrainType.BACKPROP);
		assertNotNull(n);
	}

	@Test
    final void testNetworkIntIntIntIntArray() {
		INetwork n = new Network(2, 3, 1, new int[] {3});
		n.build();
		assertNotNull(n);
		Exception ex = assertThrows(IllegalArgumentException.class,
				() -> new Network(2, 3, -1, new int[] {3}));
		assertEquals("Error cannot have negative amount of hidden layers.", ex.getMessage());
		ex = assertThrows(IllegalArgumentException.class,
				() -> new Network(2, -3, 1, new int[] {3}));
		assertEquals("Error cannot have negative amount of output layers.", ex.getMessage());
		ex = assertThrows(IllegalArgumentException.class,
				() -> new Network(-2, 3, 1, new int[] {3}));
		assertEquals("Error cannot have negative amount of input layers.", ex.getMessage());
	}

	@Test
    final void testNetworkIntInt() {
		INetwork n = new Network(5, 5);
		n.build();
		assertNotNull(n);
	}

	@Test
    final void testGetNeuron() {
		INetwork n = new Network(5, 5, 3,
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
		INetwork n = new Network(5, 5, 3,
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
		INetwork n = new Network(5, 5, 3,
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
		INetwork n = new Network(5, 5, 3,
				new int[] {4, 2, 4});
		n.build();
		assertEquals(5, n.getHeight());
	}

	@Test
    final void testRunInputs() {
		INetwork n = new Network(5, 5, 3,
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
		INetwork n = new Network(5, 5, 3,
				new int[] {4, 2, 4});
		assertEquals(n.getHeight(), 3 + 2);
		n.build();
		n.reset();
	}

	@Test
    final void testTrainVectorOfDouble() {
		INetwork n = new Network(5, 5, 3,
				new int[] {4, 2, 4});
		assertEquals(n.getHeight(), 3 + 2);
		n.build();
	}

	@Test
    final void testTrainTrainingStackErrorValue() {
		INetwork n = new Network(5, 5, 3,
				new int[] {4, 2, 4});
		assertEquals(n.getHeight(), 3 + 2);
		n.build();
	}

	@Test
    final void testGetOutputLayer() {
		INetwork n = new Network(5, 5, 3,
				new int[] {4, 2, 4});
		assertEquals(n.getHeight(), 3 + 2);
		n.build();
		assertEquals(LayerType.OUTPUT, n.getOutputLayer().getLayerType());
	}

	@Test
    final void testSetOutputLayer() {
		INetwork n = new Network(5, 5, 3,
				new int[] {4, 2, 4});
		n.setOutputLayer(new OutputLayer(3));
		n.build();
		assertEquals(LayerType.OUTPUT, n.getOutputLayer().getLayerType());
		assertEquals(3, n.getOutputLayer().getSize());
	}

	@Test
    final void testAddHiddenLayer() {
		INetwork n = new Network(5, 5, 3,
				new int[] {4, 2, 4});
		n.addHiddenLayer(new HiddenLayer(3 * 2, 3));
		n.build();
		IHiddenLayer h = n.getHiddenLayer(3);
		assertEquals(LayerType.HIDDEN, h.getLayerType());
		assertEquals(3 * 2, h.getSize());
	}

	@Test
    final void testGetHiddenLayer() {
		INetwork n = new Network(5, 5, 3,
				new int[] {4, 2, 4});
		n.build();
		assertEquals(LayerType.HIDDEN, n.getHiddenLayer(0).getLayerType());
		assertEquals(LayerType.HIDDEN, n.getHiddenLayer(1).getLayerType());
		assertEquals(LayerType.HIDDEN, n.getHiddenLayer(2).getLayerType());
	}

	@Test
    final void testGetInputLayer() {
		INetwork n = new Network(5, 5, 3,
				new int[] {4, 2, 4});
		n.build();
		assertEquals(LayerType.INPUT, n.getInputLayer().getLayerType());
	}

	@Test
    final void testSetInputLayer() {
		INetwork n = new Network(5, 5, 3,
				new int[] {4, 2, 4});
		n.setInputLayer(new InputLayer(3 * 2));
		n.build();
		assertEquals(LayerType.INPUT, n.getInputLayer().getLayerType());
		assertEquals(3 * 2, n.getInputLayer().getSize());
	}

	@Test
    final void testBuild() {
		INetwork n = new Network(5, 5, 3,
				new int[] {4, 2, 4});
		n.build();
		assertNotNull(n);
	}

	@Test
    final void testToString() {
		INetwork n = new Network(5, 5, 3,
				new int[] {4, 2, 4});
		n.build();
		LOGGER.debug(n.toString());
		assertNotEquals("IN(0.0) IN(0.0) IN(0.0) IN(0.0) IN(0.0)",
		           n.toString());
	}

}
