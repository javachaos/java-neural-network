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
import com.neuralnetwork.shared.functions.IActivationFunction;
import com.neuralnetwork.shared.functions.SigmoidFunction;
import com.neuralnetwork.shared.links.ILink;
import com.neuralnetwork.shared.links.Link;

/**
 * An abstract Neuron class implements the INeuron interface.
 * 
 * @author fredladeroute
 *
 */
public abstract class Neuron implements INeuron {
    
    /**
     * Logger instance.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Neuron.class);
    
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
    private List<ILink> inputLinks;
    
    /**
     * Array of all ouput links connected to this Neuron.
     */
    private List<ILink> outputLinks;
    
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
    protected Neuron(final NeuronType t, final Double v) {
        this.type = t;
        this.value = v;
        inputLinks = new ArrayList<>();
        setOutputs(new ArrayList<>());
    }
    
    /**
     * Construct a new hidden Neuron.
     * With its value set randomly.
     */
    protected Neuron() {
        this.type = NeuronType.HIDDEN;
        this.value = Math.random();
        inputLinks = new ArrayList<>();
        setOutputs(new ArrayList<>());
    }
    
    @Override
    public final ILink addInputLink(
            final INeuron inode, final Double weight) {
        this.inputLinks.add(++numInputLinks, new Link(this, inode, weight));
        return inputLinks.get(numInputLinks);
    }

    @Override
    public final ILink addInputLink(final INeuron inode) {
        return addInputLink(inode, Math.random());
    }

    @Override
    public final ILink getInputLink(final int linkId) {
        return inputLinks.get(linkId);
    }

    @Override
    public final ILink[] getInputLinks(final int... ids) {
        ArrayList<ILink> temp = new ArrayList<>();
        
        for (int j : ids) {
            temp.add(getInputLink(j));
        }
        
        ILink[] t = new ILink[0];
        return temp.toArray(t);
    }
    
    @Override
    public final ILink[] getInputLinks() {
        ILink[] t = new ILink[0];
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
    public final INeuron getNextParent() {
        return getInputLink(++idParentCounter).getTail();
    }
    
    @Override
    public final INeuron getNextChild() {
        return getOutputLink(++idChildCounter).getTail();
    }

    @Override
    public final ILink addOutputLink(
            final INeuron inode, final Double weight) {
        ILink l = new Link(this, inode, weight);
        this.getOutputs().add(++numOutputLinks, l);
        inode.addInputLink(this, weight);
        return getOutputs().get(numOutputLinks);
    }

    @Override
    public final ILink addOutputLink(final INeuron inode) {
        return addOutputLink(inode, Math.random());
    }

    @Override
    public final ILink getOutputLink(final int linkId) {
        return getOutputs().get(linkId);
    }

    @Override
    public final ILink[] getOutputLinks(final int... ids) {
        ArrayList<ILink> temp = new ArrayList<>();
        
        for (int j : ids) {
            temp.add(getOutputLink(j));
        }
        
        ILink[] t = new ILink[0];
        return temp.toArray(t);
    }
    
    @Override
    public final ILink setInputLink(final int i, final ILink l) {
        ILink r = getInputLink(i);
        inputLinks.set(i, l);
        return r;
    }

    @Override
    public final ILink[] setInputLinks(final ILink[] links) {
        ILink[] oldLinks = inputLinks.toArray(new ILink[0]);
        inputLinks.clear();
        Collection<ILink> c = Arrays.asList(links);
        inputLinks.addAll(c);
        return oldLinks;
    }

    @Override
    public final ILink setOutputLink(final int i, final ILink l) {
        ILink r = getOutputLink(i);
        getOutputs().set(i, l);
        return r;
    }

    @Override
    public final ILink[] setOutputLinks(final ILink[] links) {
        ILink[] oldLinks = getOutputs().toArray(new ILink[0]);
        getOutputs().clear();
        Collection<ILink> c = Arrays.asList(links);
        getOutputs().addAll(c);
        return oldLinks;
    }
    
    @Override
    public final ILink[] getOutputLinks() {
        ILink[] t = new ILink[0];
        return getOutputs().toArray(t);
    }

    @Override
    public final void reset() {
        for (ILink i : getOutputs()) {
            i.setWeight(Math.random());
        }
    }

    @Override
    public final void setActivationFunction(final IActivationFunction f) {
        this.function = (SigmoidFunction) f;
    }

    @Override
    public final IActivationFunction getActivationFunction() {
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
		Neuron other = (Neuron) obj;
		return Objects.equals(function, other.function) && Objects.equals(inputLinks, other.inputLinks)
				&& numInputLinks == other.numInputLinks && numOutputLinks == other.numOutputLinks
				&& Objects.equals(outputLinks, other.outputLinks) && type == other.type
				&& Objects.equals(value, other.value);
	}

	/**
     * @return the inputLinks
     */
    public final List<ILink> getInputs() {
        return inputLinks;
    }

    /**
     * @param inputLink
     *      the inputLinks to set
     */
    public final void setInputs(final List<ILink> inputLink) {
    	if (inputLink == null || inputLink.isEmpty()) {
    		LOGGER.warn("Input link array is empty or null.");
    	}
        this.inputLinks = inputLink;
    }

    /**
     * @return the outputLinks
     */
    public final List<ILink> getOutputs() {
        return outputLinks;
    }

    /**
     * @param outputLink
     *      the outputLinks to set
     */
    public final void setOutputs(final List<ILink> outputLink) {
        this.outputLinks = outputLink;
    }
}
