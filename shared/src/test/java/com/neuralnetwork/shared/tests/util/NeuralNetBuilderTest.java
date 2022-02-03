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
package com.neuralnetwork.shared.tests.util;

import com.neuralnetwork.shared.layers.HiddenNeuronLayer;
import com.neuralnetwork.shared.network.LayerType;
import com.neuralnetwork.shared.network.Network;
import com.neuralnetwork.shared.util.NeuralNetBuilder;
import com.neuralnetwork.shared.util.SimpleNetworkConfigs;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class NeuralNetBuilderTest {

	private static final int NUM_INPUTS = 5;
	private static final int NUM_OUTPUTS = 5;
	private static final int NUM_HIDDEN = 3;

	@Test
	final void testNeuralNetBuilderIntInt() {
		NeuralNetBuilder b = new NeuralNetBuilder(NUM_INPUTS, NUM_OUTPUTS);
		assertEquals(NUM_INPUTS, b.build().getInputLayer().getSize());
		assertEquals(NUM_OUTPUTS, b.build().getOutputLayer().getSize());
	}

	@Test
	final void testNeuralNetBuilderNetworkConfig() {
		NeuralNetBuilder b = new NeuralNetBuilder(
				SimpleNetworkConfigs.CONFIG_5_4_3_4_5);
		assertEquals(NUM_INPUTS, b.build().getInputLayer().getSize());
		assertEquals(NUM_OUTPUTS, b.build().getOutputLayer().getSize());
	}

	@Test
	final void testAddHiddenLayer() {
		NeuralNetBuilder b = new NeuralNetBuilder(NUM_INPUTS, NUM_OUTPUTS);
		b.addHiddenLayer(new HiddenNeuronLayer(NUM_HIDDEN, 0));
		b.build();
		assertEquals(NUM_HIDDEN, b.getNetwork().getHiddenLayer(0).getSize());
		assertEquals(LayerType.HIDDEN, b.getNetwork()
				.getHiddenLayer(0).getLayerType());
	}

	@Test
	final void testBuild() {
		NeuralNetBuilder b = new NeuralNetBuilder(NUM_INPUTS, NUM_OUTPUTS);
		Network n = b.addHiddenLayer(new HiddenNeuronLayer(NUM_HIDDEN, 0)).build();
		assertNotNull(b);
		assertNotNull(n);
	}

	@Test
	final void testGetNetwork() {
		NeuralNetBuilder b = new NeuralNetBuilder(NUM_INPUTS, NUM_OUTPUTS);
		b.addHiddenLayer(new HiddenNeuronLayer(NUM_HIDDEN, 0));
		b.build();
		assertEquals(NUM_HIDDEN, b.getNetwork().getHiddenLayer(0).getSize());
		assertEquals(LayerType.HIDDEN, b.getNetwork()
				.getHiddenLayer(0).getLayerType());
	}

}
