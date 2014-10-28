package com.neuralnetwork.shared.training;

import java.util.Iterator;
import java.util.Stack;
import java.util.Vector;

import com.neuralnetwork.shared.network.INetwork;
import com.neuralnetwork.shared.neurons.IInputNeuron;
import com.neuralnetwork.shared.neurons.IOutputNeuron;
import com.neuralnetwork.shared.util.ErrorFunctions;
import com.neuralnetwork.shared.util.MathTools;
import com.neuralnetwork.shared.values.ErrorValue;

/**
 * Backpropagation algorithm implementation.
 * 
 * @author alfred
 *
 */
public final class BackpropAlgorithm implements ITrainAlgorithm {

	/**
	 * Network to be trained.
	 */
	private INetwork network;
	
	/**
	 * Expected error value.
	 */
	private ErrorValue expectedError;
	
	/**
	 * Current error value.
	 */
	private ErrorValue currError;

	/**
	 * Training vector.
	 */
	private Vector<Double> trainVector;

	/**
	 * Create the backprop algorithm.
	 * 
	 * @param trainingVector
	 * 		the training vector.
	 * 
	 * @param net
	 * 		the network to run the algorithm on.
	 * 
	 * @param expErr
	 * 		desired error value.
	 */
	public BackpropAlgorithm(
			final Vector<Double> trainingVector,
			final INetwork net, final ErrorValue expErr) {
		this.trainVector = trainingVector;
		this.network = net;
		this.expectedError = expErr;
	}

	@Override
	public int getProgress() {
		return 0;
	}

	@Override
	public ErrorValue getErrorStatus() {
		return new ErrorValue(
				expectedError.getValue()
				- currError.getValue());
	}

	/**
	 * Compute MSE.
	 * @return
	 * 		the mean squared error of the network.
	 */
	public Double compute() {
		//TODO Fix, and get right.
		Vector<Double> output = network.runInputs(trainVector);
		currError = new ErrorValue(
				ErrorFunctions.getInstance().meanSquaredError(
						output, trainVector));
		Iterator<IOutputNeuron> iter = network.getOutputLayer().iterator();
		Stack<Double> outputs = new Stack<Double>();
		outputs.addAll(output);
		//Skip bias neuron.
		iter.next();
		while (iter.hasNext()) {
			iter.next().propagateError(outputs.pop());
		}
		
		Vector<Double> errVector = new Vector<Double>();
		
		Iterator<IInputNeuron> iterIn = network.getInputLayer().iterator();
		
		while (iterIn.hasNext()) {
			errVector.add(iterIn.next().getValue().getValue());
		}
		
		return MathTools.sum(errVector);
		
	}

}
