package com.github.javachaos.javaneuralnetwork.examples;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Reports an automatic checkpoint attempt made by a worker.
 */
public record NeuroEvolutionCheckpointEvent(
        NeuroEvolutionProblem problem,
        XorNeuroEvolution.CandidateScore score,
        Path path,
        boolean saved,
        String message) {

    public NeuroEvolutionCheckpointEvent {
        Objects.requireNonNull(problem, "Problem cannot be null.");
        Objects.requireNonNull(score, "Score cannot be null.");
        Objects.requireNonNull(path, "Path cannot be null.");
    }
}
