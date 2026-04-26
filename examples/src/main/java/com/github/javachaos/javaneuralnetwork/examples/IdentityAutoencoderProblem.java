package com.github.javachaos.javaneuralnetwork.examples;

import java.util.List;

/**
 * Two-channel reconstruction task for testing autoencoder-style learners.
 */
public final class IdentityAutoencoderProblem extends NeuroEvolutionProblem {

    private static final String KEY = "identity-autoencoder";
    private static final String DESCRIPTION = "Identity autoencoder is a two-output reconstruction task. The learner "
            + "receives a coordinate pair and must reproduce both normalized coordinates at the output. This tests "
            + "whether an evolved topology can preserve information through its representation, hidden layers, memory, "
            + "and transfer functions instead of only learning a single classification boundary.";

    public IdentityAutoencoderProblem(final int gridSize) {
        super(
                KEY,
                "Identity autoencoder",
                DESCRIPTION,
                List.of("autoencoder", "reconstruction", "identity"),
                2,
                2,
                vectorGridSamples(5, IdentityAutoencoderProblem::targetValue),
                vectorGridSamples(Math.max(gridSize, DEFAULT_GRID_SIZE), IdentityAutoencoderProblem::targetValue),
                vectorGridSamples(3, IdentityAutoencoderProblem::targetValue),
                gridInputs(DEFAULT_KERNEL_GRID_SIZE),
                false,
                IdentityAutoencoderProblem::targetValue);
    }

    @Override
    public String outputLabel(final int outputIndex) {
        return switch (outputIndex) {
            case 0 -> "x";
            case 1 -> "y";
            default -> throw new IllegalArgumentException("Output index must be 0 or 1.");
        };
    }

    private static double[] targetValue(final double[] input) {
        return new double[] {clamp01(input[0]), clamp01(input[1])};
    }
}
