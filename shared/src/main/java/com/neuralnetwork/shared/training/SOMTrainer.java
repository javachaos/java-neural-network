/*
 * SOMTrainer.java
 *
 * Created on December 13, 2002, 2:37 PM
 */

package com.neuralnetwork.shared.training;

import java.util.Vector;

import com.neuralnetwork.shared.nodes.SOMLattice;
import com.neuralnetwork.shared.nodes.SOMLayer;
import com.neuralnetwork.shared.nodes.SOMNeuron;

/**
 * SOM Trainer responsible for training a SOM
 * neural network.
 * 
 * @author fredladeroute
 *
 */
public class SOMTrainer implements Runnable {
    
    /**
     * Initial learning rate.
     */
	private static final double INITIAL_LEARNING_RATE = 0.07;
	
	/**
	 * The number of iterations to perform.
	 */
	private static final int	NUM_ITERATIONS = 500;
	
	/**
	 * The radius of the lattice.
	 */
	private double latticeRadius;
	
	/**
	 * Time constant.
	 */
	private double timeConstant;
	
	/**
	 * Number of milliseconds to sleep while stopping thread.
	 */
	private static final int SLEEP_INTERVAL  = 250;
	
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
	private static boolean running;
	
	/**
	 * Execution thread to run the training on.
	 */
	private Thread runner;
	
	/**
	 * Create a new SOMTrainer object.
	 */
	public SOMTrainer() {
		running = false;
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
	private double getDistanceFalloff(final double squaredDist, final double r) {
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
		int latticeWidth = lattice.getWidth();
		int latticeHeight = lattice.getHeight();
		int xstart, ystart, xend, yend;
		double dist, dFalloff;
		// These two values are used in the training algorithm
		latticeRadius = Math.max(latticeWidth, latticeHeight) / 2;
		timeConstant = NUM_ITERATIONS / Math.log(latticeRadius);
		
		int iteration = 0;
		double nbhRadius;
		SOMNeuron bmu = null, temp = null;
		SOMLayer curInput = null;
		double learningRate = INITIAL_LEARNING_RATE;
		
		while (iteration < NUM_ITERATIONS && running) {
			nbhRadius = getNeighborhoodRadius(iteration);
			// For each of the input vectors, look for the best matching
			// unit, then adjust the weights for the BMU's neighborhood
			for (int i = 0; i < inputs.size(); i++) {
				curInput = (SOMLayer) inputs.elementAt(i);
				bmu = lattice.getBMU(curInput);
				// We have the BMU for this input now, so adjust everything in
				// it's neighborhood
				
				// Optimization:  Only go through the X/Y values that fall within
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
								temp.updateWeights(curInput, learningRate, dFalloff);
							}
					}
				}
			}
			iteration++;
			learningRate = INITIAL_LEARNING_RATE 
			        * Math.exp(-(double) iteration / NUM_ITERATIONS);
		}
		running = false;
	}

	/**
	 * Stop training.
	 */
	public final void stop() {
		if (runner != null) {
			running = false;
			while (runner.isAlive()) {
			    try {
                    Thread.sleep(SLEEP_INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
			}
		}
	}
}
