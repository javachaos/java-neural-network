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
 * Represents a SOM Layer.
 * 
 * @author fredladeroute
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
}
