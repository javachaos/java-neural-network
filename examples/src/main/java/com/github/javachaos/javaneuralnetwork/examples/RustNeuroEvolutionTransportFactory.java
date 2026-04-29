package com.github.javachaos.javaneuralnetwork.examples;

import java.io.IOException;
import java.util.function.Consumer;

@FunctionalInterface
public interface RustNeuroEvolutionTransportFactory {

    RustNeuroEvolutionTransport open(
            Consumer<RustNeuroEvolutionSidecar.RustWorkerEvent> eventConsumer) throws IOException;
}
