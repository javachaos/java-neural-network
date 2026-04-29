package com.github.javachaos.javaneuralnetwork.examples;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;

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
    private static final int STAGNATION_PATIENCE_PER_CANDIDATE = 30;
    private static final int MIN_STAGNATION_PATIENCE = 250;
    private static final double BASE_IMMIGRANT_FRACTION = 0.06;
    private static final double STAGNATION_RESEED_START_PRESSURE = 0.25;
    private static final double MAX_STAGNATION_RESEED_FRACTION = 0.65;
    private static final double REHEATED_MUTATION_MULTIPLIER = 3.5;
    private static final double MAX_REHEATED_MUTATION_INTENSITY = 0.45;

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
            double predictiveFreeEnergy,
            double sensoryPredictionEnergy,
            double latentPredictionEnergy,
            double complexityPriorEnergy,
            List<NeuroEvolutionOutputGroupScore> outputGroupScores,
            int generation) {

        public CandidateScore {
            Objects.requireNonNull(genome, "Genome cannot be null.");
            if (Double.isNaN(groupRelativeGeneralizationError) || groupRelativeGeneralizationError < 0.0) {
                throw new IllegalArgumentException("Group-relative generalization error must be non-negative.");
            }
            requireNonNegativeScore(predictiveFreeEnergy, "Predictive free energy");
            requireNonNegativeScore(sensoryPredictionEnergy, "Sensory prediction energy");
            requireNonNegativeScore(latentPredictionEnergy, "Latent prediction energy");
            requireNonNegativeScore(complexityPriorEnergy, "Complexity prior energy");
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
                    0.0,
                    0.0,
                    0.0,
                    0.0,
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
                    predictiveFreeEnergy,
                    sensoryPredictionEnergy,
                    latentPredictionEnergy,
                    complexityPriorEnergy,
                    updatedOutputGroupScores,
                    generation);
        }

        public CandidateScore withFreeEnergyMetrics(final NeuroEvolutionFreeEnergyMetrics metrics) {
            Objects.requireNonNull(metrics, "Free-energy metrics cannot be null.");
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
                    metrics.value(),
                    metrics.sensoryPredictionEnergy(),
                    metrics.latentPredictionEnergy(),
                    metrics.complexityPriorEnergy(),
                    outputGroupScores,
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

    public record EvolutionProgress(
            NeuroEvolutionProblem problem,
            EvolutionConfig config,
            int generation,
            CandidateScore champion,
            CandidateScore best,
            int generationsSinceImprovement) {

        public EvolutionProgress {
            Objects.requireNonNull(problem, "Problem cannot be null.");
            Objects.requireNonNull(config, "Config cannot be null.");
            Objects.requireNonNull(champion, "Champion cannot be null.");
            Objects.requireNonNull(best, "Best score cannot be null.");
            if (generation < 0) {
                throw new IllegalArgumentException("Generation cannot be negative.");
            }
            if (generationsSinceImprovement < 0) {
                throw new IllegalArgumentException("Generations since improvement cannot be negative.");
            }
        }
    }

    public static EvolutionResult evolve() {
        return evolve(EvolutionConfig.defaults());
    }

    public static EvolutionResult evolve(final EvolutionConfig config) {
        return evolve(new XorProblem(config.generalizationGridSize()), config);
    }

    public static EvolutionResult evolve(final NeuroEvolutionProblem problem, final EvolutionConfig config) {
        return evolve(problem, config, progress -> {
        });
    }

    public static EvolutionResult evolve(
            final NeuroEvolutionProblem problem,
            final EvolutionConfig config,
            final Consumer<EvolutionProgress> progressSink) {
        Objects.requireNonNull(problem, "Problem cannot be null.");
        Objects.requireNonNull(config, "Config cannot be null.");
        Objects.requireNonNull(progressSink, "Progress sink cannot be null.");
        Random random = new Random(config.seed());
        List<EvolvableXorGenome> population = new ArrayList<>(config.populationSize());
        for (int i = 0; i < config.populationSize(); i++) {
            population.add(EvolvableXorGenome.random(random));
        }

        CandidateScore best = null;
        List<CandidateScore> champions = new ArrayList<>();
        int generationsSinceImprovement = 0;
        ForkJoinPool evaluationPool =
                NeuroEvolutionParallelism.newPool(config.parallelism(), config.populationSize());
        try {
            for (int generation = 0; generation < config.generations(); generation++) {
                List<CandidateScore> scored = scorePopulation(problem, population, config, generation, evaluationPool);
                CandidateScore champion = scored.get(0);
                champions.add(champion);
                if (best == null || champion.score() < best.score()) {
                    best = champion;
                    generationsSinceImprovement = 0;
                } else {
                    generationsSinceImprovement++;
                }
                progressSink.accept(new EvolutionProgress(
                        problem,
                        config,
                        generation,
                        champion,
                        best,
                        generationsSinceImprovement));
                population = nextGeneration(scored, config, random, best, generationsSinceImprovement);
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
        double predictiveFreeEnergyTotal = 0.0;
        double sensoryPredictionEnergyTotal = 0.0;
        double latentPredictionEnergyTotal = 0.0;
        double complexityPriorEnergyTotal = 0.0;
        double complexity = genome.complexityCost(problem);
        double normalizedComplexity = normalizedComplexity(problem, complexity);
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
            NeuroEvolutionFreeEnergyMetrics freeEnergy =
                    learner.predictiveFreeEnergy(problem.generalizationSamples(), normalizedComplexity);
            predictiveFreeEnergyTotal += freeEnergy.value();
            sensoryPredictionEnergyTotal += freeEnergy.sensoryPredictionEnergy();
            latentPredictionEnergyTotal += freeEnergy.latentPredictionEnergy();
            complexityPriorEnergyTotal += freeEnergy.complexityPriorEnergy();
        }
        double meanSquaredError = mseTotal / config.evaluationRepeats();
        double configuredLoss = lossTotal / config.evaluationRepeats();
        double accuracy = accuracyTotal / config.evaluationRepeats();
        double generalizationMeanSquaredError = generalizationTotal / config.evaluationRepeats();
        double groupRelativeGeneralizationError =
                groupRelativeGeneralizationTotal / config.evaluationRepeats();
        double jitterMeanSquaredError = jitterTotal / config.evaluationRepeats();
        double smoothnessPenalty = smoothnessTotal / config.evaluationRepeats();
        double predictiveFreeEnergy = predictiveFreeEnergyTotal / config.evaluationRepeats();
        double sensoryPredictionEnergy = sensoryPredictionEnergyTotal / config.evaluationRepeats();
        double latentPredictionEnergy = latentPredictionEnergyTotal / config.evaluationRepeats();
        double complexityPriorEnergy = complexityPriorEnergyTotal / config.evaluationRepeats();
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
                predictiveFreeEnergy,
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
                predictiveFreeEnergy,
                sensoryPredictionEnergy,
                latentPredictionEnergy,
                complexityPriorEnergy,
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

    static List<EvolvableXorGenome> nextGeneration(
            final List<CandidateScore> scored,
            final EvolutionConfig config,
            final Random random,
            final CandidateScore best,
            final int generationsSinceImprovement) {
        Objects.requireNonNull(scored, "Scored population cannot be null.");
        Objects.requireNonNull(config, "Config cannot be null.");
        Objects.requireNonNull(random, "Random cannot be null.");
        if (scored.isEmpty()) {
            throw new IllegalArgumentException("Scored population cannot be empty.");
        }
        if (generationsSinceImprovement < 0) {
            throw new IllegalArgumentException("Generations since improvement cannot be negative.");
        }
        boolean reheated = reheated(config, generationsSinceImprovement);
        double mutationIntensity = adaptiveMutationIntensity(config, generationsSinceImprovement);
        int eliteCount = Math.max(2, config.populationSize() / 10);
        int diverseEliteCount = Math.max(eliteCount, config.populationSize() / 4);
        int immigrantCount = Math.max(
                1,
                (int) Math.round(config.populationSize()
                        * stagnationReseedFraction(config, generationsSinceImprovement)));
        int localRefinementCount = reheated ? Math.max(2, config.populationSize() / 6) : 0;
        int breedingLimit = Math.max(1, config.populationSize() - immigrantCount);
        List<EvolvableXorGenome> next = new ArrayList<>(config.populationSize());
        if (best != null) {
            addIfAbsent(next, best.genome(), config.populationSize());
        }
        for (EvolvableXorGenome elite : diverseElites(scored, eliteCount, diverseEliteCount)) {
            addIfAbsent(next, elite, config.populationSize());
        }
        for (int i = 0; i < localRefinementCount && next.size() < breedingLimit; i++) {
            addIfAbsent(
                    next,
                    localRefinement(best == null ? scored.get(0).genome() : best.genome(), random, mutationIntensity),
                    breedingLimit);
        }
        List<List<CandidateScore>> speciesPools = speciesPools(scored);
        int maxMutations = config.maxMutationsPerChild() + (reheated ? 2 : 0);
        while (next.size() < breedingLimit) {
            EvolvableXorGenome parentA = architectureTournament(scored, speciesPools, random).genome();
            EvolvableXorGenome parentB = architectureTournament(scored, speciesPools, random).genome();
            EvolvableXorGenome child = parentA.crossover(parentB, random);
            int mutations = 1 + random.nextInt(maxMutations);
            for (int i = 0; i < mutations; i++) {
                child = child.mutate(random, mutationIntensity);
            }
            next.add(child);
        }
        while (next.size() < config.populationSize()) {
            next.add(immigrant(random, reheated, mutationIntensity));
        }
        return next;
    }

    static int stagnationPatience(final EvolutionConfig config) {
        Objects.requireNonNull(config, "Config cannot be null.");
        return Math.max(MIN_STAGNATION_PATIENCE, config.populationSize() * STAGNATION_PATIENCE_PER_CANDIDATE);
    }

    static boolean reheated(final EvolutionConfig config, final int generationsSinceImprovement) {
        return generationsSinceImprovement >= stagnationPatience(config);
    }

    static double stagnationReseedFraction(
            final EvolutionConfig config,
            final int generationsSinceImprovement) {
        Objects.requireNonNull(config, "Config cannot be null.");
        if (generationsSinceImprovement < 0) {
            throw new IllegalArgumentException("Generations since improvement cannot be negative.");
        }
        double pressure = stagnationPressure(config, generationsSinceImprovement);
        if (pressure <= STAGNATION_RESEED_START_PRESSURE) {
            return BASE_IMMIGRANT_FRACTION;
        }
        double ramp = (pressure - STAGNATION_RESEED_START_PRESSURE)
                / (1.0 - STAGNATION_RESEED_START_PRESSURE);
        return BASE_IMMIGRANT_FRACTION
                + (MAX_STAGNATION_RESEED_FRACTION - BASE_IMMIGRANT_FRACTION) * Math.min(1.0, ramp);
    }

    static boolean reseeding(final EvolutionConfig config, final int generationsSinceImprovement) {
        return stagnationReseedFraction(config, generationsSinceImprovement) > BASE_IMMIGRANT_FRACTION;
    }

    static double adaptiveMutationIntensity(
            final EvolutionConfig config,
            final int generationsSinceImprovement) {
        Objects.requireNonNull(config, "Config cannot be null.");
        double pressure = stagnationPressure(config, generationsSinceImprovement);
        double multiplier = 1.0 + pressure * (REHEATED_MUTATION_MULTIPLIER - 1.0);
        return Math.min(MAX_REHEATED_MUTATION_INTENSITY, config.mutationIntensity() * multiplier);
    }

    static String evolutionStrategySummary(
            final EvolutionConfig config,
            final int generationsSinceImprovement) {
        Objects.requireNonNull(config, "Config cannot be null.");
        String mode;
        if (reheated(config, generationsSinceImprovement)) {
            mode = "reseed";
        } else if (reseeding(config, generationsSinceImprovement)) {
            mode = "diversify";
        } else {
            mode = "search";
        }
        return "Strategy: " + mode
                + ", stale " + generationsSinceImprovement + "/" + stagnationPatience(config)
                + ", mutation " + formatShort(adaptiveMutationIntensity(config, generationsSinceImprovement))
                + ", reseed " + formatShort(100.0
                        * stagnationReseedFraction(config, generationsSinceImprovement)) + "%";
    }

    private static double stagnationPressure(
            final EvolutionConfig config,
            final int generationsSinceImprovement) {
        Objects.requireNonNull(config, "Config cannot be null.");
        if (generationsSinceImprovement < 0) {
            throw new IllegalArgumentException("Generations since improvement cannot be negative.");
        }
        return Math.min(1.0, generationsSinceImprovement / (double) stagnationPatience(config));
    }

    private static List<EvolvableXorGenome> diverseElites(
            final List<CandidateScore> scored,
            final int eliteCount,
            final int diverseEliteCount) {
        List<EvolvableXorGenome> elites = new ArrayList<>();
        Set<ArchitectureSpecies> species = new HashSet<>();
        for (int i = 0; i < Math.min(eliteCount, scored.size()); i++) {
            EvolvableXorGenome genome = scored.get(i).genome();
            addIfAbsent(elites, genome, diverseEliteCount);
            species.add(ArchitectureSpecies.from(genome));
        }
        for (CandidateScore candidate : scored) {
            if (elites.size() >= diverseEliteCount) {
                break;
            }
            EvolvableXorGenome genome = candidate.genome();
            if (species.add(ArchitectureSpecies.from(genome))) {
                addIfAbsent(elites, genome, diverseEliteCount);
            }
        }
        for (CandidateScore candidate : scored) {
            if (elites.size() >= diverseEliteCount) {
                break;
            }
            addIfAbsent(elites, candidate.genome(), diverseEliteCount);
        }
        return elites;
    }

    private static List<List<CandidateScore>> speciesPools(final List<CandidateScore> scored) {
        Map<ArchitectureSpecies, List<CandidateScore>> grouped = new LinkedHashMap<>();
        for (CandidateScore candidate : scored) {
            grouped.computeIfAbsent(
                    ArchitectureSpecies.from(candidate.genome()),
                    ignored -> new ArrayList<>()).add(candidate);
        }
        return new ArrayList<>(grouped.values());
    }

    private static CandidateScore architectureTournament(
            final List<CandidateScore> scored,
            final List<List<CandidateScore>> speciesPools,
            final Random random) {
        if (!speciesPools.isEmpty() && random.nextDouble() < 0.45) {
            return tournament(speciesPools.get(random.nextInt(speciesPools.size())), random);
        }
        return tournament(scored, random);
    }

    private static EvolvableXorGenome localRefinement(
            final EvolvableXorGenome genome,
            final Random random,
            final double mutationIntensity) {
        EvolvableXorGenome refined = genome;
        int mutations = 2 + random.nextInt(4);
        double localIntensity = Math.max(0.01, mutationIntensity * 0.35);
        for (int i = 0; i < mutations; i++) {
            refined = refined.mutate(random, XorMutationType.PERTURB_NUMERIC_PARAMETER, localIntensity);
        }
        return refined;
    }

    private static EvolvableXorGenome immigrant(
            final Random random,
            final boolean reheated,
            final double mutationIntensity) {
        EvolvableXorGenome genome = EvolvableXorGenome.random(random);
        if (!reheated) {
            return genome;
        }
        XorMutationType[] explorationMutations = {
                XorMutationType.ADD_NEURON,
                XorMutationType.ADD_NEURON,
                XorMutationType.ADD_HIDDEN_LAYER,
                XorMutationType.ADD_KERNEL_MEMORY,
                XorMutationType.ADD_PHASE_ENCODING,
                XorMutationType.CHANGE_INPUT_REPRESENTATION,
                XorMutationType.CHANGE_ACTIVATION,
                XorMutationType.ADD_NORMALIZATION,
                XorMutationType.ADD_SECOND_DERIVATIVE_ESTIMATE
        };
        int mutations = 2 + random.nextInt(5);
        for (int i = 0; i < mutations; i++) {
            genome = genome.mutate(
                    random,
                    explorationMutations[random.nextInt(explorationMutations.length)],
                    mutationIntensity);
        }
        return genome;
    }

    private static void addIfAbsent(
            final List<EvolvableXorGenome> genomes,
            final EvolvableXorGenome genome,
            final int limit) {
        if (genomes.size() < limit && !genomes.contains(genome)) {
            genomes.add(genome);
        }
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

    private static String formatShort(final double value) {
        if (!Double.isFinite(value)) {
            return Double.toString(value);
        }
        return String.format(java.util.Locale.ROOT, "%.3f", value);
    }

    private record ArchitectureSpecies(
            int hiddenLayers,
            int hiddenNeurons,
            int memoryCells,
            boolean kernelMemory,
            boolean phaseEncoding,
            XorInputRepresentation inputRepresentation,
            XorActivationFunction hiddenActivation) {

        static ArchitectureSpecies from(final EvolvableXorGenome genome) {
            return new ArchitectureSpecies(
                    genome.hiddenLayers(),
                    genome.hiddenNeurons(),
                    genome.memoryCells(),
                    genome.kernelMemory(),
                    genome.phaseEncoding(),
                    genome.inputRepresentation(),
                    genome.hiddenActivation());
        }
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
            final double predictiveFreeEnergy,
            final EvolutionConfig config) {
        Objects.requireNonNull(problem, "Problem cannot be null.");
        if (!Double.isFinite(meanSquaredError)
                || !Double.isFinite(configuredLoss)
                || !Double.isFinite(generalizationMeanSquaredError)
                || !Double.isFinite(jitterMeanSquaredError)
                || !Double.isFinite(smoothnessPenalty)
                || !Double.isFinite(predictiveFreeEnergy)) {
            return Double.POSITIVE_INFINITY;
        }
        double normalizedComplexity = complexity / problem.complexityScale();
        double boundedGroupRelativeGeneralizationError =
                boundedGroupRelativeGeneralizationError(groupRelativeGeneralizationError);
        NeuroEvolutionFreeEnergyProfile freeEnergyProfile = problem.freeEnergyProfile();
        return meanSquaredError
                + configuredLoss * 0.1
                + (1.0 - accuracy) * 0.8
                + generalizationMeanSquaredError * config.generalizationWeight()
                + jitterMeanSquaredError * config.jitterWeight()
                + smoothnessPenalty * config.smoothnessWeight()
                + normalizedComplexity * config.complexityPenalty()
                + boundedGroupRelativeGeneralizationError * GROUP_RELATIVE_GENERALIZATION_WEIGHT
                + predictiveFreeEnergy * freeEnergyProfile.objectiveWeight();
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

    private static void requireNonNegativeScore(final double value, final String label) {
        if (Double.isNaN(value) || value < 0.0) {
            throw new IllegalArgumentException(label + " must be non-negative.");
        }
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
