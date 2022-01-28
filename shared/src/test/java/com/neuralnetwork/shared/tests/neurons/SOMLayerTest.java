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

import com.neuralnetwork.shared.neurons.SOMLayer;
import com.neuralnetwork.shared.values.Constants;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Fred
 *
 */
class SOMLayerTest {

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.SOMLayer
	 * #SOMLayer()}.
	 */
	@Test
	final void testSOMLayer() {
		SOMLayer l = new SOMLayer();

		assertNotNull(l);
		assertEquals(0, l.size());
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.SOMLayer
	 * #SOMLayer(int)}.
	 */
	@Test
	final void testSOMLayerInt() {
		SOMLayer l = new SOMLayer(Constants.TEN);

		assertNotNull(l);
		assertEquals(Constants.TEN, l.size());
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.SOMLayer
	 * #dist(com.neuralnetwork.shared.neurons.SOMLayer)}.
	 */
	@Test
	final void testDist() {
		SOMLayer l = new SOMLayer(Constants.FIVE);
		SOMLayer l1 = new SOMLayer(Constants.FIVE);

		assertEquals(0.0, l.dist(l1), Constants.TEN * Math.ulp(0));
		
		l = new SOMLayer();
		l1 = new SOMLayer();
		
		l.add(Constants.TEN_D);
		l.add(Constants.TEN_D);
		l.add(Constants.TEN_D);
		l.add(Constants.TEN_D);
		l.add(Constants.TEN_D);
		l1.add(Constants.TEN_D);
		l1.add(Constants.TEN_D);
		l1.add(Constants.TEN_D);
		l1.add(Constants.TEN_D);
		l1.add(Constants.TEN_D);
		
		assertEquals(0.0, l.dist(l1), Constants.TEN * Math.ulp(0));
		
		l1.add(Constants.TEN_D);
		assertNotEquals(0.0, l.dist(l1));
	}

}
