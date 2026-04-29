package com.github.javachaos.javaneuralnetwork.examples;

import java.util.Objects;

/**
 * Precision-weighted predictive-coding diagnostics for one evaluated learner.
 */
public record NeuroEvolutionFreeEnergyMetrics(
        double value,
        double sensoryPredictionEnergy,
        double latentPredictionEnergy,
        double complexityPriorEnergy) {

    private static final NeuroEvolutionFreeEnergyMetrics DISABLED =
            new NeuroEvolutionFreeEnergyMetrics(0.0, 0.0, 0.0, 0.0);

    public NeuroEvolutionFreeEnergyMetrics {
        requireNonNegative(value, "Predictive free energy");
        requireNonNegative(sensoryPredictionEnergy, "Sensory prediction energy");
        requireNonNegative(latentPredictionEnergy, "Latent prediction energy");
        requireNonNegative(complexityPriorEnergy, "Complexity prior energy");
    }

    public static NeuroEvolutionFreeEnergyMetrics disabled() {
        return DISABLED;
    }

    public static NeuroEvolutionFreeEnergyMetrics from(
            final NeuroEvolutionFreeEnergyProfile profile,
            final double sensoryPredictionEnergy,
            final double latentPredictionEnergy,
            final double complexityPriorEnergy) {
        Objects.requireNonNull(profile, "Free-energy profile cannot be null.");
        if (!profile.enabled()) {
            return disabled();
        }
        double rawEnergy = profile.sensoryPredictionWeight() * sensoryPredictionEnergy
                + profile.latentPredictionWeight() * latentPredictionEnergy
                + profile.complexityPriorWeight() * complexityPriorEnergy;
        double boundedEnergy = Double.isFinite(rawEnergy)
                ? Math.min(profile.maximumEnergy(), Math.max(0.0, rawEnergy))
                : profile.maximumEnergy();
        return new NeuroEvolutionFreeEnergyMetrics(
                boundedEnergy,
                sensoryPredictionEnergy,
                latentPredictionEnergy,
                complexityPriorEnergy);
    }

    private static void requireNonNegative(final double value, final String label) {
        if (Double.isNaN(value) || value < 0.0) {
            throw new IllegalArgumentException(label + " must be non-negative.");
        }
    }
}
