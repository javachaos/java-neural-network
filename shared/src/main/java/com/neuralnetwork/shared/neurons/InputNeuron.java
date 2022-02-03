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

import com.neuralnetwork.shared.network.NeuralNetContext;

/**
 * Represents an input neuron.
 * 
 * @author fredladeroute
 *
 */
public interface InputNeuron extends Neuron {

    /**
     * Cause this neuron to activate,
     * sum up all the inputs * weights, and then
     * run them through the IActivationFunction finally
     * update the weights of the output links.
     * 
     * @param nnctx 
     * 		the neural network context to be passed along.
     * @return 
     * 		ErrorValue
     */
	Double feedforward(NeuralNetContext nnctx);
}
