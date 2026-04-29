package com.github.javachaos.javaneuralnetwork.examples;

/**
 * Problem-owned predictive-coding objective weights.
 */
public record NeuroEvolutionFreeEnergyProfile(
        double objectiveWeight,
        double sensoryPredictionWeight,
        double latentPredictionWeight,
        double complexityPriorWeight,
        double maximumEnergy) {

    private static final NeuroEvolutionFreeEnergyProfile DISABLED =
            new NeuroEvolutionFreeEnergyProfile(0.0, 0.0, 0.0, 0.0, 1.0);

    public NeuroEvolutionFreeEnergyProfile {
        requireNonNegative(objectiveWeight, "Objective weight");
        requireNonNegative(sensoryPredictionWeight, "Sensory prediction weight");
        requireNonNegative(latentPredictionWeight, "Latent prediction weight");
        requireNonNegative(complexityPriorWeight, "Complexity prior weight");
        if (!Double.isFinite(maximumEnergy) || maximumEnergy <= 0.0) {
            throw new IllegalArgumentException("Maximum energy must be positive and finite.");
        }
    }

    public static NeuroEvolutionFreeEnergyProfile disabled() {
        return DISABLED;
    }

    public static NeuroEvolutionFreeEnergyProfile predictiveCoding(
            final double objectiveWeight,
            final double sensoryPredictionWeight,
            final double latentPredictionWeight,
            final double complexityPriorWeight,
            final double maximumEnergy) {
        return new NeuroEvolutionFreeEnergyProfile(
                objectiveWeight,
                sensoryPredictionWeight,
                latentPredictionWeight,
                complexityPriorWeight,
                maximumEnergy);
    }

    public boolean enabled() {
        return objectiveWeight > 0.0
                && (sensoryPredictionWeight > 0.0
                || latentPredictionWeight > 0.0
                || complexityPriorWeight > 0.0);
    }

    private static void requireNonNegative(final double value, final String label) {
        if (!Double.isFinite(value) || value < 0.0) {
            throw new IllegalArgumentException(label + " must be finite and non-negative.");
        }
    }
}
