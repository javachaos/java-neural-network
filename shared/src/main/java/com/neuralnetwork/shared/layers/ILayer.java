package com.neuralnetwork.shared.layers;

import com.neuralnetwork.shared.links.ILink;
import com.neuralnetwork.shared.nodes.INeuron;

/**
 * Represents a Layer class.
 * 
 * @author fredladeroute
 * 
 * @param <T>
 *      the type of layer this is
 *
 */
public interface ILayer<T extends Number> extends ILink<T> {

    /**
     * Get the number of INodes in this layer.
     * 
     * @return
     *      the number of INode values.
     */
    int getSize();
    
    /**
     * Get a node from this layer.
     * 
     * @param idx
     *      the index to the node
     *
     * @return
     *      the INode at index idx
     */
    INeuron<T> getNode(int idx);
    
}
