package com.neuralnetwork.opencl.node;

import com.neuralnetwork.shared.links.ILink;
import com.neuralnetwork.shared.nodes.INeuron;
import com.neuralnetwork.shared.nodes.Neuron;

/**
 * OpenCL node.
 * 
 * @author fredladeroute
 *
 */
public class OpenCLNode extends Neuron<Double> implements INeuron<Double> {

    /**
     * Construct a new OpenCL Node.
     * 
     * @param nodeId
     *      the id of this node.
     */
    public OpenCLNode(int nodeId) {
        super(nodeId);
    }

    public ILink addLink(INeuron... inode) {
        
        return null;
    }



}
