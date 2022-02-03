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
package com.neuralnetwork.shared.layers;

import java.util.List;

import com.neuralnetwork.shared.network.NeuralNetContext;
import com.neuralnetwork.shared.neurons.InputNeuron;

/**
 * Represents an InputLayer class.
 */
public interface InputLayer extends Layer<InputNeuron>, Buildable {

    /**
     * Add a value to this InputLayer.
     * 
     * @param v
     *      the value to add to the input layer
     *      
     * @param index
     *      which INode to insert the IValue at
     */
    void addValue(Double v, int index);
    
    /**
     * Add a list of values to this InputLayer.
     * 
     * @param values
     *      the list of values to be added.
     */
    void addValues(final List<Double> values);
    
    /**
     * Propagate the values from this InputLayer to the
     * next Layer.
     * 
     * @param nnctx
     *      neural net context parameter
     * @return
     *      the output layer of the network
     */
    OutputLayer propagate(NeuralNetContext nnctx);

    /**
     * Build the input layer using Neurons.
     */
    void build();
}
