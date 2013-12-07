/*******************************************************************************
 * Copyright (c) 2013 Fred Laderoute.
 * All rights reserved. This program and the accompanying
 * materials are made available under the terms of the GNU 
 * Public License v3.0 which accompanies this distribution, 
 * and is available at http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *      Fred Laderoute - initial API and implementation
 *******************************************************************************/
package com.neuralnetwork.shared.layers;

import com.neuralnetwork.shared.network.INeuralNetContext;
import com.neuralnetwork.shared.neurons.IHiddenNeuron;
import com.neuralnetwork.shared.neurons.IInputNeuron;

/**
 * Interface for an IHiddenLayer.
 * @author fredladeroute
 *
 */
public interface IHiddenLayer extends ILayer<IHiddenNeuron>, IBuildable {
    
    /**
     * Propagate the values from this IHiddenLayer to the
     * next ILayer.
     * 
     * @param nnctx
     *      neural net context parameter
     */
    void propagate(INeuralNetContext nnctx);

    /**
     * Return a reference to this HiddenLayers BiasNeuron.
     * @return
     *      a reference to this HiddenLayers BiasNeuron
     */
    IInputNeuron getBiasNeuron();
}
