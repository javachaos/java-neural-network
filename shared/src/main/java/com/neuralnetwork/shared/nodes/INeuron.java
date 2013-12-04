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

import com.neuralnetwork.shared.functions.IActivationFunction;
import com.neuralnetwork.shared.links.ILink;
import com.neuralnetwork.shared.values.IValue;

/**
 * INode interface for the Nodes of the network.
 * 
 * @author fredladeroute
 *
 *
 */
public interface INeuron {
    
    /**
     * Get the value that this neuron holds.
     * 
     * @return
     *      the value that this neuron holds
     */
    IValue<?> getValue();
    
    /**
     * Set the value that this neuron will hold.
     * 
     * @param v
     *      the value to set for this neuron
     */
    void setValue(IValue<?> v);
    
    /**
     * Adds a link from this node to inode
     * with weight weight and return the ILink.
     * 
     * @param inode 
     *      the node to connect to.
     *      
     * @param weight 
     *      the weight value of this connection.
     * 
     * @return a new ILink.
     */
    ILink addInputLink(INeuron inode, IValue<?> weight);
    
    /**
     * Adds a link from this node to inode
     * with a random weight (uniformly distributed) value in range [0-1]
     * and return the ILink.
     * 
     * @param inode
     *      the node to connect to.
     * 
     * @return a new ILink.
     */
    ILink addInputLink(INeuron inode);
    
    /**
     * Get a ILink by the id of the other INode.
     * 
     * @param linkId 
     *      the id of the node to get the link of.
     *      
     * @return the ILink
     */
    ILink getInputLink(int linkId);
    
    /**
     * Get ILinks by the id of the other INode ids.
     * 
     * @param ids 
     *      the ids of the nodes to get the links of.
     *      
     * @return the ILink.
     */
    ILink[] getInputLinks(int... ids);
    
    /**
     * Adds a link from this node to inode
     * with weight weight and return the ILink.
     * 
     * Also adds the input link for the inode neuron.
     * Thus created a full 2 way connection between
     * this neuron and inode.
     * 
     * @param inode 
     *      the node to connect to.
     *      
     * @param weight 
     *      the weight value of this connection.
     * 
     * @return a new ILink.
     */
    ILink addOutputLink(INeuron inode, IValue<?> weight);
    
    /**
     * Adds a link from this node to inode
     * with a random weight (uniformly distributed) value in range [0-1]
     * and return the ILink.
     * 
     * @param inode
     *      the node to connect to.
     * 
     * @return a new ILink.
     * 
     * @see #addOutputLink(INeuron, IValue)
     */
    ILink addOutputLink(INeuron inode);
    
    /**
     * Get a ILink by the id of the other INode.
     * 
     * @param linkId 
     *      the id of the node to get the link of.
     *      
     * @return the ILink
     */
    ILink getOutputLink(int linkId);
    
    /**
     * Get ILinks by the id of the other INode ids.
     * 
     * @param ids 
     *      the ids of the nodes to get the links of.
     *      
     * @return the ILink.
     */
    ILink[] getOutputLinks(int... ids);
    
    /**
     * Return the id of this INode.
     * 
     * @return
     *      the id of this INode
     */
    NeuronType getId();
    
    /**
     * Return the next child INode.
     * 
     * @return
     *      the next child INode 
     *      and increment the child id counter.
     */
    INeuron getNextChild();

    /**
     * Return the next parent INode.
     * 
     * @return
     *      the next parent INode 
     *      and increment the parent id counter
     */
    INeuron getNextParent();

    /**
     * Reset the weights of all OUTPUT weight values to
     * a uniform randomly distributed value between [0,1].
     */
    void reset();
    
    /**
     * Sets the activation function which this neuron
     * will use.
     * 
     * @param f
     *      the IActivationFunction to use
     */
    void setActivationFunction(IActivationFunction f);
    
    /**
     * Get the activation function for this INeuron.
     * 
     * @return
     *      the IActivationFunction of this INeuron
     */
    IActivationFunction getActivationFunction();

}
