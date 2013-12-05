/**
 * 
 */
package com.neuralnetwork.shared.tests.links;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.neuralnetwork.shared.exceptions.NeuronLinkException;
import com.neuralnetwork.shared.links.ILink;
import com.neuralnetwork.shared.links.Link;
import com.neuralnetwork.shared.neurons.HiddenNeuron;
import com.neuralnetwork.shared.neurons.IHiddenNeuron;
import com.neuralnetwork.shared.neurons.INeuron;
import com.neuralnetwork.shared.values.DoubleValue;
import com.neuralnetwork.shared.values.OneValue;
import com.neuralnetwork.shared.values.ZeroValue;

/**
 * @author Fred
 *
 */
public class LinkTest {

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.links.Link#Link(com.neuralnetwork.shared.neurons.INeuron, com.neuralnetwork.shared.neurons.INeuron, com.neuralnetwork.shared.values.IValue)}.
	 */
	@Test
	public void testLink() {
		INeuron n = new HiddenNeuron();
		ILink l = new Link(n, n, new OneValue());
		assertNotNull(l);
		
		try {
			new Link(null, n, new OneValue());
		} catch (NeuronLinkException e) {
			assertEquals(e.getMessage(), "Head link was null.");
		}
		
		try {
			new Link(n, null, new OneValue());
		} catch (NeuronLinkException e) {
			assertEquals(e.getMessage(), "Tail link was null.");
		}
		
		l = new Link(n, n, null);
		assertEquals(l.getWeight(), new ZeroValue());
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.links.Link#getHead()}.
	 */
	@Test
	public void testGetHead() {
		IHiddenNeuron h = new HiddenNeuron();
		IHiddenNeuron t = new HiddenNeuron();
		ILink l = new Link(h, t, new OneValue());
		assertEquals(l.getHead(), h);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.links.Link#getTail()}.
	 */
	@Test
	public void testGetTail() {
		IHiddenNeuron h = new HiddenNeuron();
		IHiddenNeuron t = new HiddenNeuron();
		ILink l = new Link(h, t, new OneValue());
		assertEquals(l.getTail(), t);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.links.Link#getWeight()}.
	 */
	@Test
	public void testGetWeight() {
		IHiddenNeuron h = new HiddenNeuron();
		IHiddenNeuron t = new HiddenNeuron();
		ILink l = new Link(h, t, new OneValue());
		assertEquals(l.getWeight(), new OneValue());
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.links.Link#setHead(com.neuralnetwork.shared.neurons.INeuron)}.
	 */
	@Test
	public void testSetHead() {
		IHiddenNeuron h = new HiddenNeuron();
		IHiddenNeuron t = new HiddenNeuron();
		ILink l = new Link(h, t, new OneValue());
		l.setHead(t);
		assertEquals(l.getHead(), t);
		
		try {
			l.setHead(null);
		} catch (NeuronLinkException e) {
			assertEquals(e.getMessage(), "Error cannot set null head.");
		}
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.links.Link#setTail(com.neuralnetwork.shared.neurons.INeuron)}.
	 */
	@Test
	public void testSetTail() {
		IHiddenNeuron h = new HiddenNeuron();
		IHiddenNeuron t = new HiddenNeuron();
		ILink l = new Link(h, t, new OneValue());
		l.setTail(h);
		assertEquals(l.getTail(), h);
		
		try {
			l.setTail(null);
		} catch (NeuronLinkException e) {
			assertEquals(e.getMessage(), "Error cannot set null tail.");
		}
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.links.Link#updateWeight(com.neuralnetwork
	 * .shared.values.IValue)}.
	 */
	@Test
	public void testUpdateWeight() {
		IHiddenNeuron h = new HiddenNeuron();
		IHiddenNeuron t = new HiddenNeuron();
		ILink l = new Link(h, t, new ZeroValue());
		l.updateWeight(new DoubleValue(0.1));
		assertEquals(l.getWeight(), new DoubleValue(0.1));
		l.updateWeight(new DoubleValue(-0.1));
		assertEquals(l.getWeight(), new DoubleValue(0.0));
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.links.Link#setWeight(com.neuralnetwork
	 * .shared.values.IValue)}.
	 */
	@Test
	public void testSetWeight() {
		IHiddenNeuron h = new HiddenNeuron();
		IHiddenNeuron t = new HiddenNeuron();
		ILink l = new Link(h, t, new OneValue());
		l.setWeight(new DoubleValue(0.234239));
		assertEquals(l.getWeight(), new DoubleValue(0.234239));
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.links.Link#getAge()}.
	 */
	@Test
	public void testGetAge() {
		IHiddenNeuron h = new HiddenNeuron();
		IHiddenNeuron t = new HiddenNeuron();
		ILink l = new Link(h, t, new OneValue());
		l.updateWeight(new DoubleValue(-0.2));
		l.updateWeight(new DoubleValue(-0.2));
		l.updateWeight(new DoubleValue(-0.2));
		l.updateWeight(new DoubleValue(-0.2));
		assertEquals(l.getAge(), 4);
	}

}
