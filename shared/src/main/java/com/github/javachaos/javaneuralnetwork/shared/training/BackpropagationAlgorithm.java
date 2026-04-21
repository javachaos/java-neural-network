package com.github.javachaos.javaneuralnetwork.shared.training;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

import com.github.javachaos.javaneuralnetwork.shared.network.Network;
import com.github.javachaos.javaneuralnetwork.shared.neurons.OutputNeuron;
import com.github.javachaos.javaneuralnetwork.shared.util.ErrorFunctions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Backpropagation algorithm implementation.
 */
public final class BackpropagationAlgorithm implements ITrainAlgorithm {
    /**
     * Logger instance.
     */
    private static final Logger LOGGER =
    		LogManager.getLogger(BackpropagationAlgorithm.class);
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
		List<Double> expectedOutput = trainSample.getOutputs();
		if (output.size() != expectedOutput.size()) {
			throw new IllegalArgumentException(
					"Output vector does not match expected output dimension.");
		}
		LOGGER.debug("Network output: {}", output);
		currError =	ErrorFunctions.getInstance().meanSquaredError(
						output, expectedOutput);
		Iterator<OutputNeuron> iterator = network.getOutputLayer().iterator();
		LinkedBlockingDeque<Double> outputs = new LinkedBlockingDeque<>(output);
		LinkedBlockingDeque<Double> expectedOutputs =
				new LinkedBlockingDeque<>(expectedOutput);
		//Skip bias neuron.
		iterator.next();
		while (iterator.hasNext()) {
			iterator.next().propagateError(outputs.pop() - expectedOutputs.pop());
		}
		return currError;
	}

}
