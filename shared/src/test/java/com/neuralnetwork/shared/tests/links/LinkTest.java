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
package com.neuralnetwork.shared.tests.links;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.neuralnetwork.shared.exceptions.NeuronLinkException;
import com.neuralnetwork.shared.links.ILink;
import com.neuralnetwork.shared.links.Link;
import com.neuralnetwork.shared.neurons.HiddenNeuron;
import com.neuralnetwork.shared.neurons.IHiddenNeuron;
import com.neuralnetwork.shared.neurons.INeuron;
import com.neuralnetwork.shared.tests.values.TestConstants;

/**
 * @author Fred
 *
 */
class LinkTest {
	
    /**
     * Value used in testing.
     */
    private static final double WEIGHT_TEST_VALUE = 0.1;
    
    /**
     * Value used in testing.
     */
    private static final double WEIGHT_TEST_VALUE2 = 0.2;
    
    /**
     * Value used in testing.
     */
    private static final double WEIGHT_TEST_VALUE3 = 0.234239;
    
	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.links.Link#Link(com.neuralnetwork.shared.neurons.INeuron,
	 * com.neuralnetwork.shared.neurons.INeuron,
	 * com.neuralnetwork.shared.values.IValue)}.
	 */
	@Test
    final void testLink() {
		INeuron n = new HiddenNeuron();
		ILink l = new Link(n, n, 1.0);
		assertNotNull(l);
		
		try {
			new Link(null, n, 1.0);
		} catch (NeuronLinkException e) {
			assertEquals("Head link was null.", e.getMessage());
		}
		
		try {
			new Link(n, null, 1.0);
		} catch (NeuronLinkException e) {
			assertEquals("Tail link was null.", e.getMessage());
		}
		
		l = new Link(n, n, null);

        //assertTrue(Math.abs(l.getNeuron(0).getValue() - 1.0) < EPSILON);
		assertEquals(0.0, l.getWeight(), TestConstants.DELTA);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.links.Link#getHead()}.
	 */
	@Test
	final void testGetHead() {
		IHiddenNeuron h = new HiddenNeuron();
		IHiddenNeuron t = new HiddenNeuron();
		ILink l = new Link(h, t, 1.0);
		assertEquals(l.getHead(), h);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.links.Link#getTail()}.
	 */
	@Test
    final void testGetTail() {
		IHiddenNeuron h = new HiddenNeuron();
		IHiddenNeuron t = new HiddenNeuron();
		ILink l = new Link(h, t, 1.0);
		assertEquals(l.getTail(), t);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.links.Link#getWeight()}.
	 */
	@Test
    final void testGetWeight() {
		IHiddenNeuron h = new HiddenNeuron();
		IHiddenNeuron t = new HiddenNeuron();
		ILink l = new Link(h, t, 1.0);
		assertEquals(1.0, l.getWeight(), TestConstants.DELTA);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.links.Link#setHead(com.neuralnetwork.shared.neurons.INeuron)}.
	 */
	@Test
    final void testSetHead() {
		IHiddenNeuron h = new HiddenNeuron();
		IHiddenNeuron t = new HiddenNeuron();
		ILink l = new Link(h, t, 1.0);
		l.setHead(t);
		assertEquals(l.getHead(), t);
		
		try {
			l.setHead(null);
		} catch (NeuronLinkException e) {
			assertEquals("Error cannot set null head.", e.getMessage());
		}
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.links.Link#setTail(com.neuralnetwork.shared.neurons.INeuron)}.
	 */
	@Test
    final void testSetTail() {
		IHiddenNeuron h = new HiddenNeuron();
		IHiddenNeuron t = new HiddenNeuron();
		ILink l = new Link(h, t, 1.0);
		l.setTail(h);
		assertEquals(l.getTail(), h);
		
		try {
			l.setTail(null);
		} catch (NeuronLinkException e) {
			assertEquals("Error cannot set null tail.", e.getMessage());
		}
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.links.Link#updateWeight(com.neuralnetwork
	 * .shared.values.IValue)}.
	 */
	@Test
    final void testUpdateWeight() {
		IHiddenNeuron h = new HiddenNeuron();
		IHiddenNeuron t = new HiddenNeuron();
		ILink l = new Link(h, t, 0.0);
		l.updateWeight(WEIGHT_TEST_VALUE);
		assertEquals(WEIGHT_TEST_VALUE, l.getWeight(), TestConstants.DELTA);
		l.updateWeight(-WEIGHT_TEST_VALUE);
		assertEquals(0.0, l.getWeight(), TestConstants.DELTA);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.links.Link#setWeight(com.neuralnetwork
	 * .shared.values.IValue)}.
	 */
	@Test
    final void testSetWeight() {
		IHiddenNeuron h = new HiddenNeuron();
		IHiddenNeuron t = new HiddenNeuron();
		ILink l = new Link(h, t, 1.0);
		l.setWeight(WEIGHT_TEST_VALUE3);
		assertEquals(WEIGHT_TEST_VALUE3, l.getWeight(), TestConstants.DELTA);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.links.Link#getAge()}.
	 */
	@Test
    final void testGetAge() {
		IHiddenNeuron h = new HiddenNeuron();
		IHiddenNeuron t = new HiddenNeuron();
		ILink l = new Link(h, t, 1.0);
		l.updateWeight(-WEIGHT_TEST_VALUE2);
		l.updateWeight(-WEIGHT_TEST_VALUE2);
		l.updateWeight(-WEIGHT_TEST_VALUE2);
		l.updateWeight(-WEIGHT_TEST_VALUE2);
		assertEquals(l.getAge(), 2 * 2);
	}

}
