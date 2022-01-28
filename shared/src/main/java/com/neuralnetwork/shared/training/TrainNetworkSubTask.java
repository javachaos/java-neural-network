package com.neuralnetwork.shared.training;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neuralnetwork.shared.network.INetwork;

/**
 * Mini-task to compute neuron MSE.
 * 
 * @author alfred
 */
public final class TrainNetworkSubTask implements Callable<Double> {
//TODO Test
    /**
     * Logger instance.
     */
    public static final Logger LOGGER = 
            LoggerFactory.getLogger(TrainNetworkSubTask.class);
    
	/**
	 * Training vector.
	 */
	private final List<Double> trainVector;

	/**
	 * Neuron to do computation on.
	 */
	private final INetwork network;

	/**
	 * Create a new neuron compute task.
	 * 
	 * @param n
	 * 		the network to do computation with.
	 * 
	 * @param trainingVector
	 * 		the training vector.
	 * 
	 */
	public TrainNetworkSubTask(final INetwork n,
			final List<Double> trainingVector) {
		this.network = n;
		this.trainVector = trainingVector;
	}

	@Override
	public Double call() throws Exception {
		Double d = network.train(false,
				trainVector, 0.00001);
		LOGGER.debug("{}", network);
		LOGGER.info("Mean squared error: {}", d);
		return d;
	}

}
