package com.github.javachaos.javaneuralnetwork.examples;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * GUI-facing worker for a Rust process running on another machine.
 */
public final class RemoteRustNeuroEvolutionWorker implements NeuroEvolutionWorker {

    private final RustNeuroEvolutionWorker delegate;

    public RemoteRustNeuroEvolutionWorker(
            final String host,
            final int port,
            final NeuroEvolutionDistributedConfig distributedConfig) {
        this(host, port, distributedConfig, event -> {
        });
    }

    public RemoteRustNeuroEvolutionWorker(
            final String host,
            final int port,
            final NeuroEvolutionDistributedConfig distributedConfig,
            final Consumer<RustNeuroEvolutionSidecar.RustWorkerEvent> eventConsumer) {
        Objects.requireNonNull(host, "Host cannot be null.");
        Objects.requireNonNull(distributedConfig, "Distributed config cannot be null.");
        this.delegate = new RustNeuroEvolutionWorker(
                GrpcNeuroEvolutionTransport.factory(host, port, distributedConfig),
                eventConsumer);
    }

    @Override
    public NeuroEvolutionRunHandle start(
            final NeuroEvolutionRunRequest request,
            final NeuroEvolutionRunListener listener) {
        return delegate.start(request, listener);
    }

    @Override
    public void close() {
        delegate.close();
    }
}
