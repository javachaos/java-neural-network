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
package com.neuralnetwork.shared.neurons;

import java.util.ArrayList;
import java.util.Vector;

import com.neuralnetwork.shared.functions.IActivationFunction;
import com.neuralnetwork.shared.functions.SigmoidFunction;
import com.neuralnetwork.shared.links.ILink;
import com.neuralnetwork.shared.links.Link;
import com.neuralnetwork.shared.values.DoubleValue;
import com.neuralnetwork.shared.values.RandomValue;

/**
 * An abstract Neuron class implements the INeuron interface.
 * 
 * @author fredladeroute
 *
 */
public abstract class Neuron implements INeuron {
    
    /**
     * The value for this neuron.
     */
    private DoubleValue value;
    
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
     * @param nodeId
     *      the type of this Neuron
     *  
     */
    public Neuron(final NeuronType nodeId) {
        this.type = nodeId;
        this.value = new RandomValue();
        inputLinks = new Vector<ILink>();
        outputLinks = new Vector<ILink>();
    }
    
    /**
     * Construct a new hidden Neuron.
     * With it's value set randomly.
     */
    public Neuron() {
        this.type = NeuronType.HIDDEN;
        this.value = new RandomValue();
        inputLinks = new Vector<ILink>();
        outputLinks = new Vector<ILink>();
    }
    
    @Override
    public final ILink addInputLink(
            final INeuron inode, final DoubleValue weight) {
        this.inputLinks.add(++numInputLinks, new Link(this, inode, weight));
        return inputLinks.get(numInputLinks);
    }

    @Override
    public final ILink addInputLink(final INeuron inode) {
        return addInputLink(inode, new RandomValue());
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
            final INeuron inode, final DoubleValue weight) {
        this.outputLinks.add(++numOutputLinks, new Link(this, inode, weight));
        inode.addInputLink(this);
        return outputLinks.get(numOutputLinks);
    }

    @Override
    public final ILink addOutputLink(final INeuron inode) {
        return addOutputLink(inode, new RandomValue());
    }

    @Override
    public final ILink getOutputLink(final int linkId) {
        return outputLinks.get(linkId);
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
    public final ILink[] getOutputLinks() {
        ILink[] t = new ILink[0];
        return outputLinks.toArray(t);
    }

    @Override
    public final void reset() {
        for (ILink i : outputLinks) {
            i.setWeight(new RandomValue());
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
    public final DoubleValue getValue() {
        return value;
    }

    @Override
    public final void setValue(final DoubleValue v) {
        this.value = v;
    }

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((function == null) ? 0 : function.hashCode());
		result = prime * result + idChildCounter;
		result = prime * result + idParentCounter;
		result = prime * result
				+ inputLinks.hashCode();
		result = prime * result + numInputLinks;
		result = prime * result + numOutputLinks;
		result = prime * result
				+ outputLinks.hashCode();
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
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
		if (!outputLinks.equals(other.outputLinks)) {
			return false;
		}
		if (type != other.type) {
			return false;
		}
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}

}
