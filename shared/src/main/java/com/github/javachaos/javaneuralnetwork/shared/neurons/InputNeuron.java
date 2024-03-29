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
 * Represents an input neuron.
 *
 */
public interface InputNeuron extends Neuron {

    /**
     * Cause this neuron to activate,
     * sum up all the inputs * weights, and then
     * run them through the IActivationFunction finally
     * update the weights of the output links.
     * 
     * @param neuralNetContext
     * 		the neural network context to be passed along.
     * @return 
     * 		ErrorValue
     */
	double feedforward(NeuralNetContext neuralNetContext);
}
