package com.github.javachaos.javaneuralnetwork.examples;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Objects;

/**
 * Content-addressed artifact reference for future distributed worker storage.
 *
 * <p>Large payloads such as populations, batches, checkpoints, and tensor
 * blocks can later move through a DHT or object store while worker messages
 * carry only these compact references.</p>
 */
public record NeuroEvolutionArtifactRef(
        String namespace,
        String digestAlgorithm,
        String digest,
        long sizeBytes,
        String mediaType) {

    public NeuroEvolutionArtifactRef {
        namespace = requireText(namespace, "Namespace");
        digestAlgorithm = requireText(digestAlgorithm, "Digest algorithm").toLowerCase(Locale.ROOT);
        digest = requireText(digest, "Digest").toLowerCase(Locale.ROOT);
        if (sizeBytes < 0L) {
            throw new IllegalArgumentException("Size bytes cannot be negative.");
        }
        mediaType = requireText(mediaType, "Media type");
    }

    public static NeuroEvolutionArtifactRef sha256(
            final String namespace,
            final String digest,
            final long sizeBytes,
            final String mediaType) {
        return new NeuroEvolutionArtifactRef(namespace, "sha-256", digest, sizeBytes, mediaType);
    }

    public static NeuroEvolutionArtifactRef sha256(
            final String namespace,
            final byte[] payload,
            final String mediaType) {
        Objects.requireNonNull(payload, "Payload cannot be null.");
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return sha256(namespace, hex(digest.digest(payload)), payload.length, mediaType);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 digest support is unavailable.", exception);
        }
    }

    public String uri() {
        return "neuro-dht://" + namespace + "/" + digestAlgorithm + "/" + digest;
    }

    private static String hex(final byte[] bytes) {
        StringBuilder text = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            text.append(Character.forDigit((value >>> 4) & 0xf, 16));
            text.append(Character.forDigit(value & 0xf, 16));
        }
        return text.toString();
    }

    private static String requireText(final String value, final String label) {
        String trimmed = Objects.requireNonNull(value, label + " cannot be null.").trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(label + " cannot be blank.");
        }
        return trimmed;
    }
}
