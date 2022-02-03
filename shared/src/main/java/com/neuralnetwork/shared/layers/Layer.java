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
package com.neuralnetwork.shared.layers;

import com.neuralnetwork.shared.network.LayerType;
import com.neuralnetwork.shared.neurons.Neuron;

/**
 * Represents a Layer of Neurons.
 * 
 *@param <T>
 *      the type of this Layer
 */
public interface Layer<T extends Neuron> extends Iterable<T> {
    
    /**
     * Get a neuron from this layer.
     * 
     * @param idx
     *      the index to the node
     *
     * @return
     *      the neuron at index idx
     */
    T getNeuron(int idx);
    
    /**
     * Returns the layer type of this Layer.
     * 
     * @return
     *      the layer type of this Layer
     */
    LayerType getLayerType();
    
    /**
     * Returns the size of this layer.
     * (Without bias neuron) so [size() - 1]
     * such that size() is the true size of the layer including
     * the bias term.
     * 
     * @return
     * 		the number of neurons in this layer.
     */
    int getSize();
    
}
