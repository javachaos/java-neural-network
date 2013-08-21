package com.neuralnetwork.shared.layers;

import com.neuralnetwork.shared.network.INeuralNetContext;
import com.neuralnetwork.shared.network.LayerType;
import com.neuralnetwork.shared.nodes.BiasNeuron;
import com.neuralnetwork.shared.nodes.IInputNeuron;
import com.neuralnetwork.shared.nodes.InputNeuron;
import com.neuralnetwork.shared.values.IValue;

/**
 * Represents an Input layer to the network.
 * 
 * @author fredladeroute
 *
 */
public final class InputLayer extends Layer<IInputNeuron> implements IInputLayer {

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
        super.setLayerType(LayerType.INPUT);
    }
    
    /**
     * @return the biasNeuron
     */
    public IInputNeuron getBiasNeuron() {
        return biasNeuron;
    }

    @Override
    public void addValue(final IValue<?> v, final int index) {
        super.set(index, new InputNeuron(v));
    }

    @Override
    public boolean isLayerFull() {
        return super.size() == getWidth();
    }

    @Override
    public void propagate(final INeuralNetContext nnctx) {
        // TODO Implement
    }

}
