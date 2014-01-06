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

import com.neuralnetwork.shared.network.LayerType;
import com.neuralnetwork.shared.neurons.BiasNeuron;
import com.neuralnetwork.shared.neurons.HiddenNeuron;
import com.neuralnetwork.shared.neurons.IHiddenNeuron;

/**
 * Represents a hidden neural network layer.
 * 
 * @author fredladeroute
 *
 */
public final class HiddenLayer 
        extends Layer<IHiddenNeuron> implements IHiddenLayer {

    /**
     * Generated Serial Version UID.
     */
    private static final long serialVersionUID = 5485729609664280674L;

    /**
     * Constructs a new hidden layer.
     * 
     * @param w
     *      the length of this hidden layer
     */
    public HiddenLayer(final int w) {
        super(w + 1);
        add(new BiasNeuron());
        super.setLayerType(LayerType.HIDDEN);
    }

    @Override
    public void build() {
        while (size() != getWidth()) {
            add(new HiddenNeuron());
        }
    }

	@Override
	public int getSize() {
		return size() - 1;
	}

}
