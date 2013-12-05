/**
 * 
 */
package com.neuralnetwork.shared.tests.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
import com.neuralnetwork.shared.nodes.NeuronType;

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
	 * Test method for {@link com.neuralnetwork
	 * .shared.network.Network#Network(int, int, int)}.
	 */
	@Test
	public void testNetworkIntIntIntIntArray() {
		INetwork n = new Network(2, 3, 1, new int[] {3});
		n.build();
		assertNotNull(n);
		
		try {
			n = new Network(2, 3, -1, new int[] {3});
		} catch (IllegalArgumentException e) {
			assertEquals(e.getMessage(), 
					"Error cannot have negative amount of hidden layers.");
		}
		
		try {
			n = new Network(2, -3, 1, new int[] {3});
		} catch (IllegalArgumentException e) {
			assertEquals(e.getMessage(), 
					"Error cannot have negative amount of output layers.");
		}
		
		try {
			n = new Network(-2, 3, 1, new int[] {3});
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
	public void testNetworkIntInt() {
		INetwork n = new Network(5, 5);
		n.build();
		assertNotNull(n);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.network.Network#getNeuron(int, int)}.
	 */
	@Test
	public void testGetNeuron() {
		INetwork n = new Network(5, 5, 3, new int[] {4, 2, 4});
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
		
		assertNull(n.getNeuron(-1, 0));
		assertNull(n.getNeuron(0, -1));
		assertNull(n.getNeuron(0, 6));
		assertNull(n.getNeuron(-1, -1));
		assertNull(n.getNeuron(-1, 6));

	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.network.Network#getOutputNeuron(int)}.
	 */
	@Test
	public void testGetOutputNeuron() {
		INetwork n = new Network(5, 5, 3, new int[] {4, 2, 4});
		n.build();
		assertEquals(n.getOutputNeuron(0).getType(), NeuronType.OUTPUT);
		assertEquals(n.getOutputNeuron(1).getType(), NeuronType.OUTPUT);
		assertEquals(n.getOutputNeuron(2).getType(), NeuronType.OUTPUT);
		assertEquals(n.getOutputNeuron(3).getType(), NeuronType.OUTPUT);
		assertEquals(n.getOutputNeuron(4).getType(), NeuronType.OUTPUT);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.network.Network#getInputNeuron(int)}.
	 */
	@Test
	public void testGetInputNeuron() {
		INetwork n = new Network(5, 5, 3, new int[] {4, 2, 4});
		n.build();
		assertEquals(n.getInputNeuron(0).getType(), NeuronType.INPUT);
		assertEquals(n.getInputNeuron(1).getType(), NeuronType.INPUT);
		assertEquals(n.getInputNeuron(2).getType(), NeuronType.INPUT);
		assertEquals(n.getInputNeuron(3).getType(), NeuronType.INPUT);
		assertEquals(n.getInputNeuron(4).getType(), NeuronType.INPUT);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.network.Network#getHeight()}.
	 */
	@Test
	public void testGetHeight() {
		INetwork n = new Network(5, 5, 3, new int[] {4, 2, 4});
		n.build();
		assertEquals(5, n.getHeight());
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.network.Network#runInputs(java.util.Vector)}.
	 */
	@Test
	public void testRunInputs() {
		INetwork n = new Network(5, 5, 3, new int[] {4, 2, 4});
		n.build();
		n.runInputs(null);
		//TODO Complete.
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.network.Network#reset()}.
	 */
	@Test
	public void testReset() {
		INetwork n = new Network(5, 5, 3, new int[] {4, 2, 4});
		n.build();
		n.reset();
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.network.Network#train(java.util.Vector)}.
	 */
	@Test
	public void testTrainVectorOfDouble() {
		INetwork n = new Network(5, 5, 3, new int[] {4, 2, 4});
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
	public void testTrainTrainingStackErrorValue() {
		INetwork n = new Network(5, 5, 3, new int[] {4, 2, 4});
		n.build();
		n.train(null, null);
		//TODO Complete.
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.network.Network#getOutputLayer()}.
	 */
	@Test
	public void testGetOutputLayer() {
		INetwork n = new Network(5, 5, 3, new int[] {4, 2, 4});
		n.build();
		assertEquals(LayerType.OUTPUT, n.getOutputLayer().getLayerType());
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.network.Network#setOutputLayer(com.neuralnetwork
	 * .shared.layers.IOutputLayer)}.
	 */
	@Test
	public void testSetOutputLayer() {
		INetwork n = new Network(5, 5, 3, new int[] {4, 2, 4});
		n.setOutputLayer(new OutputLayer(3));
		n.build();
		assertEquals(LayerType.OUTPUT, n.getOutputLayer().getLayerType());
		assertEquals(3, n.getOutputLayer().getSize());
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.network.Network#addHiddenLayer(com.neuralnetwork
	 * .shared.layers.IHiddenLayer)}.
	 */
	@Test
	public void testAddHiddenLayer() {
		INetwork n = new Network(5, 5, 3, new int[] {4, 2, 4});
		n.addHiddenLayer(new HiddenLayer(6));
		n.build();
		IHiddenLayer h = n.getHiddenLayer(3);
		assertEquals(LayerType.HIDDEN, h.getLayerType());
		assertEquals(6, h.getSize());
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.network.Network#getHiddenLayer(int)}.
	 */
	@Test
	public void testGetHiddenLayer() {
		INetwork n = new Network(5, 5, 3, new int[] {4, 2, 4});
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
	public void testGetInputLayer() {
		INetwork n = new Network(5, 5, 3, new int[] {4, 2, 4});
		n.build();
		assertEquals(LayerType.INPUT, n.getInputLayer().getLayerType());
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.network.Network#setInputLayer(com.neuralnetwork
	 * .shared.layers.IInputLayer)}.
	 */
	@Test
	public void testSetInputLayer() {
		INetwork n = new Network(5, 5, 3, new int[] {4, 2, 4});
		n.setInputLayer(new InputLayer(6));
		n.build();
		assertEquals(LayerType.INPUT, n.getInputLayer().getLayerType());
		assertEquals(6, n.getInputLayer().getSize());
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.network.Network#build()}.
	 */
	@Test
	public void testBuild() {
		INetwork n = new Network(5, 5, 3, new int[] {4, 2, 4});
		n.build();
		assertNotNull(n);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.network.Network#toString()}.
	 */
	@Test
	public void testToString() {
		INetwork n = new Network(5, 5, 3, new int[] {4, 2, 4});
		n.build();
		LOGGER.debug(n.toString());
		assertTrue(n.toString().contains("IN(0.0) IN(0.0) IN(0.0) IN(0.0) IN(0.0)"));
	}

}
