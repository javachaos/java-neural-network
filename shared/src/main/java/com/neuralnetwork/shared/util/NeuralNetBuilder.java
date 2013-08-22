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
package com.neuralnetwork.shared.util;

import com.neuralnetwork.shared.layers.IHiddenLayer;
import com.neuralnetwork.shared.network.INetwork;
import com.neuralnetwork.shared.network.Network;

/**
 * Class to build neural network data structures.
 * 
 * @author fredladeroute
 *
 */
public final class NeuralNetBuilder {
    
    /**
     * The builder instance.
     */
    private static NeuralNetBuilder netBuilder = null;
    
    /**
     * Network instance.
     */
    private INetwork network;
    
    /**
     * Neural net constructor.
     * 
     * @param numInputs
     *      the number of inputs to the neural network to build
     *      
     * @param numOutputs
     *      the number of outputs to the neural network to build
     */
    private NeuralNetBuilder(final int numInputs, final int numOutputs) {
        network = new Network(numInputs, numOutputs);
    }
    
    /**
     * Create a new basic neural network structure.
     * 
     * @param numInputs
     *      the number of inputs the network should have
     *  
     * @param numOutputs
     *      the number of outputs the network should have
     * 
     * @return
     *      a basic NeuralNetBuilder instance to allow for chaining
     */
    public static NeuralNetBuilder createNew(
            final int numInputs, final int numOutputs) {
        netBuilder = new NeuralNetBuilder(numInputs, numOutputs);
        return netBuilder;
    }
    
    /**
     * Add a hidden layer to the network.
     * 
     * @param l
     *      the hidden layer to be added to the network
     * 
     * @return
     *      the neural net builder instance.
     */
    public static NeuralNetBuilder addHiddenLayer(final IHiddenLayer l) {
        netBuilder.network.addHiddenLayer(l);
        return netBuilder;
    }
    
    /**
     * Build the neural network.
     * 
     * @return
     *      a reference to the built neural network.
     */
    public static INetwork build() {
        netBuilder.network.build();
        return netBuilder.network;
    }

}
