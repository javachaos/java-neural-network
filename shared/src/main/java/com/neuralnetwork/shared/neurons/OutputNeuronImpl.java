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
 * Defines an output neuron.
 * 
 * @author fredladeroute
 *
 */
public final class OutputNeuronImpl extends AbstractOutputNeuron {

	public OutputNeuronImpl() {
		super();
	}

    @Override
    public String toString() {
        return "ON(" + getValue() + ") ";
    }

    @Override
    public Double feedforward(final Double v,
    		final NeuralNetContext nnctx) {
        double sum = 0.0;
        for (Link il : getInputs()) {
            sum += il.getWeight() * v;
        }
        Double n = getActivationFunction().activate(sum);
        setValue(n);
		return n;
    }

	@Override
	public Double propagateError(final Double e) {
		Link[] links = getInputLinks();
		double error = e;
		error = getActivationFunction().derivative(error);
		for (Link il : links) {
			il.getHead().propagateError(error);
		}
		return error;
	}

	@Override
	public Double getError() {
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
