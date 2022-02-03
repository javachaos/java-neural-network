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
package com.neuralnetwork.shared.layers;

import com.neuralnetwork.shared.neurons.Neuron;

/**
 * Represents a Lattice of neurons
 */
public interface Lattice<T extends Neuron> {

    /**
     * Return the layer at index idx.
     * 
     * @param idx
     *      the index to the layer idx
     *      
     * @return
     *      the Layer at layer idx
     */
    Layer<T> getLayer(int idx);
    
    /**
     * Get a Neuron from the Lattice.
     * 
     * @param x
     *      the x co-ordinate of the Neuron
     *      
     * @param y
     *      the y co-ordinate of the Neuron
     *      
     * @return
     *      the Neuron at (x,y) in this Lattices
     */
    T getNeuron(int x, int y);
    
    /**
     * Returns the width of this Lattice.
     * 
     * @return
     *      the width of this Lattice
     */
    int getWidth();
    
    /**
     * Returns the height of this Lattice.
     * 
     * @return
     *      the height of this Lattice
     */
    int getHeight();
    
    /**
     * Return the total number of Neurons in this Lattice.
     * 
     * @return
     *      the total number of Neurons in this Lattince.
     */
    int getSize();
}
