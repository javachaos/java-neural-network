package com.neuralnetwork.shared.layers;

import com.neuralnetwork.shared.network.LayerType;
import com.neuralnetwork.shared.nodes.IOutputNeuron;

/**
 * Represents an output layer of a network.
 * 
 * @author fredladeroute
 *
 */
public class OutputLayer extends Layer<IOutputNeuron> implements IOutputLayer {

    /**
     * Generated serial version UID.
     */
    private static final long serialVersionUID = -6278154162004288836L;

    /**
     * Construct a new Output layer.
     * 
     * @param w
     *      the length of the layer
     */
    public OutputLayer(final int w) {
        super(w);
        super.setLayerType(LayerType.OUTPUT);
    }

}
