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
package com.neuralnetwork.shared.tests.training;

import java.util.Vector;

import com.neuralnetwork.shared.training.TrainingStack;
import com.neuralnetwork.shared.util.Pair;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Fred
 *
 */
public class TrainingStackTest {

	/**
	 * Testing constant.
	 */
	private static final int I_4 = 4;
	
	/**
	 * Testing constant.
	 */
	private static final double D_0_03 = 0.03;
	
	/**
	 * Testing constant.
	 */
	private static final double D_0_01 = 0.01;

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
		rawData.add(D_0_01);
		rawData.add(D_0_01);
		rawData.add(D_0_01);
		rawData.add(D_0_01);
		expectedData.add(D_0_03);
		expectedData.add(D_0_03);
		expectedData.add(D_0_03);
		expectedData.add(D_0_03);
		expectedData.add(D_0_03);
		TrainingStack s = new TrainingStack(I_4);
		s.addTrainingSample(rawData);
		TrainingStack s1 = new TrainingStack(1);
		s1.addTrainingSample(rawData);
		s1 = new TrainingStack(1);
		s1.addTrainingSample(null);		
		s1 = new TrainingStack(1);
		s1.addTrainingSample(null);		
		s1 = new TrainingStack(1);
		s1.addTrainingSample(rawData);		
		expectedData.remove(0);
		s1 = new TrainingStack(I_4);
		s1.addTrainingSample(rawData);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.training.TrainingStack#popSample()}.
	 */
	@Test
	public final void testPopSample() {
		Vector<Double> rawData = new Vector<Double>();
		Vector<Double> expectedData = new Vector<Double>();
		rawData.add(D_0_01);
		rawData.add(D_0_01);
		rawData.add(D_0_01);
		rawData.add(D_0_01);
		expectedData.add(D_0_03);
		expectedData.add(D_0_03);
		expectedData.add(D_0_03);
		expectedData.add(D_0_03);
		Pair<Vector<Double>, Vector<Double>> p =
				new Pair<Vector<Double>, 
				Vector<Double>>(rawData, expectedData);
		TrainingStack s = new TrainingStack(I_4);
		s.addTrainingSample(rawData);
		
		assertEquals(s.popSample(), p.getFirst());
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
		rawData.add(D_0_01);
		rawData.add(D_0_01);
		rawData.add(D_0_01);
		rawData.add(D_0_01);
		expectedData.add(D_0_03);
		expectedData.add(D_0_03);
		expectedData.add(D_0_03);
		expectedData.add(D_0_03);
		Pair<Vector<Double>, Vector<Double>> p =
				new Pair<Vector<Double>, 
				Vector<Double>>(rawData, expectedData);
		TrainingStack s = new TrainingStack(I_4);
		s.addTrainingSample(rawData);
		
		assertEquals(s.peekSample(), p.getFirst());
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.training.TrainingStack#getData()}.
	 */
	@Test
	public final void testGetData() {
		Vector<Double> rawData = new Vector<Double>();
		rawData.add(D_0_01);
		rawData.add(D_0_01);
		rawData.add(D_0_01);
		rawData.add(D_0_01);
		TrainingStack s = new TrainingStack(I_4);
		s.addTrainingSample(rawData);
		
		assertEquals(s.getData().size(), 1);
		assertEquals(s.getNumFeatures(), I_4);
	}

}
