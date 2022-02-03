/*******************************************************************************
 * Copyright (c) 2013 Fred .
 * All rights reserved. This program and the accompanying
 * materials are made available under the terms of the GNU
 * Public License v3.0 which accompanies this distribution,
 * and is available at http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *      Fred  - initial API and implementation
 ******************************************************************************/
package com.neuralnetwork.opencl.node;

import com.neuralnetwork.shared.network.NeuralNetContext;
import com.neuralnetwork.shared.neurons.Neuron;
import com.neuralnetwork.shared.neurons.NeuronImpl;
import com.neuralnetwork.shared.neurons.NeuronType;

/**
 * OpenCL node.
 *
 * 
 *
 */
public class OpenCLNode extends NeuronImpl implements Neuron {

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
    public final double feedforward(final double v,
            final NeuralNetContext neuralNetContext) {
           return 0.0;
    }

    @Override
    public final double getError() {
        return 0.0;
    }

    @Override
    public final void propagateError(final double e) {
    }
}
