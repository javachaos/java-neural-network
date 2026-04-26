package com.github.javachaos.javaneuralnetwork.examples;

import java.util.List;

public final class OrProblem extends TwoInputBooleanProblem {

    private static final String KEY = "or";
    private static final String DESCRIPTION = "OR is another low-friction boolean benchmark. The target is high when "
            + "either input is high. It is intentionally easier than XOR and is useful as a control problem for "
            + "watching whether mutation and selection can preserve simple, robust solutions.";

    public OrProblem(final int gridSize) {
        super(KEY, "OR", DESCRIPTION, List.of(), gridSize, OrProblem::targetValue);
    }

    private static double targetValue(final double[] input) {
        return clamp01(input[0] + input[1] - input[0] * input[1]);
    }
}
