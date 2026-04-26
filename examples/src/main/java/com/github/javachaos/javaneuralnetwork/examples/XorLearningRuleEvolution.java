package com.github.javachaos.javaneuralnetwork.examples;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;

/**
 * Evolves local learning rules by asking each candidate to teach a small learner XOR.
 */
public final class XorLearningRuleEvolution {

    private static final int DEFAULT_POPULATION_SIZE = 48;
    private static final int DEFAULT_GENERATIONS = 32;
    private static final int DEFAULT_MAX_EPOCHS = 1_200;
    private static final double DEFAULT_TARGET_MSE = 0.01;
    private static final int DEFAULT_EVALUATION_REPEATS = 3;
    private static final double DEFAULT_MUTATION_INTENSITY = 0.08;
    private static final int DEFAULT_PARALLELISM = NeuroEvolutionParallelism.defaultParallelism();

    private XorLearningRuleEvolution() {
    }

    public record EvolutionConfig(
            int populationSize,
            int generations,
            int maxEpochs,
            double targetMeanSquaredError,
            int evaluationRepeats,
            double mutationIntensity,
            int parallelism,
            long seed) {

        public EvolutionConfig(
                final int populationSize,
                final int generations,
                final int maxEpochs,
                final double targetMeanSquaredError,
                final int evaluationRepeats,
                final double mutationIntensity,
                final long seed) {
            this(
                    populationSize,
                    generations,
                    maxEpochs,
                    targetMeanSquaredError,
                    evaluationRepeats,
                    mutationIntensity,
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
                    DEFAULT_PARALLELISM,
                    4_242L);
        }
    }

    public record CandidateScore(
            XorLearningRuleGenome genome,
            double score,
            double meanSquaredError,
            double accuracy,
            int generation) {

        public CandidateScore {
            Objects.requireNonNull(genome, "Genome cannot be null.");
        }
    }

    public record EvolutionResult(
            EvolutionConfig config,
            CandidateScore best,
            List<CandidateScore> champions) {

        public EvolutionResult {
            Objects.requireNonNull(config, "Config cannot be null.");
            Objects.requireNonNull(best, "Best score cannot be null.");
            champions = List.copyOf(Objects.requireNonNull(champions, "Champions cannot be null."));
        }
    }

    public static EvolutionResult evolve() {
        return evolve(EvolutionConfig.defaults());
    }

    public static EvolutionResult evolve(final EvolutionConfig config) {
        Objects.requireNonNull(config, "Config cannot be null.");
        Random random = new Random(config.seed());
        List<XorLearningRuleGenome> population = new ArrayList<>(config.populationSize());
        for (int i = 0; i < config.populationSize(); i++) {
            population.add(XorLearningRuleGenome.random(random));
        }

        CandidateScore best = null;
        List<CandidateScore> champions = new ArrayList<>();
        ForkJoinPool evaluationPool =
                NeuroEvolutionParallelism.newPool(config.parallelism(), config.populationSize());
        try {
            for (int generation = 0; generation < config.generations(); generation++) {
                List<CandidateScore> scored = scorePopulation(population, config, generation, evaluationPool);
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

        return new EvolutionResult(config, best, champions);
    }

    public static CandidateScore evaluate(
            final XorLearningRuleGenome genome,
            final EvolutionConfig config,
            final int generation) {
        return evaluate(genome, config, generation, 0);
    }

    private static CandidateScore evaluate(
            final XorLearningRuleGenome genome,
            final EvolutionConfig config,
            final int generation,
            final int candidateIndex) {
        Objects.requireNonNull(genome, "Genome cannot be null.");
        Objects.requireNonNull(config, "Config cannot be null.");
        if (candidateIndex < 0) {
            throw new IllegalArgumentException("Candidate index cannot be negative.");
        }
        double mseTotal = 0.0;
        double accuracyTotal = 0.0;
        long candidateSeed = config.seed() + 1_000_003L * candidateIndex;
        for (int repeat = 0; repeat < config.evaluationRepeats(); repeat++) {
            Random learnerRandom = new Random(candidateSeed + 10_000L * generation + 97L * repeat);
            EvolvedXorLearner learner = new EvolvedXorLearner(genome, learnerRandom);
            double mse = learner.train(config.maxEpochs(), config.targetMeanSquaredError());
            mseTotal += mse;
            accuracyTotal += learner.accuracy();
        }
        double meanSquaredError = mseTotal / config.evaluationRepeats();
        double accuracy = accuracyTotal / config.evaluationRepeats();
        double score = score(meanSquaredError, accuracy);
        return new CandidateScore(genome, score, meanSquaredError, accuracy, generation);
    }

    public static void main(final String[] args) {
        EvolutionResult result = evolve();
        CandidateScore best = result.best();
        System.out.println("Best score: " + best.score());
        System.out.println("Best MSE: " + best.meanSquaredError());
        System.out.println("Best accuracy: " + best.accuracy());
        System.out.println("Parallelism: " + result.config().parallelism());
        System.out.println("Best genome: " + best.genome());
    }

    private static List<CandidateScore> scorePopulation(
            final List<XorLearningRuleGenome> population,
            final EvolutionConfig config,
            final int generation,
            final ForkJoinPool evaluationPool) {
        return NeuroEvolutionParallelism.mapAndSort(
                population.size(),
                evaluationPool,
                index -> evaluate(population.get(index), config, generation, index),
                Comparator.comparingDouble(CandidateScore::score));
    }

    private static List<XorLearningRuleGenome> nextGeneration(
            final List<CandidateScore> scored,
            final EvolutionConfig config,
            final Random random) {
        int eliteCount = Math.max(2, config.populationSize() / 8);
        List<XorLearningRuleGenome> next = new ArrayList<>(config.populationSize());
        for (int i = 0; i < eliteCount; i++) {
            next.add(scored.get(i).genome());
        }
        while (next.size() < config.populationSize()) {
            XorLearningRuleGenome parentA = tournament(scored, random).genome();
            XorLearningRuleGenome parentB = tournament(scored, random).genome();
            next.add(parentA.crossover(parentB, random).mutate(random, config.mutationIntensity()));
        }
        return next;
    }

    private static CandidateScore tournament(final List<CandidateScore> scored, final Random random) {
        CandidateScore best = null;
        int tournamentSize = Math.min(4, scored.size());
        for (int i = 0; i < tournamentSize; i++) {
            CandidateScore candidate = scored.get(random.nextInt(scored.size()));
            if (best == null || candidate.score() < best.score()) {
                best = candidate;
            }
        }
        return best;
    }

    private static double score(final double meanSquaredError, final double accuracy) {
        if (!Double.isFinite(meanSquaredError)) {
            return Double.POSITIVE_INFINITY;
        }
        return meanSquaredError + (1.0 - accuracy) * 0.75;
    }
}
