package com.github.javachaos.javaneuralnetwork.examples;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Input encodings the evolutionary search may select or augment.
 */
public enum XorInputRepresentation {
    RAW,
    POLYNOMIAL,
    PHASE_COMPLEX,
    KERNEL_DISTANCE,
    MIXED;

    public double[] encode(
            final double x,
            final double y,
            final boolean phaseEncoding,
            final boolean kernelMemory,
            final double kernelSharpness) {
        return encode(
                new double[] {x, y},
                List.of(
                        new double[] {0.0, 0.0},
                        new double[] {0.0, 1.0},
                        new double[] {1.0, 0.0},
                        new double[] {1.0, 1.0}),
                phaseEncoding,
                kernelMemory,
                kernelSharpness);
    }

    public double[] encode(
            final double[] input,
            final List<double[]> kernelCenters,
            final boolean phaseEncoding,
            final boolean kernelMemory,
            final double kernelSharpness) {
        Objects.requireNonNull(input, "Input cannot be null.");
        Objects.requireNonNull(kernelCenters, "Kernel centers cannot be null.");
        if (input.length == 0) {
            throw new IllegalArgumentException("Input must have at least one dimension.");
        }
        List<Double> values = new ArrayList<>();
        switch (this) {
            case RAW -> addRaw(values, input);
            case POLYNOMIAL -> addPolynomial(values, input);
            case PHASE_COMPLEX -> {
                addRaw(values, input);
                addPhase(values, input);
            }
            case KERNEL_DISTANCE -> {
                addRaw(values, input);
                addKernel(values, input, kernelCenters, kernelSharpness);
            }
            case MIXED -> {
                addPolynomial(values, input);
                addPhase(values, input);
                addKernel(values, input, kernelCenters, kernelSharpness);
            }
        }
        if (phaseEncoding && this != PHASE_COMPLEX && this != MIXED) {
            addPhase(values, input);
        }
        if (kernelMemory && this != KERNEL_DISTANCE && this != MIXED) {
            addKernel(values, input, kernelCenters, kernelSharpness);
        }
        return toArray(values);
    }

    private static void addRaw(final List<Double> values, final double[] input) {
        values.add(1.0);
        for (double value : input) {
            values.add(value);
        }
    }

    private static void addPolynomial(final List<Double> values, final double[] input) {
        addRaw(values, input);
        for (int left = 0; left < input.length; left++) {
            for (int right = left + 1; right < input.length; right++) {
                values.add(input[left] * input[right]);
            }
        }
        for (double value : input) {
            values.add(value * value);
        }
        for (int left = 0; left < input.length; left++) {
            for (int right = left + 1; right < input.length; right++) {
                values.add(Math.abs(input[left] - input[right]));
            }
        }
    }

    private static void addPhase(final List<Double> values, final double[] input) {
        for (double value : input) {
            values.add(Math.sin(Math.PI * value));
            values.add(Math.cos(Math.PI * value));
        }
        for (int left = 0; left < input.length; left++) {
            for (int right = left + 1; right < input.length; right++) {
                double difference = input[left] - input[right];
                values.add(Math.sin(Math.PI * difference));
                values.add(Math.cos(Math.PI * difference));
            }
        }
    }

    private static void addKernel(
            final List<Double> values,
            final double[] input,
            final List<double[]> kernelCenters,
            final double kernelSharpness) {
        for (double[] center : kernelCenters) {
            values.add(kernel(input, center, kernelSharpness));
        }
    }

    private static double kernel(
            final double[] input,
            final double[] center,
            final double sharpness) {
        if (center.length != input.length) {
            throw new IllegalArgumentException("Kernel center dimension must match input dimension.");
        }
        double distanceSquared = 0.0;
        for (int i = 0; i < input.length; i++) {
            double delta = input[i] - center[i];
            distanceSquared += delta * delta;
        }
        return Math.exp(-Math.max(0.001, sharpness) * distanceSquared);
    }

    private static double[] toArray(final List<Double> values) {
        double[] result = new double[values.size()];
        for (int i = 0; i < values.size(); i++) {
            result[i] = values.get(i);
        }
        return result;
    }
}
