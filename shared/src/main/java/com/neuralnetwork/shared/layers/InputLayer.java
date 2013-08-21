package com.neuralnetwork.shared.layers;

import com.neuralnetwork.shared.nodes.BiasNeuron;
import com.neuralnetwork.shared.nodes.IInputNeuron;

/**
 * Represents an Input layer to the network.
 * 
 * @author fredladeroute
 *
 */
public class InputLayer extends Layer {

    /**
     * Generated Serial Version UID. 
     */
    private static final long serialVersionUID = 7502031311250577255L;
    
    /**
     * The single bias neuron for this layer.
     */
    private IInputNeuron biasNeuron = new BiasNeuron();
    
    /**
     * Constructs a new input layer of length w.
     * 
     * @param w
     *      the length of the input layer.
     */
    public InputLayer(final int w) {
        super(w);
    }
    
    /**
     * @return the biasNeuron
     */
    public final IInputNeuron getBiasNeuron() {
        return biasNeuron;
    }

}
