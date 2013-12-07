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
 * Describes the type of a Neuron.
 * 
 * @author fred
 *
 */
public enum NeuronType {

	/**
	 * Hidden neuron type.
	 */
	HIDDEN,

	/**
	 * Input neuron type.
	 */
	INPUT,

	/**
	 * Output neuron type.
	 */
	OUTPUT,

	/**
	 * Self organizing map neuron.
	 */
	SOM;
}
