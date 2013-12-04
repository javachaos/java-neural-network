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

import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A SOM Layer.
 * 
 * @author fredladeroute
 *
 */
public class SOMLayer extends Vector<Double> implements ISOMLayer {
    
    /**
     * Logger instance.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SOMLayer.class);
    
    /**
     * Generated Serial Version UID.
     */
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
           temp = ((Double) elementAt(i)) - ((Double) layer.elementAt(i));
           temp = Math.pow(temp, 2);
           distance += temp;
       }
       
       return distance;
   }
   
}