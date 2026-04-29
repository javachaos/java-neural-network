package com.github.javachaos.javaneuralnetwork.examples;

import com.github.javachaos.javaneuralnetwork.examples.RichEvolvedProblemLearner.NetworkSnapshot;

import java.util.Objects;

/**
 * Progress payload emitted by a local or remote evolution worker.
 */
public record NeuroEvolutionRunProgress(
        NeuroEvolutionProblem problem,
        XorNeuroEvolution.EvolutionConfig config,
        int generation,
        XorNeuroEvolution.CandidateScore champion,
        XorNeuroEvolution.CandidateScore best,
        String populationSummary,
        String strategySummary,
        double generationsPerSecond,
        NetworkSnapshot snapshot,
        NetworkSnapshot bestSnapshot) {

    public NeuroEvolutionRunProgress {
        Objects.requireNonNull(problem, "Problem cannot be null.");
        Objects.requireNonNull(config, "Config cannot be null.");
        Objects.requireNonNull(champion, "Champion score cannot be null.");
        Objects.requireNonNull(best, "Best score cannot be null.");
        Objects.requireNonNull(populationSummary, "Population summary cannot be null.");
        Objects.requireNonNull(strategySummary, "Strategy summary cannot be null.");
        Objects.requireNonNull(snapshot, "Snapshot cannot be null.");
        Objects.requireNonNull(bestSnapshot, "Best snapshot cannot be null.");
        if (generation < 0) {
            throw new IllegalArgumentException("Generation cannot be negative.");
        }
        if (!Double.isFinite(generationsPerSecond) || generationsPerSecond < 0.0) {
            throw new IllegalArgumentException("Generation rate must be finite and non-negative.");
        }
    }
}
