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
package com.neuralnetwork.shared.neurons;

/**
 * Represents an input neuron.
 */
public abstract class AbstractInputNeuronImpl
        extends NeuronImpl implements InputNeuron {

    protected AbstractInputNeuronImpl() {
        super(NeuronType.INPUT, 0.0);
    }
    
    /**
     * Construct a neuron with initial value v.
     * 
     * @param v
     *      the initial value of the neuron
     */
    protected AbstractInputNeuronImpl(final Double v) {
        super(NeuronType.INPUT, 0.0);
        this.setValue(v);
    }

}
