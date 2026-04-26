package com.github.javachaos.javaneuralnetwork.examples;

import java.util.List;

public final class CircleProblem extends GeometricClassificationProblem {

    private static final String KEY = "circle";
    private static final String DESCRIPTION = "Circle is a two-dimensional classification problem with a curved "
            + "decision boundary. Points inside a centered disk are class 1 and points outside are class 0. It "
            + "tests whether the learner can represent geometry that a plain linear separator cannot capture.";

    public CircleProblem(final int gridSize) {
        super(KEY, "Circle", DESCRIPTION, List.of("disk"), gridSize, CircleProblem::targetValue);
    }

    private static double targetValue(final double[] input) {
        return distanceFromCenter(input) <= 0.34 ? 1.0 : 0.0;
    }
}
