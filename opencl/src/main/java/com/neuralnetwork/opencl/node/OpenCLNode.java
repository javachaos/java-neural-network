/*******************************************************************************
 * Copyright (c) 2013 Fred Laderoute.
 * All rights reserved. This program and the accompanying
 * materials are made available under the terms of the GNU
 * Public License v3.0 which accompanies this distribution,
 * and is available at http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *      Fred Laderoute - initial API and implementation
 ******************************************************************************/
package com.neuralnetwork.opencl.node;

import com.neuralnetwork.shared.network.INeuralNetContext;
import com.neuralnetwork.shared.neurons.INeuron;
import com.neuralnetwork.shared.neurons.Neuron;
import com.neuralnetwork.shared.neurons.NeuronType;

/**
 * OpenCL node.
 *
 * @author fredladeroute
 *
 */
public class OpenCLNode extends Neuron implements INeuron {

    /**
     * Construct a new OpenCL Node.
     *
     * @param nodeId
     *      the id of this node.
     */
    public OpenCLNode(final NeuronType nodeId) {
        super(nodeId, 0.0);
    }

    @Override
    public final Double feedforward(final Double v,
            final INeuralNetContext nnctx) {
           return null;
        //TODO improve.
    }

    @Override
    public final Double getError() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public final Double propagateError(final Double e) {
        // TODO Auto-generated method stub
        return null;
    }
}
