package com.neuralnetwork.shared.training;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neuralnetwork.shared.network.Network;
import com.neuralnetwork.shared.neurons.InputNeuron;
import com.neuralnetwork.shared.neurons.OutputNeuron;
import com.neuralnetwork.shared.util.ErrorFunctions;
import com.neuralnetwork.shared.util.MathTools;

/**
 * Backpropagation algorithm implementation.
 */
public final class BackpropagationAlgorithm implements ITrainAlgorithm {
    /**
     * Logger instance.
     */
    private static final Logger LOGGER = 
    		LoggerFactory.getLogger(BackpropagationAlgorithm.class);
	private final Network network;
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
	public BackpropagationAlgorithm(final TrainSample trainSample,
									final Network net, final Double expErr) {
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
		Iterator<OutputNeuron> iterator = network.getOutputLayer().iterator();
		LinkedBlockingDeque<Double> outputs = new LinkedBlockingDeque<>(output);
		//Skip bias neuron.
		iterator.next();
		while (iterator.hasNext()) {
			iterator.next().propagateError(outputs.pop());
		}
		List<Double> errVector = new ArrayList<>();
		for (InputNeuron iInputNeuron : network.getInputLayer()) {
			errVector.add(iInputNeuron.getValue());
		}
		return MathTools.sum(errVector);
	}

}
