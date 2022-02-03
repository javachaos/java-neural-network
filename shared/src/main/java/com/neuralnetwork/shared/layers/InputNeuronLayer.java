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

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neuralnetwork.shared.network.NeuralNetContext;
import com.neuralnetwork.shared.network.LayerType;
import com.neuralnetwork.shared.neurons.BiasNeuron;
import com.neuralnetwork.shared.neurons.InputNeuron;
import com.neuralnetwork.shared.neurons.InputNeuronImpl;

/**
 * Represents an Input layer to the network.
 * 
 * @author fredladeroute
 *
 */
public final class InputNeuronLayer extends NeuronLayer<InputNeuron>
        implements InputLayer {

    /**
     * Logger instance.
     */
    private static final Logger LOGGER =
    		LoggerFactory.getLogger(InputNeuronLayer.class);
    
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
    public InputNeuronLayer(final int w) {
        super(w + 1);
        add(new BiasNeuron());
        setLayerType(LayerType.INPUT);
        build();
    }

    @Override
    public void addValue(final Double v, final int index) {
        set(index + 1, new InputNeuronImpl(v));
    }
    
    @Override
    public void addValues(final List<Double> values) {
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
            set(i + 1, new InputNeuronImpl(values.get(i)));
        }
    }

    @Override
    public OutputLayer propagate(final NeuralNetContext nnctx) {
    	AtomicReference<Double> v = new AtomicReference<>(Double.MAX_VALUE);
        IntStream.range(0, getSize()).parallel().forEach(i -> v.updateAndGet(v1 -> v1 + getNeuron(i).feedforward(nnctx)));
    	LOGGER.debug("Propagation Error: {}", v);
        return nnctx.getNetwork().getOutputLayer();
    }

    @Override
    public void build() {
        while (size() < getWidth()) {
            add(new InputNeuronImpl());
        }
    }

	@Override
	public int getSize() {
		return size() - 1;
	}

}
