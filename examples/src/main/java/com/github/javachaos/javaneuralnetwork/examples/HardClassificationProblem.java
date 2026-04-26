package com.github.javachaos.javaneuralnetwork.examples;

import java.util.List;

/**
 * Base class for denser two-dimensional stress-test classification tasks.
 */
public abstract class HardClassificationProblem extends NeuroEvolutionProblem {

    protected HardClassificationProblem(
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
                gridSamples(HARD_TRAINING_GRID_SIZE, targetFunction),
                gridSamples(Math.max(gridSize, HARD_GENERALIZATION_GRID_SIZE), targetFunction),
                gridSamples(DEFAULT_KERNEL_GRID_SIZE, targetFunction),
                gridInputs(HARD_KERNEL_GRID_SIZE),
                true,
                targetFunction);
    }
}
