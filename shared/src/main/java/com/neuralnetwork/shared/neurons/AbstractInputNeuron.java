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

import com.neuralnetwork.shared.values.DoubleValue;
import com.neuralnetwork.shared.values.ZeroValue;

/**
 * Represents an input neuron.
 * 
 * @author fredladeroute
 *
 */
public abstract class AbstractInputNeuron 
        extends Neuron implements IInputNeuron {
    
    /**
     * Construct an input neuron with 
     * no initial input value.
     */
    protected AbstractInputNeuron() {
        super(NeuronType.INPUT, new ZeroValue());
    }
    
    /**
     * Construct a neuron with initial value v.
     * 
     * @param v
     *      the initial value of the neuron
     */
    protected AbstractInputNeuron(final DoubleValue v) {
        super(NeuronType.INPUT, new ZeroValue());
        this.setValue(v);
    }

}
