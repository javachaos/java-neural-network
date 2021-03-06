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
package com.neuralnetwork.shared.tests.neurons;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Vector;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neuralnetwork.shared.functions.SigmoidFunction;
import com.neuralnetwork.shared.layers.HiddenLayer;
import com.neuralnetwork.shared.links.ILink;
import com.neuralnetwork.shared.network.NeuralNetContext;
import com.neuralnetwork.shared.neurons.HiddenNeuron;
import com.neuralnetwork.shared.neurons.InputNeuron;
import com.neuralnetwork.shared.neurons.Neuron;
import com.neuralnetwork.shared.neurons.NeuronType;
import com.neuralnetwork.shared.neurons.OutputNeuron;
import com.neuralnetwork.shared.tests.util.TestConstants;
import com.neuralnetwork.shared.values.Constants;

/**
 * @author Fred
 *
 */
public class NeuronTest {

    /**
     * Logger instance.
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(NeuronTest.class);
    
    /**
     * Value used for testing.
     */
    private static final double TEST_VALUE1 = 0.123;
    
    /**
     * Number of times to fail before failing test case.
     */
    private static final int REGRESSION_LIMIT = 100;

    /**
     * Double value of 0.1.
     */
    private static final double D_0_1 = 0.1;
    
	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.Neuron#hashCode()}.
	 */
	@Test
	public final void testHashCode() {
		Neuron n = new HiddenNeuron();
		assertEquals(n.hashCode(), n.hashCode());
	
		n.setValue(null);
		n.setType(null);
		n.addOutputLink(new HiddenNeuron());
		assertEquals(n.hashCode(), n.hashCode());
		Neuron v = new OutputNeuron();
		v.setValue(null);
		v.setType(null);
		v.setActivationFunction(null);
		assertEquals(v.hashCode(), v.hashCode());
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.Neuron#Neuron(
	 * com.neuralnetwork.shared.neurons.NeuronType)}.
	 */
	@Test
	public final void testNeuronNeuronType() {

		Neuron n = new InputNeuron();
		Neuron m = new OutputNeuron();
		Neuron o = new HiddenNeuron();
		
		assertEquals(n.getType(), NeuronType.INPUT);
		assertEquals(m.getType(), NeuronType.OUTPUT);
		assertEquals(o.getType(), NeuronType.HIDDEN);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.Neuron#Neuron()}.
	 */
	@Test
	public final void testNeuron() {
		Neuron n = new InputNeuron(0.0);
		Neuron m = new InputNeuron(1.0);
		assertTrue(!n.equals(m));

		n = new InputNeuron();
		m = new InputNeuron();
		assertTrue(!n.equals(m));
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.Neuron#addInputLink(
	 * com.neuralnetwork.shared.neurons.INeuron,
	 * com.neuralnetwork.shared.values.IValue)}.
	 */
	@Test
	public final void testAddInputLinkINeuronIValueOfQ() {
		Neuron n = new InputNeuron();
		Neuron m = new InputNeuron();
		n.addInputLink(m, TEST_VALUE1);
		ILink l = n.getInputLink(0);
		assertEquals(l.getWeight(), TEST_VALUE1, TestConstants.DELTA);
		assertEquals(l.getHead(), n);
		assertEquals(l.getTail(), m);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.Neuron#addInputLink(
	 * com.neuralnetwork.shared.neurons.INeuron)}.
	 */
	@Test
	public final void testAddInputLinkINeuron() {
		Neuron n = new InputNeuron();
		Neuron m = new InputNeuron();
		n.addInputLink(m);
		ILink l = n.getInputLink(0);
		assertEquals(l.getHead(), n);
		assertEquals(l.getTail(), m);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.Neuron#getInputLink(int)}.
	 */
	@Test
	public final void testGetInputLink() {
		Neuron n = new InputNeuron();
		Neuron m = new InputNeuron();
		n.addInputLink(m);
		ILink l = n.getInputLink(0);
		assertEquals(l.getHead(), n);
		assertEquals(l.getTail(), m);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.Neuron#getInputLinks(int[])}.
	 */
	@Test
	public final void testGetInputLinks() {		
		Neuron n = new InputNeuron();	
		Neuron n1 = new InputNeuron();
		Neuron m = new HiddenNeuron();
		m.addInputLink(n);
		m.addInputLink(n1);
		ILink[] l = m.getInputLinks(0, 1);
		
		assertEquals(l[0].getTail(), n);
		assertEquals(l[1].getTail(), n1);
		
		l = m.getInputLinks();
		assertEquals(l[0].getTail(), n);
		assertEquals(l[1].getTail(), n1);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.Neuron#getType()}.
	 */
	@Test
	public final void testGetType() {
		Neuron n = new InputNeuron();
		Neuron m = new OutputNeuron();
		Neuron o = new HiddenNeuron();
		
		assertEquals(n.getType(), NeuronType.INPUT);
		assertEquals(m.getType(), NeuronType.OUTPUT);
		assertEquals(o.getType(), NeuronType.HIDDEN);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.Neuron#getNextParent()}.
	 */
	@Test
	public final void testGetNextParent() {
		Neuron n = new InputNeuron();	
		Neuron n1 = new InputNeuron();
		Neuron m = new HiddenNeuron();
		Neuron o = new OutputNeuron();
		Neuron o1 = new OutputNeuron();
		n.addOutputLink(m);
		n1.addOutputLink(m);
		m.addOutputLink(o);
		m.addOutputLink(o1);
		
		assertEquals(m.getNextParent(), n);
		assertEquals(m.getNextParent(), n1);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.Neuron#getNextChild()}.
	 */
	@Test
	public final void testGetNextChild() {		
		Neuron n = new InputNeuron();	
		Neuron n1 = new InputNeuron();
		Neuron m = new HiddenNeuron();
		Neuron o = new OutputNeuron();
		Neuron o1 = new OutputNeuron();
		n.addOutputLink(m);
		n1.addOutputLink(m);
		m.addOutputLink(o);
		m.addOutputLink(o1);
		
		assertEquals(m.getNextChild(), o);
		assertEquals(m.getNextChild(), o1);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.Neuron#addOutputLink(
	 * com.neuralnetwork.shared.neurons.INeuron, 
	 * com.neuralnetwork.shared.values.IValue)}.
	 */
	@Test
	public final void testAddOutputLinkINeuronIValueOfQ() {
		Neuron n = new InputNeuron();	
		Neuron n1 = new InputNeuron();
		Neuron m = new HiddenNeuron();
		Neuron o = new OutputNeuron();
		Neuron o1 = new OutputNeuron();
		n.addOutputLink(m, TEST_VALUE1);
		n1.addOutputLink(m, TEST_VALUE1);
		assertEquals(TEST_VALUE1, n.getOutputLink(0).getWeight(), 
		            TestConstants.DELTA);
		assertEquals(TEST_VALUE1, n1.getOutputLink(0).getWeight(), 
		             TestConstants.DELTA);
		m.addOutputLink(o);
		m.addOutputLink(o1);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.Neuron#addOutputLink(
	 * com.neuralnetwork.shared.neurons.INeuron)}.
	 */
	@Test
	public final void testAddOutputLinkINeuron() {
		Neuron n = new InputNeuron();	
		Neuron n1 = new InputNeuron();
		Neuron m = new HiddenNeuron();
		Neuron o = new OutputNeuron();
		Neuron o1 = new OutputNeuron();
		n.addOutputLink(m);
		assertNotNull(n.getOutputLink(0));
		n1.addOutputLink(m);
		assertNotNull(n1.getOutputLink(0));
		m.addOutputLink(o);
		m.addOutputLink(o1);

		assertNotNull(m.getOutputLink(0));
		assertNotNull(m.getOutputLink(1));
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.Neuron#getOutputLink(int)}.
	 */
	@Test
	public final void testGetOutputLink() {
		Neuron n = new InputNeuron();	
		Neuron n1 = new InputNeuron();
		Neuron m = new HiddenNeuron();
		Neuron o = new OutputNeuron();
		Neuron o1 = new OutputNeuron();
		n.addOutputLink(m);
		n1.addOutputLink(m);
		m.addOutputLink(o);
		m.addOutputLink(o1);
		
		assertEquals(n.getOutputLink(0).getTail(), m);
		assertEquals(n1.getOutputLink(0).getTail(), m);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.Neuron#getOutputLinks(int[])}.
	 */
	@Test
	public final void testGetOutputLinks() {
		Neuron n = new InputNeuron();	
		Neuron n1 = new InputNeuron();
		Neuron m = new HiddenNeuron();
		Neuron o = new OutputNeuron();
		Neuron o1 = new OutputNeuron();
		n.addOutputLink(m);
		n1.addOutputLink(m);
		m.addOutputLink(o);
		m.addOutputLink(o1);
		
		ILink[] l = m.getOutputLinks(0, 1);
		
		assertEquals(l[0].getTail(), o);
		assertEquals(l[1].getTail(), o1);
		
		l = m.getOutputLinks();
		
		assertEquals(l[0].getTail(), o);
		assertEquals(l[1].getTail(), o1);
	}
	
	/**
     * Test method for {@link com.neuralnetwork
     * .shared.neurons.Neuron#setOutputLink(int,ILink)}.
     */
	@Test
	public final void testSetOutputLinkIntILink() {
	       Neuron n = new InputNeuron();   
	        Neuron n1 = new InputNeuron();
	        Neuron m = new HiddenNeuron();
	        Neuron o = new OutputNeuron();
	        Neuron o1 = new OutputNeuron();
	        n.addOutputLink(m);
	        n1.addOutputLink(m);
	        m.addOutputLink(o);
	        m.addOutputLink(o1);
	        
	        ILink[] l = m.getOutputLinks(0, 1);
	        m.setOutputLink(0, l[1]);
            m.setOutputLink(1, l[0]);
	        assertEquals(l[0].getTail(), o);
	        assertEquals(l[1].getTail(), o1);
	}
	
	/**
     * Test method for {@link com.neuralnetwork
     * .shared.neurons.Neuron#setOutputLinks(ILink[])}.
     */
    @Test
    public final void testSetOutputLinkILinkArray() {
           Neuron n = new InputNeuron();   
            Neuron n1 = new InputNeuron();
            Neuron m = new HiddenNeuron();
            Neuron o = new OutputNeuron();
            Neuron o1 = new OutputNeuron();
            n.addOutputLink(m);
            n1.addOutputLink(m);
            m.addOutputLink(o);
            m.addOutputLink(o1);
            
            ILink[] l = m.getOutputLinks(0, 1);
            m.setOutputLinks(l);
            assertEquals(l[0].getTail(), o);
            assertEquals(l[1].getTail(), o1);
            
            l = m.getInputLinks(0, 1);
	        m.setInputLink(0, l[1]);
            m.setInputLink(1, l[0]);
            assertEquals(l[0].getTail(), n);
            assertEquals(l[1].getTail(), n1);
    }
    
    /**
     * Test method for {@link com.neuralnetwork
     * .shared.neurons.Neuron#setInputLink(int, ILink)}.
     */
    @Test
    public final void testSetInputLink() {
    	Neuron n = new InputNeuron();   
        Neuron n1 = new InputNeuron();
        Neuron m = new HiddenNeuron();
        Neuron o = new OutputNeuron();
        Neuron o1 = new OutputNeuron();
        m.addInputLink(n);
        m.addInputLink(n1);
        m.addOutputLink(o);
        m.addOutputLink(o1);
        
        ILink[] l = m.getOutputLinks(0, 1);
        m.setInputLinks(l);
        assertEquals(l[0].getHead(), m);
        assertEquals(l[1].getHead(), m);
        assertEquals(l[0].getTail(), o);
        assertEquals(l[1].getTail(), o1);
    }
    
    /**
     * Test method for {@link com.neuralnetwork
     * .shared.neurons.Neuron#setInputs(Vector)}.
     */
    @Test
    public final void testInputs() {
    	Neuron n = new InputNeuron(); 
        Neuron n1 = new InputNeuron();
        Neuron m = new HiddenNeuron();

        m.addInputLink(n);
        m.addInputLink(n1);
        Vector<ILink> v = new Vector<ILink>();
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
        assertEquals(m.getInputs().size(), 0);
    }

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.Neuron#reset()}.
	 */
	@Test
	public final void testReset() {
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
		Neuron m = new HiddenNeuron();
		Neuron o = new OutputNeuron();
		Neuron o1 = new OutputNeuron();
		m.addOutputLink(o);
		m.addOutputLink(o1);
		
		ILink[] l = m.getOutputLinks(0, 1);
		Double v = l[0].getWeight();
		Double v1 = l[1].getWeight();
		
		m.reset();
		
		ILink[] l1 = m.getOutputLinks(0, 1);
		Double v2 = l1[0].getWeight();
		Double v3 = l1[1].getWeight();
		return v.equals(v2) && v1.equals(v3);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.Neuron#setActivationFunction(
	 * com.neuralnetwork.shared.functions.IActivationFunction)}.
	 */
	@Test
	public final void testSetActivationFunction() {
		Neuron m = new HiddenNeuron();
		Neuron o = new OutputNeuron();
		Neuron o1 = new OutputNeuron();
		m.addOutputLink(o);
		m.addOutputLink(o1);
		m.setActivationFunction(null);
		assertEquals(m.getActivationFunction(), null);
		m.setActivationFunction(new SigmoidFunction());
		assertEquals(m.getActivationFunction(), new SigmoidFunction());
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.Neuron#getActivationFunction()}.
	 */
	@Test
	public final void testGetActivationFunction() {
		Neuron m = new HiddenNeuron();
		Neuron o = new OutputNeuron();
		Neuron o1 = new OutputNeuron();
		m.addOutputLink(o);
		m.addOutputLink(o1);
		m.setActivationFunction(null);
		assertEquals(m.getActivationFunction(), null);
		m.setActivationFunction(new SigmoidFunction());
		assertEquals(m.getActivationFunction(), new SigmoidFunction());
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.Neuron#getValue()}.
	 */
	@Test
	public final void testGetValue() {
		Neuron n = new InputNeuron();	
		Neuron n1 = new InputNeuron();
		
		assertFalse(n.getValue().equals(0.0));
		assertFalse(n1.getValue().equals(0.0));
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.Neuron#setValue(
	 * com.neuralnetwork.shared.values.IValue)}.
	 */
	@Test
	public final void testSetValue() {
		Neuron n = new InputNeuron();	
		Neuron n1 = new InputNeuron();
		n.setValue(1.0);
		n1.setValue(1.0);
		assertEquals(1.0, n.getValue(), TestConstants.DELTA);
		assertEquals(1.0, n1.getValue(), TestConstants.DELTA);
	}
	
	/**
     * Test method for {@link com.neuralnetwork
     * .shared.neurons.Neuron#feedforward()}.
     */
    @Test
    public final void testFeedforward() {
        Neuron n = new InputNeuron(D_0_1);
        Neuron n1 = new InputNeuron(D_0_1);
        Neuron m = new HiddenNeuron();
        Neuron o = new OutputNeuron();
        Neuron o1 = new OutputNeuron();
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
        		new NeuralNetContext(null));
        
        LOGGER.debug("======= After Feedforward =======");
        LOGGER.debug(n + " " + n1);
        LOGGER.debug(m.toString());
        LOGGER.debug(o + " " + o1);
        LOGGER.debug("=================================");
    }

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.Neuron
	 * #equals(java.lang.Object)}.
	 */
	@Test
	public final void testEqualsObject() {
		Double val = Math.random();
		Neuron n = new InputNeuron(val);
		Neuron n1 = new InputNeuron(val);

		assertEquals(n, n1);
		n.setType(null);
		n1.setType(null);
		assertEquals(n, n1);
		n1 = null;
		assertFalse(n.equals(n1));
		HiddenLayer n2 = new HiddenLayer(1);
		assertFalse(n.equals(n2));
		n.setActivationFunction(null);
		n1 = new InputNeuron(val);
		assertFalse(n.equals(n1));
		n1.setType(null);
		n1.setActivationFunction(null);
		assertEquals(n, n1);
		n.setActivationFunction(new SigmoidFunction());
		assertFalse(n.equals(n1));
		
		Neuron i = new InputNeuron(val);
		Neuron i1 = new InputNeuron(val);
		Neuron m = new HiddenNeuron();
		Neuron o = new OutputNeuron();
		Neuron o1 = new OutputNeuron();
		
		i.addOutputLink(m);
		i1.addOutputLink(m);
		m.addOutputLink(o);
		m.addOutputLink(o1);
		
		i.getNextChild();
		assertFalse(i.equals(i1));
		
		o.getNextParent();
		assertFalse(o.equals(o1));
		assertFalse(i1.equals(o1));
		Neuron k = new OutputNeuron();
		Neuron k1 = new OutputNeuron();
		k1.addOutputLink(o);
		assertFalse(k.equals(k1));
		
		Neuron l = new OutputNeuron();
		Neuron l1 = new InputNeuron();
		assertFalse(l.equals(l1));
		
		Neuron p = new OutputNeuron();
		p.setValue(1.0);
		Neuron p1 = new OutputNeuron();
		p1.setValue(Constants.TEN_D);
		assertFalse(p.equals(p1));
		
		p.setValue(null);
		p1.setValue(Constants.ELEVEN_D);
		assertFalse(p.equals(p1));
		
		p.setValue(null);
		p1.setValue(null);
		assertEquals(p, p1);
		
	}

}
