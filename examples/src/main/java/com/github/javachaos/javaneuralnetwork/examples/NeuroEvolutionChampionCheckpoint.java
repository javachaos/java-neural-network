package com.github.javachaos.javaneuralnetwork.examples;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

/**
 * Saves and reloads the best discovered genome plus enough run metadata to
 * recreate its visual champion view.
 */
public final class NeuroEvolutionChampionCheckpoint {

    private static final int VERSION = 1;
    private static final Path DEFAULT_DIRECTORY = Path.of(".github", "neuro-evolution-checkpoints");

    private NeuroEvolutionChampionCheckpoint() {
    }

    public record LoadedChampion(
            NeuroEvolutionProblem problem,
            XorNeuroEvolution.EvolutionConfig config,
            XorNeuroEvolution.CandidateScore score,
            Path path,
            long savedAtEpochMillis) {

        public LoadedChampion {
            Objects.requireNonNull(problem, "Problem cannot be null.");
            Objects.requireNonNull(config, "Config cannot be null.");
            Objects.requireNonNull(score, "Score cannot be null.");
            Objects.requireNonNull(path, "Path cannot be null.");
        }
    }

    public static Path defaultPath(final NeuroEvolutionProblem problem) {
        Objects.requireNonNull(problem, "Problem cannot be null.");
        return DEFAULT_DIRECTORY.resolve(problem.key() + "-best.properties");
    }

    public static void save(
            final Path path,
            final NeuroEvolutionProblem problem,
            final XorNeuroEvolution.EvolutionConfig config,
            final XorNeuroEvolution.CandidateScore score) throws IOException {
        Objects.requireNonNull(path, "Path cannot be null.");
        Objects.requireNonNull(problem, "Problem cannot be null.");
        Objects.requireNonNull(config, "Config cannot be null.");
        Objects.requireNonNull(score, "Score cannot be null.");

        Properties properties = new Properties();
        properties.setProperty("version", Integer.toString(VERSION));
        properties.setProperty("savedAtEpochMillis", Long.toString(System.currentTimeMillis()));
        properties.setProperty("problem.key", problem.key());
        properties.setProperty("problem.name", problem.name());
        writeConfig(properties, config);
        writeScore(properties, score);
        writeGenome(properties, score.genome());

        Path parent = path.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (Writer writer = Files.newBufferedWriter(path)) {
            properties.store(writer, "Java Neural Network neuro-evolution champion checkpoint");
        }
    }

    public static LoadedChampion load(
            final Path path,
            final NeuroEvolutionProblemCatalog catalog) throws IOException {
        Objects.requireNonNull(path, "Path cannot be null.");
        Objects.requireNonNull(catalog, "Problem catalog cannot be null.");

        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(path)) {
            properties.load(reader);
        }
        int version = intProperty(properties, "version");
        if (version != VERSION) {
            throw new IOException("Unsupported checkpoint version: " + version);
        }
        XorNeuroEvolution.EvolutionConfig config = readConfig(properties);
        String problemKey = required(properties, "problem.key");
        NeuroEvolutionProblem problem = catalog.find(problemKey, config.generalizationGridSize());
        return new LoadedChampion(
                problem,
                config,
                readScore(properties),
                path,
                longProperty(properties, "savedAtEpochMillis"));
    }

    private static void writeConfig(
            final Properties properties,
            final XorNeuroEvolution.EvolutionConfig config) {
        properties.setProperty("config.populationSize", Integer.toString(config.populationSize()));
        properties.setProperty("config.generations", Integer.toString(config.generations()));
        properties.setProperty("config.maxEpochs", Integer.toString(config.maxEpochs()));
        properties.setProperty("config.targetMeanSquaredError", Double.toString(config.targetMeanSquaredError()));
        properties.setProperty("config.evaluationRepeats", Integer.toString(config.evaluationRepeats()));
        properties.setProperty("config.mutationIntensity", Double.toString(config.mutationIntensity()));
        properties.setProperty("config.complexityPenalty", Double.toString(config.complexityPenalty()));
        properties.setProperty("config.maxMutationsPerChild", Integer.toString(config.maxMutationsPerChild()));
        properties.setProperty("config.generalizationGridSize", Integer.toString(config.generalizationGridSize()));
        properties.setProperty("config.jitterSamplesPerCorner", Integer.toString(config.jitterSamplesPerCorner()));
        properties.setProperty("config.jitterRadius", Double.toString(config.jitterRadius()));
        properties.setProperty("config.generalizationWeight", Double.toString(config.generalizationWeight()));
        properties.setProperty("config.jitterWeight", Double.toString(config.jitterWeight()));
        properties.setProperty("config.smoothnessWeight", Double.toString(config.smoothnessWeight()));
        properties.setProperty("config.parallelism", Integer.toString(config.parallelism()));
        properties.setProperty("config.seed", Long.toString(config.seed()));
    }

    private static XorNeuroEvolution.EvolutionConfig readConfig(final Properties properties) {
        return new XorNeuroEvolution.EvolutionConfig(
                intProperty(properties, "config.populationSize"),
                intProperty(properties, "config.generations"),
                intProperty(properties, "config.maxEpochs"),
                doubleProperty(properties, "config.targetMeanSquaredError"),
                intProperty(properties, "config.evaluationRepeats"),
                doubleProperty(properties, "config.mutationIntensity"),
                doubleProperty(properties, "config.complexityPenalty"),
                intProperty(properties, "config.maxMutationsPerChild"),
                intProperty(properties, "config.generalizationGridSize"),
                intProperty(properties, "config.jitterSamplesPerCorner"),
                doubleProperty(properties, "config.jitterRadius"),
                doubleProperty(properties, "config.generalizationWeight"),
                doubleProperty(properties, "config.jitterWeight"),
                doubleProperty(properties, "config.smoothnessWeight"),
                intProperty(properties, "config.parallelism"),
                longProperty(properties, "config.seed"));
    }

    private static void writeScore(
            final Properties properties,
            final XorNeuroEvolution.CandidateScore score) {
        properties.setProperty("score.generation", Integer.toString(score.generation()));
        properties.setProperty("score.score", Double.toString(score.score()));
        properties.setProperty("score.meanSquaredError", Double.toString(score.meanSquaredError()));
        properties.setProperty("score.configuredLoss", Double.toString(score.configuredLoss()));
        properties.setProperty("score.accuracy", Double.toString(score.accuracy()));
        properties.setProperty(
                "score.generalizationMeanSquaredError",
                Double.toString(score.generalizationMeanSquaredError()));
        properties.setProperty(
                "score.groupRelativeGeneralizationError",
                Double.toString(score.groupRelativeGeneralizationError()));
        properties.setProperty("score.jitterMeanSquaredError", Double.toString(score.jitterMeanSquaredError()));
        properties.setProperty("score.smoothnessPenalty", Double.toString(score.smoothnessPenalty()));
        properties.setProperty("score.complexity", Double.toString(score.complexity()));
        properties.setProperty("score.predictiveFreeEnergy", Double.toString(score.predictiveFreeEnergy()));
        properties.setProperty("score.sensoryPredictionEnergy", Double.toString(score.sensoryPredictionEnergy()));
        properties.setProperty("score.latentPredictionEnergy", Double.toString(score.latentPredictionEnergy()));
        properties.setProperty("score.complexityPriorEnergy", Double.toString(score.complexityPriorEnergy()));
    }

    private static XorNeuroEvolution.CandidateScore readScore(final Properties properties) {
        return new XorNeuroEvolution.CandidateScore(
                readGenome(properties),
                doubleProperty(properties, "score.score"),
                doubleProperty(properties, "score.meanSquaredError"),
                doubleProperty(properties, "score.configuredLoss"),
                doubleProperty(properties, "score.accuracy"),
                doubleProperty(properties, "score.generalizationMeanSquaredError"),
                doubleProperty(properties, "score.groupRelativeGeneralizationError"),
                doubleProperty(properties, "score.jitterMeanSquaredError"),
                doubleProperty(properties, "score.smoothnessPenalty"),
                doubleProperty(properties, "score.complexity"),
                doubleProperty(properties, "score.predictiveFreeEnergy", 0.0),
                doubleProperty(properties, "score.sensoryPredictionEnergy", 0.0),
                doubleProperty(properties, "score.latentPredictionEnergy", 0.0),
                doubleProperty(properties, "score.complexityPriorEnergy", 0.0),
                List.of(),
                intProperty(properties, "score.generation"));
    }

    private static void writeGenome(final Properties properties, final EvolvableXorGenome genome) {
        properties.setProperty("genome.hiddenNeurons", Integer.toString(genome.hiddenNeurons()));
        properties.setProperty("genome.hiddenLayers", Integer.toString(genome.hiddenLayers()));
        properties.setProperty("genome.recurrentConnections", Integer.toString(genome.recurrentConnections()));
        properties.setProperty("genome.memoryCells", Integer.toString(genome.memoryCells()));
        properties.setProperty("genome.connectionDensity", Double.toString(genome.connectionDensity()));
        properties.setProperty("genome.hiddenActivation", genome.hiddenActivation().name());
        properties.setProperty("genome.outputActivation", genome.outputActivation().name());
        properties.setProperty("genome.inputRepresentation", genome.inputRepresentation().name());
        properties.setProperty("genome.lossFunction", genome.lossFunction().name());
        properties.setProperty("genome.learningSchedule", genome.learningSchedule().name());
        properties.setProperty("genome.hebbianUpdate", Boolean.toString(genome.hebbianUpdate()));
        properties.setProperty("genome.gradientUpdate", Boolean.toString(genome.gradientUpdate()));
        properties.setProperty("genome.normalization", Boolean.toString(genome.normalization()));
        properties.setProperty("genome.kernelMemory", Boolean.toString(genome.kernelMemory()));
        properties.setProperty("genome.phaseEncoding", Boolean.toString(genome.phaseEncoding()));
        properties.setProperty(
                "genome.secondDerivativeEstimate",
                Boolean.toString(genome.secondDerivativeEstimate()));
        properties.setProperty("genome.inputLearningRate", Double.toString(genome.inputLearningRate()));
        properties.setProperty("genome.outputLearningRate", Double.toString(genome.outputLearningRate()));
        properties.setProperty("genome.recurrentLearningRate", Double.toString(genome.recurrentLearningRate()));
        properties.setProperty("genome.hebbianLearningRate", Double.toString(genome.hebbianLearningRate()));
        properties.setProperty("genome.memoryLearningRate", Double.toString(genome.memoryLearningRate()));
        properties.setProperty("genome.momentum", Double.toString(genome.momentum()));
        properties.setProperty("genome.weightDecay", Double.toString(genome.weightDecay()));
        properties.setProperty("genome.normalizationStrength", Double.toString(genome.normalizationStrength()));
        properties.setProperty("genome.activationSlope", Double.toString(genome.activationSlope()));
        properties.setProperty("genome.errorClip", Double.toString(genome.errorClip()));
        properties.setProperty("genome.kernelSharpness", Double.toString(genome.kernelSharpness()));
    }

    private static EvolvableXorGenome readGenome(final Properties properties) {
        return new EvolvableXorGenome(
                intProperty(properties, "genome.hiddenNeurons"),
                intProperty(properties, "genome.hiddenLayers"),
                intProperty(properties, "genome.recurrentConnections"),
                intProperty(properties, "genome.memoryCells"),
                doubleProperty(properties, "genome.connectionDensity"),
                enumProperty(properties, "genome.hiddenActivation", XorActivationFunction.class),
                enumProperty(properties, "genome.outputActivation", XorActivationFunction.class),
                enumProperty(properties, "genome.inputRepresentation", XorInputRepresentation.class),
                enumProperty(properties, "genome.lossFunction", XorLossFunction.class),
                enumProperty(properties, "genome.learningSchedule", XorLearningSchedule.class),
                booleanProperty(properties, "genome.hebbianUpdate"),
                booleanProperty(properties, "genome.gradientUpdate"),
                booleanProperty(properties, "genome.normalization"),
                booleanProperty(properties, "genome.kernelMemory"),
                booleanProperty(properties, "genome.phaseEncoding"),
                booleanProperty(properties, "genome.secondDerivativeEstimate"),
                doubleProperty(properties, "genome.inputLearningRate"),
                doubleProperty(properties, "genome.outputLearningRate"),
                doubleProperty(properties, "genome.recurrentLearningRate"),
                doubleProperty(properties, "genome.hebbianLearningRate"),
                doubleProperty(properties, "genome.memoryLearningRate"),
                doubleProperty(properties, "genome.momentum"),
                doubleProperty(properties, "genome.weightDecay"),
                doubleProperty(properties, "genome.normalizationStrength"),
                doubleProperty(properties, "genome.activationSlope"),
                doubleProperty(properties, "genome.errorClip"),
                doubleProperty(properties, "genome.kernelSharpness"));
    }

    private static String required(final Properties properties, final String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Checkpoint is missing required property: " + key);
        }
        return value;
    }

    private static int intProperty(final Properties properties, final String key) {
        return Integer.parseInt(required(properties, key));
    }

    private static long longProperty(final Properties properties, final String key) {
        return Long.parseLong(required(properties, key));
    }

    private static double doubleProperty(final Properties properties, final String key) {
        return Double.parseDouble(required(properties, key));
    }

    private static double doubleProperty(
            final Properties properties,
            final String key,
            final double fallback) {
        String value = properties.getProperty(key);
        return value == null || value.isBlank() ? fallback : Double.parseDouble(value);
    }

    private static boolean booleanProperty(final Properties properties, final String key) {
        return Boolean.parseBoolean(required(properties, key));
    }

    private static <T extends Enum<T>> T enumProperty(
            final Properties properties,
            final String key,
            final Class<T> enumType) {
        return Enum.valueOf(enumType, required(properties, key));
    }
}
