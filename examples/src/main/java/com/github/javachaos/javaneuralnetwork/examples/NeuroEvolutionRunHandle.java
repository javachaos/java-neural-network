package com.github.javachaos.javaneuralnetwork.examples;

/**
 * Control handle for an active evolution job.
 */
public interface NeuroEvolutionRunHandle {

    void pause();

    void resume();

    void stop();

    boolean isStopped();
}
