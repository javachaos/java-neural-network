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
package com.github.javachaos.javaneuralnetwork.shared.neurons;

/**
 * Represents a SOM Layer.
 * 
 * 
 *
 */
public interface SOMLayer {

    /** 
     *  Calculates the euclidean distance between this vector and 
     *  vector v.
     *  
     *  @param v
     *       the vector to calculate the distance to
     *       
     *  @return
     *       Integer.MIN_VALUE if the vectors are not the same length,
     *       otherwise returns the square of the distance.
     */
    double dist(SOMLayerImpl v);

    /**
     * Get the value at the ith location in this SOM Layer.
     * @param i the index
     * @return  the ith element
     */
    double getNeuron(int i);
}
