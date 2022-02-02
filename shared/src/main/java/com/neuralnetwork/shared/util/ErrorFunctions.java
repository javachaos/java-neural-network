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
package com.neuralnetwork.shared.util;

import java.util.List;

import com.neuralnetwork.shared.links.ILink;

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
	private static final ErrorFunctions INSTANCE = new ErrorFunctions();
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
    public double meanSquaredError(final List<Double> v1, 
        final List<Double> v2) {
        double n = v1.size();
        double error = 0;
        for (int i = 0; i < v1.size(); i++) {
            error += Math.pow(v2.get(i) - v1.get(i), 2);
        }
        
        return error / n;
    }
    
    /**
     * Calculate the mean squared error of lists of links v1 and v2.
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
    public double meanSquaredErrorLink(final List<ILink> v1, 
        final List<ILink> v2) {
        double n = v1.size();
        double error = 0;
        for (int i = 0; i < v1.size(); i++) {
            error += (1.0 / 2) * Math.pow(
            		v2.get(i).getWeight()
            		- v1.get(i).getWeight(), 2);
        }
        
        return error / n;
    }

    /**
     * Get an instance of the ErrorFunctions class.
     * @return
     *      an instance of the ErrorFunctions class.
     */
	public static ErrorFunctions getInstance() {
		return INSTANCE;
	}

}
