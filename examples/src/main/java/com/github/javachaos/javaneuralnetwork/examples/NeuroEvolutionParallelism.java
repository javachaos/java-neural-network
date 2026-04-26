package com.github.javachaos.javaneuralnetwork.examples;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

/**
 * Shared parallel scoring helpers for independent evolutionary candidates.
 */
final class NeuroEvolutionParallelism {

    private NeuroEvolutionParallelism() {
    }

    static int defaultParallelism() {
        return Math.max(1, Runtime.getRuntime().availableProcessors());
    }

    static ForkJoinPool newPool(final int configuredParallelism, final int workItems) {
        int parallelism = effectiveParallelism(configuredParallelism, workItems);
        if (parallelism == 1) {
            return null;
        }
        return new ForkJoinPool(parallelism);
    }

    static void shutdown(final ForkJoinPool pool) {
        if (pool != null) {
            pool.shutdown();
        }
    }

    static <T> List<T> mapAndSort(
            final int workItems,
            final ForkJoinPool pool,
            final IntFunction<T> evaluator,
            final Comparator<? super T> comparator) {
        if (workItems < 0) {
            throw new IllegalArgumentException("Work items cannot be negative.");
        }
        if (pool == null || workItems <= 1) {
            return IntStream.range(0, workItems)
                    .mapToObj(evaluator)
                    .sorted(comparator)
                    .toList();
        }
        return pool.submit(() -> IntStream.range(0, workItems)
                .parallel()
                .mapToObj(evaluator)
                .sorted(comparator)
                .toList()).join();
    }

    private static int effectiveParallelism(final int configuredParallelism, final int workItems) {
        if (configuredParallelism < 1) {
            throw new IllegalArgumentException("Parallelism must be positive.");
        }
        return Math.max(1, Math.min(configuredParallelism, Math.max(1, workItems)));
    }
}
