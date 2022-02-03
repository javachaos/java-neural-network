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
import com.neuralnetwork.shared.neurons.SOMLayerImpl;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SOMNeuronImplLayerTest {

	@Test
	final void testSOMLayer() {
		SOMLayerImpl l = new SOMLayerImpl();

		assertNotNull(l);
		assertEquals(0, l.size());
	}

	@Test
	final void testSOMLayerInt() {
		SOMLayerImpl l = new SOMLayerImpl(10);

		assertNotNull(l);
		assertEquals(10, l.size());
	}

	@Test
	final void testDist() {
		SOMLayerImpl l = new SOMLayerImpl(5);
		SOMLayerImpl l1 = new SOMLayerImpl(5);

		assertEquals(0.0, l.dist(l1), 10 * Math.ulp(0));
		
		l = new SOMLayerImpl();
		l1 = new SOMLayerImpl();
		
		l.add(10.0);
		l.add(10.0);
		l.add(10.0);
		l.add(10.0);
		l.add(10.0);
		l1.add(10.0);
		l1.add(10.0);
		l1.add(10.0);
		l1.add(10.0);
		l1.add(10.0);
		
		assertEquals(0.0, l.dist(l1), 10 * Math.ulp(0));
		
		l1.add(10.0);
		assertNotEquals(0.0, l.dist(l1));
	}

}
