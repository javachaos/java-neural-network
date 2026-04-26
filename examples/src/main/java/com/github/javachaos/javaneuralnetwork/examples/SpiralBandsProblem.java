package com.github.javachaos.javaneuralnetwork.examples;

import java.util.List;

public final class SpiralBandsProblem extends GeometricClassificationProblem {

    private static final String KEY = "spiral-bands";
    private static final String DESCRIPTION = "Spiral bands is a two-dimensional classification problem with curved, "
            + "periodic bands wrapped around the center. It is deliberately friendly to phase features and sine-like "
            + "hidden units, and it exposes whether the model is learning the surface or merely memorizing grid samples.";

    public SpiralBandsProblem(final int gridSize) {
        super(KEY, "Spiral bands", DESCRIPTION, List.of("spiral"), gridSize, SpiralBandsProblem::targetValue);
    }

    private static double targetValue(final double[] input) {
        double dx = input[0] - 0.5;
        double dy = input[1] - 0.5;
        double radius = Math.min(1.0, Math.sqrt(dx * dx + dy * dy) / Math.sqrt(0.5));
        double angle = Math.atan2(dy, dx);
        if (angle < 0.0) {
            angle += TWO_PI;
        }
        return Math.sin(11.0 * radius + 3.0 * angle) >= 0.0 ? 1.0 : 0.0;
    }
}
