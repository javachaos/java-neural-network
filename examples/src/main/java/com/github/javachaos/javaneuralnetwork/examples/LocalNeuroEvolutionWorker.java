package com.github.javachaos.javaneuralnetwork.examples;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;

/**
 * In-process Java implementation of the worker boundary.
 */
public final class LocalNeuroEvolutionWorker implements NeuroEvolutionWorker {

    @Override
    public NeuroEvolutionRunHandle start(
            final NeuroEvolutionRunRequest request,
            final NeuroEvolutionRunListener listener) {
        LocalRun run = new LocalRun(
                Objects.requireNonNull(request, "Run request cannot be null."),
                Objects.requireNonNull(listener, "Run listener cannot be null."));
        Thread thread = new Thread(run, "local-neuro-evolution-worker-" + request.problem().key());
        thread.setDaemon(true);
        run.setThread(thread);
        thread.start();
        return run;
    }

    private static final class LocalRun implements Runnable, NeuroEvolutionRunHandle {

        private final NeuroEvolutionRunRequest request;
        private final NeuroEvolutionRunListener listener;
        private volatile boolean stopped;
        private volatile boolean paused;
        private volatile boolean completed;
        private Thread thread;
        private XorNeuroEvolution.CandidateScore cachedBestScore;
        private NeuroEvolutionChampionView cachedBestView;

        LocalRun(
                final NeuroEvolutionRunRequest request,
                final NeuroEvolutionRunListener listener) {
            this.request = request;
            this.listener = listener;
        }

        void setThread(final Thread thread) {
            this.thread = Objects.requireNonNull(thread, "Thread cannot be null.");
        }

        @Override
        public void run() {
            XorNeuroEvolution.CandidateScore best = null;
            try {
                best = runEvolution();
                if (best != null) {
                    completed = true;
                    listener.onComplete(new NeuroEvolutionRunCompletion(
                            request.problem(),
                            request.config(),
                            best,
                            stopped));
                }
            } catch (RuntimeException exception) {
                listener.onFailure(exception);
            }
        }

        private XorNeuroEvolution.CandidateScore runEvolution() {
            NeuroEvolutionProblem problem = request.problem();
            XorNeuroEvolution.EvolutionConfig config = request.config();
            Random random = new Random(config.seed());
            List<EvolvableXorGenome> population = initialPopulation(config, random);
            XorNeuroEvolution.CandidateScore best = null;
            int generationsSinceImprovement = 0;
            long startNanos = System.nanoTime();
            long lastProgressNanos = startNanos - request.progressIntervalNanos();
            ForkJoinPool evaluationPool =
                    NeuroEvolutionParallelism.newPool(config.parallelism(), config.populationSize());
            try {
                for (int generation = 0; generation < config.generations() && !stopped; generation++) {
                    waitWhilePaused();
                    if (stopped) {
                        break;
                    }
                    List<XorNeuroEvolution.CandidateScore> scored =
                            scorePopulation(problem, population, config, generation, evaluationPool);
                    XorNeuroEvolution.CandidateScore champion = scored.get(0);
                    if (best == null || champion.score() < best.score()) {
                        best = champion;
                        generationsSinceImprovement = 0;
                        saveCheckpoint(best);
                    } else {
                        generationsSinceImprovement++;
                    }
                    long now = System.nanoTime();
                    if (shouldEmitProgress(generation, now, lastProgressNanos)) {
                        listener.onProgress(progress(
                                generation,
                                champion,
                                best,
                                scored,
                                generationsSinceImprovement,
                                startNanos,
                                now));
                        lastProgressNanos = System.nanoTime();
                    }
                    population = XorNeuroEvolution.nextGeneration(
                            scored,
                            config,
                            random,
                            best,
                            generationsSinceImprovement);
                }
            } finally {
                NeuroEvolutionParallelism.shutdown(evaluationPool);
            }
            return best;
        }

        @Override
        public void pause() {
            paused = true;
        }

        @Override
        public void resume() {
            paused = false;
        }

        @Override
        public void stop() {
            stopped = true;
            paused = false;
            if (thread != null) {
                thread.interrupt();
            }
        }

        @Override
        public boolean isStopped() {
            return stopped || completed || thread == null || !thread.isAlive();
        }

        private List<EvolvableXorGenome> initialPopulation(
                final XorNeuroEvolution.EvolutionConfig config,
                final Random random) {
            List<EvolvableXorGenome> population = new ArrayList<>(config.populationSize());
            for (int i = 0; i < config.populationSize(); i++) {
                population.add(EvolvableXorGenome.random(random));
            }
            return population;
        }

        private List<XorNeuroEvolution.CandidateScore> scorePopulation(
                final NeuroEvolutionProblem problem,
                final List<EvolvableXorGenome> population,
                final XorNeuroEvolution.EvolutionConfig config,
                final int generation,
                final ForkJoinPool evaluationPool) {
            return NeuroEvolutionParallelism.mapAndSort(
                    population.size(),
                    evaluationPool,
                    index -> XorNeuroEvolution.evaluateCandidate(
                            problem,
                            population.get(index),
                            config,
                            generation,
                            index),
                    Comparator.comparingDouble(XorNeuroEvolution.CandidateScore::score));
        }

        private NeuroEvolutionRunProgress progress(
                final int generation,
                final XorNeuroEvolution.CandidateScore champion,
                final XorNeuroEvolution.CandidateScore best,
                final List<XorNeuroEvolution.CandidateScore> scored,
                final int generationsSinceImprovement,
                final long startNanos,
                final long nowNanos) {
            NeuroEvolutionChampionView bestView = championView(best);
            return new NeuroEvolutionRunProgress(
                    request.problem(),
                    request.config(),
                    generation,
                    champion,
                    bestView.score(),
                    populationSummary(scored),
                    XorNeuroEvolution.evolutionStrategySummary(request.config(), generationsSinceImprovement),
                    generationsPerSecond(generation, startNanos, nowNanos),
                    bestView.snapshot(),
                    bestView.snapshot());
        }

        private NeuroEvolutionChampionView championView(final XorNeuroEvolution.CandidateScore bestScore) {
            if (bestScore.equals(cachedBestScore) && cachedBestView != null) {
                return cachedBestView;
            }
            cachedBestScore = bestScore;
            cachedBestView = NeuroEvolutionChampionViews.build(request.problem(), request.config(), bestScore);
            return cachedBestView;
        }

        private void saveCheckpoint(final XorNeuroEvolution.CandidateScore bestScore) {
            if (!request.checkpointOnImprovement()) {
                return;
            }
            try {
                NeuroEvolutionChampionCheckpoint.save(
                        request.checkpointPath(),
                        request.problem(),
                        request.config(),
                        bestScore);
                listener.onCheckpoint(new NeuroEvolutionCheckpointEvent(
                        request.problem(),
                        bestScore,
                        request.checkpointPath(),
                        true,
                        null));
            } catch (IOException exception) {
                listener.onCheckpoint(new NeuroEvolutionCheckpointEvent(
                        request.problem(),
                        bestScore,
                        request.checkpointPath(),
                        false,
                        exception.getMessage()));
            }
        }

        private boolean shouldEmitProgress(
                final int generation,
                final long nowNanos,
                final long lastProgressNanos) {
            long interval = request.progressIntervalNanos();
            return generation == 0
                    || generation == request.config().generations() - 1
                    || interval == 0L
                    || nowNanos - lastProgressNanos >= interval;
        }

        private static double generationsPerSecond(
                final int generation,
                final long startNanos,
                final long nowNanos) {
            long elapsed = Math.max(1L, nowNanos - startNanos);
            return (generation + 1.0) * 1_000_000_000.0 / elapsed;
        }

        private static String populationSummary(final List<XorNeuroEvolution.CandidateScore> scored) {
            int minHidden = Integer.MAX_VALUE;
            int maxHidden = Integer.MIN_VALUE;
            int minLayers = Integer.MAX_VALUE;
            int maxLayers = Integer.MIN_VALUE;
            int minMemory = Integer.MAX_VALUE;
            int maxMemory = Integer.MIN_VALUE;
            int minRecurrent = Integer.MAX_VALUE;
            int maxRecurrent = Integer.MIN_VALUE;
            for (XorNeuroEvolution.CandidateScore candidate : scored) {
                EvolvableXorGenome genome = candidate.genome();
                minHidden = Math.min(minHidden, genome.hiddenNeurons());
                maxHidden = Math.max(maxHidden, genome.hiddenNeurons());
                minLayers = Math.min(minLayers, genome.hiddenLayers());
                maxLayers = Math.max(maxLayers, genome.hiddenLayers());
                minMemory = Math.min(minMemory, genome.memoryCells());
                maxMemory = Math.max(maxMemory, genome.memoryCells());
                minRecurrent = Math.min(minRecurrent, genome.recurrentConnections());
                maxRecurrent = Math.max(maxRecurrent, genome.recurrentConnections());
            }
            return "Population: H " + minHidden + "-" + maxHidden
                    + ", L " + minLayers + "-" + maxLayers
                    + ", M " + minMemory + "-" + maxMemory
                    + ", R " + minRecurrent + "-" + maxRecurrent;
        }

        private void waitWhilePaused() {
            while (paused && !stopped) {
                sleep(75L);
            }
        }

        private static void sleep(final long millis) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
