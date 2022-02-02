package com.neuralnetwork.shared.training;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neuralnetwork.shared.network.INetwork;
import com.neuralnetwork.shared.neurons.IInputNeuron;
import com.neuralnetwork.shared.neurons.IOutputNeuron;
import com.neuralnetwork.shared.util.ErrorFunctions;
import com.neuralnetwork.shared.util.MathTools;

/**
 * Backpropagation algorithm implementation.
 * 
 * @author alfred
 *
 */
public final class BackpropAlgorithm implements ITrainAlgorithm {
    /**
     * Logger instance.
     */
    private static final Logger LOGGER = 
    		LoggerFactory.getLogger(BackpropAlgorithm.class);
	private final INetwork network;
	private final Double expectedError;
	private Double currError;
	private final TrainSample trainSample;

	/**
	 * Create the backprop algorithm.
	 * 
	 * @param trainSample
	 * 		the training vector.
	 *
	 * @param net
	 * 		the network to run the algorithm on.
	 * 
	 * @param expErr
	 * 		desired error value.
	 * 
	 */
	public BackpropAlgorithm(final TrainSample trainSample,
			final INetwork net, final Double expErr) {
		this.trainSample = trainSample;
		this.network = net;
		this.expectedError = expErr;
	}

	@Override
	public int getProgress() {
		return 0;
	}

	@Override
	public Double getErrorStatus() {
		return expectedError - currError;
	}

	/**
	 * Compute MSE.
	 * @return
	 * 		the mean squared error of the network.
	 */
	public synchronized Double compute() {
		List<Double> output = network.runInputs(trainSample.getInputs());
		LOGGER.debug("Network output: {}", output);
		currError =	ErrorFunctions.getInstance().meanSquaredError(
						output, trainSample.getInputs());
		Iterator<IOutputNeuron> iter = network.getOutputLayer().iterator();
		LinkedBlockingDeque<Double> outputs = new LinkedBlockingDeque<>(output);
		//Skip bias neuron.
		iter.next();
		while (iter.hasNext()) {
			iter.next().propagateError(outputs.pop());
		}
		List<Double> errVector = new ArrayList<>();
		for (IInputNeuron iInputNeuron : network.getInputLayer()) {
			errVector.add(iInputNeuron.getValue());
		}
		return MathTools.sum(errVector);
	}

}
