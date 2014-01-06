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
package com.neuralnetwork.shared.neurons;

import java.util.Vector;

import com.neuralnetwork.shared.links.ILink;
import com.neuralnetwork.shared.values.DoubleValue;
import com.neuralnetwork.shared.values.ZeroValue;

/**
 * Implementation of an Input neuron.
 * @author fredladeroute
 *
 */
public class InputNeuron extends AbstractInputNeuron {
    
    /**
     * Constructs a new input value with value v.
     * 
     * @param v
     *      the initial input value
     */
    public InputNeuron(final DoubleValue v) {
        super(v);
    }
    
    /**
     * Constracts a new input value with value
     * ZeroValue.
     */
    public InputNeuron() {
        super();
        setValue(new ZeroValue());
    }
    
    @Override
    public final void feedforward(final DoubleValue v) {
        feedforward();
    }
    
    @Override
    public final void feedforward() {
        Vector<ILink> o = getOutputs();
        for (int i = 0; i < o.size(); i++) {
            ILink l = o.get(i);
            l.getTail().feedforward(getValue());
        }
    }
    
    @Override
    public final String toString() {
        return "IN(" + getValue() + ") ";
    }

}
