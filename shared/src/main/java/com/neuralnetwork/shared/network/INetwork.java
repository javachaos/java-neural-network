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

import com.neuralnetwork.shared.layers.ILattice;
import com.neuralnetwork.shared.layers.ILayer;
import com.neuralnetwork.shared.training.TrainingSet;
import com.neuralnetwork.shared.values.ErrorValue;

/**
 * Represents an Artificial neural network of INodes.
 * 
 * @author fredladeroute
 * 
 *
 */
public interface INetwork extends ILattice {
    
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
     * Add a layer to the network at the bottom
     * of the network.
     * 
     * @param l
     *      the layer to be added to the network
     * 
     * @param t
     *      the layer type t to add
     */
    void addLayer(ILayer l, LayerType t);
    
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

}
