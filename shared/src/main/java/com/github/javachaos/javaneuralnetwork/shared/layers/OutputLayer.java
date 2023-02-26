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
package com.github.javachaos.javaneuralnetwork.shared.layers;

import java.util.List;

import com.github.javachaos.javaneuralnetwork.shared.neurons.OutputNeuron;

/**
 * Output layer.
 * 
 * 
 *
 */
public interface OutputLayer extends Layer<OutputNeuron>, Buildable {
    
    /**
     * Return the output values of the network.
     * @return
     *      the output values of the network
     */
	List<Double> getOutputValues();
}
