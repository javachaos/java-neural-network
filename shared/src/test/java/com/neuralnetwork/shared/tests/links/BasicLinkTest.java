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
package com.neuralnetwork.shared.tests.links;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.neuralnetwork.shared.exceptions.NeuronLinkException;
import com.neuralnetwork.shared.links.Link;
import com.neuralnetwork.shared.links.BasicLink;
import com.neuralnetwork.shared.neurons.HiddenNeuronImpl;
import com.neuralnetwork.shared.neurons.HiddenNeuron;
import com.neuralnetwork.shared.neurons.Neuron;
import com.neuralnetwork.shared.tests.values.TestConstants;

/**
 * @author Fred
 *
 */
class BasicLinkTest {

    private static final double WEIGHT_TEST_VALUE = 0.1;
    private static final double WEIGHT_TEST_VALUE2 = 0.2;
    private static final double WEIGHT_TEST_VALUE3 = 0.234239;
	private static final double NULL = 0.0;

	@Test
    final void testLink() {
		Neuron n = new HiddenNeuronImpl();
		Link l = new BasicLink(n, n, 1.0);
		assertNotNull(l);
		
		try {
			new BasicLink(null, n, 1.0);
		} catch (NeuronLinkException e) {
			assertEquals("Head link was null.", e.getMessage());
		}
		
		try {
			new BasicLink(n, null, 1.0);
		} catch (NeuronLinkException e) {
			assertEquals("Tail link was null.", e.getMessage());
		}
		
		l = new BasicLink(n, n, NULL);
		assertEquals(0.0, l.getWeight(), TestConstants.DELTA);
	}

	@Test
	final void testGetHead() {
		HiddenNeuron h = new HiddenNeuronImpl();
		HiddenNeuron t = new HiddenNeuronImpl();
		Link l = new BasicLink(h, t, 1.0);
		assertEquals(l.getHead(), h);
	}

	@Test
    final void testGetTail() {
		HiddenNeuron h = new HiddenNeuronImpl();
		HiddenNeuron t = new HiddenNeuronImpl();
		Link l = new BasicLink(h, t, 1.0);
		assertEquals(l.getTail(), t);
	}

	@Test
    final void testGetWeight() {
		HiddenNeuron h = new HiddenNeuronImpl();
		HiddenNeuron t = new HiddenNeuronImpl();
		Link l = new BasicLink(h, t, 1.0);
		assertEquals(1.0, l.getWeight(), TestConstants.DELTA);
	}

	@Test
    final void testSetHead() {
		HiddenNeuron h = new HiddenNeuronImpl();
		HiddenNeuron t = new HiddenNeuronImpl();
		Link l = new BasicLink(h, t, 1.0);
		l.setHead(t);
		assertEquals(l.getHead(), t);
		
		try {
			l.setHead(null);
		} catch (NeuronLinkException e) {
			assertEquals("Error cannot set null head.", e.getMessage());
		}
	}

	@Test
    final void testSetTail() {
		HiddenNeuron h = new HiddenNeuronImpl();
		HiddenNeuron t = new HiddenNeuronImpl();
		Link l = new BasicLink(h, t, 1.0);
		l.setTail(h);
		assertEquals(l.getTail(), h);
		
		try {
			l.setTail(null);
		} catch (NeuronLinkException e) {
			assertEquals("Error cannot set null tail.", e.getMessage());
		}
	}

	@Test
    final void testUpdateWeight() {
		HiddenNeuron h = new HiddenNeuronImpl();
		HiddenNeuron t = new HiddenNeuronImpl();
		Link l = new BasicLink(h, t, 0.0);
		l.updateWeight(WEIGHT_TEST_VALUE);
		assertEquals(WEIGHT_TEST_VALUE, l.getWeight(), TestConstants.DELTA);
		l.updateWeight(-WEIGHT_TEST_VALUE);
		assertEquals(0.0, l.getWeight(), TestConstants.DELTA);
	}

	@Test
    final void testSetWeight() {
		HiddenNeuron h = new HiddenNeuronImpl();
		HiddenNeuron t = new HiddenNeuronImpl();
		Link l = new BasicLink(h, t, 1.0);
		l.setWeight(WEIGHT_TEST_VALUE3);
		assertEquals(WEIGHT_TEST_VALUE3, l.getWeight(), TestConstants.DELTA);
	}

	@Test
    final void testGetAge() {
		HiddenNeuron h = new HiddenNeuronImpl();
		HiddenNeuron t = new HiddenNeuronImpl();
		Link l = new BasicLink(h, t, 1.0);
		l.updateWeight(-WEIGHT_TEST_VALUE2);
		l.updateWeight(-WEIGHT_TEST_VALUE2);
		l.updateWeight(-WEIGHT_TEST_VALUE2);
		l.updateWeight(-WEIGHT_TEST_VALUE2);
		assertEquals(l.getAge(), 2 * 2);
	}

}
