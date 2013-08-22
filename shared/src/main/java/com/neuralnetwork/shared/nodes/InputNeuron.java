package com.neuralnetwork.shared.nodes;

import com.neuralnetwork.shared.values.IValue;

/**
 * Implementation of an Input neuron.
 * @author fredladeroute
 *
 */
public class InputNeuron extends AbstractInputNeuron {
   
    /**
     * Constructs a new input value with value v.
     * 
     * @param v
     *      the initial input value
     */
    public InputNeuron(final IValue<?> v) {
        super(v);
    }
    
    /**
     * Constracts a new input value with value
     * ZeroValue.
     */
    public InputNeuron() {
        super();
    }

}
