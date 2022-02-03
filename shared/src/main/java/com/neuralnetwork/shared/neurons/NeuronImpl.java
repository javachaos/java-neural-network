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
package com.neuralnetwork.shared.neurons;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.neuralnetwork.shared.functions.ActivationFunction;
import com.neuralnetwork.shared.functions.SigmoidFunction;
import com.neuralnetwork.shared.links.Link;
import com.neuralnetwork.shared.links.BasicLink;

/**
 * An abstract Neuron class implements the INeuron interface.
 * 
 * @author fredladeroute
 *
 */
public abstract class NeuronImpl implements Neuron {
    
    /**
     * Logger instance.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(NeuronImpl.class);
    
    /**
     * The value for this neuron.
     */
    private Double value;
    
    /**
     * The activation function for this INeuron.
     * 
     * Default: SigmoidFunction
     */
    private SigmoidFunction function = new SigmoidFunction();
    
    /**
     * Array of all input links connected to this Neuron.
     */
    private List<Link> inputLinks;
    
    /**
     * Array of all ouput links connected to this Neuron.
     */
    private List<Link> outputLinks;
    
    /**
     * Current number of input links attached to this Neuron.
     */
    private int numInputLinks = -1;
    
    /**
     * Current number of output links attached to this Neuron.
     */
    private int numOutputLinks = -1;
    
    /**
     * The type of this Neuron.
     */
    private NeuronType type;

    /**
     * The counter used in the getNextParent method for
     * keeping track of which parent neuron to get.
     */
    private int idParentCounter = -1;

    /**
     * The counter used in the getNextChild method
     * for keeping track of which child neuron to get.
     */
    private int idChildCounter = -1;
    
    /**
     * Construct a new Neuron.
     * With its value set randomly.
     * 
     * @param t
     *      the type of this Neuron
     * @param v
     *      the default value of the neuron
     *  
     */
    protected NeuronImpl(final NeuronType t, final Double v) {
        this.type = t;
        this.value = v;
        inputLinks = new ArrayList<>();
        setOutputs(new ArrayList<>());
    }
    
    /**
     * Construct a new hidden Neuron.
     * With its value set randomly.
     */
    protected NeuronImpl() {
        this.type = NeuronType.HIDDEN;
        this.value = Math.random();
        inputLinks = new ArrayList<>();
        setOutputs(new ArrayList<>());
    }
    
    @Override
    public final Link addInputLink(
            final Neuron inode, final Double weight) {
        this.inputLinks.add(++numInputLinks, new BasicLink(this, inode, weight));
        return inputLinks.get(numInputLinks);
    }

    @Override
    public final Link addInputLink(final Neuron inode) {
        return addInputLink(inode, Math.random());
    }

    @Override
    public final Link getInputLink(final int linkId) {
        return inputLinks.get(linkId);
    }

    @Override
    public final Link[] getInputLinks(final int... ids) {
        ArrayList<Link> temp = new ArrayList<>();
        
        for (int j : ids) {
            temp.add(getInputLink(j));
        }
        
        Link[] t = new Link[0];
        return temp.toArray(t);
    }
    
    @Override
    public final Link[] getInputLinks() {
        Link[] t = new Link[0];
        return inputLinks.toArray(t);
    }
    
    @Override
    public final NeuronType getType() {
        return type;
    }
    
    @Override
    public final void setType(final NeuronType t) {
        this.type = t;
    }
    
    @Override
    public final Neuron getNextParent() {
        return getInputLink(++idParentCounter).getTail();
    }
    
    @Override
    public final Neuron getNextChild() {
        return getOutputLink(++idChildCounter).getTail();
    }

    @Override
    public final Link addOutputLink(
            final Neuron inode, final Double weight) {
        Link l = new BasicLink(this, inode, weight);
        this.getOutputs().add(++numOutputLinks, l);
        inode.addInputLink(this, weight);
        return getOutputs().get(numOutputLinks);
    }

    @Override
    public final Link addOutputLink(final Neuron inode) {
        return addOutputLink(inode, Math.random());
    }

    @Override
    public final Link getOutputLink(final int linkId) {
        return getOutputs().get(linkId);
    }

    @Override
    public final Link[] getOutputLinks(final int... ids) {
        ArrayList<Link> temp = new ArrayList<>();
        
        for (int j : ids) {
            temp.add(getOutputLink(j));
        }
        
        Link[] t = new Link[0];
        return temp.toArray(t);
    }
    
    @Override
    public final Link setInputLink(final int i, final Link l) {
        Link r = getInputLink(i);
        inputLinks.set(i, l);
        return r;
    }

    @Override
    public final Link[] setInputLinks(final Link[] links) {
        Link[] oldLinks = inputLinks.toArray(new Link[0]);
        inputLinks.clear();
        Collection<Link> c = Arrays.asList(links);
        inputLinks.addAll(c);
        return oldLinks;
    }

    @Override
    public final Link setOutputLink(final int i, final Link l) {
        Link r = getOutputLink(i);
        getOutputs().set(i, l);
        return r;
    }

    @Override
    public final Link[] setOutputLinks(final Link[] links) {
        Link[] oldLinks = getOutputs().toArray(new Link[0]);
        getOutputs().clear();
        Collection<Link> c = Arrays.asList(links);
        getOutputs().addAll(c);
        return oldLinks;
    }
    
    @Override
    public final Link[] getOutputLinks() {
        Link[] t = new Link[0];
        return getOutputs().toArray(t);
    }

    @Override
    public final void reset() {
        for (Link i : getOutputs()) {
            i.setWeight(Math.random());
        }
    }

    @Override
    public final void setActivationFunction(final ActivationFunction f) {
        this.function = (SigmoidFunction) f;
    }

    @Override
    public final ActivationFunction getActivationFunction() {
        return function;
    }

    @Override
    public final Double getValue() {
        return value;
    }

    @Override
    public final void setValue(final Double v) {
        this.value = v;
    }

	@Override
	public int hashCode() {
		return Objects.hash(function, inputLinks, numInputLinks, numOutputLinks, outputLinks, type, value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NeuronImpl other = (NeuronImpl) obj;
		return Objects.equals(function, other.function) && Objects.equals(inputLinks, other.inputLinks)
				&& numInputLinks == other.numInputLinks && numOutputLinks == other.numOutputLinks
				&& Objects.equals(outputLinks, other.outputLinks) && type == other.type
				&& Objects.equals(value, other.value);
	}

	/**
     * @return the inputLinks
     */
    public final List<Link> getInputs() {
        return inputLinks;
    }

    /**
     * @param inputLink
     *      the inputLinks to set
     */
    public final void setInputs(final List<Link> inputLink) {
    	if (inputLink == null || inputLink.isEmpty()) {
    		LOGGER.warn("Input link array is empty or null.");
    	}
        this.inputLinks = inputLink;
    }

    /**
     * @return the outputLinks
     */
    public final List<Link> getOutputs() {
        return outputLinks;
    }

    /**
     * @param outputLink
     *      the outputLinks to set
     */
    public final void setOutputs(final List<Link> outputLink) {
        this.outputLinks = outputLink;
    }
}
