package com.github.javachaos.javaneuralnetwork.examples;

import java.util.List;

/**
 * Base class for two-dimensional classification tasks sampled on a square grid.
 */
public abstract class GeometricClassificationProblem extends NeuroEvolutionProblem {

    protected GeometricClassificationProblem(
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
                2,
                gridSamples(DEFAULT_TRAINING_GRID_SIZE, targetFunction),
                gridSamples(Math.max(gridSize, DEFAULT_GRID_SIZE), targetFunction),
                gridSamples(DEFAULT_KERNEL_GRID_SIZE, targetFunction),
                gridInputs(DEFAULT_KERNEL_GRID_SIZE),
                true,
                targetFunction);
    }
}
