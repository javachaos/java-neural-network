package com.github.javachaos.javaneuralnetwork.examples;

import java.util.Objects;

/**
 * A supervised sample for neuro-evolution tasks.
 */
public record NeuroEvolutionSample(double[] input, double[] targets) {

    public NeuroEvolutionSample {
        Objects.requireNonNull(input, "Input cannot be null.");
        Objects.requireNonNull(targets, "Targets cannot be null.");
        if (input.length == 0) {
            throw new IllegalArgumentException("Input must have at least one dimension.");
        }
        if (targets.length == 0) {
            throw new IllegalArgumentException("Targets must have at least one channel.");
        }
        for (double value : input) {
            if (!Double.isFinite(value)) {
                throw new IllegalArgumentException("Input values must be finite.");
            }
        }
        for (double target : targets) {
            if (!Double.isFinite(target) || target < 0.0 || target > 1.0) {
                throw new IllegalArgumentException("Targets must be finite and in [0, 1].");
            }
        }
        input = input.clone();
        targets = targets.clone();
    }

    public NeuroEvolutionSample(final double[] input, final double target) {
        this(input, new double[] {target});
    }

    public static NeuroEvolutionSample of(final double[] input, final double target) {
        return new NeuroEvolutionSample(input, target);
    }

    public static NeuroEvolutionSample of(final double[] input, final double[] targets) {
        return new NeuroEvolutionSample(input, targets);
    }

    public double target() {
        if (targets.length != 1) {
            throw new IllegalStateException("Sample has " + targets.length + " target channels, not one.");
        }
        return targets[0];
    }

    public double[] targetVector() {
        return targets();
    }

    public int outputDimensions() {
        return targets.length;
    }

    @Override
    public double[] input() {
        return input.clone();
    }

    @Override
    public double[] targets() {
        return targets.clone();
    }
}
