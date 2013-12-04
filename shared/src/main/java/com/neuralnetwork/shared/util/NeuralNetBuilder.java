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

import com.neuralnetwork.shared.network.INetwork;
import com.neuralnetwork.shared.values.IValue;

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
     * The number of inputs for this network.
     */
    private int nInputs;
    
    /**
     * The number of outputs for this network.
     */
    private int nOutputs;
    
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
        this.setNumInputs(numInputs);
        this.nOutputs = numOutputs;
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
     * 
     * @return
     */
    public static NeuralNetBuilder addNode() {
        return netBuilder;
    }
    
    /**
     * Build the neural network.
     * 
     * @return
     *      a new network.
     */
    public static INetwork build(final IValue<?> value) {
        //return new INetwork();
        return null;
    }

    /**
     * Returns a reference to the network.
     * @return
     *      a reference to the neural network.
     */
    public INetwork getNetwork() {
        return network;
    }

    /**
     * Get the number of inputs for the network being built.
     * @return
     *      the number of inputs to the network.
     */
    public int getNumInputs() {
        return nInputs;
    }

    /**
     * Set the number of inputs for the network to be built.
     * @param inputs
     *      the number of inputs to the network.
     */
    public void setNumInputs(final int inputs) {
        this.nInputs = inputs;
    }

}
