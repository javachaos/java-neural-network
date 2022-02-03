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
package com.neuralnetwork.shared.tests.neurons;

import com.neuralnetwork.shared.functions.SigmoidFunction;
import com.neuralnetwork.shared.layers.HiddenNeuronLayer;
import com.neuralnetwork.shared.links.Link;
import com.neuralnetwork.shared.network.NetworkContext;
import com.neuralnetwork.shared.neurons.*;
import com.neuralnetwork.shared.tests.values.TestConstants;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Vector;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Fred
 *
 */
class NeuronImplTest {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(NeuronImplTest.class);

    private static final double TEST_VALUE1 = 0.123;
    private static final int REGRESSION_LIMIT = 100;
    private static final double D_0_1 = 0.1;

	@Test
	final void testHashCode() {
		HiddenNeuronImpl n = new HiddenNeuronImpl();
		assertEquals(n.hashCode(), n.hashCode());
	
		n.setValue(null);
		n.setType(null);
		n.addOutputLink(new HiddenNeuronImpl());
		assertEquals(n.hashCode(), n.hashCode());
		OutputNeuronImpl v = new OutputNeuronImpl();
		v.setValue(null);
		v.setType(null);
		v.setActivationFunction(null);
		assertEquals(v.hashCode(), v.hashCode());
	}

	@Test
	final void testNeuronNeuronType() {

		NeuronImpl n = new InputNeuronImpl();
		NeuronImpl m = new OutputNeuronImpl();
		NeuronImpl o = new HiddenNeuronImpl();
		
		assertEquals(NeuronType.INPUT, n.getType());
		assertEquals(NeuronType.OUTPUT, m.getType());
		assertEquals(NeuronType.HIDDEN, o.getType());
	}

	@Test
	final void testNeuron() {
		NeuronImpl n = new InputNeuronImpl(0.0);
		NeuronImpl m = new InputNeuronImpl(1.0);
		assertNotEquals(n, m);

		n = new InputNeuronImpl();
		m = new InputNeuronImpl();
		assertNotEquals(n, m);
	}

	@Test
	final void testAddInputLinkINeuronIValueOfQ() {
		NeuronImpl n = new InputNeuronImpl();
		NeuronImpl m = new InputNeuronImpl();
		n.addInputLink(m, TEST_VALUE1);
		Link l = n.getInputLink(0);
		assertEquals(TEST_VALUE1, l.getWeight(), TestConstants.DELTA);
		assertEquals(l.getHead(), n);
		assertEquals(l.getTail(), m);
	}

	@Test
	final void testAddInputLinkINeuron() {
		NeuronImpl n = new InputNeuronImpl();
		NeuronImpl m = new InputNeuronImpl();
		n.addInputLink(m);
		Link l = n.getInputLink(0);
		assertEquals(l.getHead(), n);
		assertEquals(l.getTail(), m);
	}

	@Test
	final void testGetInputLink() {
		NeuronImpl n = new InputNeuronImpl();
		NeuronImpl m = new InputNeuronImpl();
		n.addInputLink(m);
		Link l = n.getInputLink(0);
		assertEquals(l.getHead(), n);
		assertEquals(l.getTail(), m);
	}

	@Test
	final void testGetInputLinks() {		
		NeuronImpl n = new InputNeuronImpl();
		NeuronImpl n1 = new InputNeuronImpl();
		NeuronImpl m = new HiddenNeuronImpl();
		m.addInputLink(n);
		m.addInputLink(n1);
		Link[] l = m.getInputLinks(0, 1);
		
		assertEquals(l[0].getTail(), n);
		assertEquals(l[1].getTail(), n1);
		
		l = m.getInputLinks();
		assertEquals(l[0].getTail(), n);
		assertEquals(l[1].getTail(), n1);
	}

	@Test
	final void testGetType() {
		NeuronImpl n = new InputNeuronImpl();
		NeuronImpl m = new OutputNeuronImpl();
		NeuronImpl o = new HiddenNeuronImpl();
		
		assertEquals(NeuronType.INPUT, n.getType());
		assertEquals(NeuronType.OUTPUT, m.getType());
		assertEquals(NeuronType.HIDDEN, o.getType());
	}

	@Test
	final void testGetNextParent() {
		NeuronImpl n = new InputNeuronImpl();
		NeuronImpl n1 = new InputNeuronImpl();
		NeuronImpl m = new HiddenNeuronImpl();
		NeuronImpl o = new OutputNeuronImpl();
		NeuronImpl o1 = new OutputNeuronImpl();
		n.addOutputLink(m);
		n1.addOutputLink(m);
		m.addOutputLink(o);
		m.addOutputLink(o1);
		
		assertEquals(m.getNextParent(), n);
		assertEquals(m.getNextParent(), n1);
	}

	@Test
	final void testGetNextChild() {		
		NeuronImpl n = new InputNeuronImpl();
		NeuronImpl n1 = new InputNeuronImpl();
		NeuronImpl m = new HiddenNeuronImpl();
		NeuronImpl o = new OutputNeuronImpl();
		NeuronImpl o1 = new OutputNeuronImpl();
		n.addOutputLink(m);
		n1.addOutputLink(m);
		m.addOutputLink(o);
		m.addOutputLink(o1);
		
		assertEquals(m.getNextChild(), o);
		assertEquals(m.getNextChild(), o1);
	}

	@Test
	final void testAddOutputLinkINeuronIValueOfQ() {
		NeuronImpl n = new InputNeuronImpl();
		NeuronImpl n1 = new InputNeuronImpl();
		NeuronImpl m = new HiddenNeuronImpl();
		NeuronImpl o = new OutputNeuronImpl();
		NeuronImpl o1 = new OutputNeuronImpl();
		n.addOutputLink(m, TEST_VALUE1);
		n1.addOutputLink(m, TEST_VALUE1);
		assertEquals(TEST_VALUE1, n.getOutputLink(0).getWeight(), 
		            TestConstants.DELTA);
		assertEquals(TEST_VALUE1, n1.getOutputLink(0).getWeight(), 
		             TestConstants.DELTA);
		m.addOutputLink(o);
		m.addOutputLink(o1);
	}

	@Test
	final void testAddOutputLinkINeuron() {
		NeuronImpl n = new InputNeuronImpl();
		NeuronImpl n1 = new InputNeuronImpl();
		NeuronImpl m = new HiddenNeuronImpl();
		NeuronImpl o = new OutputNeuronImpl();
		NeuronImpl o1 = new OutputNeuronImpl();
		n.addOutputLink(m);
		assertNotNull(n.getOutputLink(0));
		n1.addOutputLink(m);
		assertNotNull(n1.getOutputLink(0));
		m.addOutputLink(o);
		m.addOutputLink(o1);

		assertNotNull(m.getOutputLink(0));
		assertNotNull(m.getOutputLink(1));
	}

	@Test
	final void testGetOutputLink() {
		NeuronImpl n = new InputNeuronImpl();
		NeuronImpl n1 = new InputNeuronImpl();
		NeuronImpl m = new HiddenNeuronImpl();
		NeuronImpl o = new OutputNeuronImpl();
		NeuronImpl o1 = new OutputNeuronImpl();
		n.addOutputLink(m);
		n1.addOutputLink(m);
		m.addOutputLink(o);
		m.addOutputLink(o1);
		
		assertEquals(n.getOutputLink(0).getTail(), m);
		assertEquals(n1.getOutputLink(0).getTail(), m);
	}

	@Test
	final void testGetOutputLinks() {
		NeuronImpl n = new InputNeuronImpl();
		NeuronImpl n1 = new InputNeuronImpl();
		NeuronImpl m = new HiddenNeuronImpl();
		NeuronImpl o = new OutputNeuronImpl();
		NeuronImpl o1 = new OutputNeuronImpl();
		n.addOutputLink(m);
		n1.addOutputLink(m);
		m.addOutputLink(o);
		m.addOutputLink(o1);
		
		Link[] l = m.getOutputLinks(0, 1);
		
		assertEquals(l[0].getTail(), o);
		assertEquals(l[1].getTail(), o1);
		
		l = m.getOutputLinks();
		
		assertEquals(l[0].getTail(), o);
		assertEquals(l[1].getTail(), o1);
	}

	@Test
	final void testSetOutputLinkIntILink() {
	       NeuronImpl n = new InputNeuronImpl();
	        NeuronImpl n1 = new InputNeuronImpl();
	        NeuronImpl m = new HiddenNeuronImpl();
	        NeuronImpl o = new OutputNeuronImpl();
	        NeuronImpl o1 = new OutputNeuronImpl();
	        n.addOutputLink(m);
	        n1.addOutputLink(m);
	        m.addOutputLink(o);
	        m.addOutputLink(o1);
	        
	        Link[] l = m.getOutputLinks(0, 1);
	        m.setOutputLink(0, l[1]);
            m.setOutputLink(1, l[0]);
	        assertEquals(l[0].getTail(), o);
	        assertEquals(l[1].getTail(), o1);
	}

    @Test
    final void testSetOutputLinkILinkArray() {
           NeuronImpl n = new InputNeuronImpl();
            NeuronImpl n1 = new InputNeuronImpl();
            NeuronImpl m = new HiddenNeuronImpl();
            NeuronImpl o = new OutputNeuronImpl();
            NeuronImpl o1 = new OutputNeuronImpl();
            n.addOutputLink(m);
            n1.addOutputLink(m);
            m.addOutputLink(o);
            m.addOutputLink(o1);
            
            Link[] l = m.getOutputLinks(0, 1);
            m.setOutputLinks(l);
            assertEquals(l[0].getTail(), o);
            assertEquals(l[1].getTail(), o1);
            
            l = m.getInputLinks(0, 1);
	        m.setInputLink(0, l[1]);
            m.setInputLink(1, l[0]);
            assertEquals(l[0].getTail(), n);
            assertEquals(l[1].getTail(), n1);
    }

    @Test
    final void testSetInputLink() {
    	NeuronImpl n = new InputNeuronImpl();
        NeuronImpl n1 = new InputNeuronImpl();
        NeuronImpl m = new HiddenNeuronImpl();
        NeuronImpl o = new OutputNeuronImpl();
        NeuronImpl o1 = new OutputNeuronImpl();
        m.addInputLink(n);
        m.addInputLink(n1);
        m.addOutputLink(o);
        m.addOutputLink(o1);
        
        Link[] l = m.getOutputLinks(0, 1);
        m.setInputLinks(l);
        assertEquals(l[0].getHead(), m);
        assertEquals(l[1].getHead(), m);
        assertEquals(l[0].getTail(), o);
        assertEquals(l[1].getTail(), o1);
    }

    @Test
    final void testInputs() {
    	NeuronImpl n = new InputNeuronImpl();
        NeuronImpl n1 = new InputNeuronImpl();
        NeuronImpl m = new HiddenNeuronImpl();

        m.addInputLink(n);
        m.addInputLink(n1);
        Vector<Link> v = new Vector<>();
        v.add(m.getInputLinks(0)[0]);
        v.add(m.getInputLinks(1)[0]);
        
    	m.setInputs(v);
        assertEquals(v.get(0).getHead(), m);
        assertEquals(v.get(1).getHead(), m);
        assertEquals(v.get(0).getTail(), n);
        assertEquals(v.get(1).getTail(), n1);
    	m.setInputs(null);
        assertNull(m.getInputs());
        v.clear();
    	m.setInputs(v);
        assertEquals(0, m.getInputs().size());
    }

	@Test
	final void testReset() {
		int i = 0;
		while (getResetTestResult()) {
			if (i++ == REGRESSION_LIMIT) {
				fail("Reset method not correct.");
			}
		}
	}
	
	/**
	 * Method called by testReset() to test the reset method.
	 * 
	 * @return
	 * 		true if the reset method is not working correctly.
	 */
	private boolean getResetTestResult() {
		NeuronImpl m = new HiddenNeuronImpl();
		NeuronImpl o = new OutputNeuronImpl();
		NeuronImpl o1 = new OutputNeuronImpl();
		m.addOutputLink(o);
		m.addOutputLink(o1);
		
		Link[] l = m.getOutputLinks(0, 1);
		Double v = l[0].getWeight();
		Double v1 = l[1].getWeight();
		
		m.reset();
		
		Link[] l1 = m.getOutputLinks(0, 1);
		Double v2 = l1[0].getWeight();
		Double v3 = l1[1].getWeight();
		return v.equals(v2) && v1.equals(v3);
	}

	@Test
	final void testSetActivationFunction() {
		NeuronImpl m = new HiddenNeuronImpl();
		NeuronImpl o = new OutputNeuronImpl();
		NeuronImpl o1 = new OutputNeuronImpl();
		m.addOutputLink(o);
		m.addOutputLink(o1);
		m.setActivationFunction(null);
		assertNull(m.getActivationFunction());
		m.setActivationFunction(new SigmoidFunction());
		assertEquals(m.getActivationFunction(), new SigmoidFunction());
	}

	@Test
	final void testGetActivationFunction() {
		NeuronImpl m = new HiddenNeuronImpl();
		NeuronImpl o = new OutputNeuronImpl();
		NeuronImpl o1 = new OutputNeuronImpl();
		m.addOutputLink(o);
		m.addOutputLink(o1);
		m.setActivationFunction(null);
		assertNull(m.getActivationFunction());
		m.setActivationFunction(new SigmoidFunction());
		assertEquals(m.getActivationFunction(), new SigmoidFunction());
	}

	@Test
	final void testGetValue() {
		NeuronImpl n = new InputNeuronImpl();
		NeuronImpl n1 = new InputNeuronImpl();
		assertNotEquals(0.0, n.getValue());
		assertNotEquals(0.0, n1.getValue());
	}

	@Test
	final void testSetValue() {
		NeuronImpl n = new InputNeuronImpl();
		NeuronImpl n1 = new InputNeuronImpl();
		n.setValue(1.0);
		n1.setValue(1.0);
		assertEquals(1.0, n.getValue(), TestConstants.DELTA);
		assertEquals(1.0, n1.getValue(), TestConstants.DELTA);
	}

    @Test
    final void testFeedforward() {
        NeuronImpl n = new InputNeuronImpl(D_0_1);
        NeuronImpl n1 = new InputNeuronImpl(D_0_1);
        NeuronImpl m = new HiddenNeuronImpl();
        NeuronImpl o = new OutputNeuronImpl();
        NeuronImpl o1 = new OutputNeuronImpl();
        n.addOutputLink(m);
        n1.addOutputLink(m);
        m.addOutputLink(o);
        m.addOutputLink(o1);

        assertEquals(m.getInputLink(0), n.getOutputLink(0));

        LOGGER.debug("====== Before Feedforward ======");
        LOGGER.debug(n + " " + n1);
        LOGGER.debug(m.toString());
        LOGGER.debug(o + " " + o1);
        LOGGER.debug("================================");
        
        n.feedforward(D_0_1,
        		new NetworkContext(null));
        LOGGER.debug("======= After Feedforward =======");
        LOGGER.debug(n + " " + n1);
        LOGGER.debug(m.toString());
        LOGGER.debug(o + " " + o1);
        LOGGER.debug("=================================");
    }

	@Test
	final void testEqualsObject() {
		Double val = Math.random();
		NeuronImpl n = new InputNeuronImpl(val);
		NeuronImpl n1 = new InputNeuronImpl(val);

		assertEquals(n, n1);
		n.setType(null);
		n1.setType(null);
		assertEquals(n, n1);
		assertNotEquals(null, n);
		HiddenNeuronLayer n2 = new HiddenNeuronLayer(1, 0);
		n.setActivationFunction(null);
		n1 = new InputNeuronImpl(val);
		assertNotEquals(n1, n);
		n1.setType(null);
		n1.setActivationFunction(null);
		assertEquals(n, n1);
		n.setActivationFunction(new SigmoidFunction());
		assertNotEquals(n1, n);
		
		NeuronImpl i = new InputNeuronImpl(val);
		NeuronImpl i1 = new InputNeuronImpl(val);
		NeuronImpl m = new HiddenNeuronImpl();
		NeuronImpl o = new OutputNeuronImpl();
		NeuronImpl o1 = new OutputNeuronImpl();
		
		i.addOutputLink(m);
		i1.addOutputLink(m);
		m.addOutputLink(o);
		m.addOutputLink(o1);
		
		i.getNextChild();
		assertNotEquals(i, i1);
		
		o.getNextParent();
		assertNotEquals(o, o1);
		assertNotEquals(i1, o1);
		NeuronImpl k = new OutputNeuronImpl();
		NeuronImpl k1 = new OutputNeuronImpl();
		k1.addOutputLink(o);
		assertNotEquals(k, k1);
				
		NeuronImpl l = new OutputNeuronImpl();
		NeuronImpl l1 = new InputNeuronImpl();

		assertNotEquals(l, l1);
		
		NeuronImpl p = new OutputNeuronImpl();
		p.setValue(1.0);
		NeuronImpl p1 = new OutputNeuronImpl();
		p1.setValue(10.0);

		assertNotEquals(p, p1);
		
		p.setValue(null);
		p1.setValue(11.0);
		assertNotEquals(p, p1);
		
		p.setValue(null);
		p1.setValue(null);
		assertEquals(p, p1);
		
	}

}
