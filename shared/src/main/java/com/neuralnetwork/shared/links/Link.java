package com.neuralnetwork.shared.links;

import com.neuralnetwork.shared.nodes.INeuron;
import com.neuralnetwork.shared.values.IValue;

/**
 * A basic link which connects two INodes.
 * 
 * @author fredladeroute
 *
 *@param <T>
 *      the type of this Link
 */
public class Link<T extends Number> implements ILink<T> {

    /**
     * The head of this link.
     */
    private INeuron head;
    
    /**
     * The tail of this link.
     */
    private INeuron tail;

    /**
     * The weight of this link
     * initialized to 0.
     */
    private IValue<T> weight;
    
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
            final INeuron ihead, 
            final INeuron itail, 
            final IValue<T> linkWeight) {
        
        this.head = ihead;
        this.tail = itail;
        this.weight = linkWeight;
    }

    @Override
    public final INeuron getHead() {
        return head;
    }

    @Override
    public final INeuron getTail() {
        return tail;
    }

    @Override
    public final IValue<T> getWeight() {
        return weight;
    }
    
    @Override
    public final void setHead(final INeuron ihead) {
        if (ihead == null) {
            throw new NullPointerException("Error cannot set null head INode.");
        } else {
            this.head = ihead;
        }
    }

    @Override
    public final void setTail(final INeuron itail) {
        if (itail == null) {
            throw new NullPointerException("Error cannot set null tail INode.");
        } else {
            this.tail = itail;
        }
    }

    @Override
    public final void updateWeight(final IValue<T> value) {
        this.weight.updateValue(value);
    }

    @Override
    public final void setWeight(final IValue<T> weightValue) {
        this.weight = weightValue;
    }

}
