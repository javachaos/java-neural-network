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
package com.neuralnetwork.shared.tests.training;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import com.neuralnetwork.shared.training.TrainingStack;
import com.neuralnetwork.shared.util.Pair;

class TrainingStackTest {

	private static final int I_4 = 4;
	private static final double D_0_03 = 0.03;
	private static final double D_0_01 = 0.01;

	@Test
	final void testTrainingStack() {
		TrainingStack s = new TrainingStack(1);
		assertNotNull(s);
		assertNotNull(s.getData());
	}

	@Test
	final void testAddTrainingSample() {
		ArrayList<Double> rawData = new ArrayList<>();
		rawData.add(D_0_01);
		rawData.add(D_0_01);
		rawData.add(D_0_01);
		rawData.add(D_0_01);
		TrainingStack s = new TrainingStack(I_4);
		assertNotNull(s);
		s.addTrainingSample(rawData);
		TrainingStack s1 = new TrainingStack(1);
		assertNotNull(s1);
		s1.addTrainingSample(rawData);
		s1 = new TrainingStack(1);
		assertNotNull(s1);
		s1.addTrainingSample(null);		
		s1 = new TrainingStack(1);
		assertNotNull(s1);
		s1.addTrainingSample(null);		
		s1 = new TrainingStack(1);
		assertNotNull(s1);
		s1.addTrainingSample(rawData);
		s1 = new TrainingStack(I_4);
		assertNotNull(s1);
		s1.addTrainingSample(rawData);
	}

	@Test
	final void testPopSample() {
		ArrayList<Double> rawData = new ArrayList<>();
		ArrayList<Double> expectedData = new ArrayList<>();
		rawData.add(D_0_01);
		rawData.add(D_0_01);
		rawData.add(D_0_01);
		rawData.add(D_0_01);
		expectedData.add(D_0_03);
		expectedData.add(D_0_03);
		expectedData.add(D_0_03);
		expectedData.add(D_0_03);
		Pair<ArrayList<Double>, ArrayList<Double>> p =
				new Pair<>(rawData, expectedData);
		TrainingStack s = new TrainingStack(I_4);
		s.addTrainingSample(rawData);
		
		assertEquals(s.popSample(), p.getFirst());
		assertTrue(s.getData().isEmpty());
	}

	@Test
	final void testPeekSample() {
		ArrayList<Double> rawData = new ArrayList<>();
		ArrayList<Double> expectedData = new ArrayList<>();
		rawData.add(D_0_01);
		rawData.add(D_0_01);
		rawData.add(D_0_01);
		rawData.add(D_0_01);
		expectedData.add(D_0_03);
		expectedData.add(D_0_03);
		expectedData.add(D_0_03);
		expectedData.add(D_0_03);
		Pair<ArrayList<Double>, ArrayList<Double>> p =
				new Pair<>(rawData, expectedData);
		TrainingStack s = new TrainingStack(I_4);
		s.addTrainingSample(rawData);
		
		assertEquals(s.peekSample(), p.getFirst());
	}

	@Test
	final void testGetData() {
		ArrayList<Double> rawData = new ArrayList<>();
		rawData.add(D_0_01);
		rawData.add(D_0_01);
		rawData.add(D_0_01);
		rawData.add(D_0_01);
		TrainingStack s = new TrainingStack(I_4);
		s.addTrainingSample(rawData);
		
		assertEquals(1, s.getData().size());
		assertEquals(I_4, s.getNumFeatures());
	}

}
