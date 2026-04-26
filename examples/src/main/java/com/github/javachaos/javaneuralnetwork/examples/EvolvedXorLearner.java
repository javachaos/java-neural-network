package com.github.javachaos.javaneuralnetwork.examples;

import java.util.Objects;
import java.util.Random;

/**
 * A tiny learner used to evaluate candidate local update rules on XOR.
 */
public final class EvolvedXorLearner {

    private static final double[][] INPUTS = {
            {1.0, 0.0, 0.0},
            {1.0, 0.0, 1.0},
            {1.0, 1.0, 0.0},
            {1.0, 1.0, 1.0}
    };

    private static final double[] TARGETS = {0.0, 1.0, 1.0, 0.0};

    private final XorLearningRuleGenome genome;
    private final double[][] hiddenWeights;
    private final double[][] previousHiddenDeltas;
    private final double[] outputWeights;
    private final double[] previousOutputDeltas;

    public EvolvedXorLearner(final XorLearningRuleGenome genome, final Random random) {
        this.genome = Objects.requireNonNull(genome, "Genome cannot be null.");
        Objects.requireNonNull(random, "Random cannot be null.");
        this.hiddenWeights = new double[2][3];
        this.previousHiddenDeltas = new double[2][3];
        this.outputWeights = new double[3];
        this.previousOutputDeltas = new double[3];
        initialize(random);
    }

    public double train(final int maxEpochs, final double targetMeanSquaredError) {
        if (maxEpochs <= 0) {
            throw new IllegalArgumentException("Max epochs must be positive.");
        }
        if (!Double.isFinite(targetMeanSquaredError) || targetMeanSquaredError <= 0.0) {
            throw new IllegalArgumentException("Target mean squared error must be positive and finite.");
        }

        double meanSquaredError = evaluateMeanSquaredError();
        for (int epoch = 0; epoch < maxEpochs && meanSquaredError > targetMeanSquaredError; epoch++) {
            for (int index = 0; index < INPUTS.length; index++) {
                trainSample(INPUTS[index], TARGETS[index]);
            }
            meanSquaredError = evaluateMeanSquaredError();
            if (!Double.isFinite(meanSquaredError)) {
                return Double.POSITIVE_INFINITY;
            }
        }
        return meanSquaredError;
    }

    public double predict(final double x, final double y) {
        return forward(new double[] {1.0, x, y}).output();
    }

    public double evaluateMeanSquaredError() {
        double total = 0.0;
        for (int i = 0; i < INPUTS.length; i++) {
            double error = TARGETS[i] - forward(INPUTS[i]).output();
            total += error * error;
        }
        return total / INPUTS.length;
    }

    public double accuracy() {
        int correct = 0;
        for (int i = 0; i < INPUTS.length; i++) {
            double prediction = forward(INPUTS[i]).output() >= 0.5 ? 1.0 : 0.0;
            if (prediction == TARGETS[i]) {
                correct++;
            }
        }
        return correct / (double) INPUTS.length;
    }

    private void trainSample(final double[] input, final double target) {
        ForwardPass pass = forward(input);
        double rawError = target - pass.output();
        double outputSignal = signedPower(
                clip(rawError, genome.errorClip()),
                genome.outputErrorPower())
                * derivative(pass.output())
                * genome.activationSlope();

        updateOutputWeights(pass.hiddenWithBias(), outputSignal);
        updateHiddenWeights(input, rawError, outputSignal, pass.hidden());
    }

    private void updateOutputWeights(final double[] hiddenWithBias, final double outputSignal) {
        for (int i = 0; i < outputWeights.length; i++) {
            double delta = genome.outputLearningRate() * outputSignal * hiddenWithBias[i]
                    + genome.momentum() * previousOutputDeltas[i]
                    - genome.weightDecay() * outputWeights[i];
            outputWeights[i] += delta;
            previousOutputDeltas[i] = delta;
        }
    }

    private void updateHiddenWeights(
            final double[] input,
            final double rawError,
            final double outputSignal,
            final double[] hidden) {
        for (int neuron = 0; neuron < hiddenWeights.length; neuron++) {
            double credit = genome.hiddenCreditScale() * outputSignal * outputWeights[neuron + 1]
                    + genome.feedbackCreditScale() * rawError;
            double hiddenSignal = signedPower(
                    clip(credit, genome.errorClip()),
                    genome.hiddenErrorPower())
                    * derivative(hidden[neuron])
                    * genome.activationSlope();
            for (int weight = 0; weight < hiddenWeights[neuron].length; weight++) {
                double delta = genome.inputLearningRate() * hiddenSignal * input[weight]
                        + genome.momentum() * previousHiddenDeltas[neuron][weight]
                        - genome.weightDecay() * hiddenWeights[neuron][weight];
                hiddenWeights[neuron][weight] += delta;
                previousHiddenDeltas[neuron][weight] = delta;
            }
        }
    }

    private ForwardPass forward(final double[] input) {
        double[] hidden = new double[hiddenWeights.length];
        double[] hiddenWithBias = new double[hiddenWeights.length + 1];
        hiddenWithBias[0] = 1.0;
        for (int neuron = 0; neuron < hiddenWeights.length; neuron++) {
            hidden[neuron] = sigmoid(genome.activationSlope() * dot(hiddenWeights[neuron], input));
            hiddenWithBias[neuron + 1] = hidden[neuron];
        }
        double output = sigmoid(genome.activationSlope() * dot(outputWeights, hiddenWithBias));
        return new ForwardPass(hidden, hiddenWithBias, output);
    }

    private void initialize(final Random random) {
        for (int neuron = 0; neuron < hiddenWeights.length; neuron++) {
            for (int weight = 0; weight < hiddenWeights[neuron].length; weight++) {
                hiddenWeights[neuron][weight] = randomWeight(random);
            }
        }
        for (int weight = 0; weight < outputWeights.length; weight++) {
            outputWeights[weight] = randomWeight(random);
        }
    }

    private static double randomWeight(final Random random) {
        return random.nextDouble(-1.0, 1.0);
    }

    private static double dot(final double[] left, final double[] right) {
        double total = 0.0;
        for (int i = 0; i < left.length; i++) {
            total += left[i] * right[i];
        }
        return total;
    }

    private static double sigmoid(final double value) {
        if (value > 40.0) {
            return 1.0;
        }
        if (value < -40.0) {
            return 0.0;
        }
        return 1.0 / (1.0 + Math.exp(-value));
    }

    private static double derivative(final double activatedValue) {
        return activatedValue * (1.0 - activatedValue);
    }

    private static double signedPower(final double value, final double power) {
        return Math.copySign(Math.pow(Math.abs(value), power), value);
    }

    private static double clip(final double value, final double clip) {
        return Math.max(-clip, Math.min(clip, value));
    }

    private record ForwardPass(double[] hidden, double[] hiddenWithBias, double output) {
    }
}
