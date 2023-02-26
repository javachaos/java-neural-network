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

import com.github.javachaos.javaneuralnetwork.shared.network.NeuralNetContext;

/**
 * Represents a bias neuron whose value is always 1.
 * But the weights can vary.
 */
public class BiasNeuron extends NeuronImpl
        implements InputNeuron, HiddenNeuron, OutputNeuron {

    public BiasNeuron() {
        super(NeuronType.BIAS, 1.0);
    }

    @Override
	public final double feedforward(final NeuralNetContext neuralNetContext) {
		return 0.0;        
    }

    @Override
	public final double feedforward(final double v,
    		final NeuralNetContext neuralNetContext) {
	    return 0.0;
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
	public final double getError() {
		//Bias has no input
		return 0.0;
	}

	@Override
	public final void propagateError(final double e) {
    }

}
