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
package com.neuralnetwork.shared.nodes;

/**
 * Represents an SOM Lattice.
 * 
 * @author fredladeroute
 *
 */
public interface ISOMLattice {
    
    /**
     * Return the neuron at x,y of this lattice.
     * 
     * @param x
     *         the x co-ordinate of the neuron to get
     *         
     * @param y
     *         the y co-ordinate of the neuron to get
     *         
     * @return
     *         the neuron at position x,y in the lattice
     */
    SOMNeuron getNeuron(int x, int y);
    
    /**
     * Return the width of the lattice.
     * As the number of neurons along the x axis.
     * 
     * @return
     *         the width of the lattice
     */
    int getWidth(); 
    
    /**
     * Return the height of the lattice.
     * 
     * @return
     *         the height of the lattice
     */
    int getHeight();
    
    /**
     * Find the BMU (Best Matching Unit) for the input vector.
     *
     * @param inputVector
     *         the input vector
     *         
     * @return
     *     the best matching neuron
     */
    SOMNeuron getBMU(SOMLayer inputVector);
}
