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
package com.neuralnetwork.shared.tests.util;

import com.neuralnetwork.shared.neurons.HiddenNeuronImpl;
import com.neuralnetwork.shared.util.Pair;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PairTest {

	private static final double D_1_1 = 1.1;
	private static final double D_2_1 = 2.1;
	private static final double D_0_1 = 0.1;

	Pair<Object, Object> p;
	Pair<Object, Object> p1;

	@Test
	final void testHashCode() {
		p = new Pair<>(null, null);
		p1 = new Pair<>(null, null);
		assertEquals(p.hashCode(), p1.hashCode());
		p = new Pair<>(null, D_0_1);
		p1 = new Pair<>(null, D_0_1);
		assertEquals(p.hashCode(), p1.hashCode());
		p = new Pair<>(D_0_1, null);
		p1 = new Pair<>(D_0_1, null);
		assertEquals(p.hashCode(), p1.hashCode());
	}

	@Test
	final void testPair() {
		p =	new Pair<>(D_0_1, D_0_1);
		assertNotNull(p);
	}

	@Test
	final void testEqualsObject() {
		p = new Pair<>(null, null);
		p1 = new Pair<>(null, null);
		assertEquals(p, p1);
		p = new Pair<>(null, D_0_1);
		p1 = new Pair<>(null, D_0_1);
		assertEquals(p, p1);
		p = new Pair<>(D_0_1, null);
		p1 = new Pair<>(D_0_1, null);
		assertEquals(p, p1);
		p = new Pair<>(D_0_1, null);
		p1 = new Pair<>(null, D_0_1);
		assertNotEquals(p, p1);
		p = new Pair<>(D_0_1, null);
		p1 = new Pair<>(D_0_1,
				D_0_1);
		assertNotEquals(p, p1);
		p = new Pair<>(D_0_1,
				D_2_1);
		p1 = new Pair<>(D_0_1,
				D_0_1);
		assertNotEquals(p, p1);
		p = new Pair<>(null, D_0_1);
		p1 = new Pair<>(D_0_1, null);
		assertNotEquals(p, p1);
		p = new Pair<>(D_0_1, null);
		p1 = null;
		assertNotEquals(p, p1);
		p = new Pair<>(D_0_1, null);
		HiddenNeuronImpl n = new HiddenNeuronImpl();
		p = new Pair<>(D_0_1, null);
		p1 = new Pair<>(D_0_1, null);
		assertEquals(p, p1);
		assertEquals(p, p);
	}

	@Test
	final void testGetSecond() {
		p = new Pair<>(null, D_0_1);
		p1 = new Pair<>(null, D_0_1);
		assertEquals(p.getSecond(), p1.getSecond());
	}

	@Test
	final void testSetSecond() {
		p = new Pair<>(null, D_0_1);
		p1 = new Pair<>(null, D_0_1);
		assertEquals(p.getSecond(), p1.getSecond());
		p.setSecond(D_1_1);
		p1.setSecond(D_1_1);
		assertEquals(D_1_1, p.getSecond());
		assertEquals(D_1_1, p1.getSecond());
	}

	@Test
	final void testGetFirst() {
		p = new Pair<>(null, D_0_1);
		p1 = new Pair<>(null, D_0_1);
		assertEquals(p.getFirst(), p1.getFirst());
	}

	@Test
	final void testSetFirst() {
		p = new Pair<>(null, D_0_1);
		p1 = new Pair<>(null, D_0_1);
		assertEquals(p.getFirst(), p1.getFirst());
		p.setFirst(D_1_1);
		p1.setFirst(D_1_1);
		assertEquals(D_1_1, p.getFirst());
		assertEquals(D_1_1, p1.getFirst());
	}

}
