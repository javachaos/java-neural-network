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
package com.neuralnetwork.shared.neurons;


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
        super(NeuronType.INPUT, 0.0);
    }
    
    /**
     * Construct a neuron with initial value v.
     * 
     * @param v
     *      the initial value of the neuron
     */
    protected AbstractInputNeuron(final Double v) {
        super(NeuronType.INPUT, 0.0);
        this.setValue(v);
    }

}
