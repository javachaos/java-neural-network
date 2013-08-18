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

import com.neuralnetwork.shared.links.ILink;
import com.neuralnetwork.shared.values.IValue;

/**
 * INode interface for the Nodes of the network.
 * 
 * @author fredladeroute
 * 
 * @param <T>
 *      the type of this neuron
 *
 */
public interface INeuron<T extends Number> {
    
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
    ILink<T> addInputLink(INeuron<T> inode, IValue<?> weight);
    
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
    ILink<T> addInputLink(INeuron<T> inode);
    
    /**
     * Get a ILink by the id of the other INode.
     * 
     * @param linkId 
     *      the id of the node to get the link of.
     *      
     * @return the ILink
     */
    ILink<T> getInputLink(int linkId);
    
    /**
     * Get ILinks by the id of the other INode ids.
     * 
     * @param ids 
     *      the ids of the nodes to get the links of.
     *      
     * @return the ILink.
     */
    ILink<T>[] getInputLinks(int... ids);
    
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
    ILink<T> addOutputLink(INeuron<T> inode, IValue<?> weight);
    
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
    ILink<T> addOutputLink(INeuron<T> inode);
    
    /**
     * Get a ILink by the id of the other INode.
     * 
     * @param linkId 
     *      the id of the node to get the link of.
     *      
     * @return the ILink
     */
    ILink<T> getOutputLink(int linkId);
    
    /**
     * Get ILinks by the id of the other INode ids.
     * 
     * @param ids 
     *      the ids of the nodes to get the links of.
     *      
     * @return the ILink.
     */
    ILink<T>[] getOutputLinks(int... ids);
    
    /**
     * Return the id of this INode.
     * 
     * @return
     *      the id of this INode
     */
    int getId();
    
    /**
     * Return the next child INode.
     * 
     * @return
     *      the next child INode 
     *      and increment the child id counter.
     */
    INeuron<T> getNextChild();

    /**
     * Return the next parent INode.
     * 
     * @return
     *      the next parent INode 
     *      and increment the parent id counter
     */
    INeuron<T> getNextParent();

}
