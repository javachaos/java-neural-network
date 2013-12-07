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
package com.neuralnetwork.shared.tests.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.neuralnetwork.shared.layers.HiddenLayer;
import com.neuralnetwork.shared.network.LayerType;
import com.neuralnetwork.shared.util.NeuralNetBuilder;
import com.neuralnetwork.shared.util.SimpleNetworkConfigs;

/**
 * @author Fred
 *
 */
public class NeuralNetBuilderTest {

	/**
	 * Number of inputs for the network.
	 */
	private static final int NUM_INPUTS = 5;
	
	/**
	 * Number of outputs for the network.
	 */
	private static final int NUM_OUTPUTS = 5;
	
	/**
	 * Number of hidden neurons for the network.
	 */
	private static final int NUM_HIDDEN = 3;
	
	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.util.NeuralNetBuilder
	 * #NeuralNetBuilder(int, int)}.
	 */
	@Test
	public final void testNeuralNetBuilderIntInt() {
		NeuralNetBuilder b = new NeuralNetBuilder(NUM_INPUTS, NUM_OUTPUTS);
		assertEquals(b.build().getInputLayer().getSize(), NUM_INPUTS);
		assertEquals(b.build().getOutputLayer().getSize(), NUM_OUTPUTS);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.util.NeuralNetBuilder
	 * #NeuralNetBuilder(com.neuralnetwork.shared.util.NetworkConfig)}.
	 */
	@Test
	public final void testNeuralNetBuilderNetworkConfig() {
		NeuralNetBuilder b = new NeuralNetBuilder(SimpleNetworkConfigs.CONFIG_5_4_3_4_5);
		assertEquals(b.build().getInputLayer().getSize(), NUM_INPUTS);
		assertEquals(b.build().getOutputLayer().getSize(), NUM_OUTPUTS);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.util.NeuralNetBuilder
	 * #addHiddenLayer(com.neuralnetwork.shared.layers.IHiddenLayer)}.
	 */
	@Test
	public final void testAddHiddenLayer() {
		NeuralNetBuilder b = new NeuralNetBuilder(NUM_INPUTS, NUM_OUTPUTS);
		b.addHiddenLayer(new HiddenLayer(NUM_HIDDEN));
		b.build();
		assertEquals(b.getNetwork().getHiddenLayer(0).getSize(), NUM_HIDDEN);
		assertEquals(b.getNetwork()
				.getHiddenLayer(0).getLayerType(), LayerType.HIDDEN);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.util.NeuralNetBuilder#build()}.
	 */
	@Test
	public final void testBuild() {
		NeuralNetBuilder b = new NeuralNetBuilder(NUM_INPUTS, NUM_OUTPUTS);
		b.addHiddenLayer(new HiddenLayer(NUM_HIDDEN));
		b.build();
		assertEquals(b.getNetwork().getHiddenLayer(0).getSize(), NUM_HIDDEN);
		assertEquals(b.getNetwork()
				.getHiddenLayer(0).getLayerType(), LayerType.HIDDEN);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.util.NeuralNetBuilder#getNetwork()}.
	 */
	@Test
	public final void testGetNetwork() {
		NeuralNetBuilder b = new NeuralNetBuilder(NUM_INPUTS, NUM_OUTPUTS);
		b.addHiddenLayer(new HiddenLayer(NUM_HIDDEN));
		b.build();
		assertEquals(b.getNetwork().getHiddenLayer(0).getSize(), NUM_HIDDEN);
		assertEquals(b.getNetwork()
				.getHiddenLayer(0).getLayerType(), LayerType.HIDDEN);
	}

}
