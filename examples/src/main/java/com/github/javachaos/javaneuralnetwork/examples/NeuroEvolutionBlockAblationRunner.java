package com.github.javachaos.javaneuralnetwork.examples;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Runs a focused frozen-block ablation through the Rust worker.
 *
 * <p>The Java side only loads the saved champion and serializes the existing
 * benchmark payload. Rust trains the frozen block, augments the samples with
 * its predictions, and compares probe learners on original versus mixed
 * inputs.</p>
 */
public final class NeuroEvolutionBlockAblationRunner {

    private static final String DEFAULT_PROBLEM = "transformer-block";
    private static final int DEFAULT_GRID_SIZE = 9;
    private static final long DEFAULT_TIMEOUT_MILLIS = 300_000L;

    private NeuroEvolutionBlockAblationRunner() {
    }

    public static void main(final String[] args) throws Exception {
        RunOptions options = RunOptions.from(args);
        NeuroEvolutionProblemCatalog catalog = new DefaultNeuroEvolutionProblemCatalog();
        Path checkpointPath = options.checkpointPath(catalog);
        NeuroEvolutionChampionCheckpoint.LoadedChampion champion =
                NeuroEvolutionChampionCheckpoint.load(checkpointPath, catalog);
        RustNeuroEvolutionSidecar.BlockAblationConfig ablationConfig =
                new RustNeuroEvolutionSidecar.BlockAblationConfig(
                        options.probeTrials(),
                        options.probeHiddenNeurons(),
                        options.probeMaxEpochs(),
                        options.controlMode(),
                        options.featureSlice());

        System.out.println(startJson(champion, checkpointPath, ablationConfig));
        runRustAblation(champion, ablationConfig, options.timeoutMillis());
    }

    private static void runRustAblation(
            final NeuroEvolutionChampionCheckpoint.LoadedChampion champion,
            final RustNeuroEvolutionSidecar.BlockAblationConfig ablationConfig,
            final long timeoutMillis) throws IOException, InterruptedException {
        CountDownLatch done = new CountDownLatch(1);
        AtomicReference<String> error = new AtomicReference<>();
        try (RustNeuroEvolutionSidecar sidecar = new RustNeuroEvolutionSidecar(
                RustNeuroEvolutionSidecar.defaultLocalCommand(),
                event -> {
                    System.out.println(event.rawJson());
                    if ("error".equals(event.type())) {
                        error.compareAndSet(null, event.message());
                        done.countDown();
                    } else if ("blockAblation".equals(event.type())
                            || "blockAblationStopped".equals(event.type())) {
                        done.countDown();
                    }
                })) {
            sidecar.start();
            sidecar.submitBlockAblation(champion, ablationConfig);
            if (!done.await(timeoutMillis, TimeUnit.MILLISECONDS)) {
                sidecar.stop();
                throw new IllegalStateException(
                        "Timed out waiting for Rust block ablation after " + timeoutMillis + " ms.");
            }
            sidecar.stop();
            sidecar.awaitExit(2_000L);
        }
        if (error.get() != null) {
            throw new IllegalStateException("Rust block ablation failed: " + error.get());
        }
    }

    private static String startJson(
            final NeuroEvolutionChampionCheckpoint.LoadedChampion champion,
            final Path checkpointPath,
            final RustNeuroEvolutionSidecar.BlockAblationConfig ablationConfig) {
        return "{"
                + "\"event\":\"block_ablation_start\","
                + "\"problem_key\":\"" + escape(champion.problem().key()) + "\","
                + "\"checkpoint\":\"" + escape(checkpointPath.toAbsolutePath().normalize().toString()) + "\","
                + "\"probe_trials\":" + ablationConfig.probeTrials() + ","
                + "\"probe_hidden_neurons\":" + ablationConfig.probeHiddenNeurons() + ","
                + "\"probe_max_epochs\":" + ablationConfig.probeMaxEpochs() + ","
                + "\"control_mode\":\"" + escape(ablationConfig.controlMode()) + "\","
                + "\"feature_slice\":\"" + escape(ablationConfig.featureSlice()) + "\""
                + "}";
    }

    private static String escape(final String value) {
        StringBuilder escaped = new StringBuilder(value.length());
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            switch (character) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> escaped.append(character);
            }
        }
        return escaped.toString();
    }

    private record RunOptions(
            String problemKey,
            Path checkpointPath,
            int probeTrials,
            int probeHiddenNeurons,
            int probeMaxEpochs,
            String controlMode,
            String featureSlice,
            long timeoutMillis) {

        private RunOptions {
            if (problemKey == null || problemKey.isBlank()) {
                problemKey = DEFAULT_PROBLEM;
            }
            if (probeTrials <= 0) {
                probeTrials = RustNeuroEvolutionSidecar.BlockAblationConfig.defaults().probeTrials();
            }
            if (probeHiddenNeurons <= 0) {
                probeHiddenNeurons = RustNeuroEvolutionSidecar.BlockAblationConfig.defaults()
                        .probeHiddenNeurons();
            }
            if (probeMaxEpochs <= 0) {
                probeMaxEpochs = RustNeuroEvolutionSidecar.BlockAblationConfig.defaults().probeMaxEpochs();
            }
            if (controlMode == null || controlMode.isBlank()) {
                controlMode = RustNeuroEvolutionSidecar.BlockAblationConfig.defaults().controlMode();
            }
            if (featureSlice == null || featureSlice.isBlank()) {
                featureSlice = RustNeuroEvolutionSidecar.BlockAblationConfig.defaults().featureSlice();
            }
            if (timeoutMillis <= 0) {
                timeoutMillis = DEFAULT_TIMEOUT_MILLIS;
            }
        }

        private Path checkpointPath(final NeuroEvolutionProblemCatalog catalog) {
            if (checkpointPath != null) {
                return checkpointPath;
            }
            NeuroEvolutionProblem problem = catalog.find(problemKey, DEFAULT_GRID_SIZE);
            Path defaultPath = NeuroEvolutionChampionCheckpoint.defaultPath(problem);
            if (Files.exists(defaultPath)) {
                return defaultPath;
            }
            Path examplesPath = Path.of("examples").resolve(defaultPath).normalize();
            return Files.exists(examplesPath) ? examplesPath : defaultPath;
        }

        private static RunOptions from(final String[] args) {
            String problemKey = DEFAULT_PROBLEM;
            Path checkpointPath = null;
            int probeTrials = RustNeuroEvolutionSidecar.BlockAblationConfig.defaults().probeTrials();
            int probeHiddenNeurons = RustNeuroEvolutionSidecar.BlockAblationConfig.defaults()
                    .probeHiddenNeurons();
            int probeMaxEpochs = RustNeuroEvolutionSidecar.BlockAblationConfig.defaults().probeMaxEpochs();
            String controlMode = RustNeuroEvolutionSidecar.BlockAblationConfig.defaults().controlMode();
            String featureSlice = RustNeuroEvolutionSidecar.BlockAblationConfig.defaults().featureSlice();
            long timeoutMillis = DEFAULT_TIMEOUT_MILLIS;
            for (int index = 0; index < args.length; index++) {
                String option = args[index];
                switch (option) {
                    case "--problem" -> problemKey = requireValue(args, ++index, option);
                    case "--checkpoint" -> checkpointPath = Path.of(requireValue(args, ++index, option));
                    case "--trials" -> probeTrials = Integer.parseInt(requireValue(args, ++index, option));
                    case "--probe-hidden" -> probeHiddenNeurons =
                            Integer.parseInt(requireValue(args, ++index, option));
                    case "--probe-epochs" -> probeMaxEpochs =
                            Integer.parseInt(requireValue(args, ++index, option));
                    case "--control" -> controlMode = requireValue(args, ++index, option);
                    case "--features", "--feature-slice" -> featureSlice = requireValue(args, ++index, option);
                    case "--timeout-ms" -> timeoutMillis =
                            Long.parseLong(requireValue(args, ++index, option));
                    default -> {
                        if (option.startsWith("-")) {
                            throw new IllegalArgumentException("Unknown option: " + option);
                        }
                        checkpointPath = Path.of(option);
                    }
                }
            }
            return new RunOptions(
                    problemKey,
                    checkpointPath,
                    probeTrials,
                    probeHiddenNeurons,
                    probeMaxEpochs,
                    controlMode,
                    featureSlice,
                    timeoutMillis);
        }

        private static String requireValue(final String[] args, final int index, final String option) {
            if (index >= args.length) {
                throw new IllegalArgumentException("Missing value for " + option);
            }
            return args[index];
        }
    }
}
