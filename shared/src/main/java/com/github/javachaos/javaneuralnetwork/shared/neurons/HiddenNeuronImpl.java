/*******************************************************************************
 * Copyright (c) 2014 Fred .
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *     Fred  - initial API and implementation
 ******************************************************************************/
package com.github.javachaos.javaneuralnetwork.shared.neurons;

import com.github.javachaos.javaneuralnetwork.shared.links.Link;
import com.github.javachaos.javaneuralnetwork.shared.network.NeuralNetContext;

/**
 * Represents a hidden neuron.
 */
public class HiddenNeuronImpl extends NeuronImpl implements HiddenNeuron {

	private static final double LEARNING_FACTOR = 0.000016;

	public HiddenNeuronImpl() {
		super();
	}

    @Override
    public final String toString() {
        return "HN(" + getValue() + ") ";
    }

    @Override
    public final double feedforward(final double v,
    		final NeuralNetContext neuralNetContext) {
        double sum = 0.0;
        for (Link il : getInputs()) {
        	double val = il.getWeight();
            sum += val * v;
            il.setWeight(val + (v * LEARNING_FACTOR));
        }
        double n = getActivationFunction().activate(sum);
        setValue(n);
        double e = 0.0;
        for (Link ol : getOutputs()) {
            e += ol.getTail().feedforward(n, neuralNetContext);
        }
		return e;
    }

	@Override
	public final void propagateError(final double e) {
		Link[] links = getInputLinks();
		double error = e;
		error = getActivationFunction().derivative(error);
		for (Link il : links) {
			il.getHead().propagateError(error);
		}
    }

	@Override
	public final double getError() {
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
