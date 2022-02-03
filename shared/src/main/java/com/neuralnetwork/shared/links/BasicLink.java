/*******************************************************************************
 * Copyright (c) 2014 Fred Laderoute.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *     Fred Laderoute - initial API and implementation
 ******************************************************************************/
package com.neuralnetwork.shared.links;

import com.neuralnetwork.shared.exceptions.NeuronLinkException;
import com.neuralnetwork.shared.neurons.Neuron;

import java.util.Objects;

/**
 * A basic link which connects two INeurons.
 *
 */
public class BasicLink implements Link {

    private Neuron head;
    private Neuron tail;
    
    /**
     * The number of times this link is
     * updated. During training if the age of a link
     * is less than 3 for 100 training samples the
     * link dies.
     */
    private int age;
    private Double weight;
    private final Double id;

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
    public BasicLink(
            final Neuron ihead,
            final Neuron itail,
            final Double linkWeight) {
        if (ihead == null) {
        	throw new NeuronLinkException("Head link was null.");
        } else if (itail == null) {
        	throw new NeuronLinkException("Tail link was null.");
        }
        this.weight = linkWeight;
        this.id = linkWeight;
        if (this.weight == null) {
        	this.weight = 0.0;
        }
        this.head = ihead;
        this.tail = itail;
    }

    @Override
    public final Neuron getHead() {
        return head;
    }

    @Override
    public final Neuron getTail() {
        return tail;
    }

    @Override
    public final Double getWeight() {
        return weight;
    }
    
    @Override
    public final void setHead(final Neuron ihead) {
        if (ihead == null) {
            throw new NeuronLinkException("Error cannot set null head.");
        } else {
            this.head = ihead;
        }
    }

    @Override
    public final void setTail(final Neuron itail) {
        if (itail == null) {
            throw new NeuronLinkException("Error cannot set null tail.");
        } else {
            this.tail = itail;
        }
    }

    @Override
    public final void updateWeight(final Double value) {
        age++;
        this.weight += value;
    }

    @Override
    public final void setWeight(final Double weightValue) {
        age = 0;
        this.weight = weightValue;
    }

    @Override
    public final int getAge() {
        return age;
    }

	@Override
	public int hashCode() {
		return Objects.hash(age, id, weight);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BasicLink other = (BasicLink) obj;
		return age == other.age && Objects.equals(id, other.id) && Objects.equals(weight, other.weight);
	}
    
}
