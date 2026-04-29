package com.github.javachaos.javaneuralnetwork.examples;

import java.io.ByteArrayOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * JSONL process bridge for the Rust evolution worker.
 *
 * <p>This is the transport layer only. The Rust process can already be launched
 * and controlled from Java; the heavy evolution kernel can be moved behind this
 * bridge incrementally.</p>
 */
public final class RustNeuroEvolutionSidecar implements RustNeuroEvolutionTransport {

    public static final int PROTOCOL_VERSION = 1;
    private static final String RUST_WORKER_PROFILE_PROPERTY = "neuroEvolution.rustWorkerProfile";
    private static final String SEED_LANES_PROPERTY = "neuroEvolution.seedLanes";
    private static final String RELEASE_PROFILE = "release";
    private static final String DEBUG_PROFILE = "debug";

    private final List<String> command;
    private final NeuroEvolutionDistributedConfig distributedConfig;
    private final Consumer<RustWorkerEvent> eventConsumer;
    private Process process;
    private BufferedWriter input;
    private Thread stdoutThread;
    private Thread stderrThread;
    private volatile boolean closed;

    public RustNeuroEvolutionSidecar(
            final List<String> command,
            final Consumer<RustWorkerEvent> eventConsumer) {
        this(command, NeuroEvolutionDistributedConfig.local(), eventConsumer);
    }

    public RustNeuroEvolutionSidecar(
            final List<String> command,
            final NeuroEvolutionDistributedConfig distributedConfig,
            final Consumer<RustWorkerEvent> eventConsumer) {
        if (command == null || command.isEmpty()) {
            throw new IllegalArgumentException("Rust worker command cannot be empty.");
        }
        this.command = List.copyOf(command);
        this.distributedConfig = Objects.requireNonNull(
                distributedConfig,
                "Distributed config cannot be null.");
        this.eventConsumer = Objects.requireNonNull(eventConsumer, "Event consumer cannot be null.");
    }

    public static Path defaultBinaryPath(final Path repositoryRoot) {
        return binaryPath(repositoryRoot, configuredBuildProfile());
    }

    private static Path binaryPath(final Path repositoryRoot, final String buildProfile) {
        Objects.requireNonNull(repositoryRoot, "Repository root cannot be null.");
        String executableName = System.getProperty("os.name", "")
                .toLowerCase()
                .contains("win")
                ? "neuro-evolution-worker.exe"
                : "neuro-evolution-worker";
        return repositoryRoot
                .resolve("rust-worker")
                .resolve("target")
                .resolve(buildProfile)
                .resolve(executableName)
                .normalize();
    }

    private static String configuredBuildProfile() {
        String profile = System.getProperty(RUST_WORKER_PROFILE_PROPERTY, RELEASE_PROFILE)
                .trim()
                .toLowerCase();
        return DEBUG_PROFILE.equals(profile) ? DEBUG_PROFILE : RELEASE_PROFILE;
    }

    public static List<String> defaultCommand(final Path repositoryRoot) {
        return List.of(defaultBinaryPath(repositoryRoot).toString());
    }

    public static List<String> defaultLocalCommand() {
        return defaultLocalCommand(discoverRepositoryRoot());
    }

    public static List<String> defaultLocalCommand(final Path repositoryRoot) {
        String preferredProfile = configuredBuildProfile();
        Path preferredBinaryPath = binaryPath(repositoryRoot, preferredProfile);
        if (Files.isExecutable(preferredBinaryPath)) {
            return List.of(preferredBinaryPath.toString());
        }
        Path manifestPath = repositoryRoot.resolve("rust-worker").resolve("Cargo.toml");
        if (RELEASE_PROFILE.equals(preferredProfile)) {
            return List.of(
                    "cargo",
                    "run",
                    "--release",
                    "--quiet",
                    "--manifest-path",
                    manifestPath.toString());
        }
        return List.of(
                "cargo",
                "run",
                "--quiet",
                "--manifest-path",
                manifestPath.toString());
    }

    public record BlockAblationConfig(
            int probeTrials,
            int probeHiddenNeurons,
            int probeMaxEpochs,
            String controlMode,
            String featureSlice) {

        public BlockAblationConfig {
            if (probeTrials <= 0) {
                probeTrials = 8;
            }
            if (probeHiddenNeurons <= 0) {
                probeHiddenNeurons = 6;
            }
            if (probeMaxEpochs <= 0) {
                probeMaxEpochs = 240;
            }
            if (controlMode == null || controlMode.isBlank()) {
                controlMode = "champion";
            }
            if (featureSlice == null || featureSlice.isBlank()) {
                featureSlice = "all";
            }
        }

        public static BlockAblationConfig defaults() {
            return new BlockAblationConfig(8, 6, 240, "champion", "all");
        }
    }

    public static Path discoverRepositoryRoot() {
        Path current = Path.of("").toAbsolutePath().normalize();
        while (current != null) {
            if (Files.exists(current.resolve("rust-worker").resolve("Cargo.toml"))) {
                return current;
            }
            current = current.getParent();
        }
        return Path.of("").toAbsolutePath().normalize();
    }

    @Override
    public synchronized void start() throws IOException {
        if (process != null && process.isAlive()) {
            return;
        }
        closed = false;
        process = new ProcessBuilder(command).start();
        input = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8));
        stdoutThread = readerThread("rust-neuro-evolution-worker-stdout", process.getInputStream(), this::dispatch);
        stderrThread = readerThread("rust-neuro-evolution-worker-stderr", process.getErrorStream(), this::dispatchError);
        stdoutThread.start();
        stderrThread.start();
    }

    @Override
    public void submit(final NeuroEvolutionRunRequest request) throws IOException {
        Objects.requireNonNull(request, "Run request cannot be null.");
        send(startJson(request, distributedConfig));
    }

    public void submitBlockAblation(
            final NeuroEvolutionChampionCheckpoint.LoadedChampion champion,
            final BlockAblationConfig ablationConfig) throws IOException {
        Objects.requireNonNull(champion, "Champion cannot be null.");
        Objects.requireNonNull(ablationConfig, "Block ablation config cannot be null.");
        send(blockAblationJson(champion, ablationConfig));
    }

    @Override
    public void pause() throws IOException {
        send(typeJson("pause"));
    }

    @Override
    public void resume() throws IOException {
        send(typeJson("resume"));
    }

    @Override
    public void ping() throws IOException {
        send(typeJson("ping"));
    }

    @Override
    public void status() throws IOException {
        send(typeJson("status"));
    }

    @Override
    public void stop() throws IOException {
        send(typeJson("stop"));
    }

    @Override
    public boolean isAlive() {
        return process != null && process.isAlive();
    }

    @Override
    public boolean awaitExit(final long timeoutMillis) throws InterruptedException {
        return process == null || process.waitFor(timeoutMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public synchronized void close() {
        closed = true;
        if (input != null) {
            try {
                input.close();
            } catch (IOException ignored) {
                // Closing should not mask the caller's original outcome.
            }
        }
        if (process != null && process.isAlive()) {
            process.destroy();
        }
    }

    private synchronized void send(final String json) throws IOException {
        if (closed) {
            throw new IOException("Rust worker sidecar is closed.");
        }
        if (input == null) {
            throw new IOException("Rust worker sidecar has not been started.");
        }
        input.write(json);
        input.newLine();
        input.flush();
    }

    private void dispatch(final String line) {
        eventConsumer.accept(RustWorkerEvent.fromJson(line));
    }

    private void dispatchError(final String line) {
        eventConsumer.accept(new RustWorkerEvent(
                "stderr",
                0,
                null,
                null,
                null,
                null,
                line,
                line));
    }

    private static Thread readerThread(
            final String name,
            final InputStream inputStream,
            final Consumer<String> lineConsumer) {
        Thread thread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lineConsumer.accept(line);
                }
            } catch (IOException exception) {
                lineConsumer.accept("Reader failed: " + exception.getMessage());
            }
        }, name);
        thread.setDaemon(true);
        return thread;
    }

    static String typeJson(final String type) {
        return "{\"type\":\"" + jsonString(type) + "\",\"protocol\":" + PROTOCOL_VERSION + "}";
    }

    static String startJson(
            final NeuroEvolutionRunRequest request,
            final NeuroEvolutionDistributedConfig distributedConfig) {
        NeuroEvolutionProblem problem = request.problem();
        XorNeuroEvolution.EvolutionConfig config = request.config();
        StringBuilder json = new StringBuilder(512);
        json.append("{\"type\":\"start\",\"protocol\":").append(PROTOCOL_VERSION);
        json.append(",\"cluster\":{");
        field(json, "clusterId", distributedConfig.clusterId()).append(',');
        field(json, "coordinatorId", distributedConfig.coordinatorId()).append(',');
        field(json, "workerId", distributedConfig.workerId());
        json.append('}');
        json.append(",\"storage\":{");
        field(json, "storageMode", distributedConfig.storageMode().name()).append(',');
        numberField(json, "replicationFactor", distributedConfig.replicationFactor()).append(',');
        field(json, "artifactNamespace", distributedConfig.artifactNamespace());
        json.append('}');
        json.append(',');
        field(json, "backend", distributedConfig.computeBackend().name());
        json.append(",\"problem\":{");
        field(json, "key", problem.key()).append(',');
        field(json, "name", problem.name()).append(',');
        numberField(json, "inputDimensions", problem.inputDimensions()).append(',');
        numberField(json, "outputDimensions", problem.outputDimensions()).append(',');
        numberField(json, "trainingSamples", problem.trainingSamples().size()).append(',');
        numberField(json, "generalizationSamples", problem.generalizationSamples().size()).append(',');
        booleanField(json, "classification", problem.classification());
        json.append('}');
        json.append(',');
        field(json, "problemPayloadHex", problemPayloadHex(problem, config));
        json.append(",\"config\":{");
        numberField(json, "populationSize", config.populationSize()).append(',');
        numberField(json, "generations", config.generations()).append(',');
        numberField(json, "maxEpochs", config.maxEpochs()).append(',');
        numberField(json, "targetMeanSquaredError", config.targetMeanSquaredError()).append(',');
        numberField(json, "evaluationRepeats", config.evaluationRepeats()).append(',');
        numberField(json, "mutationIntensity", config.mutationIntensity()).append(',');
        numberField(json, "complexityPenalty", config.complexityPenalty()).append(',');
        numberField(json, "maxMutationsPerChild", config.maxMutationsPerChild()).append(',');
        numberField(json, "generalizationGridSize", config.generalizationGridSize()).append(',');
        numberField(json, "jitterSamplesPerCorner", config.jitterSamplesPerCorner()).append(',');
        numberField(json, "jitterRadius", config.jitterRadius()).append(',');
        numberField(json, "generalizationWeight", config.generalizationWeight()).append(',');
        numberField(json, "jitterWeight", config.jitterWeight()).append(',');
        numberField(json, "smoothnessWeight", config.smoothnessWeight()).append(',');
        numberField(json, "parallelism", config.parallelism()).append(',');
        numberField(json, "seedLanes", seedLanes(config)).append(',');
        numberField(json, "seed", config.seed());
        json.append('}');
        json.append(",\"checkpoint\":{");
        booleanField(json, "enabled", request.checkpointOnImprovement());
        if (request.checkpointOnImprovement()) {
            json.append(',');
            field(json, "path", request.checkpointPath().toString());
        }
        json.append('}');
        json.append(',');
        numberField(json, "progressIntervalNanos", request.progressIntervalNanos());
        json.append('}');
        return json.toString();
    }

    static String blockAblationJson(
            final NeuroEvolutionChampionCheckpoint.LoadedChampion champion,
            final BlockAblationConfig ablationConfig) {
        NeuroEvolutionProblem problem = champion.problem();
        XorNeuroEvolution.EvolutionConfig config = champion.config();
        StringBuilder json = new StringBuilder(768);
        json.append("{\"type\":\"blockAblation\",\"protocol\":").append(PROTOCOL_VERSION);
        json.append(",\"problem\":{");
        field(json, "key", problem.key()).append(',');
        field(json, "name", problem.name()).append(',');
        numberField(json, "inputDimensions", problem.inputDimensions()).append(',');
        numberField(json, "outputDimensions", problem.outputDimensions()).append(',');
        booleanField(json, "classification", problem.classification());
        json.append('}');
        json.append(',');
        field(json, "problemPayloadHex", problemPayloadHex(problem, config));
        json.append(',');
        field(json, "blockGenome", genomeEncoding(champion.score().genome()));
        json.append(",\"config\":{");
        numberField(json, "populationSize", config.populationSize()).append(',');
        numberField(json, "generations", config.generations()).append(',');
        numberField(json, "maxEpochs", config.maxEpochs()).append(',');
        numberField(json, "targetMeanSquaredError", config.targetMeanSquaredError()).append(',');
        numberField(json, "evaluationRepeats", config.evaluationRepeats()).append(',');
        numberField(json, "mutationIntensity", config.mutationIntensity()).append(',');
        numberField(json, "complexityPenalty", config.complexityPenalty()).append(',');
        numberField(json, "maxMutationsPerChild", config.maxMutationsPerChild()).append(',');
        numberField(json, "generalizationWeight", config.generalizationWeight()).append(',');
        numberField(json, "jitterWeight", config.jitterWeight()).append(',');
        numberField(json, "smoothnessWeight", config.smoothnessWeight()).append(',');
        numberField(json, "parallelism", config.parallelism()).append(',');
        numberField(json, "seed", config.seed());
        json.append('}');
        json.append(",\"ablation\":{");
        numberField(json, "probeTrials", ablationConfig.probeTrials()).append(',');
        numberField(json, "probeHiddenNeurons", ablationConfig.probeHiddenNeurons()).append(',');
        numberField(json, "probeMaxEpochs", ablationConfig.probeMaxEpochs()).append(',');
        field(json, "controlMode", ablationConfig.controlMode()).append(',');
        field(json, "featureSlice", ablationConfig.featureSlice());
        json.append('}');
        json.append('}');
        return json.toString();
    }

    static String genomeEncoding(final EvolvableXorGenome genome) {
        Objects.requireNonNull(genome, "Genome cannot be null.");
        return genome.hiddenNeurons()
                + "|" + genome.hiddenLayers()
                + "|" + genome.recurrentConnections()
                + "|" + genome.memoryCells()
                + "|" + genome.connectionDensity()
                + "|" + genome.hiddenActivation().name()
                + "|" + genome.outputActivation().name()
                + "|" + genome.inputRepresentation().name()
                + "|" + genome.lossFunction().name()
                + "|" + genome.learningSchedule().name()
                + "|" + genome.hebbianUpdate()
                + "|" + genome.gradientUpdate()
                + "|" + genome.normalization()
                + "|" + genome.kernelMemory()
                + "|" + genome.phaseEncoding()
                + "|" + genome.secondDerivativeEstimate()
                + "|" + genome.inputLearningRate()
                + "|" + genome.outputLearningRate()
                + "|" + genome.recurrentLearningRate()
                + "|" + genome.hebbianLearningRate()
                + "|" + genome.memoryLearningRate()
                + "|" + genome.momentum()
                + "|" + genome.weightDecay()
                + "|" + genome.normalizationStrength()
                + "|" + genome.activationSlope()
                + "|" + genome.errorClip()
                + "|" + genome.kernelSharpness();
    }

    private static int seedLanes(final XorNeuroEvolution.EvolutionConfig config) {
        int requestedLanes = Integer.getInteger(
                SEED_LANES_PROPERTY,
                Math.max(1, config.parallelism()));
        return Math.max(1, Math.min(config.populationSize(), requestedLanes));
    }

    private static String problemPayloadHex(
            final NeuroEvolutionProblem problem,
            final XorNeuroEvolution.EvolutionConfig config) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream data = new DataOutputStream(bytes);
            data.writeInt(0x4e45564f);
            data.writeInt(3);
            data.writeInt(problem.inputDimensions());
            data.writeInt(problem.outputDimensions());
            data.writeBoolean(problem.classification());
            data.writeBoolean(problem.statefulSamples());
            data.writeDouble(problem.complexityScale());
            data.writeDouble(problem.freeEnergyProfile().objectiveWeight());
            data.writeDouble(problem.freeEnergyProfile().sensoryPredictionWeight());
            data.writeDouble(problem.freeEnergyProfile().latentPredictionWeight());
            data.writeDouble(problem.freeEnergyProfile().complexityPriorWeight());
            data.writeDouble(problem.freeEnergyProfile().maximumEnergy());
            samples(data, problem.trainingSamples(), problem.inputDimensions(), problem.outputDimensions());
            samples(data, problem.generalizationSamples(), problem.inputDimensions(), problem.outputDimensions());
            samples(data, NeuroEvolutionEvaluationProbes.jitterSamples(problem, config),
                    problem.inputDimensions(), problem.outputDimensions());
            smoothnessProbes(data, NeuroEvolutionEvaluationProbes.smoothnessProbes(problem, config),
                    problem.inputDimensions(), problem.outputDimensions());
            vectors(data, problem.kernelCenters(), problem.inputDimensions());
            data.writeInt(problem.outputGroups().size());
            for (NeuroEvolutionOutputGroup group : problem.outputGroups()) {
                data.writeInt(group.startInclusive());
                data.writeInt(group.endExclusive());
                data.writeDouble(group.objectiveWeight());
            }
            data.flush();
            return hex(bytes.toByteArray());
        } catch (IOException exception) {
            throw new IllegalStateException("Could not encode Rust worker problem payload.", exception);
        }
    }

    private static void smoothnessProbes(
            final DataOutputStream data,
            final List<NeuroEvolutionEvaluationProbes.SmoothnessProbe> probes,
            final int inputDimensions,
            final int outputDimensions) throws IOException {
        data.writeInt(probes.size());
        for (NeuroEvolutionEvaluationProbes.SmoothnessProbe probe : probes) {
            sample(data, probe.center(), inputDimensions, outputDimensions);
            sample(data, probe.plus(), inputDimensions, outputDimensions);
            sample(data, probe.minus(), inputDimensions, outputDimensions);
        }
    }

    private static void samples(
            final DataOutputStream data,
            final List<NeuroEvolutionSample> samples,
            final int inputDimensions,
            final int outputDimensions) throws IOException {
        data.writeInt(samples.size());
        for (NeuroEvolutionSample sample : samples) {
            sample(data, sample, inputDimensions, outputDimensions);
        }
    }

    private static void sample(
            final DataOutputStream data,
            final NeuroEvolutionSample sample,
            final int inputDimensions,
            final int outputDimensions) throws IOException {
        writeVector(data, sample.input(), inputDimensions);
        writeVector(data, sample.targets(), outputDimensions);
    }

    private static void vectors(
            final DataOutputStream data,
            final List<?> vectors,
            final int dimensions) throws IOException {
        data.writeInt(vectors.size());
        for (Object vector : vectors) {
            if (vector instanceof NeuroEvolutionSample sample) {
                writeVector(data, sample.input(), dimensions);
            } else if (vector instanceof double[] values) {
                writeVector(data, values, dimensions);
            } else {
                throw new IllegalArgumentException("Unsupported payload vector type " + vector.getClass().getName());
            }
        }
    }

    private static void writeVector(
            final DataOutputStream data,
            final double[] values,
            final int dimensions) throws IOException {
        if (values.length != dimensions) {
            throw new IllegalArgumentException("Payload vector dimension mismatch.");
        }
        for (double value : values) {
            data.writeDouble(value);
        }
    }

    private static String hex(final byte[] bytes) {
        StringBuilder text = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            text.append(Character.forDigit((value >>> 4) & 0xf, 16));
            text.append(Character.forDigit(value & 0xf, 16));
        }
        return text.toString();
    }

    private static StringBuilder field(final StringBuilder json, final String name, final String value) {
        json.append('"').append(jsonString(name)).append("\":\"").append(jsonString(value)).append('"');
        return json;
    }

    private static StringBuilder numberField(final StringBuilder json, final String name, final long value) {
        json.append('"').append(jsonString(name)).append("\":").append(value);
        return json;
    }

    private static StringBuilder numberField(final StringBuilder json, final String name, final double value) {
        json.append('"').append(jsonString(name)).append("\":").append(Double.toString(value));
        return json;
    }

    private static StringBuilder booleanField(final StringBuilder json, final String name, final boolean value) {
        json.append('"').append(jsonString(name)).append("\":").append(value);
        return json;
    }

    private static String jsonString(final String value) {
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

    public record RustWorkerEvent(
            String type,
            int protocol,
            String worker,
            String clusterId,
            String storageMode,
            String status,
            String message,
            String rawJson) {

        public static RustWorkerEvent fromJson(final String json) {
            return new RustWorkerEvent(
                    stringField(json, "type"),
                    intField(json, "protocol"),
                    stringField(json, "worker"),
                    stringField(json, "clusterId"),
                    stringField(json, "storageMode"),
                    stringField(json, "status"),
                    stringField(json, "message"),
                    json);
        }

        public String stringValue(final String field) {
            return stringField(rawJson, field);
        }

        public int intValue(final String field, final int fallback) {
            String value = numberText(rawJson, field);
            if (value == null) {
                return fallback;
            }
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException exception) {
                return fallback;
            }
        }

        public double doubleValue(final String field, final double fallback) {
            String value = numberText(rawJson, field);
            if (value == null) {
                return fallback;
            }
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException exception) {
                return fallback;
            }
        }

        public boolean booleanValue(final String field, final boolean fallback) {
            String key = "\"" + field + "\"";
            int keyIndex = rawJson.indexOf(key);
            if (keyIndex < 0) {
                return fallback;
            }
            int colonIndex = rawJson.indexOf(':', keyIndex + key.length());
            if (colonIndex < 0) {
                return fallback;
            }
            int start = colonIndex + 1;
            while (start < rawJson.length() && Character.isWhitespace(rawJson.charAt(start))) {
                start++;
            }
            if (rawJson.startsWith("true", start)) {
                return true;
            }
            if (rawJson.startsWith("false", start)) {
                return false;
            }
            return fallback;
        }

        private static String stringField(final String json, final String field) {
            String key = "\"" + field + "\"";
            int keyIndex = json.indexOf(key);
            if (keyIndex < 0) {
                return null;
            }
            int colonIndex = json.indexOf(':', keyIndex + key.length());
            if (colonIndex < 0) {
                return null;
            }
            int quoteIndex = json.indexOf('"', colonIndex + 1);
            if (quoteIndex < 0) {
                return null;
            }
            StringBuilder value = new StringBuilder();
            boolean escaped = false;
            for (int index = quoteIndex + 1; index < json.length(); index++) {
                char character = json.charAt(index);
                if (escaped) {
                    value.append(switch (character) {
                        case '"' -> '"';
                        case '\\' -> '\\';
                        case 'n' -> '\n';
                        case 'r' -> '\r';
                        case 't' -> '\t';
                        default -> character;
                    });
                    escaped = false;
                } else if (character == '\\') {
                    escaped = true;
                } else if (character == '"') {
                    return value.toString();
                } else {
                    value.append(character);
                }
            }
            return null;
        }

        private static String numberText(final String json, final String field) {
            String key = "\"" + field + "\"";
            int keyIndex = json.indexOf(key);
            if (keyIndex < 0) {
                return null;
            }
            int colonIndex = json.indexOf(':', keyIndex + key.length());
            if (colonIndex < 0) {
                return null;
            }
            int start = colonIndex + 1;
            while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
                start++;
            }
            int end = start;
            while (end < json.length()) {
                char character = json.charAt(end);
                if (Character.isDigit(character)
                        || character == '-'
                        || character == '+'
                        || character == '.'
                        || character == 'e'
                        || character == 'E') {
                    end++;
                } else {
                    break;
                }
            }
            return end > start ? json.substring(start, end) : null;
        }

        private static int intField(final String json, final String field) {
            String value = numberText(json, field);
            if (value == null) {
                return 0;
            }
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException exception) {
                return 0;
            }
        }
    }
}
