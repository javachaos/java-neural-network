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

import com.neuralnetwork.shared.links.ILink;
import com.neuralnetwork.shared.links.Link;
import com.neuralnetwork.shared.values.IValue;
import com.neuralnetwork.shared.values.RandomValue;

/**
 * An abstract Neuron class implements the INeuron interface.
 * 
 * @author fredladeroute
 *
 *@param <T>
 *      the type of this Neuron.
 */
public abstract class Neuron<T extends Number> implements INeuron<T> {

    /**
     * Array of all input links connected to this Neuron.
     */
    private List<ILink<T>> inputLinks;
    
    /**
     * Array of all ouput links connected to this Neuron.
     */
    private List<ILink<T>> outputLinks;
    
    /**
     * Current number of input links attached to this Neuron.
     */
    private int numInputLinks = -1;
    
    /**
     * Current number of output links attached to this Neuron.
     */
    private int numOutputLinks = -1;
    
    /**
     * The id of this Neuron.
     */
    private int id;

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
     *      the id of this Neuron
     */
    public Neuron(final int nodeId) {
        this.id = nodeId;
    }
    
    @Override
    public final ILink<T> addInputLink(
            final INeuron<T> inode, final IValue<?> weight) {
        this.inputLinks.add(++numInputLinks, new Link<T>(this, inode, weight));
        return inputLinks.get(numInputLinks);
    }

    @Override
    public final ILink<T> addInputLink(final INeuron<T> inode) {
        return addInputLink(inode, new RandomValue());
    }

    @Override
    public final ILink<T> getInputLink(final int linkId) {
        return inputLinks.get(linkId);
    }

    @Override
    public final ILink<T>[] getInputLinks(final int... ids) {
        ArrayList<ILink<T>> temp = new ArrayList<ILink<T>>();
        
        for (int j : ids) {
            temp.add(getInputLink(j));
        }
        
        ILink<T>[] t = null;
        return temp.toArray(t);
    }
    
    @Override
    public final int getId() {
        return id;
    }
    
    @Override
    public final INeuron<T> getNextParent() {
        return getInputLink(++idParentCounter).getTail();
    }
    
    @Override
    public final INeuron<T> getNextChild() {
        return getOutputLink(++idChildCounter).getTail();
    }

    @Override
    public final ILink<T> addOutputLink(
            final INeuron<T> inode, final IValue<?> weight) {
        this.outputLinks.add(++numOutputLinks, new Link<T>(this, inode, weight));
        return outputLinks.get(numOutputLinks);
    }

    @Override
    public final ILink<T> addOutputLink(final INeuron<T> inode) {
        return addOutputLink(inode, new RandomValue());
    }

    @Override
    public final ILink<T> getOutputLink(final int linkId) {
        return outputLinks.get(linkId);
    }

    @Override
    public final ILink<T>[] getOutputLinks(final int... ids) {
        ArrayList<ILink<T>> temp = new ArrayList<ILink<T>>();
        
        for (int j : ids) {
            temp.add(getOutputLink(j));
        }
        
        ILink<T>[] t = null;
        return temp.toArray(t);
    }

}
