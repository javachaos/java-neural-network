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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neuralnetwork.shared.functions.IActivationFunction;
import com.neuralnetwork.shared.functions.SigmoidFunction;
import com.neuralnetwork.shared.links.ILink;
import com.neuralnetwork.shared.links.Link;
import com.neuralnetwork.shared.network.INeuralNetContext;

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
    private IActivationFunction function = new SigmoidFunction();
    
    /**
     * Array of all input links connected to this Neuron.
     */
    private Vector<ILink> inputLinks;
    
    /**
     * Array of all ouput links connected to this Neuron.
     */
    private Vector<ILink> outputLinks;
    
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
     * With it's value set randomly.
     * 
     * @param t
     *      the type of this Neuron
     * @param v
     *      the default value of the neuron
     *  
     */
    public Neuron(final NeuronType t, final Double v) {
        this.type = t;
        this.value = v;
        inputLinks = new Vector<ILink>();
        setOutputs(new Vector<ILink>());
    }
    
    /**
     * Construct a new hidden Neuron.
     * With it's value set randomly.
     */
    public Neuron() {
        this.type = NeuronType.HIDDEN;
        this.value = Math.random();
        inputLinks = new Vector<ILink>();
        setOutputs(new Vector<ILink>());
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
        ArrayList<ILink> temp = new ArrayList<ILink>();
        
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
        ArrayList<ILink> temp = new ArrayList<ILink>();
        
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
        ILink[] oldLinks = new ILink[inputLinks.size()];
        inputLinks.copyInto(oldLinks);
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
        ILink[] oldLinks = new ILink[getOutputs().size()];
        getOutputs().copyInto(oldLinks);
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
        this.function = f;
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
    public abstract Double feedforward(final Double v, 
    		final INeuralNetContext nnctx);

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public final int hashCode() {
		final int prime = 31;
		int result = 1;
		
		int functionValue = 0;
		if (function != null) {
		    functionValue = function.hashCode();
	    }
		
		int typeValue = 0;
		if (type != null) {
		    typeValue = type.hashCode();
		}
		
		int valueV = 0;
		if (value != null) {
		    valueV = value.hashCode();
		}
		result = prime * result
				+ functionValue;
		result = prime * result + idChildCounter;
		result = prime * result + idParentCounter;
		result = prime * result
				+ inputLinks.hashCode();
		result = prime * result + numInputLinks;
		result = prime * result + numOutputLinks;
		result = prime * result
				+ getOutputs().hashCode();
		result = prime * result + typeValue;
		result = prime * result + valueV;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public final boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Neuron)) {
			return false;
		}
		Neuron other = (Neuron) obj;
		if (function == null) {
			if (other.function != null) {
				return false;
			}
		} else if (!function.equals(other.function)) {
			return false;
		}
		if (idChildCounter != other.idChildCounter) {
			return false;
		}
		if (idParentCounter != other.idParentCounter) {
			return false;
		}
		if (!inputLinks.equals(other.inputLinks)) {
			return false;
		}
		if (!getOutputs().equals(other.getOutputs())) {
			return false;
		}
		if (type != other.type) {
			return false;
		}
		if (value == null) {
            return other.value == null;
		} else return value.equals(other.value);
    }

    /**
     * @return the inputLinks
     */
    public final Vector<ILink> getInputs() {
        return inputLinks;
    }

    /**
     * @param inputLink
     *      the inputLinks to set
     */
    public final void setInputs(final Vector<ILink> inputLink) {
    	if (inputLink == null || inputLink.isEmpty()) {
    		LOGGER.warn("Input link array is empty or null.");
    	}
        this.inputLinks = inputLink;
    }

    /**
     * @return the outputLinks
     */
    public final Vector<ILink> getOutputs() {
        return outputLinks;
    }

    /**
     * @param outputLink
     *      the outputLinks to set
     */
    public final void setOutputs(final Vector<ILink> outputLink) {
        this.outputLinks = outputLink;
    }
    
	@Override
	public abstract Double getError();

	@Override
	public abstract Double propagateError(Double e);

}
