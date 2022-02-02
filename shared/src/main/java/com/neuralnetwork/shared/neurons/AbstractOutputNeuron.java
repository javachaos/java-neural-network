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
 * Defines an abstract output neuron.
 * 
 * @author fredladeroute
 *
 */
public abstract class AbstractOutputNeuron 
        extends Neuron implements IOutputNeuron {

    /**
     * Construct a new Abstract output neuron.
     */
    protected AbstractOutputNeuron() {
        super(NeuronType.OUTPUT, Math.random());
    }

    @Override
    public final double getOutputValue() {
        return getValue();
    }

}
