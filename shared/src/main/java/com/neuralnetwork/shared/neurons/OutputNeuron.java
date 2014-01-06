/*******************************************************************************
 * Copyright (c) 2014 Fred Laderoute.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *     Fred Laderoute - initial API and implementation
 *******************************************************************************/
package com.neuralnetwork.shared.neurons;

import com.neuralnetwork.shared.links.ILink;
import com.neuralnetwork.shared.values.DoubleValue;



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
    public void feedforward(final DoubleValue v) {
        double sum = 0.0;
        for (ILink il : getInputs()) {
            sum += il.getWeight().getValue() * v.getValue();
        }
        DoubleValue n = new DoubleValue(getActivationFunction().activate(sum));
        
        setValue(n);
    }
}
