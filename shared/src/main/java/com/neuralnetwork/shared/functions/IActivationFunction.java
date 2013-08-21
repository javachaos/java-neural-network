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
     *      the input value to the activation function.
     * @return
     *      the output of the derivative of this activation function
     */
    double derivative(double v);
}
