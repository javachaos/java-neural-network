package com.neuralnetwork.shared.nodes;

import com.neuralnetwork.shared.values.IValue;

/**
 * Represents an input neuron.
 * 
 * @author fredladeroute
 *
 */
public interface IInputNeuron {

    /**
     * Sets the input value to the neuron.
     * 
     * @param v
     *      the value to be used as input for this neuron
     */
    void setInputValue(IValue<?> v);
    
    /**
     * Return the value of the input neuron.
     * 
     * @return
     *      the value of the input neuron
     */
    IValue<?> getValue();

}
