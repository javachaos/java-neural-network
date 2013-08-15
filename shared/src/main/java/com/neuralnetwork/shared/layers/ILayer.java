package com.neuralnetwork.shared.layers;

import com.neuralnetwork.shared.links.ILink;
import com.neuralnetwork.shared.nodes.INode;

/**
 * Represents a Layer class.
 * 
 * @author fredladeroute
 *
 */
public interface ILayer extends ILink {

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
    INode getNode(int idx);
    
}
