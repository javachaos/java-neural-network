package com.neuralnetwork.shared.nodes;


/**
 * Defines an output neuron.
 * 
 * @author fredladeroute
 *
 */
public final class OutputNeuron extends AbstractOutputNeuron {

    @Override
    public String toString() {
        return "ON(" + getValue() + ") ";
    }
}
