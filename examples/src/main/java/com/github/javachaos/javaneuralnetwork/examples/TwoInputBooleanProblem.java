package com.github.javachaos.javaneuralnetwork.examples;

import java.util.List;

/**
 * Base class for two-input boolean-style classification tasks.
 */
public abstract class TwoInputBooleanProblem extends NeuroEvolutionProblem {

    protected TwoInputBooleanProblem(
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
                twoInputBooleanSamples(targetFunction),
                gridSamples(gridSize, targetFunction),
                twoInputBooleanSamples(targetFunction),
                cornerInputs(),
                true,
                targetFunction);
    }
}
