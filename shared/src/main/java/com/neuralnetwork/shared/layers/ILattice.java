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
package com.neuralnetwork.shared.layers;

import com.neuralnetwork.shared.neurons.INeuron;

/**
 * Represents a Lattice of nodes, such as
 * in a SOM neural network.
 * 
 * @author fredladeroute
 *
 */
public interface ILattice {

    /**
     * Return the layer at index idx.
     * 
     * @param idx
     *      the index to the layer idx
     *      
     * @return
     *      the ILayer at layer idx
     */
    ILayer<INeuron> getLayer(int idx);
    
    /**
     * Get an INode from the ILattice.
     * 
     * @param x
     *      the x co-ordinate of the INode
     *      
     * @param y
     *      the y co-ordinate of the INode
     *      
     * @return
     *      the INode at (x,y) in this ILattices
     */
    INeuron getNode(int x, int y);
    
    /**
     * Returns the width of this ILattice.
     * 
     * @return
     *      the width of this ILattice
     */
    int getWidth();
    
    /**
     * Returns the height of this ILattice.
     * 
     * @return
     *      the height of this ILattice
     */
    int getHeight();
    
    /**
     * Return the total number of INodes in this ILattice.
     * 
     * @return
     *      the total number of INodes in this ILattince.
     */
    int getSize();
}
