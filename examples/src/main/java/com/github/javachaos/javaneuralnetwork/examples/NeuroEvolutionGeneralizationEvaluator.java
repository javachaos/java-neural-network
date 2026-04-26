package com.github.javachaos.javaneuralnetwork.examples;

import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Scores how well a learner behaves away from its direct training samples.
 */
public final class NeuroEvolutionGeneralizationEvaluator {

    private NeuroEvolutionGeneralizationEvaluator() {
    }

    public record GeneralizationMetrics(
            double generalizationMeanSquaredError,
            double groupRelativeGeneralizationError,
            double jitterMeanSquaredError,
            double smoothnessPenalty) {

        public GeneralizationMetrics(
                final double generalizationMeanSquaredError,
                final double jitterMeanSquaredError,
                final double smoothnessPenalty) {
            this(
                    generalizationMeanSquaredError,
                    Double.POSITIVE_INFINITY,
                    jitterMeanSquaredError,
                    smoothnessPenalty);
        }

        public GeneralizationMetrics {
            requireNonNegativeScore(generalizationMeanSquaredError, "Generalization mean squared error");
            requireNonNegativeScore(groupRelativeGeneralizationError, "Group-relative generalization error");
            requireNonNegativeScore(jitterMeanSquaredError, "Jitter mean squared error");
            requireNonNegativeScore(smoothnessPenalty, "Smoothness penalty");
        }
    }

    public static GeneralizationMetrics evaluate(
            final RichEvolvedProblemLearner learner,
            final NeuroEvolutionProblem problem,
            final int jitterSamplesPerAnchor,
            final double jitterRadius,
            final long seed) {
        Objects.requireNonNull(learner, "Learner cannot be null.");
        Objects.requireNonNull(problem, "Problem cannot be null.");
        if (jitterSamplesPerAnchor < 0) {
            throw new IllegalArgumentException("Jitter samples per anchor cannot be negative.");
        }
        if (!Double.isFinite(jitterRadius) || jitterRadius < 0.0) {
            throw new IllegalArgumentException("Jitter radius must be finite and non-negative.");
        }

        Random random = new Random(seed);
        RichEvolvedProblemLearner.GroupedMeanSquaredError generalization =
                learner.groupedMeanSquaredError(problem.generalizationSamples());
        double jitterMse = jitterMeanSquaredError(learner, problem, jitterSamplesPerAnchor, jitterRadius, random);
        double smoothness = smoothnessPenalty(learner, problem, problem.generalizationSamples(), jitterRadius, random);
        return new GeneralizationMetrics(
                generalization.meanSquaredError(),
                generalization.baselineRelativeMeanSquaredError(),
                jitterMse,
                smoothness);
    }

    private static double jitterMeanSquaredError(
            final RichEvolvedProblemLearner learner,
            final NeuroEvolutionProblem problem,
            final int jitterSamplesPerAnchor,
            final double jitterRadius,
            final Random random) {
        if (jitterSamplesPerAnchor == 0 || jitterRadius == 0.0) {
            return 0.0;
        }
        double total = 0.0;
        int count = 0;
        for (NeuroEvolutionSample anchor : problem.jitterAnchors()) {
            for (int sample = 0; sample < jitterSamplesPerAnchor; sample++) {
                double[] input = jitter(anchor.input(), jitterRadius, random);
                double[] targets = problem.targetVector(input);
                double[] predictions = learner.predictVector(input);
                if (!arrayIsFinite(predictions)) {
                    return Double.POSITIVE_INFINITY;
                }
                for (int output = 0; output < targets.length; output++) {
                    double error = targets[output] - predictions[output];
                    total += error * error;
                    count++;
                }
            }
        }
        return count == 0 ? 0.0 : total / count;
    }

    private static double smoothnessPenalty(
            final RichEvolvedProblemLearner learner,
            final NeuroEvolutionProblem problem,
            final List<NeuroEvolutionSample> samples,
            final double radius,
            final Random random) {
        if (samples.isEmpty() || radius == 0.0) {
            return 0.0;
        }
        double step = Math.max(0.01, radius * 0.5);
        double total = 0.0;
        int count = 0;
        int attempts = Math.min(32, samples.size());
        for (int i = 0; i < attempts; i++) {
            double[] center = samples.get(random.nextInt(samples.size())).input();
            double[] direction = randomDirection(problem.inputDimensions(), random);
            double[] plus = offset(center, direction, step);
            double[] minus = offset(center, direction, -step);

            double[] predictionPlus = learner.predictVector(plus);
            double[] predictionMinus = learner.predictVector(minus);
            double[] predictionCenter = learner.predictVector(center);
            double[] targetPlus = problem.targetVector(plus);
            double[] targetMinus = problem.targetVector(minus);
            double[] targetCenter = problem.targetVector(center);
            if (!arrayIsFinite(predictionPlus)
                    || !arrayIsFinite(predictionMinus)
                    || !arrayIsFinite(predictionCenter)
                    || !arrayIsFinite(targetPlus)
                    || !arrayIsFinite(targetMinus)
                    || !arrayIsFinite(targetCenter)) {
                return Double.POSITIVE_INFINITY;
            }
            for (int output = 0; output < targetCenter.length; output++) {
                double predictionCurvature =
                        predictionPlus[output] + predictionMinus[output] - 2.0 * predictionCenter[output];
                double targetCurvature = targetPlus[output] + targetMinus[output] - 2.0 * targetCenter[output];
                double error = targetCurvature - predictionCurvature;
                total += error * error;
                count++;
            }
        }
        return count == 0 ? 0.0 : total / count;
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

    private static void requireNonNegativeScore(final double value, final String name) {
        if (Double.isNaN(value) || value < 0.0) {
            throw new IllegalArgumentException(name + " must be non-negative.");
        }
    }

    private static boolean arrayIsFinite(final double[] values) {
        for (double value : values) {
            if (!Double.isFinite(value)) {
                return false;
            }
        }
        return true;
    }
}
