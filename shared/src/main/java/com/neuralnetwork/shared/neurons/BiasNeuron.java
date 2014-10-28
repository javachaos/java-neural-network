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

import com.neuralnetwork.shared.values.DoubleValue;
import com.neuralnetwork.shared.values.OneValue;

/**
 * Represents a bias neuron who's value is always 1.
 * But the weights can vary.
 * 
 * @author fredladeroute
 *
 */
public class BiasNeuron extends Neuron 
        implements IInputNeuron, IHiddenNeuron, IOutputNeuron {

    /**
     * Constructs a new bias neuron.
     */
    public BiasNeuron() {
        super(NeuronType.BIAS, new OneValue());
    }

    @Override
    public void feedforward() {        
    }

    @Override
    public void feedforward(final DoubleValue v) {
    }

    @Override
    public final double getOutputValue() {
        return 1;
    }
    
    @Override
    public final String toString() {
        return "BN(1.0)";
    }

	@Override
	public final Double getError() {
		//Bias has no input
		return 0.0;
	}

	@Override
	public final Double propagateError(final Double e) {
		return 0.0;
	}

}
