package com.github.javachaos.javaneuralnetwork.examples;

import java.io.IOException;

/**
 * Storage boundary for large worker artifacts.
 *
 * <p>The current implementation path keeps payloads in process, but this
 * interface is the future hand-off point for DHT-backed storage across many
 * Rust workers.</p>
 */
public interface NeuroEvolutionArtifactStore {

    NeuroEvolutionArtifactRef put(byte[] payload, String mediaType) throws IOException;

    byte[] get(NeuroEvolutionArtifactRef ref) throws IOException;
}
