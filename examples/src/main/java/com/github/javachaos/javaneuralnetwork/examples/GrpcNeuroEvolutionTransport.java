package com.github.javachaos.javaneuralnetwork.examples;

import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.MethodDescriptor;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Network transport for a Rust worker exposed through gRPC.
 */
public final class GrpcNeuroEvolutionTransport implements RustNeuroEvolutionTransport {

    private static final String SERVICE_NAME = "neuroevolution.worker.v1.NeuroEvolutionWorkerService";
    private static final MethodDescriptor<GrpcJsonMessage, GrpcJsonMessage> RUN_METHOD =
            MethodDescriptor.<GrpcJsonMessage, GrpcJsonMessage>newBuilder()
                    .setType(MethodDescriptor.MethodType.SERVER_STREAMING)
                    .setFullMethodName(MethodDescriptor.generateFullMethodName(SERVICE_NAME, "Run"))
                    .setRequestMarshaller(GrpcJsonMarshaller.INSTANCE)
                    .setResponseMarshaller(GrpcJsonMarshaller.INSTANCE)
                    .build();
    private static final MethodDescriptor<GrpcJsonMessage, GrpcJsonMessage> CONTROL_METHOD =
            MethodDescriptor.<GrpcJsonMessage, GrpcJsonMessage>newBuilder()
                    .setType(MethodDescriptor.MethodType.UNARY)
                    .setFullMethodName(MethodDescriptor.generateFullMethodName(SERVICE_NAME, "Control"))
                    .setRequestMarshaller(GrpcJsonMarshaller.INSTANCE)
                    .setResponseMarshaller(GrpcJsonMarshaller.INSTANCE)
                    .build();

    private final ManagedChannel channel;
    private final NeuroEvolutionDistributedConfig distributedConfig;
    private final Consumer<RustNeuroEvolutionSidecar.RustWorkerEvent> eventConsumer;
    private final AtomicBoolean alive = new AtomicBoolean();
    private volatile ClientCall<GrpcJsonMessage, GrpcJsonMessage> activeRun;

    public GrpcNeuroEvolutionTransport(
            final String host,
            final int port,
            final NeuroEvolutionDistributedConfig distributedConfig,
            final Consumer<RustNeuroEvolutionSidecar.RustWorkerEvent> eventConsumer) {
        this(
                ManagedChannelBuilder.forAddress(host, port).usePlaintext().build(),
                distributedConfig,
                eventConsumer);
    }

    GrpcNeuroEvolutionTransport(
            final ManagedChannel channel,
            final NeuroEvolutionDistributedConfig distributedConfig,
            final Consumer<RustNeuroEvolutionSidecar.RustWorkerEvent> eventConsumer) {
        this.channel = Objects.requireNonNull(channel, "Managed channel cannot be null.");
        this.distributedConfig = Objects.requireNonNull(
                distributedConfig,
                "Distributed config cannot be null.");
        this.eventConsumer = Objects.requireNonNull(eventConsumer, "Event consumer cannot be null.");
    }

    public static RustNeuroEvolutionTransportFactory factory(
            final String host,
            final int port,
            final NeuroEvolutionDistributedConfig distributedConfig) {
        return eventConsumer -> new GrpcNeuroEvolutionTransport(host, port, distributedConfig, eventConsumer);
    }

    @Override
    public void start() {
        alive.set(true);
    }

    @Override
    public void submit(final NeuroEvolutionRunRequest request) throws IOException {
        ensureAlive();
        ClientCall<GrpcJsonMessage, GrpcJsonMessage> call = channel.newCall(RUN_METHOD, CallOptions.DEFAULT);
        activeRun = call;
        ClientCalls.asyncServerStreamingCall(
                call,
                new GrpcJsonMessage(RustNeuroEvolutionSidecar.startJson(request, distributedConfig)),
                new EventObserver(true));
    }

    @Override
    public void pause() throws IOException {
        sendControl(RustNeuroEvolutionSidecar.typeJson("pause"));
    }

    @Override
    public void resume() throws IOException {
        sendControl(RustNeuroEvolutionSidecar.typeJson("resume"));
    }

    @Override
    public void ping() throws IOException {
        sendControl(RustNeuroEvolutionSidecar.typeJson("ping"));
    }

    @Override
    public void status() throws IOException {
        sendControl(RustNeuroEvolutionSidecar.typeJson("status"));
    }

    @Override
    public void stop() throws IOException {
        ClientCall<GrpcJsonMessage, GrpcJsonMessage> call = activeRun;
        if (call != null) {
            sendControl(RustNeuroEvolutionSidecar.typeJson("stop"));
            call.cancel("Stopped by Java GUI.", null);
        }
        alive.set(false);
    }

    @Override
    public boolean isAlive() {
        return alive.get() && !channel.isShutdown();
    }

    @Override
    public boolean awaitExit(final long timeoutMillis) throws InterruptedException {
        return channel.awaitTermination(timeoutMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public void close() {
        ClientCall<GrpcJsonMessage, GrpcJsonMessage> call = activeRun;
        if (call != null) {
            call.cancel("Transport closed.", null);
        }
        alive.set(false);
        channel.shutdownNow();
    }

    private void sendControl(final String controlJson) throws IOException {
        ensureAlive();
        ClientCalls.asyncUnaryCall(
                channel.newCall(CONTROL_METHOD, CallOptions.DEFAULT),
                new GrpcJsonMessage(controlJson),
                new EventObserver(false));
    }

    private void ensureAlive() throws IOException {
        if (!isAlive()) {
            throw new IOException("gRPC Rust worker transport is closed.");
        }
    }

    private final class EventObserver implements StreamObserver<GrpcJsonMessage> {
        private final boolean terminalOnComplete;

        private EventObserver(final boolean terminalOnComplete) {
            this.terminalOnComplete = terminalOnComplete;
        }

        @Override
        public void onNext(final GrpcJsonMessage value) {
            RustNeuroEvolutionSidecar.RustWorkerEvent event =
                    RustNeuroEvolutionSidecar.RustWorkerEvent.fromJson(value.json());
            if (terminalOnComplete && "completed".equals(event.type())) {
                activeRun = null;
                alive.set(false);
            }
            eventConsumer.accept(event);
        }

        @Override
        public void onError(final Throwable throwable) {
            alive.set(false);
            eventConsumer.accept(new RustNeuroEvolutionSidecar.RustWorkerEvent(
                    "error",
                    RustNeuroEvolutionSidecar.PROTOCOL_VERSION,
                    "grpc-rust-worker",
                    distributedConfig.clusterId(),
                    distributedConfig.storageMode().name(),
                    "error",
                    throwable.getMessage(),
                    "{\"type\":\"error\",\"status\":\"error\"}"));
        }

        @Override
        public void onCompleted() {
            if (terminalOnComplete) {
                activeRun = null;
                alive.set(false);
            }
        }
    }
}
