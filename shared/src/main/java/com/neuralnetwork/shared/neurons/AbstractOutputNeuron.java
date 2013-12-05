package com.neuralnetwork.shared.neurons;

/**
 * Defines an abstract output neuron.
 * 
 * @author fredladeroute
 *
 */
public class AbstractOutputNeuron extends Neuron implements IOutputNeuron {
    
    /**
     * Construct a new Abstract output neuron.
     */
    public AbstractOutputNeuron() {
        super(NeuronType.OUTPUT);
    }

    @Override
    public final double getOutputValue() {
        return this.getValue().getValue().doubleValue();
    }
    
    

}
