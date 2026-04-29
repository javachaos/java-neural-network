package com.github.javachaos.javaneuralnetwork.examples;

import java.io.IOException;

/**
 * Transport boundary for Rust-backed neuro-evolution workers.
 *
 * <p>Implementations may talk to a local child process, a remote gRPC server,
 * or a future distributed runtime. The GUI-facing worker only depends on this
 * small contract.</p>
 */
public interface RustNeuroEvolutionTransport extends AutoCloseable {

    void start() throws IOException;

    void submit(NeuroEvolutionRunRequest request) throws IOException;

    void pause() throws IOException;

    void resume() throws IOException;

    void ping() throws IOException;

    void status() throws IOException;

    void stop() throws IOException;

    boolean isAlive();

    boolean awaitExit(long timeoutMillis) throws InterruptedException;

    @Override
    void close();
}
