package com.neuralnetwork.shared.layers;

import com.neuralnetwork.shared.nodes.INeuron;

/**
 * Represents a Lattice of nodes, such as
 * in a SOM neural network.
 * 
 * @author fredladeroute
 *
 *@param <T>
 *      the type of lattice this is
 */
public interface ILattice<T extends Number> {

    /**
     * Return the layer at index idx.
     * 
     * @param idx
     *      the index to the layer idx
     *      
     * @return
     *      the ILayer at layer idx
     */
    ILayer<T> getLayer(int idx);
    
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
    INeuron<T> getNode(int x, int y);
    
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
