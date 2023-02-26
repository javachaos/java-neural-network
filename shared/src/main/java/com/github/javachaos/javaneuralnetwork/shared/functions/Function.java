package com.github.javachaos.javaneuralnetwork.shared.functions;

public class Function implements ActivationFunction {

    @Override
    public double derivative(double v) {
        return 0;
    }

    @Override
    public double activate(double v) {
        return 0;
    }
}
