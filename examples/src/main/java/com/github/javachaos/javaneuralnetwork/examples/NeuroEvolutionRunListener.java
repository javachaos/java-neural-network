package com.github.javachaos.javaneuralnetwork.examples;

/**
 * Receives lifecycle events from a neuro-evolution worker.
 */
public interface NeuroEvolutionRunListener {

    default void onProgress(final NeuroEvolutionRunProgress progress) {
    }

    default void onCheckpoint(final NeuroEvolutionCheckpointEvent checkpoint) {
    }

    default void onComplete(final NeuroEvolutionRunCompletion completion) {
    }

    default void onFailure(final Throwable failure) {
    }
}
