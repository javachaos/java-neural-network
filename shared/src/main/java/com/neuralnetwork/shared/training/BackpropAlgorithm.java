package com.neuralnetwork.shared.training;

import java.util.Iterator;
import java.util.Stack;
import java.util.Vector;

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
    
	/**
	 * Network to be trained.
	 */
	private INetwork network;
	
	/**
	 * Expected error value.
	 */
	private Double expectedError;
	
	/**
	 * Current error value.
	 */
	private Double currError;

	/**
	 * Training vector.
	 */
	private Vector<Double> trainVector;
	

	/**
	 * Training vector.
	 */
	private Vector<Double> desiredVector;

	/**
	 * Create the backprop algorithm.
	 * 
	 * @param trainingVector
	 * 		the training vector.
	 * 
	 * @param dV
	 * 		the desired output vector.
	 * 
	 * @param net
	 * 		the network to run the algorithm on.
	 * 
	 * @param expErr
	 * 		desired error value.
	 * 
	 */
	public BackpropAlgorithm(
			final Vector<Double> trainingVector,
			final Vector<Double> dV,
			final INetwork net, final Double expErr) {
		this.trainVector = trainingVector;
		this.network = net;
		this.expectedError = expErr;
		this.desiredVector = dV;
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
		
		Vector<Double> output = network.runInputs(trainVector);
		LOGGER.debug("Network output: " + output);
		currError =	ErrorFunctions.getInstance().meanSquaredError(
						output, trainVector);
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
			errVector.add(iterIn.next().getValue());
		}
		
		return MathTools.sum(errVector);
		
	}

	/**
	 * @return the desiredVector
	 */
	public Vector<Double> getDesiredVector() {
		return desiredVector;
	}

	/**
	 * @param dV the desiredVector to set
	 */
	public void setDesiredVector(final Vector<Double> dV) {
		this.desiredVector = dV;
	}

}
