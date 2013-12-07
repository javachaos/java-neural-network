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
 * Represents a hidden neuron.
 * @author fredladeroute
 *
 */
public class HiddenNeuron extends Neuron implements IHiddenNeuron {
    
	/**
	 * Create a new hidden neuron.
	 */
	public HiddenNeuron() {
		super();
	}
	
    @Override
    public final String toString() {
        return "HN(" + getValue() + ") ";
    }
}
