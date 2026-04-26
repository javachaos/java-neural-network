package com.github.javachaos.javaneuralnetwork.examples;

import java.util.Objects;
import java.util.Random;

/**
 * Backwards-compatible XOR learner facade over the generic scalar learner.
 */
public final class RichEvolvedXorLearner {

    private final RichEvolvedProblemLearner delegate;

    public RichEvolvedXorLearner(final EvolvableXorGenome genome, final Random random) {
        this.delegate = new RichEvolvedProblemLearner(
                new XorProblem(),
                Objects.requireNonNull(genome, "Genome cannot be null."),
                Objects.requireNonNull(random, "Random cannot be null."));
    }

    public double train(final int maxEpochs, final double targetMeanSquaredError) {
        return delegate.train(maxEpochs, targetMeanSquaredError);
    }

    public double predict(final double x, final double y) {
        return delegate.predict(x, y);
    }

    public double evaluateMeanSquaredError() {
        return delegate.evaluateMeanSquaredError();
    }

    public double configuredLoss() {
        return delegate.configuredLoss();
    }

    public double accuracy() {
        return delegate.accuracy();
    }

    public RichEvolvedProblemLearner.NetworkSnapshot snapshot() {
        return delegate.snapshot();
    }
}
