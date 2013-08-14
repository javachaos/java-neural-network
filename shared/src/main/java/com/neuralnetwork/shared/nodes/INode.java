package com.neuralnetwork.shared.nodes;

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
     * @param inode the node to connect to.
     * @param weight the weight value of this connection.
     * 
     * @return a new ILink.
     */
    ILink addLink(INode inode, double weight);
    
    /**
     * Adds a link from this node to inode
     * with a random weight value in range [0-1]
     * and return the ILink.
     * 
     * @param inode the node to connect to.
     * 
     * @return a new ILink.
     */
    ILink addLink(INode inode);

}
