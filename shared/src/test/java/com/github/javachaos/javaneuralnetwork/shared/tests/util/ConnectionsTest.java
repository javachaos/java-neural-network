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
package com.github.javachaos.javaneuralnetwork.shared.tests.util;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import com.github.javachaos.javaneuralnetwork.shared.layers.InputLayer;
import com.github.javachaos.javaneuralnetwork.shared.layers.InputNeuronLayer;
import com.github.javachaos.javaneuralnetwork.shared.layers.OutputLayer;
import com.github.javachaos.javaneuralnetwork.shared.layers.OutputNeuronLayer;
import com.github.javachaos.javaneuralnetwork.shared.links.Link;
import com.github.javachaos.javaneuralnetwork.shared.neurons.InputNeuron;
import com.github.javachaos.javaneuralnetwork.shared.neurons.Neuron;
import com.github.javachaos.javaneuralnetwork.shared.neurons.OutputNeuron;
import com.github.javachaos.javaneuralnetwork.shared.util.Connections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Fred
 *
 */
class ConnectionsTest {

	/**
	 * The size of each layer in the test.
	 */
	private static final int LAYER_SIZE = 30000;
	
	/**
	 * Testing timeout.
	 */
	private static final long TIMEOUT = 5000;

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.util.Connections
	 * #create(com.neuralnetwork.shared.layers.ILayer, 
	 * com.neuralnetwork.shared.layers.ILayer)}.
	 */
	@Test
	@Timeout(value = TIMEOUT, unit = TimeUnit.MILLISECONDS)
	final void testCreate() {
		InputLayer l1 = new InputNeuronLayer(LAYER_SIZE);
		l1.build();
		OutputLayer l2 = new OutputNeuronLayer(LAYER_SIZE);
		l2.build();
		Connections.getInstance().create(l1, l2);
		
		Iterator<InputNeuron> iter1 = l1.iterator();
        Neuron tmp;
        while (iter1.hasNext()) {
            tmp = iter1.next();
            Iterator<OutputNeuron> iter2 = l2.iterator();
            for (Link l : tmp.getOutputLinks()) {
            	if (iter2.hasNext()) {
                    assertEquals(l.getTail(), iter2.next());
            	}
            }
         }
		
	}

}
