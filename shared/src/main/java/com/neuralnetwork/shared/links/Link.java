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
     * @param weight
     *      the weight of this link [0-1].
     */
    public Link(INode ihead, INode itail, double linkWeight) {
        this.head = ihead;
        this.tail = itail;
        this.weight = linkWeight;
    }
    
    public INode getHead() {
        return head;
    }

    public INode getTail() {
        return tail;
    }

    public double getWeight() {
        return weight;
    }

    public final void setHead(INode ihead) throws NullPointerException {
        if(ihead == null) {
            throw new NullPointerException("Error cannot set null head INode.");
        } else {
            this.head = ihead;
        }
    }

    public void setTail(INode itail) throws NullPointerException {
        if(itail == null) {
            throw new NullPointerException("Error cannot set null tail INode.");
        } else {
            this.tail = itail;
        }
    }

    public void updateWeight(double value, boolean sign) {
        if(sign) {
            weight += value;
        } else {
            weight -= value;
        }
        
        if(weight > 1) {
            weight = 1;
        }
        if(weight < 0) {
            weight = 0;
        }
        
    }

    public void setWeight(double weightValue) {
        if(weightValue < 1 && weightValue > 0) {
            this.weight = weightValue;
        } else {
            throw new OutOfBoundsException("Error weight value must be within the bounds [0-1].");
        }
    }

}
