package com.neuralnetwork.opencl.node;

import com.neuralnetwork.shared.links.ILink;
import com.neuralnetwork.shared.nodes.INode;

/**
 * OpenCL node.
 * 
 * @author fredladeroute
 *
 */
public abstract class OpenCLNode implements INode {

    public abstract ILink addLink(INode inode, double weight);

    public ILink addLink(INode... inode) {
        
        return null;
    }

}
