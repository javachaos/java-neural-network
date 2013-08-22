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
import com.neuralnetwork.shared.nodes.IInputNeuron;
import com.neuralnetwork.shared.values.IValue;

/**
 * Represents an InputLayer class.
 * @author fredladeroute
 *
 */
public interface IInputLayer extends ILayer<IInputNeuron>, IBuildable {

    /**
     * Add a value to this IInputLayer.
     * 
     * @param v
     *      the value to add to the input layer
     *      
     * @param index
     *      which INode to insert the IValue at
     */
    void addValue(IValue<?> v, int index);
    
    /**
     * Propagate the values from this IInputLayer to the
     * next ILayer.
     * 
     * @param nnctx
     *      neural net context parameter
     */
    void propagate(INeuralNetContext nnctx);

    /**
     * Build the input layer using INeurons.
     */
    void build();
}
