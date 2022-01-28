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

import com.neuralnetwork.shared.neurons.NeuronType;
import com.neuralnetwork.shared.neurons.SOMLattice;
import com.neuralnetwork.shared.neurons.SOMLayer;
import com.neuralnetwork.shared.values.Constants;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Fred
 *
 */
class SOMLatticeTest {

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.SOMLattice#SOMLattice(int, int, int)}.
	 */
	@Test
	final void testSOMLattice() {
		SOMLattice s = new SOMLattice(Constants.FIVE, 
		                              Constants.FIVE, Constants.FIVE);
		assertEquals(Constants.FIVE, s.getNeuron(0, 0).getWeights().size());
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.SOMLattice#getNeuron(int, int)}.
	 */
	@Test
	final void testGetNeuron() {
		SOMLattice s = new SOMLattice(Constants.FIVE, 
		                              Constants.FIVE, Constants.FIVE);
		assertEquals(NeuronType.SOM, s.getNeuron(Constants.ONE, 
		                         Constants.FOUR).getType());
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.SOMLattice#getWidth()}.
	 */
	@Test
	final void testGetWidth() {
		SOMLattice s = new SOMLattice(Constants.FIVE, 
		                              Constants.FIVE, Constants.FIVE);
		assertEquals(Constants.FIVE, s.getWidth());
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.SOMLattice#getHeight()}.
	 */
	@Test
	final void testGetHeight() {
	    SOMLattice s = new SOMLattice(Constants.FIVE, 
	                                  Constants.FIVE, Constants.FIVE);
		assertEquals(Constants.FIVE, s.getHeight());
	}

	/**
	 * Test method for {@link com.neuralnetwork.shared
	 * .neurons.SOMLattice#getBMU(
	 * com.neuralnetwork.shared.neurons.SOMLayer)}.
	 */
	@Test
	final void testGetBMU() {
	    SOMLattice s = new SOMLattice(Constants.FIVE, 
	                                  Constants.FIVE, Constants.FIVE);
		SOMLayer i = new SOMLayer();
		i.add(0.0);
		i.add(0.0);
		i.add(0.0);
		i.add(0.0);
		i.add(0.0);
		assertEquals(NeuronType.SOM, s.getBMU(i).getType());
	}

}
