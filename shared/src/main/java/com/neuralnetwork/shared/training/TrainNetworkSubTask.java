package com.neuralnetwork.shared.training;

import java.util.Vector;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neuralnetwork.shared.network.INetwork;
import com.neuralnetwork.shared.values.ErrorValue;

/**
 * Mini-task to compute neuron MSE.
 * 
 * @author alfred
 */
public final class TrainNetworkSubTask implements Callable<Double> {

    /**
     * Logger instance.
     */
    public static final Logger LOGGER = 
            LoggerFactory.getLogger(TrainNetworkSubTask.class);
    
	/**
	 * Training vector.
	 */
	private Vector<Double> trainVector;

	/**
	 * Neuron to do computation on.
	 */
	private INetwork network;

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
			final Vector<Double> trainingVector) {
		this.network = n;
		this.trainVector = trainingVector;
	}

	@Override
	public Double call() throws Exception {
		Double d = network.train(false,
				trainVector, ErrorValue.E_0_00001).getValue();
		LOGGER.debug(network.toString());
		LOGGER.info("Mean squared error: " + d);
		return d;
	}

}
