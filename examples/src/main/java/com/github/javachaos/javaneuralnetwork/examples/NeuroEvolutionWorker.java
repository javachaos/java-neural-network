package com.github.javachaos.javaneuralnetwork.examples;

import java.util.Objects;

/**
 * Execution boundary for neuro-evolution work.
 *
 * <p>The Swing GUI depends on this interface so the current in-process Java
 * runner can later be replaced by a Rust/CUDA worker without changing the
 * visualization code.</p>
 */
public interface NeuroEvolutionWorker extends AutoCloseable {

    NeuroEvolutionRunHandle start(NeuroEvolutionRunRequest request, NeuroEvolutionRunListener listener);

    default NeuroEvolutionRunHandle start(
            final NeuroEvolutionProblem problem,
            final XorNeuroEvolution.EvolutionConfig config,
            final NeuroEvolutionRunListener listener) {
        return start(
                new NeuroEvolutionRunRequest(
                        Objects.requireNonNull(problem, "Problem cannot be null."),
                        Objects.requireNonNull(config, "Config cannot be null.")),
                listener);
    }

    @Override
    default void close() {
    }
}
