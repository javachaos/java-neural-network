package com.neuralnetwork.shared.functions;

/**
 * Linear activation function.
 * @author fredladeroute
 *
 */
public final class LinearFunction extends 
        AbstractFunction implements IActivationFunction {

    /**
     * Construct a new Sigmoid function.
     */
    public LinearFunction() {
        super(FunctionType.LINEAR);
    }
}