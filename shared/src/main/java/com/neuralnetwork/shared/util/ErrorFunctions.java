package com.neuralnetwork.shared.util;

import java.util.Vector;

/**
 * Error functions.
 * 
 * @author fredladeroute
 *
 */
public final class ErrorFunctions {
    
	/**
	 * Singleton instance.
	 */
	private static final ErrorFunctions instance = new ErrorFunctions();
    /**
     * Unused ctor.
     */
    private ErrorFunctions() {
    }
    
    /**
     * Calculate the mean squared error of vectors v1 and v2.
     * 
     * @param v1
     *      vector v1
     *      
     * @param v2
     *      vector v2
     *      
     * @return
     *      the MSE of v1 and v2
     */
    public double meanSquaredError(final Vector<Double> v1, final Vector<Double> v2) {
        double n = v1.size();
        double error = 0;
        for (int i = 0; i < v1.size(); i++) {
            error += Math.pow(v2.get(i) - v1.get(i), 2);
        }
        
        return error / n;
    }

	public static ErrorFunctions getInstance() {
		return instance;
	}

}
