/*******************************************************************************
 * Copyright (c) 2013 Fred Laderoute.
 * All rights reserved. This program and the accompanying
 * materials are made available under the terms of the GNU 
 * Public License v3.0 which accompanies this distribution, 
 * and is available at http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *      Fred Laderoute - initial API and implementation
 *******************************************************************************/
/**
 * 
 */
package com.neuralnetwork.shared.tests.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Vector;

import org.junit.Test;
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

/**
 * @author Fred
 *
 */
public class NetworkTest {

    /**
     * Logger instance.
     */
    private static final Logger LOGGER = 
            LoggerFactory.getLogger(NetworkTest.class);
    
    /**
     * One value.
     */
    private static final int ONE = 1;
    
    /**
     * Two value.
     */
    private static final int TWO = 2;
    
    /**
     * Three value.
     */
    private static final int THREE = 3;
    
    /**
     * Four value.
     */
    private static final int FOUR = 4;
    
    /**
     * Five value.
     */
    private static final int FIVE = 5;
    
    /**
     * Input values for testing the network.
     */
    private static final double NN_INPUT_VALUE = 0.0321;
    
	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.network.Network#Network(int, int, int)}.
	 */
	@Test
    public final void testNetworkIntIntIntIntArray() {
		INetwork n = new Network(TWO, THREE, ONE, new int[] {THREE});
		n.build();
		assertNotNull(n);
		
		try {
			n = new Network(TWO, THREE, -ONE, new int[] {THREE});
		} catch (IllegalArgumentException e) {
			assertEquals(e.getMessage(), 
					"Error cannot have negative amount of hidden layers.");
		}
		
		try {
			n = new Network(TWO, -THREE, ONE, new int[] {THREE});
		} catch (IllegalArgumentException e) {
			assertEquals(e.getMessage(), 
					"Error cannot have negative amount of output layers.");
		}
		
		try {
			n = new Network(-TWO, THREE, ONE, new int[] {THREE});
		} catch (IllegalArgumentException e) {
			assertEquals(e.getMessage(), 
					"Error cannot have negative amount of input layers.");
		}
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.network.Network#Network(int, int)}.
	 */
	@Test
    public final void testNetworkIntInt() {
		INetwork n = new Network(FIVE, FIVE);
		n.build();
		assertNotNull(n);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.network.Network#getNeuron(int, int)}.
	 */
	@Test
    public final void testGetNeuron() {
		INetwork n = new Network(FIVE, FIVE, THREE, new int[] {FOUR, TWO, FOUR});
		n.build();
		assertNotNull(n);
		assertNotNull(n.getNeuron(0, 0));
		assertNotNull(n.getNeuron(ONE, 0));
		assertNotNull(n.getNeuron(TWO, 0));
		assertNotNull(n.getNeuron(THREE, 0));
		assertNotNull(n.getNeuron(FOUR, 0));
		assertNotNull(n.getNeuron(0, ONE));
		assertNotNull(n.getNeuron(ONE, ONE));
		assertNotNull(n.getNeuron(TWO, ONE));
		assertNotNull(n.getNeuron(THREE, ONE));
		assertNotNull(n.getNeuron(0, TWO));
		assertNotNull(n.getNeuron(ONE, TWO));
		assertNotNull(n.getNeuron(0, THREE));
		assertNotNull(n.getNeuron(ONE, THREE));
		assertNotNull(n.getNeuron(TWO, THREE));
		assertNotNull(n.getNeuron(THREE, THREE));
		assertNotNull(n.getNeuron(0, FOUR));
		assertNotNull(n.getNeuron(ONE, FOUR));
		assertNotNull(n.getNeuron(TWO, FOUR));
		assertNotNull(n.getNeuron(THREE, FOUR));
		assertNotNull(n.getNeuron(FOUR, FOUR));
		
		assertNull(n.getNeuron(-ONE, 0));
		assertNull(n.getNeuron(0, -ONE));
		assertNull(n.getNeuron(0, THREE * TWO));
		assertNull(n.getNeuron(-ONE, -ONE));
		assertNull(n.getNeuron(-ONE, THREE * TWO));

	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.network.Network#getOutputNeuron(int)}.
	 */
	@Test
    public final void testGetOutputNeuron() {
		INetwork n = new Network(FIVE, FIVE, THREE, new int[] {FOUR, TWO, FOUR});
		n.build();
		assertEquals(n.getOutputNeuron(0).getType(), NeuronType.OUTPUT);
		assertEquals(n.getOutputNeuron(ONE).getType(), NeuronType.OUTPUT);
		assertEquals(n.getOutputNeuron(TWO).getType(), NeuronType.OUTPUT);
		assertEquals(n.getOutputNeuron(THREE).getType(), NeuronType.OUTPUT);
		assertEquals(n.getOutputNeuron(FOUR).getType(), NeuronType.OUTPUT);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.network.Network#getInputNeuron(int)}.
	 */
	@Test
    public final void testGetInputNeuron() {
		INetwork n = new Network(FIVE, FIVE, THREE, new int[] {FOUR, TWO, FOUR});
		n.build();
		assertEquals(n.getInputNeuron(0).getType(), NeuronType.INPUT);
		assertEquals(n.getInputNeuron(ONE).getType(), NeuronType.INPUT);
		assertEquals(n.getInputNeuron(TWO).getType(), NeuronType.INPUT);
		assertEquals(n.getInputNeuron(THREE).getType(), NeuronType.INPUT);
		assertEquals(n.getInputNeuron(FOUR).getType(), NeuronType.INPUT);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.network.Network#getHeight()}.
	 */
	@Test
    public final void testGetHeight() {
		INetwork n = new Network(FIVE, FIVE, THREE, new int[] {FOUR, TWO, FOUR});
		n.build();
		assertEquals(FIVE, n.getHeight());
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.network.Network#runInputs(java.util.Vector)}.
	 */
	@Test
    public final void testRunInputs() {
		INetwork n = new Network(FIVE, FIVE, THREE, new int[] {FOUR, TWO, FOUR});
		n.build();
		Vector<Double> values = new Vector<Double>();
		values.add(NN_INPUT_VALUE);
        values.add(NN_INPUT_VALUE);
        values.add(NN_INPUT_VALUE);
        values.add(NN_INPUT_VALUE);
        values.add(NN_INPUT_VALUE);
		LOGGER.debug(n.runInputs(values).toString());
		
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.network.Network#reset()}.
	 */
	@Test
    public final void testReset() {
		INetwork n = new Network(FIVE, FIVE, THREE, new int[] {FOUR, TWO, FOUR});
		n.build();
		n.reset();
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.network.Network#train(java.util.Vector)}.
	 */
	@Test
    public final void testTrainVectorOfDouble() {
		INetwork n = new Network(FIVE, FIVE, THREE, new int[] {FOUR, TWO, FOUR});
		n.build();
		n.train(null);
		//TODO Complete.
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.network.Network#train(com.neuralnetwork
	 * .shared.training.TrainingStack, com.neuralnetwork
	 * .shared.values.ErrorValue)}.
	 */
	@Test
    public final void testTrainTrainingStackErrorValue() {
		INetwork n = new Network(FIVE, FIVE, THREE, new int[] {FOUR, TWO, FOUR});
		n.build();
		n.train(null, null);
		//TODO Complete.
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.network.Network#getOutputLayer()}.
	 */
	@Test
    public final void testGetOutputLayer() {
		INetwork n = new Network(FIVE, FIVE, THREE, new int[] {FOUR, TWO, FOUR});
		n.build();
		assertEquals(LayerType.OUTPUT, n.getOutputLayer().getLayerType());
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.network.Network#setOutputLayer(com.neuralnetwork
	 * .shared.layers.IOutputLayer)}.
	 */
	@Test
    public final void testSetOutputLayer() {
		INetwork n = new Network(FIVE, FIVE, THREE, new int[] {FOUR, TWO, FOUR});
		n.setOutputLayer(new OutputLayer(THREE));
		n.build();
		assertEquals(LayerType.OUTPUT, n.getOutputLayer().getLayerType());
		assertEquals(THREE, n.getOutputLayer().getSize());
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.network.Network#addHiddenLayer(com.neuralnetwork
	 * .shared.layers.IHiddenLayer)}.
	 */
	@Test
    public final void testAddHiddenLayer() {
		INetwork n = new Network(FIVE, FIVE, THREE, new int[] {FOUR, TWO, FOUR});
		n.addHiddenLayer(new HiddenLayer(THREE * TWO));
		n.build();
		IHiddenLayer h = n.getHiddenLayer(THREE);
		assertEquals(LayerType.HIDDEN, h.getLayerType());
		assertEquals(THREE * TWO, h.getSize());
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.network.Network#getHiddenLayer(int)}.
	 */
	@Test
    public final void testGetHiddenLayer() {
		INetwork n = new Network(FIVE, FIVE, THREE, new int[] {FOUR, TWO, FOUR});
		n.build();
		assertEquals(LayerType.HIDDEN, n.getHiddenLayer(0).getLayerType());
		assertEquals(LayerType.HIDDEN, n.getHiddenLayer(1).getLayerType());
		assertEquals(LayerType.HIDDEN, n.getHiddenLayer(2).getLayerType());
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.network.Network#getInputLayer()}.
	 */
	@Test
    public final void testGetInputLayer() {
		INetwork n = new Network(FIVE, FIVE, THREE, new int[] {FOUR, TWO, FOUR});
		n.build();
		assertEquals(LayerType.INPUT, n.getInputLayer().getLayerType());
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.network.Network#setInputLayer(com.neuralnetwork
	 * .shared.layers.IInputLayer)}.
	 */
	@Test
    public final void testSetInputLayer() {
		INetwork n = new Network(FIVE, FIVE, THREE, new int[] {FOUR, TWO, FOUR});
		n.setInputLayer(new InputLayer(THREE * TWO));
		n.build();
		assertEquals(LayerType.INPUT, n.getInputLayer().getLayerType());
		assertEquals(THREE * TWO, n.getInputLayer().getSize());
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.network.Network#build()}.
	 */
	@Test
    public final void testBuild() {
		INetwork n = new Network(FIVE, FIVE, THREE, new int[] {FOUR, TWO, FOUR});
		n.build();
		assertNotNull(n);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.network.Network#toString()}.
	 */
	@Test
    public final void testToString() {
		INetwork n = new Network(FIVE, FIVE, THREE, new int[] {FOUR, TWO, FOUR});
		n.build();
		LOGGER.debug(n.toString());
		assertTrue(
		           n.toString().contains(
		           "IN(0.0) IN(0.0) IN(0.0) IN(0.0) IN(0.0)"));
	}

}
