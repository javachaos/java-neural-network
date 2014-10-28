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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Random;
import java.util.Vector;

import org.junit.BeforeClass;
import org.junit.Test;

import com.neuralnetwork.shared.neurons.SOMLattice;
import com.neuralnetwork.shared.neurons.SOMLayer;
import com.neuralnetwork.shared.training.SOMTrainer;

/**
 * @author Fred
 *
 */

public class SOMTrainerTest {

	/**
	 * Learning rate for SOMTrainer.
	 */
	private static final double LEARN_RATE = 0.04;
	
	/**
	 * Number of training iterations to perform.
	 */
	private static final int ITERATIONS = 100;
	
	/**
	 * Size of the input vector.
	 */
	private static final int INPUT_SIZE = 10;
	
    /**
     * Seed value for the random number generator.
     */
    private static final long SEED = 1234L;

    /**
     * Thread sleep time.
     */
	private static final long SLEEP_TIME = 100;

	/**
	 * Testing timeout.
	 */
	private static final long TEST_TIMEOUT = 2000;

    /**
     * Data used for input.
     */
	private static Vector<SOMLayer> inData;

	/**
	 * The input SOM Layer for training.
	 */
	private static SOMLayer input;

	/**
	 * The random number generator.
	 */
	private static Random r;

	/**
	 * The SOM lattice.
	 */
	private static SOMLattice lattice;
	
	/**
	 * Setup SOM Test.
	 */
    @BeforeClass
    public static void setUp() {
		r = new Random(SEED);
        inData = new Vector<SOMLayer>(INPUT_SIZE);
        input = new SOMLayer();
        
        for (int i = 0; i < INPUT_SIZE; i++) {
        	input = new SOMLayer();
        	for (int j = 0; j < INPUT_SIZE; j++) {
        		input.add(r.nextDouble());
        	}
        	inData.add(input);
        }
        lattice = new SOMLattice(
        		INPUT_SIZE, INPUT_SIZE, INPUT_SIZE);
    }
    
	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.training.SOMTrainer
	 * #SOMTrainer(double, int)}.
	 */
	@Test
	public final void testSOMTrainer() {
		SOMTrainer s = new SOMTrainer(LEARN_RATE, ITERATIONS);
		assertNotNull(s);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.training.SOMTrainer#setTraining(
	 * com.neuralnetwork.shared.neurons.SOMLattice, java.util.Vector)}.
	 */
	@Test
	public final void testSetTraining() {
		SOMTrainer s = new SOMTrainer(LEARN_RATE, ITERATIONS);
		s.setTraining(lattice, inData);
		assertEquals(lattice, s.getLattice());
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.training.SOMTrainer#start()}.
	 */
	@Test
	public final void testStart() {
		SOMTrainer s = new SOMTrainer(LEARN_RATE, ITERATIONS);
		s.setTraining(lattice, inData);
		s.start();
		while (s.isRunning()) {
			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		assertNotNull(s);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.training.SOMTrainer#getLattice()}.
	 */
	@Test
	public final void testGetLattice() {
		SOMTrainer s = new SOMTrainer(LEARN_RATE, ITERATIONS);
		s.setTraining(lattice, inData);
		assertEquals(lattice, s.getLattice());
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.training.SOMTrainer#isRunning()}.
	 */
	@Test(timeout = TEST_TIMEOUT)
	public final void testIsRunning() {	
		SOMTrainer s = new SOMTrainer(LEARN_RATE, ITERATIONS);
		s.setTraining(lattice, inData);
		s.start();
		while (s.isRunning()) {
			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		assertNotNull(s);
		
		SOMTrainer s1 = new SOMTrainer(LEARN_RATE, ITERATIONS);
		s1.setTraining(null, inData);
		s1.start();
		while (s1.isRunning()) {
			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		assertNotNull(s1);
		
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.training.SOMTrainer#stop()}.
	 */
	@Test(timeout = TEST_TIMEOUT)
	public final void testStop() {
		SOMTrainer s = new SOMTrainer(LEARN_RATE, ITERATIONS);
		s.setTraining(lattice, inData);
		s.start();		
		while (s.isRunning()) {
			try {
				Thread.sleep(SLEEP_TIME);
				s.stop();
				s.getThread().interrupt();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
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
		assertNotNull(s1);
	}

}
