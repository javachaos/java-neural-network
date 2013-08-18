package com.neuralnetwork.shared.nodes;

/**
 * Represents a SOM Layer.
 * 
 * @author fredladeroute
 *
 */
public interface ISOMLayer {

    /** 
     *  Calculates the euclidean distance between this vector and 
     *  vector v.
     *  
     *  @param v
     *       the vector to calculate the distance to
     *       
     *  @return
     *       Integer.MIN_VALUE if the vectors are not identical,
     *       otherwise returns the square of the distance.
     */
    double dist(SOMLayer v);
}
