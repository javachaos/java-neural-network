/*******************************************************************************
 * Copyright (c) 2014 Fred Laderoute.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *     Fred Laderoute - initial API and implementation
 *******************************************************************************/
package com.neuralnetwork.shared.layers;

import java.util.Vector;

import com.neuralnetwork.shared.network.LayerType;
import com.neuralnetwork.shared.neurons.INeuron;

/**
 * Represents a Layer of INeurons.
 * @author fredladeroute
 *
 *@param <T>
 *      the type of Layer this is
 */
public abstract class Layer<T extends INeuron> 
        extends Vector<T> implements ILayer<T> {

    /**
     * Generated Serial Version UID.
     */
    private static final long serialVersionUID = -94028484942015525L;
    
    /**
     * Represents the width of the layer.
     */
    private int width;
    
    /**
     * The layer type for this layer.
     */
    private LayerType layerType;
    
    /**
     * Construct a new Layer of width w.
     * 
     * @param w
     *      the width of the Layer to be created
     */
	public Layer(final int w) {
        super(w);
        this.width = w;
    }

    @Override
    public final T getNeuron(final int idx) {
        return get(idx);
    }

    @Override
    public final LayerType getLayerType() {
        return layerType;
    }

    /**
     * Set the layer type for this layer.
     * 
     * @param t
     *      the layerType to set
     */
    protected final void setLayerType(final LayerType t) {
        this.layerType = t;
    }

    /**
     * @return the width
     */
    public final int getWidth() {
        return width;
    }

}
