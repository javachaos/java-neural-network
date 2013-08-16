package com.neuralnetwork.shared.links;

import com.neuralnetwork.shared.nodes.INeuron;
import com.neuralnetwork.shared.values.IValue;

/**
 * Represents the link between to INode classes.
 * 
 * @author fredladeroute
 *
 *@param <T>
 *      the type of ILink
 */
public interface ILink<T extends Number> {
    
    /**
     * Get the head of the link.
     * 
     * @return 
     *      the head of the link.
     */
    INeuron<T> getHead();
    
    /**
     * Get the tail of the link.
     * 
     * @return 
     *      the tail of the link.
     */
    INeuron<T> getTail();
    
    /**
     * Set the head INode of this ILink.
     *  
     * @param ihead
     *      the INode to set as head for this link
     * 
     */
    void setHead(INeuron<T> ihead);
    
    /**
     * Set the tail INode of this ILink.
     *  
     * @param itail
     *      the INode to set as tail for this link
     */
    void setTail(INeuron<T> itail);
    
    
    /**
     * Return the weight of this link
     * normalized to [0-1].
     * 
     * @return 
     *      the weight of this link.
     */
    IValue<T> getWeight();
    
    /**
     * Update the weight of this link. If the value pushes the weight beyond the
     * bounds of [0-1] the weight will be clamped to either 0 or 1.
     * 
     * @param value
     *      the amount to update the weight by.
     * 
     */
    void updateWeight(IValue<T> value);
    
    /**
     * Change the weight of this ILink to weightValue.
     * 
     * @param weightValue
     *      the value of the new weight for this link.
     */
    void setWeight(IValue<T> weightValue);

}
