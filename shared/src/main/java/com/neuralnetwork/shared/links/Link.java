/*******************************************************************************
 * Copyright (c) 2013 Fred Laderoute.
 * All rights reserved. This program and the accompanying
 * materials are made available under the terms of the GNU 
 * Public License v3.0 which accompanies this distribution, 
 * and is available at http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *      Fred Laderoute - initial API and implementation
 *******************************************************************************/
package com.neuralnetwork.shared.links;

import com.neuralnetwork.shared.nodes.INeuron;
import com.neuralnetwork.shared.values.IValue;

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
    private INeuron head;
    
    /**
     * The tail of this link.
     */
    private INeuron tail;

    /**
     * The weight of this link
     * initialized to 0.
     */
    private IValue<?> weight;
    
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
            final IValue<?> linkWeight) {
        
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
    public final IValue<?> getWeight() {
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
    public final void updateWeight(final IValue<Number> value) {
        this.weight.updateValue(value);
    }

    @Override
    public final void setWeight(final IValue<Number> weightValue) {
        this.weight = weightValue;
    }

}
