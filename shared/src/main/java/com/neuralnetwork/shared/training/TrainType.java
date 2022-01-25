package com.neuralnetwork.shared.training;

/**
 * Defines the training algorithm used to train the neural network.
 * @author alfred
 *
 */
public enum TrainType {
	
	/**
	 * Backpropagation training algorithm.
	 */
	BACKPROP,
	
	/**
	 * RPROP Neural network training algorithm.
	 */
	RPROP,
	
	/**
	 * Quick RPROP training algortihm.
	 */
	QPROP

}
