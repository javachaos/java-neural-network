package com.neuralnetwork.shared.functions;

/**
 * Sigmoid activation function.
 * @author fredladeroute
 *
 */
public final class SigmoidFunction implements IActivationFunction {

    @Override
    public double activate(final double x) {
        return 1 / (1 + Math.exp(-x));
    }

    @Override
    public double derivative(final double x) {
        return activate(x) * (1.0 - activate(x));
    }

}
