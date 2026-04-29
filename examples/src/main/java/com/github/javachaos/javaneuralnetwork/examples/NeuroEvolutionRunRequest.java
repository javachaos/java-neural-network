package com.github.javachaos.javaneuralnetwork.examples;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Describes one evolution job submitted by a UI or another controller.
 */
public record NeuroEvolutionRunRequest(
        NeuroEvolutionProblem problem,
        XorNeuroEvolution.EvolutionConfig config,
        boolean checkpointOnImprovement,
        Path checkpointPath,
        long progressIntervalNanos) {

    public static final long DEFAULT_PROGRESS_INTERVAL_NANOS = 100_000_000L;

    public NeuroEvolutionRunRequest(
            final NeuroEvolutionProblem problem,
            final XorNeuroEvolution.EvolutionConfig config) {
        this(
                problem,
                config,
                true,
                NeuroEvolutionChampionCheckpoint.defaultPath(problem),
                DEFAULT_PROGRESS_INTERVAL_NANOS);
    }

    public NeuroEvolutionRunRequest {
        Objects.requireNonNull(problem, "Problem cannot be null.");
        Objects.requireNonNull(config, "Config cannot be null.");
        if (checkpointOnImprovement) {
            Objects.requireNonNull(checkpointPath, "Checkpoint path cannot be null when checkpointing is enabled.");
        }
        if (progressIntervalNanos < 0L) {
            throw new IllegalArgumentException("Progress interval cannot be negative.");
        }
    }
}
