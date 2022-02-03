/*******************************************************************************
 * Copyright (c) 2014 Fred .
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *     Fred  - initial API and implementation
 ******************************************************************************/
package com.neuralnetwork.shared.network;

import java.util.List;

import com.neuralnetwork.shared.layers.HiddenLayer;
import com.neuralnetwork.shared.layers.InputLayer;
import com.neuralnetwork.shared.layers.OutputLayer;
import com.neuralnetwork.shared.neurons.InputNeuron;
import com.neuralnetwork.shared.neurons.Neuron;
import com.neuralnetwork.shared.neurons.OutputNeuron;
import com.neuralnetwork.shared.training.TrainType;

/**
 * Represents an Artificial neural network of neurons.
 */
public interface Network {
    
    /**
     * Run inputs through the network.
     * 
     * @param inputLayer
     *      the values to provide to the input layer
     * 
     * @return
     *      the output values of the neural net
     */
	List<Double> runInputs(final List<Double> inputLayer);
    
    /**
     * Reset the network effectively,
     * changes all weight values to a random value between [0,1].
     */
    void reset();
    
    /**
     * Adds a hidden layer to the network at l.getIndex()
     * 
     * @param l
     *      the layer to be added to the network
     */
    void addHiddenLayer(HiddenLayer l);
    
    /**
     * Get the hidden layer at height i of the network,
     * where the first hidden layer is 0 and the last hidden
     * layer is getHeight() - 1.
     * 
     * @param i
     *      the index to the hidden layer to get
     * @return
     *      the HiddenLayer at index i
     */
    HiddenLayer getHiddenLayer(int i);
    
    /**
     * Get the output layer of this network.
     * 
     * @return
     *      the output layer of this network
     */
    OutputLayer getOutputLayer();
    
    /**
     * Set the output layer for this network.
     * 
     * @param l
     *      the output layer to be set
     */
    void setOutputLayer(OutputLayer l);
    
    /**
     * Get an INeuron from the INetwork.
     * 
     * @param x
     *      the x co-ordinate of the INetwork
     *      
     * @param y
     *      the y co-ordinate of the INetwork
     *      
     * @return
     *      the INeuron at (x,y) in this INetwork
     *      where x is the position of the neuron at layer y
     *      in the INetwork.
     */
    Neuron getNeuron(int x, int y);

    /**
     * Get the height of the network.
     * 
     * @return
     *      the height of this network
     */
    int getHeight();
    
    /**
     * Get a neuron from this networks output layer.
     * 
     * @param x
     *      the position of the neuron to get
     * @return
     *      the INeuron at position x
     */
    OutputNeuron getOutputNeuron(int x);
    
    /**
     * Get a neuron from this networks input layer.
     * 
     * @param x
     *      the position of the neuron in the input layer to get
     * @return
     *      the neuron at position x in the input layer
     */
    InputNeuron getInputNeuron(int x);
    
    /**
     * Get the input layer to this network.
     * 
     * @return 
     *      the inputLayer
     */
    InputLayer getInputLayer();
    
    /**
     * Set the input layer of this network.
     * 
     * @param l
     *      the inputLayer to set
     */
    void setInputLayer(final InputLayer l);
    
    /**
     * Set the desired training algorithm.
     * 
     * @param t
     * 		the training algorithm to be used.
     */
    void setTrainingAlgorithm(final TrainType t);
    
    /**
     * Build the neural network.
     * 
     * 1. Construct all Neurons in each layer.
     * 2. Construct all the links connecting all layers
     */
    void build();

}
