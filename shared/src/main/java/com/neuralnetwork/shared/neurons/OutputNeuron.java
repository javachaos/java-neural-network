package com.neuralnetwork.shared.neurons;


/**
 * Defines an output neuron.
 * 
 * @author fredladeroute
 *
 */
public final class OutputNeuron extends AbstractOutputNeuron {

	public OutputNeuron() {
		super();
	}
	
    @Override
    public String toString() {
        return "ON(" + getValue() + ") ";
    }
}
