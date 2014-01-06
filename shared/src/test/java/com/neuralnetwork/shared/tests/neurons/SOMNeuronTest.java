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

package com.neuralnetwork.shared.tests.neurons;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.neuralnetwork.shared.neurons.NeuronType;
import com.neuralnetwork.shared.neurons.SOMLayer;
import com.neuralnetwork.shared.neurons.SOMNeuron;
import com.neuralnetwork.shared.values.Constants;

/**
 * @author Fred
 *
 */
public class SOMNeuronTest {

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.SOMNeuron
	 * #SOMNeuron(int)}.
	 */
	@Test
	public final void testSOMNeuronInt() {
		SOMNeuron s = new SOMNeuron(1);
		assertEquals(s.getWeights().size(), 1);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.SOMNeuron
	 * #SOMNeuron(int, int, int)}.
	 */
	@Test
	public final void testSOMNeuronIntIntInt() {
		SOMNeuron s = new SOMNeuron(1, 2, 2);
		assertEquals(s.getWeights().size(), 1);
		assertEquals(s.getX(), 2);
		assertEquals(s.getY(), 2);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.SOMNeuron#setX(int)}.
	 */
	@Test
	public final void testSetX() {
		SOMNeuron s = new SOMNeuron(1);
		assertEquals(s.getWeights().size(), 1);
		s.setX(2);
		assertEquals(s.getX(), 2);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.SOMNeuron#setY(int)}.
	 */
	@Test
	public final void testSetY() {
		SOMNeuron s = new SOMNeuron(1);
		assertEquals(s.getWeights().size(), 1);
		s.setY(2);
		assertEquals(s.getY(), 2);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.SOMNeuron#getX()}.
	 */
	@Test
	public final void testGetX() {
		SOMNeuron s = new SOMNeuron(1, 2, 2);
		assertEquals(s.getWeights().size(), 1);
		assertEquals(s.getX(), 2);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.SOMNeuron#getY()}.
	 */
	@Test
	public final void testGetY() {
		SOMNeuron s = new SOMNeuron(1, 2, 2);
		assertEquals(s.getWeights().size(), 1);
		assertEquals(s.getY(), 2);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.SOMNeuron#distanceTo(
	 * com.neuralnetwork.shared.neurons.SOMNeuron)}.
	 */
	@Test
	public final void testDistanceTo() {
		SOMNeuron s = new SOMNeuron(Constants.THREE, 
		                            Constants.FIVE, Constants.TWO);
		SOMNeuron s1 = new SOMNeuron(3, 2, 2);
		
		assertEquals(90.0, s.distanceTo(s1), 10 * Math.ulp(90));
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.SOMNeuron#setWeight(int, double)}.
	 */
	@Test
	public final void testSetWeight() {
	    SOMNeuron s = new SOMNeuron(Constants.THREE, 
	                                Constants.FIVE, Constants.TWO);
		s.setWeight(0, 0.1);
		s.setWeight(1, 0.2);
		s.setWeight(2, 0.3);
		
		assertEquals(0.1, s.getWeight(0), 10 * Math.ulp(0.1));
		assertEquals(0.2, s.getWeight(1), 10 * Math.ulp(0.2));
		assertEquals(0.3, s.getWeight(2), 10 * Math.ulp(0.3));
		
		try {
			s.setWeight(4, 0.0);
		} catch (IndexOutOfBoundsException e) {
			assertEquals(e.getMessage(), 
					"Weight index was out of bounds.");
		}
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.SOMNeuron#getWeight(int)}.
	 */
	@Test
	public final void testGetWeight() {
	    SOMNeuron s = new SOMNeuron(Constants.THREE, 
	                                Constants.FIVE, Constants.TWO);
		s.setWeight(0, 0.1);
		s.setWeight(1, 0.2);
		s.setWeight(2, 0.3);
		
		assertEquals(s.getWeight(0), 0.1, 10 * Math.ulp(0.1));
		assertEquals(s.getWeight(1), 0.2, 10 * Math.ulp(0.2));
		assertEquals(s.getWeight(2), 0.3, 10 * Math.ulp(0.3));
		assertEquals(s.getWeight(3), 0.1, 10 * Math.ulp(0.1));
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.SOMNeuron#getWeights()}.
	 */
	@Test
	public final void testGetWeights() {
	    SOMNeuron s = new SOMNeuron(Constants.THREE, 
	                                Constants.FIVE, Constants.TWO);
		s.setWeight(0, 0.1);
		s.setWeight(1, 0.2);
		s.setWeight(2, 0.3);
		
		SOMLayer l = s.getWeights();
		
		assertEquals(l.get(0), 0.1, 10 * Math.ulp(0.1));
		assertEquals(l.get(1), 0.2, 10 * Math.ulp(0.2));
		assertEquals(l.get(2), 0.3, 10 * Math.ulp(0.3));
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.SOMNeuron#updateWeights(
	 * com.neuralnetwork.shared.neurons.SOMLayer, double, double)}.
	 */
	@Test
	public final void testUpdateWeights() {
	    SOMNeuron s = new SOMNeuron(Constants.THREE, 
	                                Constants.FIVE, Constants.TWO);
		s.setWeight(0, 0.1);
		s.setWeight(1, 0.2);
		s.setWeight(2, 0.3);
		SOMLayer l = s.getWeights();
		SOMNeuron s1 = new SOMNeuron(3, 2, 2);
		s1.setWeight(0, 0.0);
		s1.setWeight(1, 0.0);
		s1.setWeight(2, 0.0);
		
		s.updateWeights(s1.getWeights(), 0.001, 0.00001);
		
		assertEquals(s.getWeights(), l);
		
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.SOMNeuron#getNeuralnetwork()}.
	 */
	@Test
	public final void testGetNeuralnetwork() {
	    SOMNeuron s = new SOMNeuron(Constants.THREE, 
	                                Constants.FIVE, Constants.TWO);
		assertNotNull(s.getNeuralnetwork());
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.SOMNeuron#getType()}.
	 */
	@Test
	public final void testGetType() {
	    SOMNeuron s = new SOMNeuron(Constants.THREE, 
	                                Constants.FIVE, Constants.TWO);
		assertEquals(NeuronType.SOM, s.getType());
	}

}
