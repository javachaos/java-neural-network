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
package com.github.javachaos.javaneuralnetwork.shared.util;

/**
 * Network configuration class used to
 * configure a new network.
 * 
 * @author fred
 *
 */
public class NetworkConfig {
    
    /**
     * Number of inputs for the desired network.
     */
    private final int numInputs;
    
    /**
     * Number of outputs for the desired network.
     */
    private final int numOuputs;
    
    /**
     * The number of hidden layers for the network.
     * Note: minimum total layers of a network is 2
     * which means there will be zero hidden layers.
     * 1 input layer and 1 output layer.
     */
    private final int numHiddenLayers;
    
    /**
     * List of integers describing the number of
     * neurons per hidden layer of the network.
     */
    private int[] layerSizes;

    /**
     * NetworkConfig constructor.
     * 
     * @param inputs
     *      number of inputs for the network
     *      
     * @param outputs
     *      number of outputs for the network
     *      
     * @param sizes
     *      list of integers describing the number of
     *      neurons per hidden layer of the network
     */
    public NetworkConfig(final int inputs, 
        final int outputs, final int[] sizes) {
        
        if (inputs < 0
        ||  outputs < 0) {
            throw new IllegalArgumentException("Error bad configuration.");
        } 
        this.numInputs = inputs;
        this.numOuputs = outputs;
        this.numHiddenLayers = sizes.length;
        this.layerSizes = sizes;
    }
    
    /**
     * @return 
     *      the number of inputs
     */
    public final int getNumInputs() {
        return numInputs;
    }

    /**
     * Return the hidden layer sizes of this network
     * (Note: excluding input and output layers)
     */
    public final int[] getLayerSizes() {
        return layerSizes;
    }

    /**
     * @param sizes 
     *      list of integers describing the number of
     *      neurons per hidden layer of the network
     */
    public final void setLayerSizes(final int[] sizes) {
        this.layerSizes = sizes;
    }

    /**
     * @return the numHiddenLayers
     */
    public final int getNumHiddenLayers() {
        return numHiddenLayers;
    }

    /**
     * @return the numOuputs
     */
    public final int getNumOuputs() {
        return numOuputs;
    }
    
}
