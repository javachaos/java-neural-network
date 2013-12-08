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

import java.util.Vector;

import com.neuralnetwork.shared.network.INeuralNetContext;
import com.neuralnetwork.shared.network.LayerType;
import com.neuralnetwork.shared.neurons.BiasNeuron;
import com.neuralnetwork.shared.neurons.IInputNeuron;
import com.neuralnetwork.shared.neurons.InputNeuron;
import com.neuralnetwork.shared.values.DoubleValue;

/**
 * Represents an Input layer to the network.
 * 
 * @author fredladeroute
 *
 */
public final class InputLayer extends Layer<IInputNeuron> 
        implements IInputLayer {

    /**
     * Generated Serial Version UID. 
     */
    private static final long serialVersionUID = 7502031311250577255L;
    
    /**
     * Constructs a new input layer of length w.
     * 
     * @param w
     *      the length of the input layer.
     */
    public InputLayer(final int w) {
        super(w + 1);
        add(new BiasNeuron());
        setLayerType(LayerType.INPUT);
    }

    @Override
    public void addValue(final DoubleValue v, final int index) {
        set(index, new InputNeuron(v));
    }
    
    @Override
    public void addValues(final Vector<Double> values) {
        for (int i = 0; i < size(); i++) {
            set(i, new InputNeuron(new DoubleValue(values.get(i))));
        }
    }

    @Override
    public IOutputLayer propagate(final INeuralNetContext nnctx) {
        for (int i = 0; i < getSize(); i++) {
            getNeuron(i).feedforward();
        }
        return nnctx.getNetwork().getOutputLayer();
    }

    @Override
    public void build() {
        while (size() < getWidth()) {
            add(new InputNeuron());
        }
    }

	@Override
	public int getSize() {
		return size() - 1;
	}

}
