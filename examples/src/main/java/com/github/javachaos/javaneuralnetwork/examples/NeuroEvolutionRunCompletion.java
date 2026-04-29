package com.github.javachaos.javaneuralnetwork.examples;

import java.util.Objects;

/**
 * Final result summary for a worker run.
 */
public record NeuroEvolutionRunCompletion(
        NeuroEvolutionProblem problem,
        XorNeuroEvolution.EvolutionConfig config,
        XorNeuroEvolution.CandidateScore best,
        boolean stopped) {

    public NeuroEvolutionRunCompletion {
        Objects.requireNonNull(problem, "Problem cannot be null.");
        Objects.requireNonNull(config, "Config cannot be null.");
        Objects.requireNonNull(best, "Best score cannot be null.");
    }
}
