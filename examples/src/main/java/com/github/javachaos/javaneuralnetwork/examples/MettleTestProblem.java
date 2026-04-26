package com.github.javachaos.javaneuralnetwork.examples;

import java.util.List;

public final class MettleTestProblem extends HardClassificationProblem {

    private static final String KEY = "mettle-test";
    private static final String DESCRIPTION = "Mettle test is the stress benchmark. It combines spiral bands, "
            + "high-frequency wave interference, radial rings, and small island exceptions on a denser training "
            + "and generalization grid. It is meant to expose overfitting and pressure-test topology, kernel memory, "
            + "phase encoding, activations, and local update rules.";

    public MettleTestProblem(final int gridSize) {
        super(KEY, "Mettle test", DESCRIPTION, List.of("hard", "mettle", "gauntlet", "chaos-gauntlet"), gridSize,
                MettleTestProblem::targetValue);
    }

    private static double targetValue(final double[] input) {
        double x = clamp01(input[0]);
        double y = clamp01(input[1]);
        double dx = x - 0.5;
        double dy = y - 0.5;
        double radius = Math.min(1.0, Math.sqrt(dx * dx + dy * dy) / Math.sqrt(0.5));
        double angle = Math.atan2(dy, dx);
        if (angle < 0.0) {
            angle += TWO_PI;
        }
        double spiral = Math.sin(18.0 * radius + 5.0 * angle);
        double weave = Math.sin(8.0 * Math.PI * x + 2.5 * Math.sin(TWO_PI * y))
                * Math.cos(10.0 * Math.PI * y + 1.5 * Math.cos(TWO_PI * x));
        double rings = Math.cos(42.0 * radius);
        double positiveIslands = gaussian(x, y, 0.18, 0.82, 0.018)
                + gaussian(x, y, 0.78, 0.22, 0.015);
        double negativeIslands = gaussian(x, y, 0.52, 0.50, 0.025)
                + gaussian(x, y, 0.86, 0.74, 0.018);
        double field = spiral + 0.75 * weave + 0.55 * rings + 1.6 * positiveIslands - 1.8 * negativeIslands;
        return field >= 0.12 ? 1.0 : 0.0;
    }
}
