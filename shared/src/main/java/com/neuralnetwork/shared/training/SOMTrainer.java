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
package com.neuralnetwork.shared.training;

import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neuralnetwork.shared.neurons.SOMLattice;
import com.neuralnetwork.shared.neurons.SOMLayer;
import com.neuralnetwork.shared.neurons.SOMNeuron;

/**
 * SOM Trainer responsible for training a SOM
 * neural network.
 * 
 * @author fredladeroute
 *
 */
public class SOMTrainer implements Runnable {
    
    /**
     * Logger instance.
     */
    private static final Logger LOGGER = 
            LoggerFactory.getLogger(SOMTrainer.class);
    
    /**
     * Initial learning rate.
     */
	private double initialLearningRate;
	
	/**
	 * The number of iterations to perform.
	 */
	private int	numIterations;
	
	/**
	 * The radius of the lattice.
	 */
	private double latticeRadius;
	
	/**
	 * Time constant.
	 */
	private double timeConstant;
	
	/**
	 * The lattice object.
	 */
	private SOMLattice lattice;
	
	/**
	 * Input vector.
	 */
	private Vector<SOMLayer> inputs;
	
	/**
	 * Running flag.
	 */
	private boolean running = false;
	
	/**
	 * Execution thread to run the training on.
	 */
	private Thread runner = null;
	
	/**
	 * Create a new SOMTrainer object.
	 * 
	 * @param learnRate
	 *         defines the initial learning rate (expected [0,1])
	 *         
	 * @param iterations
	 *         the number of iterations to train for
	 */
	public SOMTrainer(final double learnRate, final int iterations) {
	    this.initialLearningRate = learnRate;
	    this.numIterations = iterations;
	}
	
	/**
	 * Get the neighborhood radius at the iteration, iter.
	 * 
	 * @param iter
	 *         the current iteration number
	 *         
	 * @return
	 *         the neighborhood radius
	 */
	private double getNeighborhoodRadius(final double iter) {
		return latticeRadius * Math.exp(-iter / timeConstant);
	}
	
	/**
	 * Return the distance fall off value.
	 * 
	 * @param squaredDist
	 *         the distance squared
	 *         
	 * @param r
	 *         the radius
	 *         
	 * @return
	 *     the distance fall off value
	 */
	private double getDistanceFalloff(final double squaredDist, 
			final double r) {
		double radiusSq = r * r;
		return Math.exp(-(squaredDist) / (2 * radiusSq));
	}
		
	/**
	 * Train the lattice latToTrain on the training sample in.
	 * 
	 * @param trainLattice
	 *         the lattice to train
	 *             
	 * @param in
	 *         the input vector to the lattice
	 */
	public final void setTraining(
	        final SOMLattice trainLattice, final Vector<SOMLayer> in) {
		lattice = trainLattice;
		inputs = in;
	}
	
	/**
	 * Start training of the network.
	 */
	public final void start() {
		if (lattice != null) {
			runner = new Thread(this);
			runner.setPriority(Thread.MIN_PRIORITY);
			running = true;
			runner.start();
		}
	}
	
	@Override
    public final void run() {

        LOGGER.debug("Training started.");
		int latticeWidth = lattice.getWidth();
		int latticeHeight = lattice.getHeight();
		int xstart, ystart, xend, yend;
		double dist, dFalloff;
		// These two values are used in the training algorithm
		latticeRadius = Math.max(latticeWidth, latticeHeight) / 2;
		timeConstant = numIterations / Math.log(latticeRadius);
		
		int iteration = 0;
		double nbhRadius;
		SOMNeuron bmu = null, temp = null;
		SOMLayer curInput = null;
		double learningRate = initialLearningRate;
		
		while (iteration < numIterations && running) {

	        LOGGER.debug("Training, iteration: " + iteration);
			nbhRadius = getNeighborhoodRadius(iteration);
			// For each of the input vectors, look for the best matching
			// unit, then adjust the weights for the BMU's neighborhood
			for (int i = 0; i < inputs.size(); i++) {
				curInput = (SOMLayer) inputs.elementAt(i);
				bmu = lattice.getBMU(curInput);
				// We have the BMU for this input now, so adjust everything in
				// it's neighborhood
				
				// Optimization:  Only go through the X/Y values that 
				// fall within
				// the radius
				xstart = (int) (bmu.getX() - nbhRadius - 1);
				ystart = (int) (bmu.getY() - nbhRadius - 1);
				xend = (int) (xstart + (nbhRadius * 2) + 1);
				yend = (int) (ystart + (nbhRadius * 2) + 1);
				if (xend > latticeWidth) {
                    xend = latticeWidth;
                }
				if (xstart < 0) {
                    xstart = 0;
                }
				if (yend > latticeHeight) {
                    yend = latticeHeight;
                }
				if (ystart < 0) {
                    ystart = 0;
                }
				
				for (int x = xstart; x < xend; x++) {
					for (int y = ystart; y < yend; y++) {
						temp = lattice.getNeuron(x, y);
							dist = bmu.distanceTo(temp);
							if (dist <= (nbhRadius * nbhRadius)) {
								dFalloff = getDistanceFalloff(dist, nbhRadius);
								temp.updateWeights(curInput, 
								                   learningRate, dFalloff);
							}
					}
				}
			}
			iteration++;
			learningRate = initialLearningRate 
			        * Math.exp(-(double) iteration / numIterations);
		}
		running = false;
	}
	
	/**
	 * Get the trained lattice.
	 * @return
	 * 		the trained SOM Lattice.
	 */
	public final SOMLattice getLattice() {
		return lattice;
	}
	
	/**
	 * Returns true if the trainer is running.
	 * @return
	 * 		true if the trainer is running.
	 */
	public final boolean isRunning() {
		return running;
	}
	
	/**
	 * Return the run thread.
	 * @return
	 * 		a reference to the run thread.
	 */
	public final Thread getThread() {
		return runner;
	}

	/**
	 * Stop training.
	 */
	public final void stop() {
		if (runner != null) {
			running = false;
		}
	}
}
