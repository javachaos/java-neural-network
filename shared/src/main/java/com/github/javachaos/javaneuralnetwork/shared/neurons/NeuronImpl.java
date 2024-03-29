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
package com.github.javachaos.javaneuralnetwork.shared.neurons;

import java.util.*;

import com.github.javachaos.javaneuralnetwork.shared.functions.ActivationFunction;
import com.github.javachaos.javaneuralnetwork.shared.functions.SigmoidFunction;
import com.github.javachaos.javaneuralnetwork.shared.links.BasicLink;
import com.github.javachaos.javaneuralnetwork.shared.links.Link;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * An abstract Neuron class implements the INeuron interface.
 * 
 * 
 *
 */
public abstract class NeuronImpl implements Neuron {
    
    /**
     * Logger instance.
     */
    private static final Logger LOGGER = LogManager.getLogger(NeuronImpl.class);
    
    /**
     * The value for this neuron.
     */
    private double value;
    
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
    protected NeuronImpl(final NeuronType t, final double v) {
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
    public final void addInputLink(
            final Neuron inode, final double weight) {
        this.inputLinks.add(++numInputLinks, new BasicLink(this, inode, weight));
    }

    @Override
    public final void addInputLink(final Neuron inode) {
        addInputLink(inode, Math.random());
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
    public final void addOutputLink(
            final Neuron inode, final double weight) {
        Link l = new BasicLink(this, inode, weight);
        this.getOutputs().add(++numOutputLinks, l);
        inode.addInputLink(this, weight);
    }

    @Override
    public final void addOutputLink(final Neuron inode) {
        addOutputLink(inode, Math.random());
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
    public final void setInputLink(final int i, final Link l) {
        Link r = getInputLink(i);
        inputLinks.set(i, l);
    }

    @Override
    public final void setInputLinks(final Link[] links) {
        Link[] oldLinks = inputLinks.toArray(new Link[0]);
        inputLinks.clear();
        Collection<Link> c = Arrays.asList(links);
        inputLinks.addAll(c);
    }

    @Override
    public final void setOutputLink(final int i, final Link l) {
        Link r = getOutputLink(i);
        getOutputs().set(i, l);
    }

    @Override
    public final void setOutputLinks(final Link[] links) {
        Link[] oldLinks = getOutputs().toArray(new Link[0]);
        getOutputs().clear();
        Collection<Link> c = Arrays.asList(links);
        getOutputs().addAll(c);
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
    public final double getValue() {
        return value;
    }

    @Override
    public final void setValue(final double v) {
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
