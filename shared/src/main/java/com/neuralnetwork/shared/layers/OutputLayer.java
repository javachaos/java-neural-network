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
import com.neuralnetwork.shared.neurons.BiasNeuron;
import com.neuralnetwork.shared.neurons.IOutputNeuron;
import com.neuralnetwork.shared.neurons.OutputNeuron;

/**
 * Represents an output layer of a network.
 * 
 * @author fredladeroute
 *
 */
public class OutputLayer extends Layer<IOutputNeuron> implements IOutputLayer {

    /**
     * Generated serial version UID.
     */
    private static final long serialVersionUID = -6278154162004288836L;

    /**
     * Construct a new Output layer.
     * 
     * @param w
     *      the length of the layer
     */
    public OutputLayer(final int w) {
        super(w + 1);
        add((IOutputNeuron) new BiasNeuron());
        super.setLayerType(LayerType.OUTPUT);
    }

    @Override
    public final void build() {
        while (getWidth() != size()) {
            add(new OutputNeuron());
        }
    }

	@Override
    public final int getSize() {
		return size() - 1;
	}

    @Override
    public final Vector<Double> getOutputValues() {
        Vector<Double> v = new Vector<Double>();
        for (int i = 0; i < getSize(); i++) {
            v.add(getNeuron(i).getValue().getValue());
        }
        return v;
    }

}
