package com.github.javachaos.javaneuralnetwork.examples;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Command-line entry point for running the generic neuro-evolution engine
 * without the Swing visualizer.
 */
public final class NeuroEvolutionHeadlessRunner {

    private static final String DEFAULT_PROBLEM = "transformer-block";
    private static final int DEFAULT_POPULATION = 24;
    private static final int DEFAULT_GENERATIONS = 1_000;
    private static final int DEFAULT_MAX_EPOCHS = 180;
    private static final double DEFAULT_TARGET_MSE = 0.01;
    private static final int DEFAULT_EVALUATION_REPEATS = 1;
    private static final double DEFAULT_MUTATION_INTENSITY = 0.09;
    private static final double DEFAULT_COMPLEXITY_PENALTY = 0.001;
    private static final int DEFAULT_MAX_MUTATIONS_PER_CHILD = 3;
    private static final int DEFAULT_PARALLELISM = NeuroEvolutionParallelism.defaultParallelism();
    private static final long DEFAULT_SEED = 86_753L;

    private NeuroEvolutionHeadlessRunner() {
    }

    public static void main(final String[] args) {
        RunOptions options = RunOptions.from(args);
        XorNeuroEvolution.EvolutionConfig config = new XorNeuroEvolution.EvolutionConfig(
                options.populationSize(),
                options.generations(),
                DEFAULT_MAX_EPOCHS,
                DEFAULT_TARGET_MSE,
                DEFAULT_EVALUATION_REPEATS,
                DEFAULT_MUTATION_INTENSITY,
                DEFAULT_COMPLEXITY_PENALTY,
                DEFAULT_MAX_MUTATIONS_PER_CHILD,
                options.parallelism(),
                options.seed());
        NeuroEvolutionProblemCatalog catalog = new DefaultNeuroEvolutionProblemCatalog();
        NeuroEvolutionProblem problem = catalog.find(options.problemKey(), config.generalizationGridSize());
        printRunStart(problem, config);
        long startNanos = System.nanoTime();
        XorNeuroEvolution.EvolutionResult result = NeuroEvolution.evolve(
                problem,
                config,
                NeuroEvolutionHeadlessRunner::printGeneration);
        long elapsedNanos = System.nanoTime() - startNanos;
        XorNeuroEvolution.CandidateScore best = result.best();
        Path checkpointPath = options.checkpointPath(problem);
        String checkpointError = null;
        try {
            NeuroEvolutionChampionCheckpoint.save(checkpointPath, problem, config, best);
        } catch (IOException exception) {
            checkpointError = exception.getMessage();
            printCheckpointSaveFailed(problem, checkpointPath, checkpointError);
        }
        printSummary(problem, config, best, checkpointPath, checkpointError, elapsedNanos);
    }

    private static void printRunStart(
            final NeuroEvolutionProblem problem,
            final XorNeuroEvolution.EvolutionConfig config) {
        System.out.println(new JsonObject()
                .string("event", "run_start")
                .string("problem_key", problem.key())
                .string("problem_name", problem.name())
                .string("mode", problem.classification() ? "classification" : "regression")
                .number("input_dimensions", problem.inputDimensions())
                .number("output_dimensions", problem.outputDimensions())
                .number("training_samples", problem.trainingSamples().size())
                .number("generalization_samples", problem.generalizationSamples().size())
                .string("sample_state_policy", problem.statefulSamples() ? "stateful" : "stateless")
                .object("config", configJson(config)));
    }

    private static void printSummary(
            final NeuroEvolutionProblem problem,
            final XorNeuroEvolution.EvolutionConfig config,
            final XorNeuroEvolution.CandidateScore best,
            final Path checkpointPath,
            final String checkpointError,
            final long elapsedNanos) {
        System.out.println(new JsonObject()
                .string("event", "run_complete")
                .string("problem_key", problem.key())
                .string("problem_name", problem.name())
                .number("elapsed_seconds", elapsedNanos / 1_000_000_000.0)
                .object("config", configJson(config))
                .object("best", scoreJson(problem, best))
                .object("genome", genomeJson(best.genome()))
                .string("checkpoint", checkpointPath.toAbsolutePath().toString())
                .bool("checkpoint_saved", checkpointError == null)
                .nullableString("checkpoint_error", checkpointError));
    }

    private static void printGeneration(final XorNeuroEvolution.EvolutionProgress progress) {
        XorNeuroEvolution.EvolutionConfig config = progress.config();
        int staleGenerations = progress.generationsSinceImprovement();
        System.out.println(new JsonObject()
                .string("event", "generation")
                .string("problem_key", progress.problem().key())
                .number("generation", progress.generation())
                .number("stale_generations", staleGenerations)
                .string("strategy", XorNeuroEvolution.evolutionStrategySummary(config, staleGenerations))
                .bool("reheated", XorNeuroEvolution.reheated(config, staleGenerations))
                .bool("reseeding", XorNeuroEvolution.reseeding(config, staleGenerations))
                .number("mutation_intensity", XorNeuroEvolution.adaptiveMutationIntensity(config, staleGenerations))
                .number("reseed_fraction", XorNeuroEvolution.stagnationReseedFraction(config, staleGenerations))
                .object("champion", scoreJson(progress.problem(), progress.champion()))
                .object("champion_genome", genomeJson(progress.champion().genome()))
                .object("best", scoreJson(progress.problem(), progress.best())));
    }

    private static void printCheckpointSaveFailed(
            final NeuroEvolutionProblem problem,
            final Path checkpointPath,
            final String message) {
        System.out.println(new JsonObject()
                .string("event", "checkpoint_save_failed")
                .string("problem_key", problem.key())
                .string("checkpoint", checkpointPath.toAbsolutePath().toString())
                .nullableString("message", message));
    }

    private static JsonObject configJson(final XorNeuroEvolution.EvolutionConfig config) {
        return new JsonObject()
                .number("population_size", config.populationSize())
                .number("generations", config.generations())
                .number("max_epochs", config.maxEpochs())
                .number("target_mean_squared_error", config.targetMeanSquaredError())
                .number("evaluation_repeats", config.evaluationRepeats())
                .number("mutation_intensity", config.mutationIntensity())
                .number("complexity_penalty", config.complexityPenalty())
                .number("max_mutations_per_child", config.maxMutationsPerChild())
                .number("generalization_grid_size", config.generalizationGridSize())
                .number("jitter_samples_per_corner", config.jitterSamplesPerCorner())
                .number("jitter_radius", config.jitterRadius())
                .number("generalization_weight", config.generalizationWeight())
                .number("jitter_weight", config.jitterWeight())
                .number("smoothness_weight", config.smoothnessWeight())
                .number("parallelism", config.parallelism())
                .number("seed", config.seed());
    }

    private static JsonObject scoreJson(
            final NeuroEvolutionProblem problem,
            final XorNeuroEvolution.CandidateScore score) {
        return new JsonObject()
                .number("generation", score.generation())
                .number("score", score.score())
                .number("training_mse", score.meanSquaredError())
                .number("configured_loss", score.configuredLoss())
                .number("accuracy", problem.classification() ? score.accuracy() : null)
                .number("generalization_mse", score.generalizationMeanSquaredError())
                .number("group_relative_generalization_error", score.groupRelativeGeneralizationError())
                .number("jitter_mse", score.jitterMeanSquaredError())
                .number("smoothness_penalty", score.smoothnessPenalty())
                .number("complexity", score.complexity())
                .number("complexity_pressure", XorNeuroEvolution.normalizedComplexity(
                        problem,
                        score.complexity()))
                .number("predictive_free_energy", score.predictiveFreeEnergy())
                .number("sensory_prediction_energy", score.sensoryPredictionEnergy())
                .number("latent_prediction_energy", score.latentPredictionEnergy())
                .number("complexity_prior_energy", score.complexityPriorEnergy())
                .number("free_energy_objective_weight", problem.freeEnergyProfile().objectiveWeight());
    }

    private static JsonObject genomeJson(final EvolvableXorGenome genome) {
        return new JsonObject()
                .number("hidden_layers", genome.hiddenLayers())
                .number("hidden_neurons_per_layer", genome.hiddenNeurons())
                .number("total_hidden_neurons", genome.totalHiddenNeurons())
                .number("memory_cells", genome.memoryCells())
                .number("recurrent_connections", genome.recurrentConnections())
                .number("connection_density", genome.connectionDensity())
                .string("input_representation", genome.inputRepresentation().name())
                .string("hidden_activation", genome.hiddenActivation().name())
                .string("output_activation", genome.outputActivation().name())
                .string("loss_function", genome.lossFunction().name())
                .string("learning_schedule", genome.learningSchedule().name())
                .bool("kernel_memory", genome.kernelMemory())
                .bool("phase_encoding", genome.phaseEncoding())
                .bool("hebbian_update", genome.hebbianUpdate())
                .bool("gradient_update", genome.gradientUpdate())
                .bool("normalization", genome.normalization())
                .bool("second_derivative_estimate", genome.secondDerivativeEstimate())
                .number("input_learning_rate", genome.inputLearningRate())
                .number("output_learning_rate", genome.outputLearningRate())
                .number("recurrent_learning_rate", genome.recurrentLearningRate())
                .number("hebbian_learning_rate", genome.hebbianLearningRate())
                .number("memory_learning_rate", genome.memoryLearningRate())
                .number("momentum", genome.momentum())
                .number("weight_decay", genome.weightDecay())
                .number("normalization_strength", genome.normalizationStrength())
                .number("activation_slope", genome.activationSlope())
                .number("error_clip", genome.errorClip())
                .number("kernel_sharpness", genome.kernelSharpness());
    }

    private record RunOptions(
            String problemKey,
            int generations,
            int populationSize,
            long seed,
            Path checkpointPath,
            int parallelism) {

        private RunOptions {
            Objects.requireNonNull(problemKey, "Problem key cannot be null.");
            if (problemKey.isBlank()) {
                problemKey = DEFAULT_PROBLEM;
            }
            if (generations <= 0) {
                generations = DEFAULT_GENERATIONS;
            }
            if (populationSize < 4) {
                populationSize = DEFAULT_POPULATION;
            }
            if (parallelism < 1) {
                parallelism = DEFAULT_PARALLELISM;
            }
        }

        static RunOptions from(final String[] args) {
            String problemKey = args.length > 0 ? args[0] : DEFAULT_PROBLEM;
            int generations = args.length > 1 ? parseInt(args[1], DEFAULT_GENERATIONS) : DEFAULT_GENERATIONS;
            int populationSize = args.length > 2 ? parseInt(args[2], DEFAULT_POPULATION) : DEFAULT_POPULATION;
            long seed = args.length > 3 ? parseLong(args[3], DEFAULT_SEED) : DEFAULT_SEED;
            Path checkpointPath = args.length > 4 && !args[4].isBlank() ? Path.of(args[4]) : null;
            int parallelism = args.length > 5 ? parseInt(args[5], DEFAULT_PARALLELISM) : DEFAULT_PARALLELISM;
            return new RunOptions(problemKey, generations, populationSize, seed, checkpointPath, parallelism);
        }

        private static int parseInt(final String raw, final int fallback) {
            try {
                return Integer.parseInt(raw);
            } catch (NumberFormatException exception) {
                return fallback;
            }
        }

        private static long parseLong(final String raw, final long fallback) {
            try {
                return Long.parseLong(raw);
            } catch (NumberFormatException exception) {
                return fallback;
            }
        }

        private Path checkpointPath(final NeuroEvolutionProblem problem) {
            if (checkpointPath != null) {
                return checkpointPath;
            }
            return Path.of(".github", "neuro-evolution-checkpoints", problem.key() + "-headless-best.properties");
        }
    }

    private static final class JsonObject {

        private final StringBuilder builder = new StringBuilder("{");
        private boolean first = true;

        JsonObject string(final String name, final String value) {
            field(name);
            appendQuoted(value);
            return this;
        }

        JsonObject nullableString(final String name, final String value) {
            field(name);
            if (value == null) {
                builder.append("null");
            } else {
                appendQuoted(value);
            }
            return this;
        }

        JsonObject number(final String name, final Number value) {
            field(name);
            if (value == null) {
                builder.append("null");
            } else if (value instanceof Double doubleValue && !Double.isFinite(doubleValue)) {
                appendQuoted(Double.toString(doubleValue));
            } else if (value instanceof Float floatValue && !Float.isFinite(floatValue)) {
                appendQuoted(Float.toString(floatValue));
            } else {
                builder.append(value);
            }
            return this;
        }

        JsonObject bool(final String name, final boolean value) {
            field(name);
            builder.append(value);
            return this;
        }

        JsonObject object(final String name, final JsonObject value) {
            field(name);
            builder.append(value);
            return this;
        }

        private void field(final String name) {
            if (!first) {
                builder.append(',');
            }
            first = false;
            appendQuoted(name);
            builder.append(':');
        }

        private void appendQuoted(final String value) {
            builder.append('"');
            for (int i = 0; i < value.length(); i++) {
                char c = value.charAt(i);
                switch (c) {
                    case '"' -> builder.append("\\\"");
                    case '\\' -> builder.append("\\\\");
                    case '\b' -> builder.append("\\b");
                    case '\f' -> builder.append("\\f");
                    case '\n' -> builder.append("\\n");
                    case '\r' -> builder.append("\\r");
                    case '\t' -> builder.append("\\t");
                    default -> {
                        if (c < 0x20) {
                            builder.append(String.format(java.util.Locale.ROOT, "\\u%04x", (int) c));
                        } else {
                            builder.append(c);
                        }
                    }
                }
            }
            builder.append('"');
        }

        @Override
        public String toString() {
            return builder + "}";
        }
    }
}
