package com.neuralnetwork.shared.network;

import com.neuralnetwork.shared.links.ILink;
import com.neuralnetwork.shared.nodes.INeuron;

/**
 * Represents a NeuralNetContext which stores all the links of a neural network.
 * 
 * @author fredladeroute
 * 
 * @param <T>
 *      the type of context this is
 */
public interface INeuralNetContext<T extends Number> {
    
    /**
     * Get the tail given the head INode.
     * 
     * @param head
     *      the head of the tail to get
     * @return
     *      the tail of the given head INode
     */
    INeuron<T> getTail(INeuron<T> head);
    
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
    ILink<T> getLink(INeuron<T> head, INeuron<T> tail);

}
