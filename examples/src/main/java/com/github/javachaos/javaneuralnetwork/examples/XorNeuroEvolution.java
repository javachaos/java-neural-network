package com.github.javachaos.javaneuralnetwork.examples;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;

/**
 * Evolves learners that can change topology, memory, representation, loss, and
 * update rules while being evaluated on supervised problems. XOR is the default
 * problem for backwards compatibility with the original demo.
 */
public final class XorNeuroEvolution {

    private static final int DEFAULT_POPULATION_SIZE = 64;
    private static final int DEFAULT_GENERATIONS = 40;
    private static final int DEFAULT_MAX_EPOCHS = 1_500;
    private static final double DEFAULT_TARGET_MSE = 0.01;
    private static final int DEFAULT_EVALUATION_REPEATS = 3;
    private static final double DEFAULT_MUTATION_INTENSITY = 0.08;
    private static final double DEFAULT_COMPLEXITY_PENALTY = 0.001;
    private static final int DEFAULT_MAX_MUTATIONS_PER_CHILD = 3;
    private static final int DEFAULT_GENERALIZATION_GRID_SIZE = 9;
    private static final int DEFAULT_JITTER_SAMPLES_PER_CORNER = 6;
    private static final double DEFAULT_JITTER_RADIUS = 0.12;
    private static final double DEFAULT_GENERALIZATION_WEIGHT = 0.6;
    private static final double DEFAULT_JITTER_WEIGHT = 0.4;
    private static final double DEFAULT_SMOOTHNESS_WEIGHT = 0.1;
    private static final double GROUP_RELATIVE_GENERALIZATION_WEIGHT = 0.08;
    private static final double MAX_GROUP_RELATIVE_GENERALIZATION_ERROR = 3.0;
    private static final int DEFAULT_PARALLELISM = NeuroEvolutionParallelism.defaultParallelism();

    private XorNeuroEvolution() {
    }

    public record EvolutionConfig(
            int populationSize,
            int generations,
            int maxEpochs,
            double targetMeanSquaredError,
            int evaluationRepeats,
            double mutationIntensity,
            double complexityPenalty,
            int maxMutationsPerChild,
            int generalizationGridSize,
            int jitterSamplesPerCorner,
            double jitterRadius,
            double generalizationWeight,
            double jitterWeight,
            double smoothnessWeight,
            int parallelism,
            long seed) {

        public EvolutionConfig(
                final int populationSize,
                final int generations,
                final int maxEpochs,
                final double targetMeanSquaredError,
                final int evaluationRepeats,
                final double mutationIntensity,
                final double complexityPenalty,
                final int maxMutationsPerChild,
                final long seed) {
            this(
                    populationSize,
                    generations,
                    maxEpochs,
                    targetMeanSquaredError,
                    evaluationRepeats,
                    mutationIntensity,
                    complexityPenalty,
                    maxMutationsPerChild,
                    DEFAULT_GENERALIZATION_GRID_SIZE,
                    DEFAULT_JITTER_SAMPLES_PER_CORNER,
                    DEFAULT_JITTER_RADIUS,
                    DEFAULT_GENERALIZATION_WEIGHT,
                    DEFAULT_JITTER_WEIGHT,
                    DEFAULT_SMOOTHNESS_WEIGHT,
                    DEFAULT_PARALLELISM,
                    seed);
        }

        public EvolutionConfig(
                final int populationSize,
                final int generations,
                final int maxEpochs,
                final double targetMeanSquaredError,
                final int evaluationRepeats,
                final double mutationIntensity,
                final double complexityPenalty,
                final int maxMutationsPerChild,
                final int parallelism,
                final long seed) {
            this(
                    populationSize,
                    generations,
                    maxEpochs,
                    targetMeanSquaredError,
                    evaluationRepeats,
                    mutationIntensity,
                    complexityPenalty,
                    maxMutationsPerChild,
                    DEFAULT_GENERALIZATION_GRID_SIZE,
                    DEFAULT_JITTER_SAMPLES_PER_CORNER,
                    DEFAULT_JITTER_RADIUS,
                    DEFAULT_GENERALIZATION_WEIGHT,
                    DEFAULT_JITTER_WEIGHT,
                    DEFAULT_SMOOTHNESS_WEIGHT,
                    parallelism,
                    seed);
        }

        public EvolutionConfig(
                final int populationSize,
                final int generations,
                final int maxEpochs,
                final double targetMeanSquaredError,
                final int evaluationRepeats,
                final double mutationIntensity,
                final double complexityPenalty,
                final int maxMutationsPerChild,
                final int generalizationGridSize,
                final int jitterSamplesPerCorner,
                final double jitterRadius,
                final double generalizationWeight,
                final double jitterWeight,
                final double smoothnessWeight,
                final long seed) {
            this(
                    populationSize,
                    generations,
                    maxEpochs,
                    targetMeanSquaredError,
                    evaluationRepeats,
                    mutationIntensity,
                    complexityPenalty,
                    maxMutationsPerChild,
                    generalizationGridSize,
                    jitterSamplesPerCorner,
                    jitterRadius,
                    generalizationWeight,
                    jitterWeight,
                    smoothnessWeight,
                    DEFAULT_PARALLELISM,
                    seed);
        }

        public EvolutionConfig {
            if (populationSize < 4) {
                throw new IllegalArgumentException("Population size must be at least four.");
            }
            if (generations < 1) {
                throw new IllegalArgumentException("Generations must be positive.");
            }
            if (maxEpochs < 1) {
                throw new IllegalArgumentException("Max epochs must be positive.");
            }
            if (!Double.isFinite(targetMeanSquaredError) || targetMeanSquaredError <= 0.0) {
                throw new IllegalArgumentException("Target mean squared error must be positive and finite.");
            }
            if (evaluationRepeats < 1) {
                throw new IllegalArgumentException("Evaluation repeats must be positive.");
            }
            if (!Double.isFinite(mutationIntensity) || mutationIntensity < 0.0) {
                throw new IllegalArgumentException("Mutation intensity must be finite and non-negative.");
            }
            if (!Double.isFinite(complexityPenalty) || complexityPenalty < 0.0) {
                throw new IllegalArgumentException("Complexity penalty must be finite and non-negative.");
            }
            if (maxMutationsPerChild < 1) {
                throw new IllegalArgumentException("Max mutations per child must be positive.");
            }
            if (generalizationGridSize < 2) {
                throw new IllegalArgumentException("Generalization grid size must be at least two.");
            }
            if (jitterSamplesPerCorner < 0) {
                throw new IllegalArgumentException("Jitter samples per corner cannot be negative.");
            }
            if (!Double.isFinite(jitterRadius) || jitterRadius < 0.0) {
                throw new IllegalArgumentException("Jitter radius must be finite and non-negative.");
            }
            if (!Double.isFinite(generalizationWeight) || generalizationWeight < 0.0) {
                throw new IllegalArgumentException("Generalization weight must be finite and non-negative.");
            }
            if (!Double.isFinite(jitterWeight) || jitterWeight < 0.0) {
                throw new IllegalArgumentException("Jitter weight must be finite and non-negative.");
            }
            if (!Double.isFinite(smoothnessWeight) || smoothnessWeight < 0.0) {
                throw new IllegalArgumentException("Smoothness weight must be finite and non-negative.");
            }
            if (parallelism < 1) {
                throw new IllegalArgumentException("Parallelism must be positive.");
            }
        }

        public static EvolutionConfig defaults() {
            return new EvolutionConfig(
                    DEFAULT_POPULATION_SIZE,
                    DEFAULT_GENERATIONS,
                    DEFAULT_MAX_EPOCHS,
                    DEFAULT_TARGET_MSE,
                    DEFAULT_EVALUATION_REPEATS,
                    DEFAULT_MUTATION_INTENSITY,
                    DEFAULT_COMPLEXITY_PENALTY,
                    DEFAULT_MAX_MUTATIONS_PER_CHILD,
                    DEFAULT_GENERALIZATION_GRID_SIZE,
                    DEFAULT_JITTER_SAMPLES_PER_CORNER,
                    DEFAULT_JITTER_RADIUS,
                    DEFAULT_GENERALIZATION_WEIGHT,
                    DEFAULT_JITTER_WEIGHT,
                    DEFAULT_SMOOTHNESS_WEIGHT,
                    DEFAULT_PARALLELISM,
                    13_337L);
        }
    }

    public record CandidateScore(
            EvolvableXorGenome genome,
            double score,
            double meanSquaredError,
            double configuredLoss,
            double accuracy,
            double generalizationMeanSquaredError,
            double groupRelativeGeneralizationError,
            double jitterMeanSquaredError,
            double smoothnessPenalty,
            double complexity,
            List<NeuroEvolutionOutputGroupScore> outputGroupScores,
            int generation) {

        public CandidateScore {
            Objects.requireNonNull(genome, "Genome cannot be null.");
            if (Double.isNaN(groupRelativeGeneralizationError) || groupRelativeGeneralizationError < 0.0) {
                throw new IllegalArgumentException("Group-relative generalization error must be non-negative.");
            }
            outputGroupScores = List.copyOf(Objects.requireNonNull(
                    outputGroupScores,
                    "Output group scores cannot be null."));
        }

        public CandidateScore(
                final EvolvableXorGenome genome,
                final double score,
                final double meanSquaredError,
                final double configuredLoss,
                final double accuracy,
                final double generalizationMeanSquaredError,
                final double jitterMeanSquaredError,
                final double smoothnessPenalty,
                final double complexity,
                final int generation) {
            this(
                    genome,
                    score,
                    meanSquaredError,
                    configuredLoss,
                    accuracy,
                    generalizationMeanSquaredError,
                    0.0,
                    jitterMeanSquaredError,
                    smoothnessPenalty,
                    complexity,
                    List.of(),
                    generation);
        }

        public CandidateScore withOutputGroupScores(
                final List<NeuroEvolutionOutputGroupScore> updatedOutputGroupScores) {
            return new CandidateScore(
                    genome,
                    score,
                    meanSquaredError,
                    configuredLoss,
                    accuracy,
                    generalizationMeanSquaredError,
                    groupRelativeGeneralizationError,
                    jitterMeanSquaredError,
                    smoothnessPenalty,
                    complexity,
                    updatedOutputGroupScores,
                    generation);
        }
    }

    public record EvolutionResult(
            NeuroEvolutionProblem problem,
            EvolutionConfig config,
            CandidateScore best,
            List<CandidateScore> champions) {

        public EvolutionResult(
                final EvolutionConfig config,
                final CandidateScore best,
                final List<CandidateScore> champions) {
            this(new XorProblem(config.generalizationGridSize()), config, best, champions);
        }

        public EvolutionResult {
            Objects.requireNonNull(problem, "Problem cannot be null.");
            Objects.requireNonNull(config, "Config cannot be null.");
            Objects.requireNonNull(best, "Best score cannot be null.");
            champions = List.copyOf(Objects.requireNonNull(champions, "Champions cannot be null."));
        }
    }

    public static EvolutionResult evolve() {
        return evolve(EvolutionConfig.defaults());
    }

    public static EvolutionResult evolve(final EvolutionConfig config) {
        return evolve(new XorProblem(config.generalizationGridSize()), config);
    }

    public static EvolutionResult evolve(final NeuroEvolutionProblem problem, final EvolutionConfig config) {
        Objects.requireNonNull(problem, "Problem cannot be null.");
        Objects.requireNonNull(config, "Config cannot be null.");
        Random random = new Random(config.seed());
        List<EvolvableXorGenome> population = new ArrayList<>(config.populationSize());
        for (int i = 0; i < config.populationSize(); i++) {
            population.add(EvolvableXorGenome.random(random));
        }

        CandidateScore best = null;
        List<CandidateScore> champions = new ArrayList<>();
        ForkJoinPool evaluationPool =
                NeuroEvolutionParallelism.newPool(config.parallelism(), config.populationSize());
        try {
            for (int generation = 0; generation < config.generations(); generation++) {
                List<CandidateScore> scored = scorePopulation(problem, population, config, generation, evaluationPool);
                CandidateScore champion = scored.get(0);
                champions.add(champion);
                if (best == null || champion.score() < best.score()) {
                    best = champion;
                }
                population = nextGeneration(scored, config, random);
            }
        } finally {
            NeuroEvolutionParallelism.shutdown(evaluationPool);
        }
        return new EvolutionResult(problem, config, best, champions);
    }

    public static CandidateScore evaluate(
            final EvolvableXorGenome genome,
            final EvolutionConfig config,
            final int generation) {
        return evaluate(new XorProblem(config.generalizationGridSize()), genome, config, generation);
    }

    public static CandidateScore evaluate(
            final NeuroEvolutionProblem problem,
            final EvolvableXorGenome genome,
            final EvolutionConfig config,
            final int generation) {
        return evaluateCandidate(problem, genome, config, generation, 0);
    }

    static CandidateScore evaluateCandidate(
            final NeuroEvolutionProblem problem,
            final EvolvableXorGenome genome,
            final EvolutionConfig config,
            final int generation,
            final int candidateIndex) {
        Objects.requireNonNull(problem, "Problem cannot be null.");
        Objects.requireNonNull(genome, "Genome cannot be null.");
        Objects.requireNonNull(config, "Config cannot be null.");
        if (candidateIndex < 0) {
            throw new IllegalArgumentException("Candidate index cannot be negative.");
        }
        double mseTotal = 0.0;
        double lossTotal = 0.0;
        double accuracyTotal = 0.0;
        double generalizationTotal = 0.0;
        double groupRelativeGeneralizationTotal = 0.0;
        double jitterTotal = 0.0;
        double smoothnessTotal = 0.0;
        long candidateSeed = config.seed() + 1_000_003L * candidateIndex;
        for (int repeat = 0; repeat < config.evaluationRepeats(); repeat++) {
            Random learnerRandom = new Random(candidateSeed + 37_000L * generation + 131L * repeat);
            RichEvolvedProblemLearner learner = new RichEvolvedProblemLearner(problem, genome, learnerRandom);
            double mse = learner.train(config.maxEpochs(), config.targetMeanSquaredError());
            NeuroEvolutionGeneralizationEvaluator.GeneralizationMetrics generalization = generalizationMetrics(
                    problem,
                    learner,
                    config,
                    generation,
                    repeat,
                    candidateIndex);
            mseTotal += mse;
            lossTotal += learner.configuredLoss();
            accuracyTotal += learner.accuracy();
            generalizationTotal += generalization.generalizationMeanSquaredError();
            groupRelativeGeneralizationTotal += generalization.groupRelativeGeneralizationError();
            jitterTotal += generalization.jitterMeanSquaredError();
            smoothnessTotal += generalization.smoothnessPenalty();
        }
        double meanSquaredError = mseTotal / config.evaluationRepeats();
        double configuredLoss = lossTotal / config.evaluationRepeats();
        double accuracy = accuracyTotal / config.evaluationRepeats();
        double generalizationMeanSquaredError = generalizationTotal / config.evaluationRepeats();
        double groupRelativeGeneralizationError =
                groupRelativeGeneralizationTotal / config.evaluationRepeats();
        double jitterMeanSquaredError = jitterTotal / config.evaluationRepeats();
        double smoothnessPenalty = smoothnessTotal / config.evaluationRepeats();
        double complexity = genome.complexityCost(problem);
        double score = score(
                problem,
                meanSquaredError,
                configuredLoss,
                accuracy,
                generalizationMeanSquaredError,
                groupRelativeGeneralizationError,
                jitterMeanSquaredError,
                smoothnessPenalty,
                complexity,
                config);
        return new CandidateScore(
                genome,
                score,
                meanSquaredError,
                configuredLoss,
                accuracy,
                generalizationMeanSquaredError,
                groupRelativeGeneralizationError,
                jitterMeanSquaredError,
                smoothnessPenalty,
                complexity,
                List.of(),
                generation);
    }

    public static void main(final String[] args) {
        EvolutionResult result = evolve();
        CandidateScore best = result.best();
        System.out.println("Problem: " + result.problem().name());
        System.out.println("Best score: " + best.score());
        System.out.println("Best MSE: " + best.meanSquaredError());
        System.out.println("Best configured loss: " + best.configuredLoss());
        System.out.println("Best accuracy: " + best.accuracy());
        System.out.println("Best generalization MSE: " + best.generalizationMeanSquaredError());
        System.out.println("Best group-relative generalization error: " + best.groupRelativeGeneralizationError());
        System.out.println("Best jitter MSE: " + best.jitterMeanSquaredError());
        System.out.println("Best smoothness penalty: " + best.smoothnessPenalty());
        System.out.println("Best complexity: " + best.complexity());
        System.out.println("Parallelism: " + result.config().parallelism());
        System.out.println("Best genome: " + best.genome());
    }

    private static List<CandidateScore> scorePopulation(
            final NeuroEvolutionProblem problem,
            final List<EvolvableXorGenome> population,
            final EvolutionConfig config,
            final int generation,
            final ForkJoinPool evaluationPool) {
        return NeuroEvolutionParallelism.mapAndSort(
                population.size(),
                evaluationPool,
                index -> evaluateCandidate(problem, population.get(index), config, generation, index),
                Comparator.comparingDouble(CandidateScore::score));
    }

    private static List<EvolvableXorGenome> nextGeneration(
            final List<CandidateScore> scored,
            final EvolutionConfig config,
            final Random random) {
        int eliteCount = Math.max(2, config.populationSize() / 10);
        List<EvolvableXorGenome> next = new ArrayList<>(config.populationSize());
        for (int i = 0; i < eliteCount; i++) {
            next.add(scored.get(i).genome());
        }
        while (next.size() < config.populationSize()) {
            EvolvableXorGenome parentA = tournament(scored, random).genome();
            EvolvableXorGenome parentB = tournament(scored, random).genome();
            EvolvableXorGenome child = parentA.crossover(parentB, random);
            int mutations = 1 + random.nextInt(config.maxMutationsPerChild());
            for (int i = 0; i < mutations; i++) {
                child = child.mutate(random, config.mutationIntensity());
            }
            next.add(child);
        }
        return next;
    }

    private static CandidateScore tournament(final List<CandidateScore> scored, final Random random) {
        CandidateScore best = null;
        int tournamentSize = Math.min(5, scored.size());
        for (int i = 0; i < tournamentSize; i++) {
            CandidateScore candidate = scored.get(random.nextInt(scored.size()));
            if (best == null || candidate.score() < best.score()) {
                best = candidate;
            }
        }
        return best;
    }

    private static double score(
            final NeuroEvolutionProblem problem,
            final double meanSquaredError,
            final double configuredLoss,
            final double accuracy,
            final double generalizationMeanSquaredError,
            final double groupRelativeGeneralizationError,
            final double jitterMeanSquaredError,
            final double smoothnessPenalty,
            final double complexity,
            final EvolutionConfig config) {
        Objects.requireNonNull(problem, "Problem cannot be null.");
        if (!Double.isFinite(meanSquaredError)
                || !Double.isFinite(configuredLoss)
                || !Double.isFinite(generalizationMeanSquaredError)
                || !Double.isFinite(jitterMeanSquaredError)
                || !Double.isFinite(smoothnessPenalty)) {
            return Double.POSITIVE_INFINITY;
        }
        double normalizedComplexity = complexity / problem.complexityScale();
        double boundedGroupRelativeGeneralizationError =
                boundedGroupRelativeGeneralizationError(groupRelativeGeneralizationError);
        return meanSquaredError
                + configuredLoss * 0.1
                + (1.0 - accuracy) * 0.8
                + generalizationMeanSquaredError * config.generalizationWeight()
                + jitterMeanSquaredError * config.jitterWeight()
                + smoothnessPenalty * config.smoothnessWeight()
                + normalizedComplexity * config.complexityPenalty()
                + boundedGroupRelativeGeneralizationError * GROUP_RELATIVE_GENERALIZATION_WEIGHT;
    }

    static double normalizedComplexity(final NeuroEvolutionProblem problem, final double complexity) {
        Objects.requireNonNull(problem, "Problem cannot be null.");
        return complexity / problem.complexityScale();
    }

    static double boundedGroupRelativeGeneralizationError(final double groupRelativeGeneralizationError) {
        if (!Double.isFinite(groupRelativeGeneralizationError)) {
            return MAX_GROUP_RELATIVE_GENERALIZATION_ERROR;
        }
        return Math.min(
                MAX_GROUP_RELATIVE_GENERALIZATION_ERROR,
                Math.max(0.0, groupRelativeGeneralizationError));
    }

    private static NeuroEvolutionGeneralizationEvaluator.GeneralizationMetrics generalizationMetrics(
            final NeuroEvolutionProblem problem,
            final RichEvolvedProblemLearner learner,
            final EvolutionConfig config,
            final int generation,
            final int repeat,
            final int candidateIndex) {
        try {
            return NeuroEvolutionGeneralizationEvaluator.evaluate(
                    learner,
                    problem,
                    config.jitterSamplesPerCorner(),
                    config.jitterRadius(),
                    config.seed() + 1_000_003L * candidateIndex + 91_000L * generation + 10_007L * repeat);
        } catch (IllegalArgumentException exception) {
            return new NeuroEvolutionGeneralizationEvaluator.GeneralizationMetrics(
                    Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY);
        }
    }

}
