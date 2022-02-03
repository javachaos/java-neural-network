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

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import com.neuralnetwork.shared.network.LayerType;
import com.neuralnetwork.shared.neurons.BiasNeuron;
import com.neuralnetwork.shared.neurons.OutputNeuron;
import com.neuralnetwork.shared.neurons.OutputNeuronImpl;

/**
 * Represents an output layer of a network.
 * 
 * 
 *
 */
public class OutputNeuronLayer extends NeuronLayer<OutputNeuron> implements OutputLayer {

    @Serial
    private static final long serialVersionUID = -6278154162004288836L;

    /**
     * Construct a new Output layer.
     * 
     * @param w
     *      the length of the layer
     */
    public OutputNeuronLayer(final int w) {
        super(w + 1);
        add(new BiasNeuron());
        super.setLayerType(LayerType.OUTPUT);
    }

    @Override
    public final void build() {
        while (getWidth() != size()) {
            add(new OutputNeuronImpl());
        }
    }

	@Override
    public final int getSize() {
		return size() - 1;
	}

    @Override
    public final List<Double> getOutputValues() {
    	List<Double> v = new ArrayList<>();
        for (int i = 0; i < getSize(); i++) {
            v.add(getNeuron(i).getValue());
        }
        return v;
    }

}
