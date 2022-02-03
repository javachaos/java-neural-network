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
package com.neuralnetwork.shared.tests.util;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import com.neuralnetwork.shared.layers.InputLayer;
import com.neuralnetwork.shared.layers.OutputLayer;
import com.neuralnetwork.shared.layers.InputNeuronLayer;
import com.neuralnetwork.shared.layers.OutputNeuronLayer;
import com.neuralnetwork.shared.links.Link;
import com.neuralnetwork.shared.neurons.InputNeuron;
import com.neuralnetwork.shared.neurons.Neuron;
import com.neuralnetwork.shared.neurons.OutputNeuron;
import com.neuralnetwork.shared.util.Connections;
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
