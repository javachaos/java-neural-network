package com.github.javachaos.javaneuralnetwork.examples;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.Objects;

/**
 * Describes how a worker participates in a local or distributed evolution run.
 *
 * <p>The default is a single-process local worker. Distributed deployments can
 * switch the storage mode to a content-addressed DHT and use the same worker
 * protocol without changing the GUI-facing worker interface.</p>
 */
public record NeuroEvolutionDistributedConfig(
        String clusterId,
        String coordinatorId,
        String workerId,
        StorageMode storageMode,
        ComputeBackend computeBackend,
        int replicationFactor,
        String artifactNamespace) {

    private static final String DEFAULT_CLUSTER_ID = "local";
    private static final String DEFAULT_COORDINATOR_ID = "java-gui";
    private static final String DEFAULT_ARTIFACT_NAMESPACE = "neuro-evolution";

    public enum StorageMode {
        LOCAL,
        CONTENT_ADDRESSED_DHT
    }

    public enum ComputeBackend {
        CPU,
        CUDA
    }

    public NeuroEvolutionDistributedConfig {
        clusterId = requireIdentifier(clusterId, "Cluster id");
        coordinatorId = requireIdentifier(coordinatorId, "Coordinator id");
        workerId = requireIdentifier(workerId, "Worker id");
        storageMode = Objects.requireNonNull(storageMode, "Storage mode cannot be null.");
        computeBackend = Objects.requireNonNull(computeBackend, "Compute backend cannot be null.");
        if (replicationFactor < 1) {
            throw new IllegalArgumentException("Replication factor must be positive.");
        }
        artifactNamespace = requireIdentifier(artifactNamespace, "Artifact namespace");
    }

    public static NeuroEvolutionDistributedConfig local() {
        return new NeuroEvolutionDistributedConfig(
                DEFAULT_CLUSTER_ID,
                DEFAULT_COORDINATOR_ID,
                localWorkerId(),
                StorageMode.LOCAL,
                ComputeBackend.CPU,
                1,
                DEFAULT_ARTIFACT_NAMESPACE);
    }

    public static NeuroEvolutionDistributedConfig cudaWorker(
            final String clusterId,
            final String coordinatorId,
            final String workerId,
            final int replicationFactor,
            final String artifactNamespace) {
        return new NeuroEvolutionDistributedConfig(
                clusterId,
                coordinatorId,
                workerId,
                StorageMode.CONTENT_ADDRESSED_DHT,
                ComputeBackend.CUDA,
                replicationFactor,
                artifactNamespace);
    }

    public static NeuroEvolutionDistributedConfig dht(
            final String clusterId,
            final String coordinatorId,
            final String workerId,
            final int replicationFactor,
            final String artifactNamespace) {
        return new NeuroEvolutionDistributedConfig(
                clusterId,
                coordinatorId,
                workerId,
                StorageMode.CONTENT_ADDRESSED_DHT,
                ComputeBackend.CPU,
                replicationFactor,
                artifactNamespace);
    }

    public NeuroEvolutionDistributedConfig withComputeBackend(final ComputeBackend backend) {
        return new NeuroEvolutionDistributedConfig(
                clusterId,
                coordinatorId,
                workerId,
                storageMode,
                backend,
                replicationFactor,
                artifactNamespace);
    }

    public boolean distributedStorage() {
        return storageMode == StorageMode.CONTENT_ADDRESSED_DHT;
    }

    private static String localWorkerId() {
        String pid = Long.toString(ProcessHandle.current().pid());
        return sanitize("java-" + hostName() + "-" + pid);
    }

    private static String hostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException exception) {
            return "localhost";
        }
    }

    private static String requireIdentifier(final String value, final String label) {
        String normalized = sanitize(Objects.requireNonNull(value, label + " cannot be null."));
        if (normalized.isBlank()) {
            throw new IllegalArgumentException(label + " cannot be blank.");
        }
        return normalized;
    }

    private static String sanitize(final String value) {
        String lower = value.trim().toLowerCase(Locale.ROOT);
        StringBuilder sanitized = new StringBuilder(lower.length());
        boolean lastWasDash = false;
        for (int index = 0; index < lower.length(); index++) {
            char character = lower.charAt(index);
            boolean accepted = character >= 'a' && character <= 'z'
                    || character >= '0' && character <= '9'
                    || character == '.'
                    || character == '_'
                    || character == '-';
            if (accepted) {
                sanitized.append(character);
                lastWasDash = false;
            } else if (!lastWasDash) {
                sanitized.append('-');
                lastWasDash = true;
            }
        }
        while (!sanitized.isEmpty() && sanitized.charAt(sanitized.length() - 1) == '-') {
            sanitized.deleteCharAt(sanitized.length() - 1);
        }
        return sanitized.toString();
    }
}
