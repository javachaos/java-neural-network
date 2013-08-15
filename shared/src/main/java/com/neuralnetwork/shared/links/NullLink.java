package com.neuralnetwork.shared.links;

import com.neuralnetwork.shared.nodes.INode;

/**
 * Represents a link with possible no endpoint
 * or possibly no origin.
 * 
 * @author fredladeroute
 *
 */
public class NullLink implements ILink {

    /**
     * The head of this null link,
     * high chance that it is null.
     */
    private INode head;
    
    /**
     * The tail of this null link,
     * very high change that it will be null.
     */
    private INode tail;

    /**
     * The weight of this link,
     * will always be zero.
     */
    private double weight;
    
    /**
     * Constructor for a null link.
     */
    public NullLink() { }
    
    /**
     * Return the head of this null link.
     * 
     * @return
     *      the head INode.
     */
    public final INode getHead() {
        return head;
    }
    
    /**
     * Sets the head of this null link.
     * @param ihead
     */
    public final void setHead(INode ihead) {
        if(ihead == null) {
            throw new NullPointerException("Error cannot set null head INode.");
        } else {
            this.head = ihead;
        }
    }

    /**
     * Return the tail of this null link.
     * 
     * @return
     *      the tail INode.
     */
    public final INode getTail() {
        return tail;
    }
    
    /**
     * Set the tail of this ILink.
     * 
     * @param tail
     *      the tail to be set
     */
    public final void setTail(INode itail) {
        if(itail == null) {
            throw new NullPointerException("Error cannot set null tail INode.");
        } else {
            this.tail = itail;
        }
    }

    /**
     * Get the weight of this NullLink.
     * Will return 0;
     * 
     * @return
     *      returns 0 since a null link if used should add negative feedback.
     */
    public final double getWeight() {
        return weight;
    }

    /**
     * Update the weight to zero.
     */
    public final void updateWeight(double value, boolean sign) { 
        this.weight = 0;
    }

    /**
     * Update the weight to zero.
     */
    public final void setWeight(double weightValue) {
        this.weight = 0;
    }

}
