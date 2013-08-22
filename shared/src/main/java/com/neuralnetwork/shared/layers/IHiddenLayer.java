package com.neuralnetwork.shared.layers;

import com.neuralnetwork.shared.network.INeuralNetContext;
import com.neuralnetwork.shared.nodes.IHiddenNeuron;

/**
 * Interface for an IHiddenLayer.
 * @author fredladeroute
 *
 */
public interface IHiddenLayer extends ILayer<IHiddenNeuron>, IBuildable {
    
    /**
     * Propagate the values from this IHiddenLayer to the
     * next ILayer.
     * 
     * @param nnctx
     *      neural net context parameter
     */
    void propagate(INeuralNetContext nnctx);
}
