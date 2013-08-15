package com.neuralnetwork.shared.links;

import com.neuralnetwork.shared.nodes.INode;

/**
 * Represents the link between to INode classes.
 * 
 * @author fredladeroute
 *
 */
public interface ILink {
    
    /**
     * Get the head of the link.
     * 
     * @return 
     *      the head of the link.
     */
    INode getHead();
    
    /**
     * Get the tail of the link.
     * 
     * @return 
     *      the tail of the link.
     */
    INode getTail();
    
    /**
     * Set the head INode of this ILink.
     *  
     * @param ihead
     *      the INode to set as head for this link
     * 
     */
    void setHead(INode ihead);
    
    /**
     * Set the tail INode of this ILink.
     *  
     * @param itail
     *      the INode to set as tail for this link
     */
    void setTail(INode itail);
    
    
    /**
     * Return the weight of this link
     * normalized to [0-1].
     * 
     * @return 
     *      the weight of this link.
     */
    double getWeight();
    
    /**
     * Update the weight of this link in
     * either positive or negative direction based
     * on the sign boolean, (true for positive false for
     * negative) by value. If the value pushes the weight beyond the
     * bounds of [0-1] the weight will be clamped to either 0 or 1.
     * 
     * @param value
     *      the amount to update the weight by.
     * 
     * @param sign
     *      the direction in which to update the weight
     *      true for positive false for negative
     */
    void updateWeight(double value, boolean sign);
    
    /**
     * Change the weight of this ILink to weightValue.
     * 
     * @param weightValue
     *      the value of the new weight for this link,
     *      must be withing [0-1]
     */
    void setWeight(double weightValue);

}
