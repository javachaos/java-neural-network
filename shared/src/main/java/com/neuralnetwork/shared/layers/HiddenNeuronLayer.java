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
import com.neuralnetwork.shared.neurons.BiasNeuron;
import com.neuralnetwork.shared.neurons.HiddenNeuronImpl;
import com.neuralnetwork.shared.neurons.HiddenNeuron;

import java.io.Serial;

/**
 * Represents a hidden neural network layer.
 */
public final class HiddenNeuronLayer
        extends NeuronLayer<HiddenNeuron> implements HiddenLayer {

    @Serial
    private static final long serialVersionUID = 5485729609664280674L;

    private final int index;

    /**
     * Constructs a new hidden layer.
     * 
     * @param w
     *      the length of this hidden layer
     * @param index
     *      the index of this hidden layer
     */
    public HiddenNeuronLayer(final int w, final int index) {
        super(w + 1);
        add(new BiasNeuron());
        super.setLayerType(LayerType.HIDDEN);
        this.index = index;
    }

    @Override
    public void build() {
        while (size() != getWidth()) {
            add(new HiddenNeuronImpl());
        }
    }

	@Override
	public int getSize() {
		return size() - 1;
	}

    @Override
    public int compareTo(HiddenLayer o) {
        int i = o.getIndex();
        return Integer.compare(index, i);
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public synchronized boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        HiddenNeuronLayer that = (HiddenNeuronLayer) o;

        return index == that.index;
    }

    @Override
    public synchronized int hashCode() {
        int result = super.hashCode();
        result = 31 * result + index;
        return result;
    }
}
