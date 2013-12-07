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
package com.neuralnetwork.shared.functions;

/**
 * Defines an activation function.
 * 
 * @author fredladeroute
 *
 */
public interface IActivationFunction {

    /**
     * Activate using the activation function.
     * 
     * @param v
     *      input value for this IActivationFunction
     * @return
     *      output value of the function
     */
    double activate(double v);
    
    /**
     * Returns the output of the
     * derivative of the activation function.
     * 
     * @param v
     *      the input value to the activation function
     * @return
     *      the output of the derivative of this activation function
     */
    double derivative(double v);
    
    /**
     * Get the function type.
     * @return
     * 		the function type
     */
    FunctionType getFunctionType();
}
