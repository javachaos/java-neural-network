package com.neuralnetwork.shared.nodes;

import com.neuralnetwork.shared.links.ILink;

/**
 * INode interface for the Nodes of the network.
 * 
 * @author fredladeroute
 *
 */
public interface INode {
    
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
    ILink addLink(INode inode, double weight);
    
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
    ILink addLink(INode inode);
    
    /**
     * Get a ILink by the id of the other INode.
     * 
     * @param linkId 
     *      the id of the node to get the link of.
     *      
     * @return the ILink NullLink if the link is null.
     */
    ILink getLink(int linkId);
    
    /**
     * Get ILinks by the id of the other INode ids.
     * 
     * @param ids 
     *      the ids of the nodes to get the links of.
     *      
     * @return the ILink.
     */
    ILink[] getLinks(int... ids);
    
    /**
     * Return the id of this INode.
     * 
     * @return
     *      the id of this INode
     */
    int getId();

}
