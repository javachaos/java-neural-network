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
package com.neuralnetwork.shared.layers;

import java.util.Vector;

import com.neuralnetwork.shared.neurons.IOutputNeuron;

/**
 * Output layer.
 * 
 * @author fredladeroute
 *
 */
public interface IOutputLayer extends ILayer<IOutputNeuron>, IBuildable {
    
    /**
     * Return the output values of the network.
     * @return
     *      the output values of the network
     */
    Vector<Double> getOutputValues();
}
