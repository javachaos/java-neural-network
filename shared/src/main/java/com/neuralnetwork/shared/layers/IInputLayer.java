package com.neuralnetwork.shared.layers;

import com.neuralnetwork.shared.network.INeuralNetContext;
import com.neuralnetwork.shared.values.IValue;

/**
 * Represents an InputLayer class.
 * @author fredladeroute
 *
 *@param <T>
 *      the type of input layer this is
 */
public interface IInputLayer<T extends Number> extends ILayer<T> {

    /**
     * Add a value to this IInputLayer.
     * 
     * @param v
     *      the value to add to the input layer
     *      
     * @param index
     *      which INode to insert the IValue at
     */
    void addValue(IValue<T> v, int index);
    
    /**
     * Checks if each IInputNode in this IInputLayer
     * is occupied by an IValue.
     * 
     * @return
     *      true if all INodes in this IInputLayer are
     *      occupied
     */
    boolean isLayerFull();
    
    /**
     * Propagate the values from this IInputLayer to the
     * next ILayer.
     * 
     * @param nnctx
     *      neural net context parameter
     */
    void propagate(INeuralNetContext<T> nnctx);
}
