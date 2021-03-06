/*******************************************************************************
 * Copyright (c) 2014 Fred Laderoute.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *     Fred Laderoute - initial API and implementation
 ******************************************************************************/
package com.neuralnetwork.shared.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neuralnetwork.shared.layers.HiddenLayer;
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
     * Logger instance.
     */
    private static final Logger LOGGER = 
            LoggerFactory.getLogger(NeuralNetBuilder.class);
    
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

    public NeuralNetBuilder(final int numInputs, final int numOutputs) {
        network = new Network(numInputs, numOutputs);
    }
    
    /**
     * Create a new neural network using 
     * the specified {@link NetworkConfig}.
     * 
     * @param networkConfig
     *      the new network configuration settings
     */
    public NeuralNetBuilder(final NetworkConfig networkConfig) {
        buildWithSettings(networkConfig);
    }
    
    /**
     * Build the network using the 
     * network settings in networkConfig.
     * 
     * @param networkConfig
     *      the new network configuration settings
     */
    private void buildWithSettings(final NetworkConfig networkConfig) {
        network = new Network(networkConfig.getNumInputs(), 
                              networkConfig.getNumOuputs());
        for (int layerSize : networkConfig.getLayerSizes()) {
            network.addHiddenLayer(new HiddenLayer(layerSize));
        }
    }

    /**
     * Add a hidden layer to the network.
     * 
     * @param l
     *      the hidden layer to be added to the network
     * 
     * @return
     *      the neural net builder instance
     */
    public NeuralNetBuilder addHiddenLayer(final IHiddenLayer l) {
        network.addHiddenLayer(l);
        LOGGER.info("New hidden layer added to network.");
        return this;
    }
    
    /**
     * Build the neural network.
     * 
     * @return
     *      a reference to the built neural network
     */
    public INetwork build() {
        network.build();
        LOGGER.info(network.toString());
        return network;
    }

    /**
     * Returns a reference to the network.
     * @return
     *      a reference to the neural network
     */
    public INetwork getNetwork() {
        return network;
    }

}
