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

import com.neuralnetwork.shared.links.ILink;
import com.neuralnetwork.shared.network.INeuralNetContext;
import com.neuralnetwork.shared.values.DoubleValue;
import com.neuralnetwork.shared.values.ErrorValue;
import com.neuralnetwork.shared.values.RandomValue;

/**
 * Implementation of an Input neuron.
 * @author fredladeroute
 *
 */
public class InputNeuron extends AbstractInputNeuron {
    
    /**
     * Constructs a new input value with value v.
     * 
     * @param v
     *      the initial input value
     */
    public InputNeuron(final DoubleValue v) {
        super(v);
    }
    
    /**
     * Constructs a new input value with value
     * RandomValue.
     */
    public InputNeuron() {
        super();
        setValue(new RandomValue());
    }
    
    @Override
    public final ErrorValue feedforward(final DoubleValue v,
    		final INeuralNetContext nnctx) {
        return feedforward(nnctx);
    }
    
    @Override
    public final ErrorValue feedforward(final INeuralNetContext nnctx) {
    	ErrorValue v = ErrorValue.ZERO;
    	for (ILink ol : getOutputs()) { //TODO FIX ME, Null at train time.
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
		ILink[] inWeights = getInputLinks();
		double sumErr = 0;
		for (int i = 0; i < inWeights.length; i++) {
			double w = inWeights[i].getWeight().getValue();
			sumErr += (1.0 / 2) * Math.pow(Math.abs(
					w - getValue().getValue()), 2);
		}
		return sumErr;
	}
}
