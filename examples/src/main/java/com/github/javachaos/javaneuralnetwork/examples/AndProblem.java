package com.github.javachaos.javaneuralnetwork.examples;

import java.util.List;

public final class AndProblem extends TwoInputBooleanProblem {

    private static final String KEY = "and";
    private static final String DESCRIPTION = "AND is a simple two-input classification sanity check. The target is "
            + "high only when both inputs are high. Strong champions should solve it quickly; if this problem "
            + "struggles, the basic training loop or scoring setup is probably unhealthy.";

    public AndProblem(final int gridSize) {
        super(KEY, "AND", DESCRIPTION, List.of(), gridSize, input -> clamp01(input[0] * input[1]));
    }
}
