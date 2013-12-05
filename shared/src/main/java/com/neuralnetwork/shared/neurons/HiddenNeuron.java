package com.neuralnetwork.shared.neurons;

/**
 * Represents a hidden neuron.
 * @author fredladeroute
 *
 */
public class HiddenNeuron extends Neuron implements IHiddenNeuron {
    
	/**
	 * Create a new hidden neuron.
	 */
	public HiddenNeuron() {
		super();
	}
	
    @Override
    public final String toString() {
        return "HN(" + getValue() + ") ";
    }
}
