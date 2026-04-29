package com.github.javachaos.javaneuralnetwork.examples;

import com.github.javachaos.javaneuralnetwork.examples.RichEvolvedProblemLearner.NetworkSnapshot;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Local Rust-backed worker adapter.
 *
 * <p>The Rust process owns the population, candidate training, scoring, and
 * selection loop. Java keeps the GUI-facing view/checkpoint shape so the rest
 * of the application can treat local and sidecar workers the same way.</p>
 */
public final class RustNeuroEvolutionWorker implements NeuroEvolutionWorker {

    private final List<String> command;
    private final NeuroEvolutionDistributedConfig distributedConfig;
    private final RustNeuroEvolutionTransportFactory transportFactory;
    private final Consumer<RustNeuroEvolutionSidecar.RustWorkerEvent> sidecarEventConsumer;
    private final List<RustRunHandle> activeRuns = new CopyOnWriteArrayList<>();

    public RustNeuroEvolutionWorker() {
        this(
                RustNeuroEvolutionSidecar.defaultLocalCommand(),
                NeuroEvolutionDistributedConfig.local(),
                event -> {
                });
    }

    public RustNeuroEvolutionWorker(final NeuroEvolutionDistributedConfig distributedConfig) {
        this(
                RustNeuroEvolutionSidecar.defaultLocalCommand(),
                distributedConfig,
                event -> {
                });
    }

    public RustNeuroEvolutionWorker(
            final List<String> command,
            final Consumer<RustNeuroEvolutionSidecar.RustWorkerEvent> sidecarEventConsumer) {
        this(command, NeuroEvolutionDistributedConfig.local(), sidecarEventConsumer);
    }

    public RustNeuroEvolutionWorker(
            final RustNeuroEvolutionTransportFactory transportFactory,
            final Consumer<RustNeuroEvolutionSidecar.RustWorkerEvent> sidecarEventConsumer) {
        this(
                List.of("remote-rust-worker"),
                NeuroEvolutionDistributedConfig.local(),
                Objects.requireNonNull(transportFactory, "Transport factory cannot be null."),
                sidecarEventConsumer);
    }

    public RustNeuroEvolutionWorker(
            final List<String> command,
            final NeuroEvolutionDistributedConfig distributedConfig,
            final Consumer<RustNeuroEvolutionSidecar.RustWorkerEvent> sidecarEventConsumer) {
        this(
                command,
                distributedConfig,
                eventConsumer -> new RustNeuroEvolutionSidecar(command, distributedConfig, eventConsumer),
                sidecarEventConsumer);
    }

    private RustNeuroEvolutionWorker(
            final List<String> command,
            final NeuroEvolutionDistributedConfig distributedConfig,
            final RustNeuroEvolutionTransportFactory transportFactory,
            final Consumer<RustNeuroEvolutionSidecar.RustWorkerEvent> sidecarEventConsumer) {
        if (command == null || command.isEmpty()) {
            throw new IllegalArgumentException("Rust worker command cannot be empty.");
        }
        this.command = List.copyOf(command);
        this.distributedConfig = Objects.requireNonNull(
                distributedConfig,
                "Distributed config cannot be null.");
        this.transportFactory = Objects.requireNonNull(
                transportFactory,
                "Transport factory cannot be null.");
        this.sidecarEventConsumer = Objects.requireNonNull(
                sidecarEventConsumer,
                "Sidecar event consumer cannot be null.");
    }

    @Override
    public NeuroEvolutionRunHandle start(
            final NeuroEvolutionRunRequest request,
            final NeuroEvolutionRunListener listener) {
        Objects.requireNonNull(request, "Run request cannot be null.");
        Objects.requireNonNull(listener, "Run listener cannot be null.");

        RustRunHandle runHandle = new RustRunHandle(request, listener);
        RustNeuroEvolutionTransport transport;
        try {
            transport = transportFactory.open(runHandle::handleSidecarEvent);
        } catch (IOException exception) {
            listener.onFailure(new IllegalStateException("Could not create Rust worker transport.", exception));
            return StoppedRunHandle.INSTANCE;
        }
        runHandle.setTransport(transport);
        activeRuns.add(runHandle);
        try {
            transport.start();
            transport.submit(request);
        } catch (IOException exception) {
            activeRuns.remove(runHandle);
            transport.close();
            listener.onFailure(new IllegalStateException("Could not start Rust worker transport.", exception));
            return StoppedRunHandle.INSTANCE;
        }
        return runHandle;
    }

    @Override
    public void close() {
        for (RustRunHandle runHandle : activeRuns) {
            runHandle.stop();
        }
        activeRuns.clear();
    }

    private void dispatchSidecarEvent(final RustNeuroEvolutionSidecar.RustWorkerEvent event) {
        sidecarEventConsumer.accept(event);
    }

    private static XorNeuroEvolution.CandidateScore parseScore(
            final RustNeuroEvolutionSidecar.RustWorkerEvent event) {
        String genome = event.stringValue("genome");
        if (genome == null || genome.isBlank()) {
            throw new IllegalArgumentException("Rust score event did not include a genome.");
        }
        return new XorNeuroEvolution.CandidateScore(
                parseGenome(genome),
                event.doubleValue("score", Double.POSITIVE_INFINITY),
                event.doubleValue("meanSquaredError", Double.POSITIVE_INFINITY),
                event.doubleValue("configuredLoss", Double.POSITIVE_INFINITY),
                event.doubleValue("accuracy", 0.0),
                event.doubleValue("generalizationMeanSquaredError", Double.POSITIVE_INFINITY),
                event.doubleValue("groupRelativeGeneralizationError", 0.0),
                event.doubleValue("jitterMeanSquaredError", 0.0),
                event.doubleValue("smoothnessPenalty", 0.0),
                event.doubleValue("complexity", 0.0),
                event.doubleValue("predictiveFreeEnergy", 0.0),
                event.doubleValue("sensoryPredictionEnergy", 0.0),
                event.doubleValue("latentPredictionEnergy", 0.0),
                event.doubleValue("complexityPriorEnergy", 0.0),
                List.of(),
                event.intValue("scoreGeneration", event.intValue("generation", 0)));
    }

    private static EvolvableXorGenome parseGenome(final String encodedGenome) {
        String[] parts = encodedGenome.split("\\|", -1);
        if (parts.length != 27) {
            throw new IllegalArgumentException("Rust genome had " + parts.length + " fields, expected 27.");
        }
        return new EvolvableXorGenome(
                intPart(parts, 0),
                intPart(parts, 1),
                intPart(parts, 2),
                intPart(parts, 3),
                doublePart(parts, 4),
                enumPart(XorActivationFunction.class, parts, 5),
                enumPart(XorActivationFunction.class, parts, 6),
                enumPart(XorInputRepresentation.class, parts, 7),
                enumPart(XorLossFunction.class, parts, 8),
                enumPart(XorLearningSchedule.class, parts, 9),
                booleanPart(parts, 10),
                booleanPart(parts, 11),
                booleanPart(parts, 12),
                booleanPart(parts, 13),
                booleanPart(parts, 14),
                booleanPart(parts, 15),
                doublePart(parts, 16),
                doublePart(parts, 17),
                doublePart(parts, 18),
                doublePart(parts, 19),
                doublePart(parts, 20),
                doublePart(parts, 21),
                doublePart(parts, 22),
                doublePart(parts, 23),
                doublePart(parts, 24),
                doublePart(parts, 25),
                doublePart(parts, 26));
    }

    private static int intPart(final String[] parts, final int index) {
        return Integer.parseInt(parts[index]);
    }

    private static double doublePart(final String[] parts, final int index) {
        return Double.parseDouble(parts[index]);
    }

    private static boolean booleanPart(final String[] parts, final int index) {
        return Boolean.parseBoolean(parts[index]);
    }

    private static <T extends Enum<T>> T enumPart(
            final Class<T> enumType,
            final String[] parts,
            final int index) {
        return Enum.valueOf(enumType, parts[index]);
    }

    private static String populationSummary(final XorNeuroEvolution.CandidateScore score) {
        EvolvableXorGenome genome = score.genome();
        return "Rust population: best H " + genome.hiddenNeurons()
                + ", L " + genome.hiddenLayers()
                + ", M " + genome.memoryCells()
                + ", R " + genome.recurrentConnections();
    }

    private static String strategySummary(
            final XorNeuroEvolution.EvolutionConfig config,
            final XorNeuroEvolution.CandidateScore score,
            final RustNeuroEvolutionSidecar.RustWorkerEvent event) {
        int staleGenerations = event.intValue("staleGenerations", -1);
        int stagnationPatience = event.intValue("stagnationPatience", -1);
        int seedLanes = event.intValue("seedLanes", 1);
        int bestLane = event.intValue("bestLane", 0);
        int laneDeaths = event.intValue("laneDeaths", 0);
        int maxLaneStale = event.intValue("maxLaneStale", -1);
        double mutationIntensity = event.doubleValue("mutationIntensity", config.mutationIntensity());
        double reseedFraction = event.doubleValue("reseedFraction", Double.NaN);
        boolean reheated = event.booleanValue("reheated", false);
        boolean reseeding = event.booleanValue("reseeding", false);
        boolean laneDied = event.booleanValue("laneDied", false);
        String mode;
        if (laneDied) {
            mode = "lane death";
        } else if (seedLanes > 1) {
            mode = "islands";
        } else if (reheated) {
            mode = "reseed";
        } else if (reseeding) {
            mode = "diversify";
        } else {
            mode = "search";
        }
        String globalStaleText = staleGenerations >= 0 && stagnationPatience > 0
                ? staleGenerations + "/" + stagnationPatience
                : "n/a";
        String laneStaleText = maxLaneStale >= 0 && stagnationPatience > 0
                ? maxLaneStale + "/" + stagnationPatience
                : "n/a";
        String reseedText = Double.isFinite(reseedFraction)
                ? String.format(java.util.Locale.ROOT, "%.1f%%", reseedFraction * 100.0)
                : "n/a";
        return "Rust strategy: " + mode
                + ", lanes " + seedLanes
                + ", best lane " + bestLane
                + ", deaths " + laneDeaths
                + ", global stale " + globalStaleText
                + ", lane stale " + laneStaleText
                + ", mutation " + String.format(java.util.Locale.ROOT, "%.3f", mutationIntensity)
                + ", reseed " + reseedText
                + ", best score=" + Double.toString(score.score());
    }

    private static double generationsPerSecond(final int generation, final long startNanos) {
        long elapsed = Math.max(1L, System.nanoTime() - startNanos);
        return (generation + 1.0) * 1_000_000_000.0 / elapsed;
    }

    private static void closeTransport(final RustNeuroEvolutionTransport transport) {
        try {
            if (transport.isAlive()) {
                transport.stop();
                transport.awaitExit(500L);
            }
        } catch (IOException ignored) {
            // The run has already produced its terminal result.
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
        } finally {
            transport.close();
        }
    }

    private final class RustRunHandle implements NeuroEvolutionRunHandle {

        private final NeuroEvolutionRunRequest request;
        private final NeuroEvolutionRunListener listener;
        private final long startNanos;
        private final AtomicBoolean terminal = new AtomicBoolean();
        private final AtomicInteger currentGeneration = new AtomicInteger();
        private final Object bestLock = new Object();
        private volatile RustNeuroEvolutionTransport transport;
        private volatile boolean stopped;
        private XorNeuroEvolution.CandidateScore best;
        private XorNeuroEvolution.CandidateScore cachedBestScore;
        private NeuroEvolutionChampionView cachedBestView;

        private RustRunHandle(
                final NeuroEvolutionRunRequest request,
                final NeuroEvolutionRunListener listener) {
            this.request = Objects.requireNonNull(request, "Run request cannot be null.");
            this.listener = Objects.requireNonNull(listener, "Run listener cannot be null.");
            this.startNanos = System.nanoTime();
        }

        private void setTransport(final RustNeuroEvolutionTransport transport) {
            this.transport = Objects.requireNonNull(transport, "Transport cannot be null.");
        }

        private void handleSidecarEvent(final RustNeuroEvolutionSidecar.RustWorkerEvent event) {
            dispatchSidecarEvent(event);
            if (event.type() == null) {
                return;
            }
            try {
                switch (event.type()) {
                    case "progress" -> handleProgress(parseScore(event), event.intValue("generation", 0), event);
                    case "completed" -> handleCompleted(parseScore(event));
                    case "error" -> handleFailure(new IllegalStateException("Rust worker error: " + event.message()));
                    case "stopped" -> stopped = true;
                    default -> {
                    }
                }
            } catch (RuntimeException exception) {
                handleFailure(exception);
            }
        }

        private void handleProgress(
                final XorNeuroEvolution.CandidateScore score,
                final int eventGeneration,
                final RustNeuroEvolutionSidecar.RustWorkerEvent event) {
            int reportedGeneration = currentGeneration.updateAndGet(
                    previous -> Math.max(previous, eventGeneration));
            XorNeuroEvolution.CandidateScore bestScore;
            synchronized (bestLock) {
                boolean improved = best == null || score.score() < best.score();
                if (improved) {
                    best = score;
                    saveCheckpoint(score);
                }
                bestScore = best == null ? score : best;
            }
            NeuroEvolutionChampionView bestView = championView(bestScore);
            NetworkSnapshot snapshot = bestView.snapshot();
            listener.onProgress(new NeuroEvolutionRunProgress(
                    request.problem(),
                    request.config(),
                    reportedGeneration,
                    score,
                    bestView.score(),
                    populationSummary(bestScore),
                    strategySummary(request.config(), bestScore, event),
                    generationsPerSecond(reportedGeneration, startNanos),
                    snapshot,
                    snapshot));
        }

        private void handleCompleted(final XorNeuroEvolution.CandidateScore score) {
            XorNeuroEvolution.CandidateScore completedBest;
            synchronized (bestLock) {
                if (best == null || score.score() < best.score()) {
                    best = score;
                    saveCheckpoint(score);
                }
                completedBest = best == null ? score : best;
            }
            if (terminal.compareAndSet(false, true)) {
                listener.onComplete(new NeuroEvolutionRunCompletion(
                        request.problem(),
                        request.config(),
                        completedBest,
                        stopped));
                activeRuns.remove(this);
                if (transport != null) {
                    closeTransport(transport);
                }
            }
        }

        private void handleFailure(final Throwable failure) {
            if (terminal.compareAndSet(false, true)) {
                listener.onFailure(failure);
                activeRuns.remove(this);
                if (transport != null) {
                    transport.close();
                }
            }
        }

        private NeuroEvolutionChampionView championView(final XorNeuroEvolution.CandidateScore score) {
            if (score.equals(cachedBestScore) && cachedBestView != null) {
                return cachedBestView;
            }
            cachedBestScore = score;
            cachedBestView = NeuroEvolutionChampionViews.build(request.problem(), request.config(), score);
            return cachedBestView;
        }

        private void saveCheckpoint(final XorNeuroEvolution.CandidateScore score) {
            if (!request.checkpointOnImprovement()) {
                return;
            }
            try {
                NeuroEvolutionChampionCheckpoint.save(
                        request.checkpointPath(),
                        request.problem(),
                        request.config(),
                        score);
                listener.onCheckpoint(new NeuroEvolutionCheckpointEvent(
                        request.problem(),
                        score,
                        request.checkpointPath(),
                        true,
                        null));
            } catch (IOException exception) {
                listener.onCheckpoint(new NeuroEvolutionCheckpointEvent(
                        request.problem(),
                        score,
                        request.checkpointPath(),
                        false,
                        exception.getMessage()));
            }
        }

        @Override
        public void pause() {
            try {
                transport.pause();
            } catch (IOException ignored) {
                // Stop remains available even if the transport has already exited.
            }
        }

        @Override
        public void resume() {
            try {
                transport.resume();
            } catch (IOException ignored) {
                // Stop remains available even if the transport has already exited.
            }
        }

        @Override
        public void stop() {
            stopped = true;
            if (terminal.compareAndSet(false, true)) {
                activeRuns.remove(this);
            }
            if (transport != null) {
                transport.close();
            }
        }

        @Override
        public boolean isStopped() {
            return stopped || terminal.get() || transport == null || !transport.isAlive();
        }
    }

    private enum StoppedRunHandle implements NeuroEvolutionRunHandle {
        INSTANCE;

        @Override
        public void pause() {
        }

        @Override
        public void resume() {
        }

        @Override
        public void stop() {
        }

        @Override
        public boolean isStopped() {
            return true;
        }
    }
}
