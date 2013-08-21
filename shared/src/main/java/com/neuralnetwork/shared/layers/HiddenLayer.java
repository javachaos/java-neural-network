package com.neuralnetwork.shared.layers;

import com.neuralnetwork.shared.nodes.BiasNeuron;
import com.neuralnetwork.shared.nodes.IInputNeuron;

/**
 * Represents a hidden neural network layer.
 * 
 * @author fredladeroute
 *
 */
public class HiddenLayer extends Layer {

    /**
     * Generated Serial Version UID.
     */
    private static final long serialVersionUID = 5485729609664280674L;
    
    /**
     * The single bias neuron for this layer.
     */
    private IInputNeuron biasNeuron = new BiasNeuron();

    /**
     * Constructs a new hidden layer.
     * 
     * @param w
     *      the length of this hidden layer
     */
    public HiddenLayer(final int w) {
        super(w);
    }

    /**
     * @return the biasNeuron
     */
    public final IInputNeuron getBiasNeuron() {
        return biasNeuron;
    }

}
