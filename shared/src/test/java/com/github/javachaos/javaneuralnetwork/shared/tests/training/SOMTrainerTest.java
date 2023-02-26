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
package com.github.javachaos.javaneuralnetwork.shared.tests.training;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import com.github.javachaos.javaneuralnetwork.shared.neurons.SOMLatticeImpl;
import com.github.javachaos.javaneuralnetwork.shared.neurons.SOMLayerImpl;
import com.github.javachaos.javaneuralnetwork.shared.training.SOMTrainer;

class SOMTrainerTest {

	private static final double LEARN_RATE = 0.04;
	private static final int ITERATIONS = 100;
	private static final int INPUT_SIZE = 10;
    private static final long SEED = 1234L;
	private static final long TEST_TIMEOUT = 2000;
	private static List<SOMLayerImpl> inData;
	private static SOMLatticeImpl lattice;

    @BeforeAll
    static void setUp() {
		Random r = new Random(SEED);
        inData = new ArrayList<>(INPUT_SIZE);
		SOMLayerImpl input;
        
        for (int i = 0; i < INPUT_SIZE; i++) {
        	input = new SOMLayerImpl();
        	for (int j = 0; j < INPUT_SIZE; j++) {
        		input.add(r.nextDouble());
        	}
        	inData.add(input);
        }
        lattice = new SOMLatticeImpl(
        		INPUT_SIZE, INPUT_SIZE, INPUT_SIZE);
    }

	@Test
	final void testSOMTrainer() {
		SOMTrainer s = new SOMTrainer(LEARN_RATE, ITERATIONS);
		assertNotNull(s);
	}

	@Test
	final void testSetTraining() {
		SOMTrainer s = new SOMTrainer(LEARN_RATE, ITERATIONS);
		s.setTraining(lattice, inData);
		assertEquals(lattice, s.getLattice());
	}

	@Test
	final void testStart() {
		SOMTrainer s = new SOMTrainer(LEARN_RATE, ITERATIONS);
		s.setTraining(lattice, inData);
		s.start();
		Awaitility.await().atMost(15, TimeUnit.SECONDS).until(done(s));
		assertNotNull(s);
	}

	@Test
	final void testGetLattice() {
		SOMTrainer s = new SOMTrainer(LEARN_RATE, ITERATIONS);
		s.setTraining(lattice, inData);
		assertEquals(lattice, s.getLattice());
	}

	@Test
	@Timeout(value = TEST_TIMEOUT, unit = TimeUnit.MILLISECONDS)
	final void testIsRunning() {	
		SOMTrainer s = new SOMTrainer(LEARN_RATE, ITERATIONS);
		s.setTraining(lattice, inData);
		s.start();
		Awaitility.await().atMost(15, TimeUnit.SECONDS).until(done(s));
		assertNotNull(s);
		
		SOMTrainer s1 = new SOMTrainer(LEARN_RATE, ITERATIONS);
		s1.setTraining(null, inData);
		s1.start();

		Awaitility.await().atMost(15, TimeUnit.SECONDS).until(done(s));
		assertNotNull(s1);
		
	}

	@Test
	@Timeout(value = TEST_TIMEOUT, unit = TimeUnit.MILLISECONDS)
	final void testStop() {
		SOMTrainer s = new SOMTrainer(LEARN_RATE, ITERATIONS);
		s.setTraining(lattice, inData);
		s.start();
		Awaitility.await().atMost(15, TimeUnit.SECONDS).until(done(s));
		assertNotNull(s);
		
		SOMTrainer s1 = new SOMTrainer(LEARN_RATE, ITERATIONS);
		s1.setTraining(lattice, inData);
		s1.start();
		s1.stop();
		s1.getThread().interrupt();
		assertNotNull(s1);
		
		SOMTrainer s2 = new SOMTrainer(LEARN_RATE, ITERATIONS);
		s2.setTraining(lattice, inData);
		s2.stop();
		Thread.currentThread().interrupt();
		assertNotNull(s2);
	}
	
	private Callable<Boolean> done(final SOMTrainer s) {
		return () -> !s.isRunning();
	}

}
