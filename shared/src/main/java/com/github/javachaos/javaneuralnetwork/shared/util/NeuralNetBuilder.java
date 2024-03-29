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

import com.github.javachaos.javaneuralnetwork.shared.layers.HiddenLayer;
import com.github.javachaos.javaneuralnetwork.shared.layers.HiddenNeuronLayer;
import com.github.javachaos.javaneuralnetwork.shared.network.Network;
import com.github.javachaos.javaneuralnetwork.shared.network.NeuralNetwork;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.stream.IntStream;

/**
 * Class to build neural network data structures.
 *
 */
public final class NeuralNetBuilder {

    private static final Logger LOGGER =
            LogManager.getLogger(NeuralNetBuilder.class);

    private Network network;
    
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
        network = new NeuralNetwork(numInputs, numOutputs);
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
        network = new NeuralNetwork(networkConfig.getNumInputs(),
                              networkConfig.getNumOuputs());
        int[] sizes = networkConfig.getLayerSizes();
        IntStream.range(0,sizes.length).parallel()
                .forEach(i -> network.addHiddenLayer(new HiddenNeuronLayer(sizes[i], i)));
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
    public NeuralNetBuilder addHiddenLayer(final HiddenLayer l) {
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
    public Network build() {
        network.build();
        String info = network.toString();
        LOGGER.info(info);
        return network;
    }

    /**
     * Returns a reference to the network.
     * @return
     *      a reference to the neural network
     */
    public Network getNetwork() {
        return network;
    }

}
