package com.github.javachaos.javaneuralnetwork.examples;

import java.util.Objects;

/**
 * Generic entry point for evolving scalar learners. The older
 * {@link XorNeuroEvolution} type remains as the implementation-compatible
 * facade used by the original demo.
 */
public final class NeuroEvolution {

    private NeuroEvolution() {
    }

    public static XorNeuroEvolution.EvolutionResult evolve(final NeuroEvolutionProblem problem) {
        return evolve(problem, XorNeuroEvolution.EvolutionConfig.defaults());
    }

    public static XorNeuroEvolution.EvolutionResult evolve(
            final NeuroEvolutionProblem problem,
            final XorNeuroEvolution.EvolutionConfig config) {
        return XorNeuroEvolution.evolve(
                Objects.requireNonNull(problem, "Problem cannot be null."),
                Objects.requireNonNull(config, "Config cannot be null."));
    }

    public static XorNeuroEvolution.CandidateScore evaluate(
            final NeuroEvolutionProblem problem,
            final EvolvableXorGenome genome,
            final XorNeuroEvolution.EvolutionConfig config,
            final int generation) {
        return XorNeuroEvolution.evaluate(
                Objects.requireNonNull(problem, "Problem cannot be null."),
                Objects.requireNonNull(genome, "Genome cannot be null."),
                Objects.requireNonNull(config, "Config cannot be null."),
                generation);
    }
}
