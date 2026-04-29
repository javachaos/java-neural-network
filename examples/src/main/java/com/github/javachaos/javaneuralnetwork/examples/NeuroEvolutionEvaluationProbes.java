package com.github.javachaos.javaneuralnetwork.examples;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Precomputed evaluation probes for workers that cannot call a problem's Java target function.
 */
final class NeuroEvolutionEvaluationProbes {

    private static final long JITTER_SEED_OFFSET = 0x5f3759dfL;
    private static final long SMOOTHNESS_SEED_OFFSET = 0x6a09e667L;
    private static final int MAX_SMOOTHNESS_PROBES = 32;
    private static final double MIN_SMOOTHNESS_STEP = 0.01;
    private static final double SMOOTHNESS_RADIUS_SCALE = 0.5;

    private NeuroEvolutionEvaluationProbes() {
    }

    record SmoothnessProbe(
            NeuroEvolutionSample center,
            NeuroEvolutionSample plus,
            NeuroEvolutionSample minus) {

        SmoothnessProbe {
            Objects.requireNonNull(center, "Center sample cannot be null.");
            Objects.requireNonNull(plus, "Plus sample cannot be null.");
            Objects.requireNonNull(minus, "Minus sample cannot be null.");
        }
    }

    static List<NeuroEvolutionSample> jitterSamples(
            final NeuroEvolutionProblem problem,
            final XorNeuroEvolution.EvolutionConfig config) {
        Objects.requireNonNull(problem, "Problem cannot be null.");
        Objects.requireNonNull(config, "Config cannot be null.");
        if (config.jitterSamplesPerCorner() == 0 || config.jitterRadius() == 0.0) {
            return List.of();
        }
        Random random = new Random(config.seed() + JITTER_SEED_OFFSET);
        List<NeuroEvolutionSample> samples = new ArrayList<>(
                problem.jitterAnchors().size() * config.jitterSamplesPerCorner());
        for (NeuroEvolutionSample anchor : problem.jitterAnchors()) {
            for (int sample = 0; sample < config.jitterSamplesPerCorner(); sample++) {
                double[] input = jitter(anchor.input(), config.jitterRadius(), random);
                samples.add(NeuroEvolutionSample.of(input, problem.targetVector(input)));
            }
        }
        return List.copyOf(samples);
    }

    static List<SmoothnessProbe> smoothnessProbes(
            final NeuroEvolutionProblem problem,
            final XorNeuroEvolution.EvolutionConfig config) {
        Objects.requireNonNull(problem, "Problem cannot be null.");
        Objects.requireNonNull(config, "Config cannot be null.");
        if (problem.generalizationSamples().isEmpty() || config.jitterRadius() == 0.0) {
            return List.of();
        }
        Random random = new Random(config.seed() + SMOOTHNESS_SEED_OFFSET);
        double step = Math.max(MIN_SMOOTHNESS_STEP, config.jitterRadius() * SMOOTHNESS_RADIUS_SCALE);
        int attempts = Math.min(MAX_SMOOTHNESS_PROBES, problem.generalizationSamples().size());
        List<SmoothnessProbe> probes = new ArrayList<>(attempts);
        for (int i = 0; i < attempts; i++) {
            double[] centerInput = problem.generalizationSamples()
                    .get(random.nextInt(problem.generalizationSamples().size()))
                    .input();
            double[] direction = randomDirection(problem.inputDimensions(), random);
            double[] plusInput = offset(centerInput, direction, step);
            double[] minusInput = offset(centerInput, direction, -step);
            probes.add(new SmoothnessProbe(
                    NeuroEvolutionSample.of(centerInput, problem.targetVector(centerInput)),
                    NeuroEvolutionSample.of(plusInput, problem.targetVector(plusInput)),
                    NeuroEvolutionSample.of(minusInput, problem.targetVector(minusInput))));
        }
        return List.copyOf(probes);
    }

    private static double[] jitter(final double[] input, final double radius, final Random random) {
        double[] result = input.clone();
        for (int i = 0; i < result.length; i++) {
            result[i] = clamp01(result[i] + random.nextDouble(-radius, radius));
        }
        return result;
    }

    private static double[] offset(final double[] input, final double[] direction, final double step) {
        double[] result = input.clone();
        for (int i = 0; i < result.length; i++) {
            result[i] = clamp01(result[i] + direction[i] * step);
        }
        return result;
    }

    private static double[] randomDirection(final int dimensions, final Random random) {
        double[] direction = new double[dimensions];
        double norm = 0.0;
        for (int i = 0; i < direction.length; i++) {
            direction[i] = random.nextGaussian();
            norm += direction[i] * direction[i];
        }
        norm = Math.sqrt(norm);
        if (norm == 0.0) {
            direction[0] = 1.0;
            return direction;
        }
        for (int i = 0; i < direction.length; i++) {
            direction[i] /= norm;
        }
        return direction;
    }

    private static double clamp01(final double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
