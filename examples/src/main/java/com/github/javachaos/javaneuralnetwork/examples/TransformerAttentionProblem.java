package com.github.javachaos.javaneuralnetwork.examples;

import java.util.ArrayList;
import java.util.List;

/**
 * Tiny key-value attention task inspired by a single transformer attention head.
 */
public final class TransformerAttentionProblem extends NeuroEvolutionProblem {

    private static final String KEY = "transformer-attention";
    private static final int INPUT_DIMENSIONS = 5;
    private static final int OUTPUT_DIMENSIONS = 3;
    private static final double ATTENTION_SHARPNESS = 14.0;
    private static final String DESCRIPTION = "Transformer attention is a tiny key-value retrieval benchmark inspired "
            + "by a single attention head. The five raw inputs are query, key A, key B, value A, and value B. The "
            + "network must infer both normalized attention weights and the attended value, so it pressures similarity, "
            + "soft selection, interpolation, and multi-output reconstruction rather than a simple boundary.";

    public TransformerAttentionProblem(final int gridSize) {
        super(
                KEY,
                "Transformer attention",
                DESCRIPTION,
                List.of("transformer", "attention", "key-value-attention"),
                INPUT_DIMENSIONS,
                OUTPUT_DIMENSIONS,
                buildTrainingSamples(),
                generalizationSamples(Math.max(gridSize, DEFAULT_GRID_SIZE)),
                jitterSamples(),
                buildKernelCenters(),
                false,
                TransformerAttentionProblem::targetValue);
    }

    @Override
    public String outputLabel(final int outputIndex) {
        return switch (outputIndex) {
            case 0 -> "wA";
            case 1 -> "wB";
            case 2 -> "V";
            default -> throw new IllegalArgumentException("Output index must be in the attention output range.");
        };
    }

    private static List<NeuroEvolutionSample> buildTrainingSamples() {
        List<NeuroEvolutionSample> samples = new ArrayList<>();
        double[] queries = {0.0, 0.25, 0.5, 0.75, 1.0};
        double[][] keyPairs = {
                {0.0, 1.0},
                {0.2, 0.8},
                {0.35, 0.65},
                {0.75, 0.25},
        };
        double[][] valuePairs = {
                {0.0, 1.0},
                {1.0, 0.0},
                {0.25, 0.75},
                {0.75, 0.25},
        };
        for (double query : queries) {
            for (double[] keys : keyPairs) {
                for (double[] values : valuePairs) {
                    samples.add(sample(query, keys[0], keys[1], values[0], values[1]));
                }
            }
        }
        return List.copyOf(samples);
    }

    private static List<NeuroEvolutionSample> generalizationSamples(final int gridSize) {
        int sampleCount = Math.max(64, gridSize * gridSize);
        List<NeuroEvolutionSample> samples = new ArrayList<>(sampleCount);
        for (int i = 0; i < sampleCount; i++) {
            samples.add(sample(
                    lowDiscrepancy(i, 0.6190339887498948),
                    lowDiscrepancy(i, 0.4142135623730950),
                    lowDiscrepancy(i, 0.7320508075688772),
                    lowDiscrepancy(i, 0.2360679774997897),
                    lowDiscrepancy(i, 0.5311288741492746)));
        }
        return List.copyOf(samples);
    }

    private static List<NeuroEvolutionSample> jitterSamples() {
        return List.of(
                sample(0.0, 0.0, 1.0, 0.0, 1.0),
                sample(0.5, 0.25, 0.75, 0.2, 0.8),
                sample(1.0, 0.0, 1.0, 1.0, 0.0),
                sample(0.65, 0.7, 0.2, 0.9, 0.1),
                sample(0.35, 0.8, 0.3, 0.1, 0.9));
    }

    private static List<double[]> buildKernelCenters() {
        return List.of(
                new double[] {0.0, 0.0, 1.0, 0.0, 1.0},
                new double[] {0.25, 0.2, 0.8, 0.25, 0.75},
                new double[] {0.5, 0.35, 0.65, 0.5, 0.5},
                new double[] {0.75, 0.8, 0.2, 0.75, 0.25},
                new double[] {1.0, 1.0, 0.0, 1.0, 0.0});
    }

    private static NeuroEvolutionSample sample(
            final double query,
            final double keyA,
            final double keyB,
            final double valueA,
            final double valueB) {
        return vectorSample(new double[] {query, keyA, keyB, valueA, valueB}, TransformerAttentionProblem::targetValue);
    }

    private static double[] targetValue(final double[] input) {
        double query = clamp01(input[0]);
        double keyA = clamp01(input[1]);
        double keyB = clamp01(input[2]);
        double valueA = clamp01(input[3]);
        double valueB = clamp01(input[4]);
        double scoreA = -ATTENTION_SHARPNESS * squared(query - keyA);
        double scoreB = -ATTENTION_SHARPNESS * squared(query - keyB);
        double max = Math.max(scoreA, scoreB);
        double expA = Math.exp(scoreA - max);
        double expB = Math.exp(scoreB - max);
        double weightA = expA / (expA + expB);
        double weightB = 1.0 - weightA;
        return new double[] {
                weightA,
                weightB,
                clamp01(weightA * valueA + weightB * valueB)
        };
    }

    private static double lowDiscrepancy(final int index, final double step) {
        double value = 0.5 + (index + 1) * step;
        return value - Math.floor(value);
    }

    private static double squared(final double value) {
        return value * value;
    }
}
