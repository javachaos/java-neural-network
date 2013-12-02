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
package com.neuralnetwork.shared.nodes;

import java.util.ArrayList;
import java.util.List;

import com.neuralnetwork.shared.functions.IActivationFunction;
import com.neuralnetwork.shared.functions.SigmoidFunction;
import com.neuralnetwork.shared.links.ILink;
import com.neuralnetwork.shared.links.Link;
import com.neuralnetwork.shared.values.IValue;
import com.neuralnetwork.shared.values.RandomValue;

/**
 * An abstract Neuron class implements the INeuron interface.
 * 
 * @author fredladeroute
 *
 */
public class Neuron implements INeuron {
    
    /**
     * The activation function for this INeuron.
     * 
     * Default: SigmoidFunction
     */
    private IActivationFunction function = new SigmoidFunction();
    
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
     * 
     * @param nodeId
     *      the type of this Neuron
     *  
     */
    public Neuron(final NeuronType nodeId) {
        this.type = nodeId;
    }
    
    /**
     * Construct a new hidden Neuron.
     */
    public Neuron() {
        this.type = NeuronType.HIDDEN;
    }
    
    @Override
    public final ILink addInputLink(
            final INeuron inode, final IValue<?> weight) {
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
        
        ILink[] t = null;
        return temp.toArray(t);
    }
    
    @Override
    public final NeuronType getId() {
        return type;
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
            final INeuron inode, final IValue<?> weight) {
        this.outputLinks.add(++numOutputLinks, new Link(this, inode, weight));
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
        
        ILink[] t = null;
        return temp.toArray(t);
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

}
