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

import com.neuralnetwork.shared.exceptions.NeuronLinkException;
import com.neuralnetwork.shared.nodes.INeuron;
import com.neuralnetwork.shared.values.IValue;
import com.neuralnetwork.shared.values.ZeroValue;

/**
 * A basic link which connects two INeurons.
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
     * The number of times this link is
     * updated. During training if the age of a link
     * is less than 3 for 100 training samples the
     * link dies.
     */
    private int age;

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

        if (ihead == null) {
        	throw new NeuronLinkException("Head link was null.");
        } else if (itail == null) {
        	throw new NeuronLinkException("Tail link was null.");
        }

        this.weight = linkWeight;
        
        if (this.weight == null) {
        	this.weight = new ZeroValue();
        }
        
        this.head = ihead;
        this.tail = itail;
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
            throw new NeuronLinkException("Error cannot set null head.");
        } else {
            this.head = ihead;
        }
    }

    @Override
    public final void setTail(final INeuron itail) {
        if (itail == null) {
            throw new NeuronLinkException("Error cannot set null tail.");
        } else {
            this.tail = itail;
        }
    }

    @Override
    public final void updateWeight(final IValue<?> value) {
        age++;
        this.weight.updateValue(value);
    }

    @Override
    public final void setWeight(final IValue<?> weightValue) {
        age = 0;
        this.weight = weightValue;
    }

    @Override
    public final int getAge() {
        return age;
    }

}
