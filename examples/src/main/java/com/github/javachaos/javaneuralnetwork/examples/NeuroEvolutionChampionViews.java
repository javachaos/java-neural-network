package com.github.javachaos.javaneuralnetwork.examples;

import java.util.Objects;
import java.util.Random;

/**
 * Builds renderable champion snapshots from saved scores.
 */
public final class NeuroEvolutionChampionViews {

    private NeuroEvolutionChampionViews() {
    }

    public static NeuroEvolutionChampionView build(
            final NeuroEvolutionProblem problem,
            final XorNeuroEvolution.EvolutionConfig config,
            final XorNeuroEvolution.CandidateScore score) {
        Objects.requireNonNull(problem, "Problem cannot be null.");
        Objects.requireNonNull(config, "Config cannot be null.");
        Objects.requireNonNull(score, "Score cannot be null.");
        RichEvolvedProblemLearner learner = new RichEvolvedProblemLearner(
                problem,
                score.genome(),
                championRandom(config, score));
        learner.train(config.maxEpochs(), config.targetMeanSquaredError());
        NeuroEvolutionFreeEnergyMetrics freeEnergy = learner.predictiveFreeEnergy(
                problem.generalizationSamples(),
                XorNeuroEvolution.normalizedComplexity(problem, score.complexity()));
        return new NeuroEvolutionChampionView(
                score
                        .withOutputGroupScores(learner.outputGroupScores())
                        .withFreeEnergyMetrics(freeEnergy),
                learner.snapshot());
    }

    private static Random championRandom(
            final XorNeuroEvolution.EvolutionConfig config,
            final XorNeuroEvolution.CandidateScore score) {
        return new Random(config.seed() + 97_531L * (score.generation() + 1L) + 17L);
    }
}
