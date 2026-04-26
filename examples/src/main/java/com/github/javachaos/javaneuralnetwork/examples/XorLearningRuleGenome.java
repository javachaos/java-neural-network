package com.github.javachaos.javaneuralnetwork.examples;

import java.util.Objects;
import java.util.Random;

/**
 * Encodes a small local learning rule that can be evolved against XOR.
 */
public record XorLearningRuleGenome(
        double inputLearningRate,
        double outputLearningRate,
        double momentum,
        double weightDecay,
        double hiddenCreditScale,
        double feedbackCreditScale,
        double outputErrorPower,
        double hiddenErrorPower,
        double activationSlope,
        double errorClip) {

    private static final double MIN_RATE = 0.001;
    private static final double MAX_RATE = 1.5;
    private static final double MIN_DECAY = 0.0;
    private static final double MAX_DECAY = 0.02;
    private static final double MIN_CREDIT = -2.0;
    private static final double MAX_CREDIT = 2.0;
    private static final double MIN_POWER = 0.25;
    private static final double MAX_POWER = 3.0;
    private static final double MIN_SLOPE = 0.2;
    private static final double MAX_SLOPE = 5.0;
    private static final double MIN_CLIP = 0.05;
    private static final double MAX_CLIP = 3.0;

    public XorLearningRuleGenome {
        requireFinite(inputLearningRate, "Input learning rate");
        requireFinite(outputLearningRate, "Output learning rate");
        requireFinite(momentum, "Momentum");
        requireFinite(weightDecay, "Weight decay");
        requireFinite(hiddenCreditScale, "Hidden credit scale");
        requireFinite(feedbackCreditScale, "Feedback credit scale");
        requireFinite(outputErrorPower, "Output error power");
        requireFinite(hiddenErrorPower, "Hidden error power");
        requireFinite(activationSlope, "Activation slope");
        requireFinite(errorClip, "Error clip");
    }

    public static XorLearningRuleGenome random(final Random random) {
        Objects.requireNonNull(random, "Random cannot be null.");
        return new XorLearningRuleGenome(
                randomRange(random, MIN_RATE, MAX_RATE),
                randomRange(random, MIN_RATE, MAX_RATE),
                randomRange(random, 0.0, 0.95),
                randomRange(random, MIN_DECAY, MAX_DECAY),
                randomRange(random, MIN_CREDIT, MAX_CREDIT),
                randomRange(random, MIN_CREDIT, MAX_CREDIT),
                randomRange(random, MIN_POWER, MAX_POWER),
                randomRange(random, MIN_POWER, MAX_POWER),
                randomRange(random, MIN_SLOPE, MAX_SLOPE),
                randomRange(random, MIN_CLIP, MAX_CLIP));
    }

    public XorLearningRuleGenome crossover(final XorLearningRuleGenome other, final Random random) {
        Objects.requireNonNull(other, "Other genome cannot be null.");
        Objects.requireNonNull(random, "Random cannot be null.");
        return new XorLearningRuleGenome(
                choose(random, inputLearningRate, other.inputLearningRate),
                choose(random, outputLearningRate, other.outputLearningRate),
                choose(random, momentum, other.momentum),
                choose(random, weightDecay, other.weightDecay),
                choose(random, hiddenCreditScale, other.hiddenCreditScale),
                choose(random, feedbackCreditScale, other.feedbackCreditScale),
                choose(random, outputErrorPower, other.outputErrorPower),
                choose(random, hiddenErrorPower, other.hiddenErrorPower),
                choose(random, activationSlope, other.activationSlope),
                choose(random, errorClip, other.errorClip));
    }

    public XorLearningRuleGenome mutate(final Random random, final double intensity) {
        Objects.requireNonNull(random, "Random cannot be null.");
        if (!Double.isFinite(intensity) || intensity < 0.0) {
            throw new IllegalArgumentException("Mutation intensity must be finite and non-negative.");
        }
        return new XorLearningRuleGenome(
                mutateRange(random, inputLearningRate, intensity, MIN_RATE, MAX_RATE),
                mutateRange(random, outputLearningRate, intensity, MIN_RATE, MAX_RATE),
                mutateRange(random, momentum, intensity, 0.0, 0.95),
                mutateRange(random, weightDecay, intensity, MIN_DECAY, MAX_DECAY),
                mutateRange(random, hiddenCreditScale, intensity, MIN_CREDIT, MAX_CREDIT),
                mutateRange(random, feedbackCreditScale, intensity, MIN_CREDIT, MAX_CREDIT),
                mutateRange(random, outputErrorPower, intensity, MIN_POWER, MAX_POWER),
                mutateRange(random, hiddenErrorPower, intensity, MIN_POWER, MAX_POWER),
                mutateRange(random, activationSlope, intensity, MIN_SLOPE, MAX_SLOPE),
                mutateRange(random, errorClip, intensity, MIN_CLIP, MAX_CLIP));
    }

    private static double choose(final Random random, final double left, final double right) {
        return random.nextBoolean() ? left : right;
    }

    private static double mutateRange(
            final Random random,
            final double value,
            final double intensity,
            final double min,
            final double max) {
        double scale = (max - min) * intensity;
        return clamp(value + random.nextGaussian() * scale, min, max);
    }

    private static double randomRange(final Random random, final double min, final double max) {
        return min + random.nextDouble() * (max - min);
    }

    private static double clamp(final double value, final double min, final double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static void requireFinite(final double value, final String label) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(label + " must be finite.");
        }
    }
}
