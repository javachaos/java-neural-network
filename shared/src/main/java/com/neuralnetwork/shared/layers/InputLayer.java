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

import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neuralnetwork.shared.network.INeuralNetContext;
import com.neuralnetwork.shared.network.LayerType;
import com.neuralnetwork.shared.neurons.BiasNeuron;
import com.neuralnetwork.shared.neurons.IInputNeuron;
import com.neuralnetwork.shared.neurons.InputNeuron;

/**
 * Represents an Input layer to the network.
 * 
 * @author fredladeroute
 *
 */
public final class InputLayer extends Layer<IInputNeuron> 
        implements IInputLayer {

    /**
     * Logger instance.
     */
    private static final Logger LOGGER =
    		LoggerFactory.getLogger(InputLayer.class);
    
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
        build();
    }

    @Override
    public void addValue(final Double v, final int index) {
        set(index + 1, new InputNeuron(v));
    }
    
    @Override
    public void addValues(final Vector<Double> values) {
    	if (values == null) {
    		throw new NullPointerException("Values vector was null.");
    	}
    	if (values.size() != this.size() - 1) {
    		throw new IllegalArgumentException(
    				"Values is not the correct dimension. Values: "
    		        + values.size()
    				+ ", InputLayer: " + size());
    	}
        for (int i = 0; i < values.size(); i++) {
            set(i + 1, new InputNeuron(values.get(i)));
        }
    }

    @Override
    public IOutputLayer propagate(final INeuralNetContext nnctx) {
    	double v = Double.MAX_VALUE;
    	synchronized (nnctx) {
	        for (int i = 0; i < getSize(); i++) {
	        	v += getNeuron(i).feedforward(nnctx);
	        }
    	}
    	LOGGER.debug("Propagation Error: " + v);
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
