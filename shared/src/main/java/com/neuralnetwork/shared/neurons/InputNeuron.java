package com.neuralnetwork.shared.neurons;

import com.neuralnetwork.shared.values.DoubleValue;

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
    public InputNeuron(final DoubleValue v) {
        super(v);
    }
    
    /**
     * Constracts a new input value with value
     * ZeroValue.
     */
    public InputNeuron() {
        super();
    }
    
    @Override
    public final String toString() {
        return "IN(" + getValue() + ") ";
    }

}
