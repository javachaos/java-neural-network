package com.github.javachaos.javaneuralnetwork.examples;

import java.util.Objects;

/**
 * Diagnostics for one named output group.
 */
public record NeuroEvolutionOutputGroupScore(
        NeuroEvolutionOutputGroup group,
        double trainingMeanSquaredError,
        double generalizationMeanSquaredError,
        double baselineTrainingMeanSquaredError,
        double baselineGeneralizationMeanSquaredError,
        double generalizationImprovementOverBaseline) {

    public NeuroEvolutionOutputGroupScore {
        group = Objects.requireNonNull(group, "Output group cannot be null.");
        requireNonNegative(trainingMeanSquaredError, "Training mean squared error");
        requireNonNegative(generalizationMeanSquaredError, "Generalization mean squared error");
        requireNonNegative(baselineTrainingMeanSquaredError, "Baseline training mean squared error");
        requireNonNegative(baselineGeneralizationMeanSquaredError, "Baseline generalization mean squared error");
        if (Double.isNaN(generalizationImprovementOverBaseline)) {
            throw new IllegalArgumentException("Generalization improvement cannot be NaN.");
        }
    }

    private static void requireNonNegative(final double value, final String label) {
        if (Double.isNaN(value) || value < 0.0) {
            throw new IllegalArgumentException(label + " must be non-negative.");
        }
    }
}
