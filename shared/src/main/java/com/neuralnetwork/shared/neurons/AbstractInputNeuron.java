package com.neuralnetwork.shared.neurons;

import com.neuralnetwork.shared.values.DoubleValue;
import com.neuralnetwork.shared.values.ZeroValue;

/**
 * Represents an input neuron.
 * 
 * @author fredladeroute
 *
 */
public abstract class AbstractInputNeuron 
        extends Neuron implements IInputNeuron {

    /**
     * Construct an input neuron with 
     * no initial input value.
     * 
     */
    protected AbstractInputNeuron() {
        super(NeuronType.INPUT);
        this.setValue(new ZeroValue());
    }
    
    /**
     * Construct a neuron with initial value v.
     * 
     * @param v
     *      the initial value of the neuron
     */
    protected AbstractInputNeuron(final DoubleValue v) {
        super(NeuronType.INPUT);
        this.setValue(v);
    }
     

}