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
package com.neuralnetwork.shared.tests.training;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Vector;

import org.junit.Test;

import com.neuralnetwork.shared.training.TrainingStack;
import com.neuralnetwork.shared.util.Pair;

/**
 * @author Fred
 *
 */
public class TrainingStackTest {

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.training.TrainingStack
	 * #TrainingStack()}.
	 */
	@Test
	public final void testTrainingStack() {
		TrainingStack s = new TrainingStack(1);
		assertNotNull(s);
		assertNotNull(s.getData());
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.training.TrainingStack#addTrainingSample(
	 * java.util.Vector, java.util.Vector)}.
	 */
	@Test
	public final void testAddTrainingSample() {
		Vector<Double> rawData = new Vector<Double>();
		Vector<Double> expectedData = new Vector<Double>();
		rawData.add(0.01);
		rawData.add(0.01);
		rawData.add(0.01);
		rawData.add(0.01);
		expectedData.add(0.03);
		expectedData.add(0.03);
		expectedData.add(0.03);
		expectedData.add(0.03);
		expectedData.add(0.03);
		TrainingStack s = new TrainingStack(4);
		s.addTrainingSample(rawData, expectedData);
		TrainingStack s1 = new TrainingStack(1);
		s1.addTrainingSample(rawData, expectedData);
		s1 = new TrainingStack(1);
		s1.addTrainingSample(null, expectedData);		
		s1 = new TrainingStack(1);
		s1.addTrainingSample(null, null);		
		s1 = new TrainingStack(1);
		s1.addTrainingSample(rawData, null);		
		expectedData.remove(0);
		s1 = new TrainingStack(4);
		s1.addTrainingSample(rawData, expectedData);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.training.TrainingStack#popSample()}.
	 */
	@Test
	public final void testPopSample() {
		Vector<Double> rawData = new Vector<Double>();
		Vector<Double> expectedData = new Vector<Double>();
		rawData.add(0.01);
		rawData.add(0.01);
		rawData.add(0.01);
		rawData.add(0.01);
		expectedData.add(0.03);
		expectedData.add(0.03);
		expectedData.add(0.03);
		expectedData.add(0.03);
		Pair<Vector<Double>, Vector<Double>> p =
				new Pair<Vector<Double>, 
				Vector<Double>>(rawData, expectedData);
		TrainingStack s = new TrainingStack(4);
		s.addTrainingSample(rawData, expectedData);
		
		assertEquals(s.popSample(), p);
		assertTrue(s.getData().isEmpty());
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.training.TrainingStack#peekSample()}.
	 */
	@Test
	public final void testPeekSample() {
		Vector<Double> rawData = new Vector<Double>();
		Vector<Double> expectedData = new Vector<Double>();
		rawData.add(0.01);
		rawData.add(0.01);
		rawData.add(0.01);
		rawData.add(0.01);
		expectedData.add(0.03);
		expectedData.add(0.03);
		expectedData.add(0.03);
		expectedData.add(0.03);
		Pair<Vector<Double>, Vector<Double>> p =
				new Pair<Vector<Double>, 
				Vector<Double>>(rawData, expectedData);
		TrainingStack s = new TrainingStack(4);
		s.addTrainingSample(rawData, expectedData);
		
		assertEquals(s.peekSample(), p);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.training.TrainingStack#getData()}.
	 */
	@Test
	public final void testGetData() {
		Vector<Double> rawData = new Vector<Double>();
		Vector<Double> expectedData = new Vector<Double>();
		rawData.add(0.01);
		rawData.add(0.01);
		rawData.add(0.01);
		rawData.add(0.01);
		expectedData.add(0.03);
		expectedData.add(0.03);
		expectedData.add(0.03);
		expectedData.add(0.03);
		TrainingStack s = new TrainingStack(4);
		s.addTrainingSample(rawData, expectedData);
		
		assertEquals(s.getData().size(), 1);
		assertEquals(s.getSampleSize(), 4);
	}

}
