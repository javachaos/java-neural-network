package com.neuralnetwork.shared.nodes;

import com.neuralnetwork.shared.values.OneValue;

/**
 * Represents a bias neuron who's value is always 1.
 * But the weights can vary.
 * 
 * @author fredladeroute
 *
 */
public class BiasNeuron extends InputNeuron implements IInputNeuron {

    /**
     * Constructs a new bias neuron.
     */
    public BiasNeuron() {
        super(new OneValue());
    }

}
