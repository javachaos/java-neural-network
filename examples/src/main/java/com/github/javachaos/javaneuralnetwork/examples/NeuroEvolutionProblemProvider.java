package com.github.javachaos.javaneuralnetwork.examples;

/**
 * Factory for pluggable neuro-evolution problems.
 */
@FunctionalInterface
public interface NeuroEvolutionProblemProvider {

    NeuroEvolutionProblem create(int gridSize);
}
