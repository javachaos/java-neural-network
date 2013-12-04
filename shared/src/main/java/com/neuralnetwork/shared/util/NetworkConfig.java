package com.neuralnetwork.shared.util;

/**
 * Network configuration class used to
 * configure a new network.
 * 
 * @author fred
 *
 */
public final class NetworkConfig {
    
    /**
     * Number of inputs for the desired network.
     */
    private int numInputs;
    
    /**
     * Number of outputs for the desired network.
     */
    private int numOuputs;
    
    /**
     * The number of hidden layers for the network.
     * Note: minimum total layers of a network is 2
     * which means there will be zero hidden layers.
     * 1 input layer and 1 output layer.
     */
    private int numHiddenLayers;
    
    /**
     * List of integers describing the number of
     * neurons per hidden layer of the network.
     */
    private int[] layerSizes;

    /**
     * @return 
     *      the number of inputs
     */
    public int
        getNumInputs() {
        return numInputs;
    }

    /**
     * @param inputs 
     *      the numInputs to set
     */
    public void setNumInputs(final int inputs) {
        this.numInputs = inputs;
    }

    /**
     * @return the layerSizes
     */
    public int[]
        getLayerSizes() {
        return layerSizes;
    }

    /**
     * @param sizes 
     *      list of integers describing the number of
     *      neurons per hidden layer of the network
     */
    public void
        setLayerSizes(final int[] sizes) {
        this.layerSizes = sizes;
    }

    /**
     * @return the numHiddenLayers
     */
    public int
        getNumHiddenLayers() {
        return numHiddenLayers;
    }

    /**
     * @param layers 
     *      the numHiddenLayers to set
     */
    public void setNumHiddenLayers(final int layers) {
        this.numHiddenLayers = layers;
    }

    /**
     * @return the numOuputs
     */
    public int
        getNumOuputs() {
        return numOuputs;
    }

    /**
     * @param outputs
     *      the numOuputs to set
     */
    public void setNumOuputs(final int outputs) {
        this.numOuputs = outputs;
    }
    
}
