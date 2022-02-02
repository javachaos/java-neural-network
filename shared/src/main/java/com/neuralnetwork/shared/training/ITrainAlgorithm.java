package com.neuralnetwork.shared.training;


/**
 * Interface for a training algorithm.
 * @author alfred
 *
 */
public interface ITrainAlgorithm {
	
	/**
	 * Return current progress.
	 * 
	 * @return
	 * 		An integer between 0-100 with 100 
	 *      being complete and 0 being incomplete.
	 */
	int getProgress();
	
	/**
	 * Return a snapshot of the current Mean Squared Error value
	 * for the network under training.
	 * 
	 * @return
	 * 		the error value.
	 */
	Double getErrorStatus();

	/**
	 * Compute a value using this algorithm
	 * @return the computed value of this algorithm
	 */
    Double compute();
}
