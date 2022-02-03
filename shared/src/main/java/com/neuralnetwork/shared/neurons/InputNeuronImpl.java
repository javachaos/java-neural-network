/*******************************************************************************
 * Copyright (c) 2014 Fred Laderoute.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *     Fred Laderoute - initial API and implementation
 ******************************************************************************/
package com.neuralnetwork.shared.neurons;

import com.neuralnetwork.shared.links.Link;
import com.neuralnetwork.shared.network.NeuralNetContext;

/**
 * Implementation of an Input neuron.
 * @author fredladeroute
 *
 */
public class InputNeuronImpl extends AbstractInputNeuronImpl {
    
    /**
     * Constructs a new input value with value v.
     * 
     * @param v
     *      the initial input value
     */
    public InputNeuronImpl(final Double v) {
        super(v);
    }
    
    /**
     * Constructs a new input value with value
     * RandomValue.
     */
    public InputNeuronImpl() {
        super();
        setValue(Math.random());
    }
    
    @Override
    public final Double feedforward(final Double v,
    		final NeuralNetContext nnctx) {
        return feedforward(nnctx);
    }
    
    @Override
    public final Double feedforward(final NeuralNetContext nnctx) {
    	Double v = 0.0;
    	for (Link ol : getOutputs()) {
            v = ol.getTail().feedforward(getValue(), nnctx);
    	}
		return v;
    }
    
    @Override
    public final String toString() {
        return "IN(" + getValue() + ") ";
    }

	@Override
	public final Double propagateError(final Double e) {
		double error = e;
		error = getActivationFunction().derivative(error);
		return error;
	}

	@Override
	public final Double getError() {
		Link[] inWeights = getInputLinks();
		double sumErr = 0;
		for (Link inWeight : inWeights) {
			double w = inWeight.getWeight();
			sumErr += (1.0 / 2) * Math.pow(Math.abs(
					w - getValue()), 2);
		}
		return sumErr;
	}
}
