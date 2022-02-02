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

import com.neuralnetwork.shared.neurons.NeuronType;
import com.neuralnetwork.shared.neurons.SOMLayer;
import com.neuralnetwork.shared.neurons.SOMNeuron;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Fred
 *
 */
class SOMNeuronTest {

	/**
	 * Expected distance value.
	 */
	private static final int DISTANCE_EXPECTED = 9;

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.SOMNeuron
	 * #SOMNeuron(int)}.
	 */
	@Test
	final void testSOMNeuronInt() {
		SOMNeuron s = new SOMNeuron(1);
		assertEquals(1, s.getWeights().size());
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.SOMNeuron
	 * #SOMNeuron(int, int, int)}.
	 */
	@Test
	final void testSOMNeuronIntIntInt() {
		SOMNeuron s = new SOMNeuron(1, 2, 2);
		assertEquals(1, s.getWeights().size());
		assertEquals(2, s.getX());
		assertEquals(2, s.getY());
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.SOMNeuron#setX(int)}.
	 */
	@Test
	final void testSetX() {
		SOMNeuron s = new SOMNeuron(1);
		assertEquals(1, s.getWeights().size());
		s.setX(2);
		assertEquals(2, s.getX());
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.SOMNeuron#setY(int)}.
	 */
	@Test
	final void testSetY() {
		SOMNeuron s = new SOMNeuron(1);
		assertEquals(1, s.getWeights().size());
		s.setY(2);
		assertEquals(2, s.getY());
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.SOMNeuron#getX()}.
	 */
	@Test
	final void testGetX() {
		SOMNeuron s = new SOMNeuron(1, 2, 2);
		assertEquals(1, s.getWeights().size());
		assertEquals(2, s.getX());
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.SOMNeuron#getY()}.
	 */
	@Test
	final void testGetY() {
		SOMNeuron s = new SOMNeuron(1, 2, 2);
		assertEquals(1, s.getWeights().size());
		assertEquals(2, s.getY());
	}

	@Test
	final void testDistanceTo() {
		SOMNeuron s = new SOMNeuron(3,  5, 2);
		SOMNeuron s1 = new SOMNeuron(3, 2, 2);
		// (5, 2) to (2, 2) distance should be (5 - 2)^2 + (2 - 2)^2 = 9
		assertEquals(DISTANCE_EXPECTED, s.distanceTo(s1),
				10 * Math.ulp(DISTANCE_EXPECTED));
		s = new SOMNeuron(3, 500, 900);
		s1 = new SOMNeuron(3, 1000, 3221);
		// (500, 900) to (1000, 3221) distance should be (500 - 900)^2 + (1000 - 3221)^2 = 5637041.0
		assertEquals(5637041.0, s.distanceTo(s1),
				10 * Math.ulp(5637041.0));
	}

	@Test
	final void testSetWeight() {
	    SOMNeuron s = new SOMNeuron(3, 
	                                5, 2);
	    double w1 = 1.0 / 10.0;
	    double w2 = 2.0 / 10.0;
	    double w3 = 3.0 / 10.0;
		s.setWeight(0, w1);
		s.setWeight(1, w2);
		s.setWeight(2, w3);
		
		assertEquals(w1, s.getWeight(0), 10 * Math.ulp(w1));
		assertEquals(w2, s.getWeight(1), 10 * Math.ulp(w2));
		assertEquals(w3, s.getWeight(2), 10 * Math.ulp(w3));
		
		try {
			s.setWeight(4, 0.0);
		} catch (IndexOutOfBoundsException e) {
			assertEquals("Weight index was out of bounds.", 
					e.getMessage());
		}
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.SOMNeuron#getWeight(int)}.
	 */
	@Test
	final void testGetWeight() {
	    SOMNeuron s = new SOMNeuron(3, 
	                                5, 2);
	    
	    double w1 = 1.0 / 10.0;
	    double w2 = 2.0 / 10.0;
	    double w3 = 3.0 / 10.0;
	    
		s.setWeight(0, w1);
		s.setWeight(1, w2);
		s.setWeight(2, w3);
		
		assertEquals(s.getWeight(0), w1, 10 * Math.ulp(w1));
		assertEquals(s.getWeight(1), w2, 10 * Math.ulp(w2));
		assertEquals(s.getWeight(2), w3, 10 * Math.ulp(w3));
		assertEquals(s.getWeight(3), w1,
				10 * Math.ulp(w1));
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.SOMNeuron#getWeights()}.
	 */
	@Test
	final void testGetWeights() {
	    SOMNeuron s = new SOMNeuron(3, 
	                                5, 2);
	    double w1 = 1.0 / 10.0;
	    double w2 = 2.0 / 10.0;
	    double w3 = 3.0 / 10.0;
		s.setWeight(0, w1);
		s.setWeight(1, w2);
		s.setWeight(2, w3);
		
		SOMLayer l = s.getWeights();
		
		assertEquals(l.get(0), w1, 10 * Math.ulp(w1));
		assertEquals(l.get(1), w2, 10 * Math.ulp(w2));
		assertEquals(l.get(2), w3, 10 * Math.ulp(w3));
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.SOMNeuron#updateWeights(
	 * com.neuralnetwork.shared.neurons.SOMLayer, double, double)}.
	 */
	@Test
	final void testUpdateWeights() {
	    SOMNeuron s = new SOMNeuron(3, 
	                                5, 2);
	    double w1 = 1.0 / 10.0;
	    double w2 = 2.0 / 10.0;
	    double w3 = 3.0 / 10.0;
		s.setWeight(0, w1);
		s.setWeight(1, w2);
		s.setWeight(2, w3);
		SOMLayer l = s.getWeights();
		SOMNeuron s1 = new SOMNeuron(3, 2, 2);
		s1.setWeight(0, 0.0);
		s1.setWeight(1, 0.0);
		s1.setWeight(2, 0.0);
		
		s.updateWeights(s1.getWeights(), 
				1.0 / Math.pow(10.0, 3.0),
				1.0 / Math.pow(10, 5.0));
		
		assertEquals(s.getWeights(), l);
		
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.SOMNeuron#getNeuralnetwork()}.
	 */
	@Test
	final void testGetNeuralnetwork() {
	    SOMNeuron s = new SOMNeuron(3, 
	                                5, 2);
		assertNotNull(s.getNeuralnetwork());
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.SOMNeuron#getType()}.
	 */
	@Test
	final void testGetType() {
	    SOMNeuron s = new SOMNeuron(3, 
	                                5, 2);
		assertEquals(NeuronType.SOM, s.getType());
	}

}
