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

import java.util.Vector;

import com.neuralnetwork.shared.links.ILink;
import com.neuralnetwork.shared.values.DoubleValue;


/**
 * Represents a hidden neuron.
 * @author fredladeroute
 *
 */
public class HiddenNeuron extends Neuron implements IHiddenNeuron {
    
	/**
	 * Create a new hidden neuron.
	 */
	public HiddenNeuron() {
		super();
	}

    @Override
    public final String toString() {
        return "HN(" + getValue() + ") ";
    }

    @Override
    public final void feedforward(final DoubleValue v) {
        
        double sum = 0.0;
        for (ILink il : getInputs()) {
            sum += il.getWeight().getValue() * v.getValue();
        }
        DoubleValue n = new DoubleValue(getActivationFunction().activate(sum));
        setValue(n);
        Vector<ILink> o = getOutputs();
        for (int i = 0; i < o.size(); i++) {
            ILink l = o.get(i);
            l.getTail().feedforward(n);
        }
    }

	@Override
	public final Double propagateError(final Double e) {
		ILink[] ilinks = getInputLinks();
		double error = e;
		error = getActivationFunction().derivative(error);
		for (ILink il : ilinks) {
			il.getHead().propagateError(error);
		}
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
