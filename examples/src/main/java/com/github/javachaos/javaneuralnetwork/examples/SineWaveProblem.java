package com.github.javachaos.javaneuralnetwork.examples;

import java.util.List;

public final class SineWaveProblem extends LineRegressionProblem {

    private static final String KEY = "sine-wave";
    private static final String DESCRIPTION = "Sine wave is a one-dimensional regression task. The learner maps x "
            + "in [0, 1] to a smooth periodic target. It is a clean test for periodic transfer functions, phase "
            + "encodings, and whether the model can generalize between sampled points.";

    public SineWaveProblem(final int gridSize) {
        super(KEY, "Sine wave", DESCRIPTION, List.of("sine", "wave"), gridSize, SineWaveProblem::targetValue);
    }

    private static double targetValue(final double[] input) {
        return 0.5 + 0.5 * Math.sin(TWO_PI * clamp01(input[0]));
    }
}
