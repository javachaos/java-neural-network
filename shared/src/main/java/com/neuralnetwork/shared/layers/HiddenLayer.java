package com.neuralnetwork.shared.layers;

import com.neuralnetwork.shared.network.INeuralNetContext;
import com.neuralnetwork.shared.network.LayerType;
import com.neuralnetwork.shared.nodes.BiasNeuron;
import com.neuralnetwork.shared.nodes.IInputNeuron;
import com.neuralnetwork.shared.nodes.INeuron;

/**
 * Represents a hidden neural network layer.
 * 
 * @author fredladeroute
 *
 */
public final class HiddenLayer extends Layer<INeuron> implements IHiddenLayer {

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
        super.setLayerType(LayerType.HIDDEN);
    }

    /**
     * @return the biasNeuron
     */
    public IInputNeuron getBiasNeuron() {
        return biasNeuron;
    }

    @Override
    public void propagate(final INeuralNetContext nnctx) {
        // TODO implement
        
    }

}
