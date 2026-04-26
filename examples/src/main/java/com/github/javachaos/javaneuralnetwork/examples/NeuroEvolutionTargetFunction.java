package com.github.javachaos.javaneuralnetwork.examples;

/**
 * Computes the expected scalar target for an input point.
 */
@FunctionalInterface
public interface NeuroEvolutionTargetFunction {

    double target(double[] input);
}
