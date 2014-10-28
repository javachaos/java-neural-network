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
	 * Testing constant.
	 */
	private static final double D_1_1 = 1.1;
	
	/**
	 * Testing constant.
	 */
	private static final double D_2_1 = 2.1;
	
	/**
	 * Testing constant.
	 */
	private static final double D_0_1 = 0.1;

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.util.Pair#hashCode()}.
	 */
	@Test
	public final void testHashCode() {
		Pair<Object, Object> p = new Pair<Object, Object>(null, null);
		Pair<Object, Object> p1 = new Pair<Object, Object>(null, null);
		
		assertEquals(p.hashCode(), p1.hashCode());
		
		p = new Pair<Object, Object>(null, new DoubleValue(D_0_1));
		p1 = new Pair<Object, Object>(null, new DoubleValue(D_0_1));
		
		assertEquals(p.hashCode(), p1.hashCode());
		
		p = new Pair<Object, Object>(new DoubleValue(D_0_1), null);
		p1 = new Pair<Object, Object>(new DoubleValue(D_0_1), null);
		
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
				new Pair<Object, Object>(
						new DoubleValue(D_0_1), new DoubleValue(D_0_1));
		
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
		
		p = new Pair<Object, Object>(null, new DoubleValue(D_0_1));
		p1 = new Pair<Object, Object>(null, new DoubleValue(D_0_1));
		assertEquals(p, p1);
		
		p = new Pair<Object, Object>(new DoubleValue(D_0_1), null);
		p1 = new Pair<Object, Object>(new DoubleValue(D_0_1), null);
		assertEquals(p, p1);
		
		p = new Pair<Object, Object>(new DoubleValue(D_0_1), null);
		p1 = new Pair<Object, Object>(null, new DoubleValue(D_0_1));
		assertFalse(p.equals(p1));
		
		p = new Pair<Object, Object>(new DoubleValue(D_0_1), null);
		p1 = new Pair<Object, Object>(new DoubleValue(D_0_1), 
				new DoubleValue(D_0_1));
		assertFalse(p.equals(p1));
		
		p = new Pair<Object, Object>(new DoubleValue(D_0_1), 
				new DoubleValue(D_2_1));
		p1 = new Pair<Object, Object>(new DoubleValue(D_0_1), 
				new DoubleValue(D_0_1));
		assertFalse(p.equals(p1));

		//Test (first == null)
		p = new Pair<Object, Object>(null, new DoubleValue(D_0_1));
		p1 = new Pair<Object, Object>(new DoubleValue(D_0_1), null);
		assertFalse(p.equals(p1));
		
		//Test (obj == null)
		p = new Pair<Object, Object>(new DoubleValue(D_0_1), null);
		p1 = null;
		assertFalse(p.equals(p1));
		
		p = new Pair<Object, Object>(new DoubleValue(D_0_1), null);
		HiddenNeuron n = new HiddenNeuron();
		assertFalse(p.equals(n));
		
		p = new Pair<Object, Object>(new DoubleValue(D_0_1), null);
		p1 = new Pair<Object, Object>(new DoubleValue(D_0_1), null);
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
		
		p = new Pair<Object, Object>(null, new DoubleValue(D_0_1));
		p1 = new Pair<Object, Object>(null, new DoubleValue(D_0_1));
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
		
		p = new Pair<Object, Object>(null, new DoubleValue(D_0_1));
		p1 = new Pair<Object, Object>(null, new DoubleValue(D_0_1));
		assertEquals(p.getSecond(), p1.getSecond());
		
		p.setSecond(new DoubleValue(D_1_1));
		p1.setSecond(new DoubleValue(D_1_1));
		
		assertEquals(p.getSecond(), new DoubleValue(D_1_1));
		assertEquals(p1.getSecond(), new DoubleValue(D_1_1));
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.util.Pair#getFirst()}.
	 */
	@Test
	public final void testGetFirst() {
		Pair<Object, Object> p;
		Pair<Object, Object> p1;
		
		p = new Pair<Object, Object>(null, new DoubleValue(D_0_1));
		p1 = new Pair<Object, Object>(null, new DoubleValue(D_0_1));
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
		
		p = new Pair<Object, Object>(null, new DoubleValue(D_0_1));
		p1 = new Pair<Object, Object>(null, new DoubleValue(D_0_1));
		assertEquals(p.getFirst(), p1.getFirst());
		
		p.setFirst(new DoubleValue(D_1_1));
		p1.setFirst(new DoubleValue(D_1_1));
		
		assertEquals(p.getFirst(), new DoubleValue(D_1_1));
		assertEquals(p1.getFirst(), new DoubleValue(D_1_1));
	}

}
