package com.github.javachaos.javaneuralnetwork.examples;

import java.util.List;

/**
 * Base class for one-dimensional regression tasks sampled on a line.
 */
public abstract class LineRegressionProblem extends NeuroEvolutionProblem {

    protected LineRegressionProblem(
            final String key,
            final String name,
            final String description,
            final List<String> aliases,
            final int gridSize,
            final NeuroEvolutionTargetFunction targetFunction) {
        super(
                key,
                name,
                description,
                aliases,
                1,
                lineSamples(9, targetFunction),
                lineSamples(Math.max(gridSize, DEFAULT_GRID_SIZE), targetFunction),
                lineSamples(5, targetFunction),
                lineInputs(DEFAULT_GRID_SIZE),
                false,
                targetFunction);
    }
}
