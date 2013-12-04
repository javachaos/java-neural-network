package com.neuralnetwork.shared.nodes;

/**
 * Represents a hidden neuron.
 * @author fredladeroute
 *
 */
public class HiddenNeuron extends Neuron implements IHiddenNeuron {
    
    @Override
    public final String toString() {
        return "HN(" + getValue() + ") ";
    }
}
