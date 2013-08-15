package com.neuralnetwork.shared.nodes;

import java.util.List;

import com.neuralnetwork.shared.links.ILink;
import com.neuralnetwork.shared.links.Link;

/**
 * An abstract Node class implements the INode interface.
 * 
 * @author fredladeroute
 *
 */
public abstract class Node implements INode {

    /**
     * Array of all links connected to this node.
     */
    private List<ILink> links;
    
    /**
     * Current number of links attached to this node.
     */
    private int numLinks = -1;
    
    /**
     * The id of this Node.
     */
    private int id;
    
    /**
     * Construct a new Node.
     * 
     * @param nodeId
     *      the id of this Node
     */
    public Node(final int nodeId) {
        this.id = nodeId;
    }
    
    @Override
    public final ILink addLink(final INode inode, final double weight) {
        this.links.add(++numLinks, new Link(this, inode, weight));
        return links.get(numLinks);
    }

    @Override
    public final ILink addLink(final INode inode) {
        return addLink(inode, Math.random());
    }

    @Override
    public final ILink getLink(final int linkId) {
        return links.get(linkId);
    }

    @Override
    public final ILink[] getLinks(final int... ids) {
        ILink[] temp = new ILink[ids.length];
        int i = -1;
        for (int j : ids) {
            temp[++i] = getLink(j);
        }
        return temp;
    }
    
    @Override
    public final int getId() {
        return id;
    }

}
