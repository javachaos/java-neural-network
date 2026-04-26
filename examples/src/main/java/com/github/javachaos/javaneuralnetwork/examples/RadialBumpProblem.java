package com.github.javachaos.javaneuralnetwork.examples;

import java.util.List;

public final class RadialBumpProblem extends NeuroEvolutionProblem {

    private static final String KEY = "radial-bump";
    private static final String DESCRIPTION = "Radial bump is a smooth two-dimensional regression surface. The target "
            + "is highest at the center of the square and fades continuously toward the edges. It rewards smooth "
            + "interpolation, Gaussian-like activations, kernel features, and low jitter.";

    public RadialBumpProblem(final int gridSize) {
        super(
                KEY,
                "Radial bump",
                DESCRIPTION,
                List.of("radial", "bump"),
                2,
                gridSamples(5, RadialBumpProblem::targetValue),
                gridSamples(gridSize, RadialBumpProblem::targetValue),
                gridSamples(3, RadialBumpProblem::targetValue),
                gridInputs(5),
                false,
                RadialBumpProblem::targetValue);
    }

    private static double targetValue(final double[] input) {
        double dx = input[0] - 0.5;
        double dy = input[1] - 0.5;
        return Math.exp(-14.0 * (dx * dx + dy * dy));
    }
}
