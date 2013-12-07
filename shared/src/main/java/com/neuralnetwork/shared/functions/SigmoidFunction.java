package com.neuralnetwork.shared.functions;

/**
 * Sigmoid activation function.
 * @author fredladeroute
 *
 */
public final class SigmoidFunction extends 
        AbstractFunction implements IActivationFunction {

    /**
     * Construct a new Sigmoid function.
     */
    public SigmoidFunction() {
        super(FunctionType.SIGMOID);
    }

}
