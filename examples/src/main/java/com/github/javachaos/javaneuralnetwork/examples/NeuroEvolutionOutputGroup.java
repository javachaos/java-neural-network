package com.github.javachaos.javaneuralnetwork.examples;

/**
 * Named contiguous output range used for diagnostics on multi-output problems.
 */
public record NeuroEvolutionOutputGroup(
        String name,
        int startInclusive,
        int endExclusive,
        double objectiveWeight) {

    public NeuroEvolutionOutputGroup(
            final String name,
            final int startInclusive,
            final int endExclusive) {
        this(name, startInclusive, endExclusive, 1.0);
    }

    public NeuroEvolutionOutputGroup {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Output group name cannot be blank.");
        }
        if (startInclusive < 0) {
            throw new IllegalArgumentException("Output group start cannot be negative.");
        }
        if (endExclusive <= startInclusive) {
            throw new IllegalArgumentException("Output group end must be greater than its start.");
        }
        if (!Double.isFinite(objectiveWeight) || objectiveWeight <= 0.0) {
            throw new IllegalArgumentException("Output group objective weight must be positive and finite.");
        }
    }

    public int width() {
        return endExclusive - startInclusive;
    }
}
