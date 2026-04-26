package com.github.javachaos.javaneuralnetwork.examples;

import java.util.List;

/**
 * Catalog of problem definitions available to the neuro-evolution GUI/engine.
 */
public interface NeuroEvolutionProblemCatalog {

    List<String> problemKeys();

    List<NeuroEvolutionProblem> problems(int gridSize);

    NeuroEvolutionProblem find(String name, int gridSize);

    NeuroEvolutionProblem defaultProblem(int gridSize);
}
