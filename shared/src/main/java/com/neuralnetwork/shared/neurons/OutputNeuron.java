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

/**
 * Defines an output neuron.
 * 
 * @author fredladeroute
 *
 */
public final class OutputNeuron extends AbstractOutputNeuron {

    /**
     * Constructs a new output neuron.
     */
	public OutputNeuron() {
		super();
	}

    @Override
    public String toString() {
        return "ON(" + getValue() + ") ";
    }

    @Override
    public ErrorValue feedforward(final DoubleValue v,
    		final INeuralNetContext nnctx) {
        double sum = 0.0;
        for (ILink il : getInputs()) {
            sum += il.getWeight().getValue() * v.getValue();
        }
        DoubleValue n = new DoubleValue(getActivationFunction().activate(sum));
        setValue(n);
		return new ErrorValue(n.getValue());
    }

	@Override
	public Double propagateError(final Double e) {
		ILink[] links = getInputLinks();
		double error = e;
		error = getActivationFunction().derivative(error);
		for (ILink il : links) {
			il.getHead().propagateError(error);
		}
		return error;
	}

	@Override
	public Double getError() {
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
