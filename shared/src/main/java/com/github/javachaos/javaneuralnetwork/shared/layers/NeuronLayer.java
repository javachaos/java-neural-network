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

import java.io.Serial;
import java.util.ArrayList;
import java.util.Objects;

import com.github.javachaos.javaneuralnetwork.shared.network.LayerType;
import com.github.javachaos.javaneuralnetwork.shared.neurons.Neuron;

/**
 * Represents a Layer of INeurons.
 * 
 *
 *@param <T>
 *      the type of Layer this is
 */
public abstract class NeuronLayer<T extends Neuron>
        extends ArrayList<T> implements Layer<T> {

    @Serial
    private static final long serialVersionUID = -94028484942015525L;

    /**
     * Represents the number of neurons in the layer.
     */
    private final int width;

    private LayerType layerType;
    
    /**
     * Construct a new Layer of width w.
     * 
     * @param w
     *      the width of the Layer to be created
     */
	NeuronLayer(final int w) {
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

	@Override
	public synchronized int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(layerType, width);
		return result;
	}

	@Override
	public synchronized boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		NeuronLayer<?> other = (NeuronLayer<?>) obj;
		return layerType == other.layerType && width == other.width;
	}

    
}
