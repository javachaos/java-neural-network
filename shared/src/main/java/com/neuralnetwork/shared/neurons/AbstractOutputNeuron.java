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

/**
 * Defines an abstract output neuron.
 * 
 * @author fredladeroute
 *
 */
public class AbstractOutputNeuron extends Neuron implements IOutputNeuron {
    
    /**
     * Construct a new Abstract output neuron.
     */
    public AbstractOutputNeuron() {
        super(NeuronType.OUTPUT);
    }

    @Override
    public final double getOutputValue() {
        return this.getValue().getValue().doubleValue();
    }
    
    

}
