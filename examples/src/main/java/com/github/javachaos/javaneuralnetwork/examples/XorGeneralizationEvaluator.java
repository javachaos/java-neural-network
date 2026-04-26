package com.github.javachaos.javaneuralnetwork.examples;

import java.util.Objects;
import java.util.Random;

/**
 * Scores whether an XOR learner discovered a usable rule beyond the four
 * Boolean corner samples.
 */
public final class XorGeneralizationEvaluator {

    private static final double[][] CORNERS = {
            {0.0, 0.0},
            {0.0, 1.0},
            {1.0, 0.0},
            {1.0, 1.0}
    };

    private static final double[] CORNER_TARGETS = {0.0, 1.0, 1.0, 0.0};

    private XorGeneralizationEvaluator() {
    }

    public record GeneralizationMetrics(
            double surfaceMeanSquaredError,
            double jitterMeanSquaredError,
            double smoothnessPenalty) {

        public GeneralizationMetrics {
            requireNonNegativeScore(surfaceMeanSquaredError, "Surface mean squared error");
            requireNonNegativeScore(jitterMeanSquaredError, "Jitter mean squared error");
            requireNonNegativeScore(smoothnessPenalty, "Smoothness penalty");
        }
    }

    public static GeneralizationMetrics evaluate(
            final RichEvolvedXorLearner learner,
            final int gridSize,
            final int jitterSamplesPerCorner,
            final double jitterRadius,
            final long seed) {
        Objects.requireNonNull(learner, "Learner cannot be null.");
        if (gridSize < 2) {
            throw new IllegalArgumentException("Grid size must be at least two.");
        }
        if (jitterSamplesPerCorner < 0) {
            throw new IllegalArgumentException("Jitter samples per corner cannot be negative.");
        }
        if (!Double.isFinite(jitterRadius) || jitterRadius < 0.0) {
            throw new IllegalArgumentException("Jitter radius must be finite and non-negative.");
        }

        double[][] predictions = continuousPredictions(learner, gridSize);
        double surfaceMeanSquaredError = surfaceMeanSquaredError(predictions);
        double jitterMeanSquaredError = jitterMeanSquaredError(
                learner,
                jitterSamplesPerCorner,
                jitterRadius,
                new Random(seed));
        double smoothnessPenalty = smoothnessPenalty(predictions);
        return new GeneralizationMetrics(surfaceMeanSquaredError, jitterMeanSquaredError, smoothnessPenalty);
    }

    public static double fuzzyXorTarget(final double x, final double y) {
        double clampedX = clamp01(x);
        double clampedY = clamp01(y);
        return clampedX + clampedY - 2.0 * clampedX * clampedY;
    }

    private static double[][] continuousPredictions(final RichEvolvedXorLearner learner, final int gridSize) {
        double[][] predictions = new double[gridSize][gridSize];
        double denominator = gridSize - 1.0;
        for (int row = 0; row < gridSize; row++) {
            double y = row / denominator;
            for (int column = 0; column < gridSize; column++) {
                double x = column / denominator;
                double prediction = learner.predict(x, y);
                if (!Double.isFinite(prediction)) {
                    throw new IllegalArgumentException("Learner produced a non-finite prediction.");
                }
                predictions[row][column] = prediction;
            }
        }
        return predictions;
    }

    private static double surfaceMeanSquaredError(final double[][] predictions) {
        double total = 0.0;
        int count = 0;
        double denominator = predictions.length - 1.0;
        for (int row = 0; row < predictions.length; row++) {
            double y = row / denominator;
            for (int column = 0; column < predictions[row].length; column++) {
                double x = column / denominator;
                double error = fuzzyXorTarget(x, y) - predictions[row][column];
                total += error * error;
                count++;
            }
        }
        return total / count;
    }

    private static double jitterMeanSquaredError(
            final RichEvolvedXorLearner learner,
            final int jitterSamplesPerCorner,
            final double jitterRadius,
            final Random random) {
        if (jitterSamplesPerCorner == 0 || jitterRadius == 0.0) {
            return 0.0;
        }
        double total = 0.0;
        int count = 0;
        for (int corner = 0; corner < CORNERS.length; corner++) {
            for (int sample = 0; sample < jitterSamplesPerCorner; sample++) {
                double x = jitter(CORNERS[corner][0], jitterRadius, random);
                double y = jitter(CORNERS[corner][1], jitterRadius, random);
                double prediction = learner.predict(x, y);
                if (!Double.isFinite(prediction)) {
                    return Double.POSITIVE_INFINITY;
                }
                double error = CORNER_TARGETS[corner] - prediction;
                total += error * error;
                count++;
            }
        }
        return total / count;
    }

    private static double smoothnessPenalty(final double[][] predictions) {
        if (predictions.length < 3) {
            return 0.0;
        }
        double total = 0.0;
        int count = 0;
        for (int row = 1; row < predictions.length - 1; row++) {
            for (int column = 1; column < predictions[row].length - 1; column++) {
                double laplacian = predictions[row][column - 1]
                        + predictions[row][column + 1]
                        + predictions[row - 1][column]
                        + predictions[row + 1][column]
                        - 4.0 * predictions[row][column];
                total += laplacian * laplacian;
                count++;
            }
        }
        return count == 0 ? 0.0 : total / count;
    }

    private static double jitter(final double value, final double radius, final Random random) {
        return clamp01(value + random.nextDouble(-radius, radius));
    }

    private static double clamp01(final double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private static void requireNonNegativeScore(final double value, final String name) {
        if (Double.isNaN(value) || value < 0.0) {
            throw new IllegalArgumentException(name + " must be non-negative.");
        }
    }
}
