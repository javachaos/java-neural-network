package com.github.javachaos.javaneuralnetwork.examples;

import java.util.List;

public final class XorProblem extends TwoInputBooleanProblem {

    private static final String KEY = "xor";
    private static final String DESCRIPTION = "XOR is the classic non-linear boolean benchmark. The learner receives "
            + "two inputs and must return 1 only when exactly one input is active. It cannot be solved by a single "
            + "linear separator, so it is a compact test for whether evolution discovers useful hidden structure.";

    public XorProblem() {
        this(DEFAULT_GRID_SIZE);
    }

    public XorProblem(final int gridSize) {
        super(KEY, "XOR", DESCRIPTION, List.of(), gridSize, XorProblem::fuzzyTarget);
    }

    private static double fuzzyTarget(final double[] input) {
        double x = clamp01(input[0]);
        double y = clamp01(input[1]);
        return x + y - 2.0 * x * y;
    }
}
