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
 * Represents a hidden neuron.
 * @author fredladeroute
 *
 */
public class HiddenNeuronImpl extends NeuronImpl implements HiddenNeuron {
    
	/**
	 * Learning Factor.
	 */
	private static final double LEARNING_FACTOR = 0.000016;

	/**
	 * Create a new hidden neuron.
	 */
	public HiddenNeuronImpl() {
		super();
	}

    @Override
    public final String toString() {
        return "HN(" + getValue() + ") ";
    }

    @Override
    public final Double feedforward(final Double v,
    		final NeuralNetContext nnctx) {
        
        double sum = 0.0;
        for (Link il : getInputs()) {
        	double val = il.getWeight();
            sum += val * v;
            il.setWeight(val + (v * LEARNING_FACTOR));
        }
        Double n = getActivationFunction().activate(sum);
        setValue(n);
        double e = 0.0;
        for (Link ol : getOutputs()) {
            e += ol.getTail().feedforward(n, nnctx);
        }
		return e;
    }

	@Override
	public final Double propagateError(final Double e) {
		Link[] ilinks = getInputLinks();
		double error = e;
		error = getActivationFunction().derivative(error);
		for (Link il : ilinks) {
			il.getHead().propagateError(error);
		}
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
