/*******************************************************************************
 * Copyright (c) 2014 Fred Laderoute.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *     Fred Laderoute - initial API and implementation
 *******************************************************************************/
/**
 * 
 */
package com.neuralnetwork.shared.tests.neurons;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.neuralnetwork.shared.neurons.NeuronType;
import com.neuralnetwork.shared.neurons.SOMLattice;
import com.neuralnetwork.shared.neurons.SOMLayer;
import com.neuralnetwork.shared.values.Constants;

/**
 * @author Fred
 *
 */
public class SOMLatticeTest {

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.SOMLattice#SOMLattice(int, int, int)}.
	 */
	@Test
	public final void testSOMLattice() {
		SOMLattice s = new SOMLattice(Constants.FIVE, 
		                              Constants.FIVE, Constants.FIVE);
		assertEquals(Constants.FIVE, s.getHeight());
		assertEquals(Constants.FIVE, s.getWidth());
		assertEquals(Constants.FIVE, s.getNeuron(0, 0).getWeights().size());
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.SOMLattice#getNeuron(int, int)}.
	 */
	@Test
	public final void testGetNeuron() {
		SOMLattice s = new SOMLattice(Constants.FIVE, 
		                              Constants.FIVE, Constants.FIVE);
		assertEquals(s.getNeuron(Constants.ONE, 
		                         Constants.FOUR).getType(), NeuronType.SOM);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.SOMLattice#getWidth()}.
	 */
	@Test
	public final void testGetWidth() {
		SOMLattice s = new SOMLattice(Constants.FIVE, 
		                              Constants.FIVE, Constants.FIVE);
		assertEquals(Constants.FIVE, s.getHeight());
		assertEquals(Constants.FIVE, s.getWidth());
		assertEquals(Constants.FIVE, s.getNeuron(0, 0).getWeights().size());
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.SOMLattice#getHeight()}.
	 */
	@Test
	public final void testGetHeight() {
	    SOMLattice s = new SOMLattice(Constants.FIVE, 
	                                  Constants.FIVE, Constants.FIVE);
		assertEquals(Constants.FIVE, s.getHeight());
		assertEquals(Constants.FIVE, s.getWidth());
		assertEquals(Constants.FIVE, s.getNeuron(0, 0).getWeights().size());
	}

	/**
	 * Test method for {@link com.neuralnetwork.shared
	 * .neurons.SOMLattice#getBMU(
	 * com.neuralnetwork.shared.neurons.SOMLayer)}.
	 */
	@Test
	public final void testGetBMU() {
	    SOMLattice s = new SOMLattice(Constants.FIVE, 
	                                  Constants.FIVE, Constants.FIVE);
		SOMLayer i = new SOMLayer();
		i.add(0.0);
		i.add(0.0);
		i.add(0.0);
		i.add(0.0);
		i.add(0.0);
		assertEquals(s.getBMU(i).getType(), NeuronType.SOM);
	}

}
