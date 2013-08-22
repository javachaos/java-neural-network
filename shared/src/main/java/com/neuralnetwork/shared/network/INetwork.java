/*******************************************************************************
 * Copyright (c) 2013 Fred Laderoute.
 * All rights reserved. This program and the accompanying
 * materials are made available under the terms of the GNU 
 * Public License v3.0 which accompanies this distribution, 
 * and is available at http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *      Fred Laderoute - initial API and implementation
 *******************************************************************************/
package com.neuralnetwork.shared.network;

import java.util.Vector;

import com.neuralnetwork.shared.layers.IHiddenLayer;
import com.neuralnetwork.shared.layers.IInputLayer;
import com.neuralnetwork.shared.layers.ILayer;
import com.neuralnetwork.shared.layers.IOutputLayer;
import com.neuralnetwork.shared.nodes.INeuron;
import com.neuralnetwork.shared.training.TrainingSet;
import com.neuralnetwork.shared.values.ErrorValue;

/**
 * Represents an Artificial neural network of INodes.
 * 
 * @author fredladeroute
 * 
 *
 */
public interface INetwork {
    
    /**
     * Run inputs through the network.
     * 
     * @param inputLayer
     *      the values to provide to the input layer
     * 
     * @return
     *      the output values of the neural net
     */
    Vector<Double> runInputs(Vector<Double> inputLayer);
    
    /**
     * Reset the network effectively,
     * changes all weight values to a random value between [0,1].
     */
    void reset();
    
    /**
     * Adds a hidden layer to the network at the bottom
     * of the network.
     * 
     * @param l
     *      the layer to be added to the network
     */
    void addHiddenLayer(IHiddenLayer l);
    
    /**
     * Get the hidden layer at height i of the network,
     * where the first hidden layer is 0 and the last hidden
     * layer is getHeight() - 1.
     * 
     * @param i
     *      the index to the hidden layer to get
     * @return
     *      the IHiddenLayer at index i
     */
    IHiddenLayer getHiddenLayer(int i);
    
    /**
     * Trains this network on one vector.
     * 
     * @param trainingVector
     *      the vector to train on the network.
     *      
     * @return
     *      the Mean squared error value
     */
    ErrorValue train(Vector<Double> trainingVector);
    
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
    
    /**
     * Get the output layer of this network.
     * 
     * @return
     *      the output layer of this network
     */
    IOutputLayer getOutputLayer();
    
    /**
     * Set the output layer for this network.
     * 
     * @param l
     *      the output layer to be set
     */
    void setOutputLayer(IOutputLayer l);
    
    /**
     * Return the layer at index idx.
     * 
     * Depricated as of {@date}
     * 
     * @param idx
     *      the index to the layer idx
     *      
     * @return
     *      the ILayer at layer idx
     * 
     * @see #getHiddenLayer(int)
     * @see #getOutputLayer()
     * @see #getInputLayer()
     */
    @Deprecated
    ILayer<INeuron> getLayer(int idx);
    
    /**
     * Get an INode from the ILattice.
     * 
     * @param x
     *      the x co-ordinate of the INode
     *      
     * @param y
     *      the y co-ordinate of the INode
     *      
     * @return
     *      the INode at (x,y) in this ILattices
     */
    INeuron getNode(int x, int y);

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
    INeuron getOutputNeuron(int x);
    
    /**
     * Get a neuron from this networks input layer.
     * 
     * @param x
     *      the position of the neuron in the input layer to get
     * @return
     *      the neuron at position x in the input layer
     */
    INeuron getInputNeuron(int x);
    
    /**
     * Get the input layer to this network.
     * 
     * @return 
     *      the inputLayer
     */
    IInputLayer getInputLayer();
    
    /**
     * Set the input layer of this network.
     * 
     * @param l
     *      the inputLayer to set
     */
    void setInputLayer(final IInputLayer l);
    
    /**
     * Build the neural network.
     * 
     * 1. Construct all Neurons in each layer.
     * 2. Construct all the links connecting all layers
     */
    void build();

}
