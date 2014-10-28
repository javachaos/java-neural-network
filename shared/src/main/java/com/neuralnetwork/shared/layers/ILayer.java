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

import com.neuralnetwork.shared.network.LayerType;

/**
 * Represents a Layer class.
 * 
 * @author fredladeroute
 *
 *@param <T>
 *      the type of Layer this 
 */
public interface ILayer<T> extends Iterable<T> {
    
    /**
     * Get a node from this layer.
     * 
     * @param idx
     *      the index to the node
     *
     * @return
     *      the INode at index idx
     */
    T getNeuron(int idx);
    
    /**
     * Returns the layer type of this ILayer.
     * 
     * @return
     *      the layer type of this ILayer
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
