/*******************************************************************************
 * Copyright (c) 2014 Fred .
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *     Fred  - initial API and implementation
 ******************************************************************************/
package com.github.javachaos.javaneuralnetwork.shared.links;

import com.github.javachaos.javaneuralnetwork.shared.exceptions.NeuronLinkException;
import com.github.javachaos.javaneuralnetwork.shared.neurons.Neuron;

import java.util.Objects;

/**
 * A basic link which connects two neurons.
 *
 */
public class BasicLink implements Link {

    private Neuron head;
    private Neuron tail;
    
    /**
     * The number of times this link is
     * updated. During training if the age of a link
     * is greater than 10000 the link dies.
     * (loses its previous weight value a sort of amnesia factor)
     */
    private int age;
    private double weight;
    private final double id;

    /**
     * Construct a new link with head, tail and weight.
     * 
     * @param head
     *      the head of this link.
     *      
     * @param tail
     *      the tail of this link.
     *      
     * @param linkWeight
     *      the weight of this link [0-1].
     */
    public BasicLink(
            final Neuron head,
            final Neuron tail,
            final double linkWeight) {
        if (head == null) {
        	throw new NeuronLinkException("Head link was null.");
        } else if (tail == null) {
        	throw new NeuronLinkException("Tail link was null.");
        }
        this.weight = linkWeight;
        this.id = linkWeight;
        this.head = head;
        this.tail = tail;
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
    public final double getWeight() {
        return weight;
    }
    
    @Override
    public final void setHead(final Neuron head) {
        if (head == null) {
            throw new NeuronLinkException("Error cannot set null head.");
        } else {
            this.head = head;
        }
    }

    @Override
    public final void setTail(final Neuron tail) {
        if (tail == null) {
            throw new NeuronLinkException("Error cannot set null tail.");
        } else {
            this.tail = tail;
        }
    }

    @Override
    public final void updateWeight(final double value) {
        age++;
        if (age > 10000) {
            this.weight = value;
        } else {
            this.weight += value;
        }
    }

    @Override
    public final void setWeight(final double weightValue) {
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
