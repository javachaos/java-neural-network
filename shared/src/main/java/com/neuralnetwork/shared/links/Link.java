package com.neuralnetwork.shared.links;

import com.neuralnetwork.shared.exceptions.OutOfBoundsException;
import com.neuralnetwork.shared.nodes.INode;

/**
 * A basic link which connects two INodes.
 * 
 * @author fredladeroute
 *
 */
public class Link implements ILink {

    /**
     * The head of this link.
     */
    private INode head;
    
    /**
     * The tail of this link.
     */
    private INode tail;

    /**
     * The weight of this link
     * initialized to 0.
     */
    private double weight = 0.0;
    
    /**
     * Construct a new link with head, tail and weight.
     * 
     * @param ihead
     *      the head of this link.
     *      
     * @param itail
     *      the tail of this link.
     *      
     * @param linkWeight
     *      the weight of this link [0-1].
     */
    public Link(
            final INode ihead, 
            final INode itail, 
            final double linkWeight) {
        
        this.head = ihead;
        this.tail = itail;
        this.weight = linkWeight;
    }

    @Override
    public final INode getHead() {
        return head;
    }

    @Override
    public final INode getTail() {
        return tail;
    }

    @Override
    public final double getWeight() {
        return weight;
    }
    
    @Override
    public final void setHead(final INode ihead) {
        if (ihead == null) {
            throw new NullPointerException("Error cannot set null head INode.");
        } else {
            this.head = ihead;
        }
    }

    @Override
    public final void setTail(final INode itail) {
        if (itail == null) {
            throw new NullPointerException("Error cannot set null tail INode.");
        } else {
            this.tail = itail;
        }
    }

    @Override
    public final void updateWeight(final double value, final boolean sign) {
        if (sign) {
            weight += value;
        } else {
            weight -= value;
        }
        
        if (weight > 1) {
            weight = 1;
        }
        if (weight < 0) {
            weight = 0;
        }
        
    }

    @Override
    public final void setWeight(final double weightValue) {
        if (weightValue < 1 && weightValue > 0) {
            this.weight = weightValue;
        } else {
            throw new OutOfBoundsException(
                    "Error weight value must be within the bounds [0-1].");
        }
    }

}
