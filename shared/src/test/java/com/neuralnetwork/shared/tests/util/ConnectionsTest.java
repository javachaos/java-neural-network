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
/**
 * 
 */
package com.neuralnetwork.shared.tests.util;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import com.neuralnetwork.shared.layers.IInputLayer;
import com.neuralnetwork.shared.layers.IOutputLayer;
import com.neuralnetwork.shared.layers.InputLayer;
import com.neuralnetwork.shared.layers.OutputLayer;
import com.neuralnetwork.shared.links.ILink;
import com.neuralnetwork.shared.neurons.IInputNeuron;
import com.neuralnetwork.shared.neurons.INeuron;
import com.neuralnetwork.shared.neurons.IOutputNeuron;
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
		IInputLayer l1 = new InputLayer(LAYER_SIZE);
		l1.build();
		IOutputLayer l2 = new OutputLayer(LAYER_SIZE);
		l2.build();
		Connections.getInstance().create(l1, l2);
		
		Iterator<IInputNeuron> iter1 = l1.iterator();
        INeuron tmp = null;
        while (iter1.hasNext()) {
            tmp = iter1.next();
            Iterator<IOutputNeuron> iter2 = l2.iterator();
            for (ILink l : tmp.getOutputLinks()) {
            	if (iter2.hasNext()) {
                    assertEquals(l.getTail(), iter2.next());
            	}
            }
         }
		
	}

}
