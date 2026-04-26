package com.github.javachaos.javaneuralnetwork.examples;

import java.util.List;

public final class CheckerboardProblem extends GeometricClassificationProblem {

    private static final String KEY = "checkerboard";
    private static final String DESCRIPTION = "Checkerboard splits the input square into a 4 by 4 alternating grid. "
            + "Nearby points can have opposite labels, so the learner needs repeated local structure, sharp "
            + "boundaries, or feature encodings that make the alternating pattern easier to separate.";

    public CheckerboardProblem(final int gridSize) {
        super(KEY, "Checkerboard", DESCRIPTION, List.of("checker"), gridSize, CheckerboardProblem::targetValue);
    }

    private static double targetValue(final double[] input) {
        int x = cellIndex(input[0], 4);
        int y = cellIndex(input[1], 4);
        return (x + y) % 2 == 0 ? 0.0 : 1.0;
    }
}
