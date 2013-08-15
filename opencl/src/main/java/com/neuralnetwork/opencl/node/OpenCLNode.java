package com.neuralnetwork.opencl.node;

import com.neuralnetwork.shared.links.ILink;
import com.neuralnetwork.shared.nodes.INode;
import com.neuralnetwork.shared.nodes.Node;

/**
 * OpenCL node.
 * 
 * @author fredladeroute
 *
 */
public class OpenCLNode extends Node implements INode {

    /**
     * Construct a new OpenCL Node.
     * 
     * @param nodeId
     *      the id of this node.
     */
    public OpenCLNode(int nodeId) {
        super(nodeId);
    }

    public ILink addLink(INode... inode) {
        
        return null;
    }

}
