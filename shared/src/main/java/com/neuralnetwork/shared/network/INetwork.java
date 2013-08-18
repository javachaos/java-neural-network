package com.neuralnetwork.shared.network;

import com.neuralnetwork.shared.layers.ILattice;
import com.neuralnetwork.shared.training.TrainingSet;
import com.neuralnetwork.shared.values.ErrorValue;

/**
 * Represents an Artificial neural network of INodes.
 * 
 * @author fredladeroute
 * 
 * @param <T>
 *      the type of network
 *
 */
public interface INetwork<T extends Number> extends ILattice<T> {
    
    /**
     * Run inputs through the network.
     * 
     * @param inputLayer
     *      the values to provide to the input layer
     * 
     * @return
     *      the output values of the neural net
     */
    Number[] runInputs(Number[] inputLayer);
    
    /**
     * Reset the network effectively,
     * changes all weight values to a random value between [0,1].
     */
    void reset();
    
    /**
     * Trains this network on one vector.
     * 
     * @param trainingVector
     *      the vector to train on the network.
     *      
     * @return
     *      the Mean squared error value
     */
    ErrorValue train(Number[] trainingVector);
    
    /**
     * Train the network on the TrainingSet until the desired error is reached
     * or until the training set in empty and return the last reported error
     * value. Training method is standard gradient decent.
     * 
     * @param trainingSet
     *      the set of training examples to train the network with
     *      
     * @param expectedError
     *      the expected error rate to stop training at
     *      
     * @return
     *      the latest mean squared error of the network
     */
    ErrorValue train(TrainingSet trainingSet, ErrorValue expectedError);

}
