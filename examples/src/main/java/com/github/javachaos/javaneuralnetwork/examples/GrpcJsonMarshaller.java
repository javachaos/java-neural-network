package com.github.javachaos.javaneuralnetwork.examples;

import io.grpc.MethodDescriptor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Minimal protobuf marshaller for the worker gRPC envelope.
 *
 * <p>The proto messages intentionally contain one string field. Keeping the
 * JSON worker protocol inside that envelope lets us add gRPC networking without
 * duplicating the entire evolution schema in generated Java code.</p>
 */
final class GrpcJsonMarshaller implements MethodDescriptor.Marshaller<GrpcJsonMessage> {

    static final GrpcJsonMarshaller INSTANCE = new GrpcJsonMarshaller();
    private static final int FIELD_ONE_STRING_TAG = 0x0a;

    private GrpcJsonMarshaller() {
    }

    @Override
    public InputStream stream(final GrpcJsonMessage value) {
        byte[] text = value.json().getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(text.length + 8);
        bytes.write(FIELD_ONE_STRING_TAG);
        writeVarint(bytes, text.length);
        bytes.writeBytes(text);
        return new ByteArrayInputStream(bytes.toByteArray());
    }

    @Override
    public GrpcJsonMessage parse(final InputStream stream) {
        try {
            byte[] bytes = stream.readAllBytes();
            int index = 0;
            while (index < bytes.length) {
                int tag = bytes[index++] & 0xff;
                VarintResult length = readVarint(bytes, index);
                index = length.nextIndex();
                if (tag == FIELD_ONE_STRING_TAG) {
                    int end = index + length.value();
                    if (end > bytes.length) {
                        throw new IllegalArgumentException("Truncated gRPC JSON message.");
                    }
                    return new GrpcJsonMessage(new String(bytes, index, length.value(), StandardCharsets.UTF_8));
                }
                index += length.value();
            }
            return new GrpcJsonMessage("{}");
        } catch (IOException exception) {
            throw new IllegalArgumentException("Could not read gRPC JSON message.", exception);
        }
    }

    private static void writeVarint(final ByteArrayOutputStream bytes, final int value) {
        int remaining = value;
        while ((remaining & ~0x7f) != 0) {
            bytes.write((remaining & 0x7f) | 0x80);
            remaining >>>= 7;
        }
        bytes.write(remaining);
    }

    private static VarintResult readVarint(final byte[] bytes, final int startIndex) {
        int value = 0;
        int shift = 0;
        int index = startIndex;
        while (index < bytes.length && shift < Integer.SIZE) {
            int current = bytes[index++] & 0xff;
            value |= (current & 0x7f) << shift;
            if ((current & 0x80) == 0) {
                return new VarintResult(value, index);
            }
            shift += 7;
        }
        throw new IllegalArgumentException("Invalid protobuf varint.");
    }

    private record VarintResult(int value, int nextIndex) {
    }
}
