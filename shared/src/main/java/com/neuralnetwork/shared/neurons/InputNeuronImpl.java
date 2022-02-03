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
package com.neuralnetwork.shared.neurons;

import com.neuralnetwork.shared.links.Link;
import com.neuralnetwork.shared.network.NeuralNetContext;

/**
 * Implementation of an Input neuron.
 */
public class InputNeuronImpl extends AbstractInputNeuronImpl {
    
    /**
     * Constructs a new input value with value v.
     * 
     * @param v
     *      the initial input value
     */
    public InputNeuronImpl(final double v) {
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
    public final double feedforward(final double v,
    		final NeuralNetContext neuralNetContext) {
        return feedforward(neuralNetContext);
    }
    
    @Override
    public final double feedforward(final NeuralNetContext neuralNetContext) {
    	double v = 0.0;
    	for (Link ol : getOutputs()) {
            v = ol.getTail().feedforward(getValue(), neuralNetContext);
    	}
		return v;
    }
    
    @Override
    public final String toString() {
        return "IN(" + getValue() + ") ";
    }

	@Override
	public final void propagateError(final double e) {
		double error = e;
		error = getActivationFunction().derivative(error);
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
