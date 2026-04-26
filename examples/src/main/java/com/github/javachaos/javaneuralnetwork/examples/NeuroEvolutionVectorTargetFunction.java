package com.github.javachaos.javaneuralnetwork.examples;

/**
 * Computes one or more normalized target channels for a supervised
 * neuro-evolution problem.
 */
@FunctionalInterface
public interface NeuroEvolutionVectorTargetFunction {

    double[] target(double[] input);
}
