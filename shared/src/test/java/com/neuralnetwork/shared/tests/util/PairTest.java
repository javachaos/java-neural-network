/**
 * 
 */
package com.neuralnetwork.shared.tests.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.neuralnetwork.shared.neurons.HiddenNeuron;
import com.neuralnetwork.shared.util.Pair;
import com.neuralnetwork.shared.values.DoubleValue;

/**
 * @author Fred
 *
 */
public class PairTest {

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.util.Pair#hashCode()}.
	 */
	@Test
	public final void testHashCode() {
		Pair<Object, Object> p = new Pair<Object, Object>(null, null);
		Pair<Object, Object> p1 = new Pair<Object, Object>(null, null);
		
		assertEquals(p.hashCode(), p1.hashCode());
		
		p = new Pair<Object, Object>(null, new DoubleValue(0.1));
		p1 = new Pair<Object, Object>(null, new DoubleValue(0.1));
		
		assertEquals(p.hashCode(), p1.hashCode());
		
		p = new Pair<Object, Object>(new DoubleValue(0.1), null);
		p1 = new Pair<Object, Object>(new DoubleValue(0.1), null);
		
		assertEquals(p.hashCode(), p1.hashCode());
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.util.Pair
	 * #Pair(java.lang.Object, java.lang.Object)}.
	 */
	@Test
	public final void testPair() {
		Pair<Object, Object> p = 
				new Pair<Object, Object>(new DoubleValue(0.1), new DoubleValue(0.1));
		
		assertNotNull(p);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.util.Pair#equals(java.lang.Object)}.
	 */
	@Test
	public final void testEqualsObject() {
		Pair<Object, Object> p = new Pair<Object, Object>(null, null);
		Pair<Object, Object> p1 = new Pair<Object, Object>(null, null);
		assertEquals(p, p1);
		
		p = new Pair<Object, Object>(null, new DoubleValue(0.1));
		p1 = new Pair<Object, Object>(null, new DoubleValue(0.1));
		assertEquals(p, p1);
		
		p = new Pair<Object, Object>(new DoubleValue(0.1), null);
		p1 = new Pair<Object, Object>(new DoubleValue(0.1), null);
		assertEquals(p, p1);
		
		p = new Pair<Object, Object>(new DoubleValue(0.1), null);
		p1 = new Pair<Object, Object>(null, new DoubleValue(0.1));
		assertFalse(p.equals(p1));
		
		p = new Pair<Object, Object>(new DoubleValue(0.1), null);
		p1 = new Pair<Object, Object>(new DoubleValue(0.1), 
				new DoubleValue(0.1));
		assertFalse(p.equals(p1));
		
		p = new Pair<Object, Object>(new DoubleValue(0.1), 
				new DoubleValue(2.1));
		p1 = new Pair<Object, Object>(new DoubleValue(0.1), 
				new DoubleValue(0.1));
		assertFalse(p.equals(p1));

		//Test (first == null)
		p = new Pair<Object, Object>(null, new DoubleValue(0.1));
		p1 = new Pair<Object, Object>(new DoubleValue(0.1), null);
		assertFalse(p.equals(p1));
		
		//Test (obj == null)
		p = new Pair<Object, Object>(new DoubleValue(0.1), null);
		p1 = null;
		assertFalse(p.equals(p1));
		
		p = new Pair<Object, Object>(new DoubleValue(0.1), null);
		HiddenNeuron n = new HiddenNeuron();
		assertFalse(p.equals(n));
		
		p = new Pair<Object, Object>(new DoubleValue(0.1), null);
		p1 = new Pair<Object, Object>(new DoubleValue(0.1), null);
		assertEquals(p, p1);
		assertEquals(p, p);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.util.Pair#getSecond()}.
	 */
	@Test
	public final void testGetSecond() {
		Pair<Object, Object> p;
		Pair<Object, Object> p1;
		
		p = new Pair<Object, Object>(null, new DoubleValue(0.1));
		p1 = new Pair<Object, Object>(null, new DoubleValue(0.1));
		assertEquals(p.getSecond(), p1.getSecond());
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.util.Pair#setSecond(java.lang.Object)}.
	 */
	@Test
	public final void testSetSecond() {
		Pair<Object, Object> p;
		Pair<Object, Object> p1;
		
		p = new Pair<Object, Object>(null, new DoubleValue(0.1));
		p1 = new Pair<Object, Object>(null, new DoubleValue(0.1));
		assertEquals(p.getSecond(), p1.getSecond());
		
		p.setSecond(new DoubleValue(1.1));
		p1.setSecond(new DoubleValue(1.1));
		
		assertEquals(p.getSecond(), new DoubleValue(1.1));
		assertEquals(p1.getSecond(), new DoubleValue(1.1));
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.util.Pair#getFirst()}.
	 */
	@Test
	public final void testGetFirst() {
		Pair<Object, Object> p;
		Pair<Object, Object> p1;
		
		p = new Pair<Object, Object>(null, new DoubleValue(0.1));
		p1 = new Pair<Object, Object>(null, new DoubleValue(0.1));
		assertEquals(p.getFirst(), p1.getFirst());
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.util.Pair#setFirst(java.lang.Object)}.
	 */
	@Test
	public final void testSetFirst() {
		Pair<Object, Object> p;
		Pair<Object, Object> p1;
		
		p = new Pair<Object, Object>(null, new DoubleValue(0.1));
		p1 = new Pair<Object, Object>(null, new DoubleValue(0.1));
		assertEquals(p.getFirst(), p1.getFirst());
		
		p.setFirst(new DoubleValue(1.1));
		p1.setFirst(new DoubleValue(1.1));
		
		assertEquals(p.getFirst(), new DoubleValue(1.1));
		assertEquals(p1.getFirst(), new DoubleValue(1.1));
	}

}
