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
package com.neuralnetwork.shared.neurons;

import com.neuralnetwork.shared.values.OneValue;

/**
 * Represents a bias neuron who's value is always 1.
 * But the weights can vary.
 * 
 * @author fredladeroute
 *
 */
public class BiasNeuron extends InputNeuron implements IInputNeuron {

    /**
     * Constructs a new bias neuron.
     */
    public BiasNeuron() {
        super(new OneValue());
    }

}
