package com.neuralnetwork.shared.neurons;

/**
 * Interface for an output neuron.
 * 
 * @author fredladeroute
 *
 */
public interface IOutputNeuron extends INeuron {

    /**
     * Get the output value of this IOutputNeuron.
     * 
     * @return
     *      the output value from the network as a double
     */
    double getOutputValue();
}
