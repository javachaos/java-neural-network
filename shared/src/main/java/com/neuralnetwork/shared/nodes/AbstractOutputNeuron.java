package com.neuralnetwork.shared.nodes;

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
        super();
    }

    @Override
    public final double getOutputValue() {
        return this.getValue().getValue().doubleValue();
    }
    
    

}
