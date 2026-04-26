package com.github.javachaos.javaneuralnetwork.examples;

import java.util.List;

public final class RingProblem extends GeometricClassificationProblem {

    private static final String KEY = "ring";
    private static final String DESCRIPTION = "Ring is a harder curved classification problem. Class 1 is a thin "
            + "annulus around the center, while both the center and outer region are class 0. It rewards learners "
            + "that can carve out two boundaries instead of just one.";

    public RingProblem(final int gridSize) {
        super(KEY, "Ring", DESCRIPTION, List.of("annulus"), gridSize, RingProblem::targetValue);
    }

    private static double targetValue(final double[] input) {
        double distance = distanceFromCenter(input);
        return distance >= 0.22 && distance <= 0.39 ? 1.0 : 0.0;
    }
}
