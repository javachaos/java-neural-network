package com.neuralnetwork.shared.layers;

import com.neuralnetwork.shared.links.ILink;
import com.neuralnetwork.shared.nodes.INeuron;

/**
 * Represents a NeuralNetContext which stores all the links of a neural network.
 * 
 * @author fredladeroute
 *
 */
public interface INeuralNetContext {
    
    /**
     * Get the tail given the head INode.
     * 
     * @param head
     *      the head of the tail to get
     * @return
     *      the tail of the given head INode
     */
    INeuron getTail(INeuron head);
    
    /**
     * Get the ILink between head and tail
     * INodes.
     * 
     * @param head
     *      the head INode
     *
     * @param tail
     *      the tail INode
     *      
     * @return
     *      the ILink between INodes head and tail
     */
    ILink getLink(INeuron head, INeuron tail);

}
