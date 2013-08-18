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
    private INetwork<?> network;
    
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
        this.nInputs = numInputs;
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
     * 
     * @return
     */
    public static INetwork<?> build(IValue<?> value) {
        //return new INetwork();
        return null;
    }

}
