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
   
   @Override
   public final double dist(final SOMLayer layer) {
       if (layer.size() != size()) {
           LOGGER.warn("Cannot compute distance, vector lengths are not the equal.");
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