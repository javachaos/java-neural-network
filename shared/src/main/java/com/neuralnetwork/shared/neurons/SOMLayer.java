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

import java.io.Serial;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A SOM Layer.
 * 
 * @author fredladeroute
 *
 */
public class SOMLayer extends ArrayList<Double> implements ISOMLayer {
    
    /**
     * Logger instance.
     */
    private static final Logger LOGGER =
    		LoggerFactory.getLogger(SOMLayer.class);

    @Serial
    private static final long serialVersionUID = -8674168620850601954L;

   /** 
    * Creates a new instance of a SOM Layer.
    */
   public SOMLayer() {
   }
   
   /**
    * Create a new instance of a SOM Layer
    * with initial size of size.
    * @param size
    * 		the initial size of the SOM Layer.
    */
   public SOMLayer(final int size) {
	   super(size);
	   for (int i = 0; i < size; i++) {
		   add(0.0);
	   }
   }
   
   @Override
   public final double dist(final SOMLayer layer) {
       if (layer.size() != super.size()) {
           LOGGER.warn("Cannot compute distance, "
                   + "vector lengths are not the equal.");
           return Integer.MIN_VALUE;
       }
       
       double distance = 0;
       double temp;
       
       for (int i = 0; i < size(); i++) {
           temp = get(i) - layer.get(i);
           temp = Math.pow(temp, 2);
           distance += temp;
       }
       
       return distance;
   }
   
}