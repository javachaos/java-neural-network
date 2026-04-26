package com.github.javachaos.javaneuralnetwork.examples;

import com.github.javachaos.javaneuralnetwork.examples.RichEvolvedProblemLearner.NetworkSnapshot;
import com.github.javachaos.javaneuralnetwork.examples.RichEvolvedProblemLearner.VisualLink;
import com.github.javachaos.javaneuralnetwork.examples.RichEvolvedProblemLearner.VisualNode;
import com.github.javachaos.javaneuralnetwork.examples.RichEvolvedProblemLearner.VisualNodeLayer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;

/**
 * Swing GUI that visualizes the current rich learner while evolution runs.
 */
public final class XorNeuroEvolutionVisualizer {

    private static final int GUI_GENERATION_LIMIT = Integer.MAX_VALUE;
    private static final long VISUAL_UPDATE_INTERVAL_NANOS = 100_000_000L;

    private XorNeuroEvolutionVisualizer() {
    }

    public static void main(final String[] args) {
        SwingUtilities.invokeLater(() -> {
            XorNeuroEvolution.EvolutionConfig config = new XorNeuroEvolution.EvolutionConfig(
                    24,
                    GUI_GENERATION_LIMIT,
                    180,
                    0.01,
                    1,
                    0.09,
                    0.001,
                    3,
                    86_753L);
            NeuroEvolutionProblemCatalog catalog = new DefaultNeuroEvolutionProblemCatalog();
            VisualizerFrame frame = new VisualizerFrame(problemFromArgs(args, config, catalog), config, catalog);
            frame.setVisible(true);
        });
    }

    private static NeuroEvolutionProblem problemFromArgs(
            final String[] args,
            final XorNeuroEvolution.EvolutionConfig config,
            final NeuroEvolutionProblemCatalog catalog) {
        if (args.length == 0 || args[0].isBlank()) {
            return catalog.defaultProblem(config.generalizationGridSize());
        }
        return catalog.find(args[0], config.generalizationGridSize());
    }

    private static final class VisualizerFrame extends JFrame {

        private final XorNeuroEvolution.EvolutionConfig config;
        private final NeuroEvolutionProblemCatalog catalog;
        private final NetworkPanel networkPanel;
        private final JLabel titleLabel;
        private final JComboBox<String> problemSelector;
        private final JTextArea problemDescriptionArea;
        private final JLabel generationLabel;
        private final JLabel scoreLabel;
        private final JLabel bestScoreLabel;
        private final JLabel accuracyLabel;
        private final JLabel mseLabel;
        private final JLabel generalizationLabel;
        private final JLabel jitterLabel;
        private final JLabel smoothnessLabel;
        private final JLabel topologyLabel;
        private final JLabel populationLabel;
        private final JLabel parallelismLabel;
        private final JLabel checkpointLabel;
        private final JLabel rateLabel;
        private final JLabel ruleLabel;
        private final JButton startButton;
        private final JButton pauseButton;
        private final JButton resetButton;
        private final JButton detailsButton;
        private final JButton saveCheckpointButton;
        private final JButton loadCheckpointButton;

        private NeuroEvolutionProblem problem;
        private EvolutionRunner runner;
        private Thread runnerThread;
        private VisualState latestState;
        private ChampionDetailsDialog detailsDialog;

        VisualizerFrame(
                final NeuroEvolutionProblem problem,
                final XorNeuroEvolution.EvolutionConfig config,
                final NeuroEvolutionProblemCatalog catalog) {
            super(windowTitle(problem));
            this.problem = Objects.requireNonNull(problem, "Problem cannot be null.");
            this.config = Objects.requireNonNull(config, "Config cannot be null.");
            this.catalog = Objects.requireNonNull(catalog, "Problem catalog cannot be null.");
            this.networkPanel = new NetworkPanel();
            this.titleLabel = new JLabel(titleText(problem));
            this.problemSelector = new JComboBox<>(catalog.problemKeys().toArray(String[]::new));
            this.problemDescriptionArea = descriptionArea(problem);
            this.generationLabel = valueLabel("Generation: 0");
            this.scoreLabel = valueLabel("Champion score: waiting");
            this.bestScoreLabel = valueLabel("Best score: waiting");
            this.accuracyLabel = valueLabel("Best accuracy: waiting");
            this.mseLabel = valueLabel("Best MSE: waiting");
            this.generalizationLabel = valueLabel("Best generalization: waiting");
            this.jitterLabel = valueLabel("Best jitter: waiting");
            this.smoothnessLabel = valueLabel("Best smoothness: waiting");
            this.topologyLabel = valueLabel("Best topology: waiting");
            this.populationLabel = valueLabel("Population: waiting");
            this.parallelismLabel = valueLabel("Parallelism: " + config.parallelism() + " workers");
            this.checkpointLabel = valueLabel("Checkpoint: waiting");
            this.rateLabel = valueLabel("Rate: waiting");
            this.ruleLabel = valueLabel("Rule: waiting");
            this.startButton = new JButton("Start");
            this.pauseButton = new JButton("Pause");
            this.resetButton = new JButton("Reset");
            this.detailsButton = new JButton("Best Details");
            this.saveCheckpointButton = new JButton("Save Best");
            this.loadCheckpointButton = new JButton("Load Best");

            titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18.0f));
            problemSelector.setSelectedItem(problem.key());
            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            setMinimumSize(new Dimension(980, 680));
            setLayout(new BorderLayout());
            add(networkPanel, BorderLayout.CENTER);
            add(sidePanel(), BorderLayout.EAST);
            pack();
            setLocationRelativeTo(null);

            startButton.addActionListener(event -> start());
            pauseButton.addActionListener(event -> pause());
            resetButton.addActionListener(event -> reset());
            detailsButton.addActionListener(event -> showBestDetails());
            saveCheckpointButton.addActionListener(event -> saveBestCheckpoint());
            loadCheckpointButton.addActionListener(event -> loadBestCheckpoint());
            problemSelector.addActionListener(event -> switchProblem((String) problemSelector.getSelectedItem()));
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(final WindowEvent event) {
                    stopRunner();
                }
            });
        }

        private JPanel sidePanel() {
            JPanel panel = new JPanel(new BorderLayout(0, 14));
            panel.setPreferredSize(new Dimension(310, 680));
            panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
            panel.setBackground(new Color(242, 244, 247));

            JPanel metrics = new JPanel(new GridLayout(0, 1, 0, 8));
            metrics.setOpaque(false);
            metrics.add(generationLabel);
            metrics.add(scoreLabel);
            metrics.add(bestScoreLabel);
            metrics.add(accuracyLabel);
            metrics.add(mseLabel);
            metrics.add(generalizationLabel);
            metrics.add(jitterLabel);
            metrics.add(smoothnessLabel);
            metrics.add(topologyLabel);
            metrics.add(populationLabel);
            metrics.add(parallelismLabel);
            metrics.add(checkpointLabel);
            metrics.add(rateLabel);
            metrics.add(ruleLabel);

            JPanel controls = new JPanel(new GridLayout(0, 2, 8, 8));
            controls.setOpaque(false);
            controls.add(startButton);
            controls.add(pauseButton);
            controls.add(resetButton);
            controls.add(detailsButton);
            controls.add(saveCheckpointButton);
            controls.add(loadCheckpointButton);

            JPanel legend = new JPanel(new GridLayout(0, 1, 0, 8));
            legend.setOpaque(false);
            legend.add(valueLabel("Teal links: positive weight"));
            legend.add(valueLabel("Coral links: negative weight"));
            legend.add(valueLabel("Gray links: bias weight"));
            legend.add(valueLabel("Curved links: recurrent"));
            legend.add(valueLabel("Node columns: features, memory, hidden, output"));

            JPanel top = new JPanel(new BorderLayout(0, 16));
            top.setOpaque(false);
            JPanel heading = new JPanel(new BorderLayout(0, 8));
            heading.setOpaque(false);
            JPanel problemControls = new JPanel(new BorderLayout(0, 8));
            problemControls.setOpaque(false);
            JScrollPane descriptionScroll = new JScrollPane(problemDescriptionArea);
            descriptionScroll.setPreferredSize(new Dimension(274, 132));
            descriptionScroll.setBorder(BorderFactory.createLineBorder(new Color(212, 217, 224)));
            problemControls.add(problemSelector, BorderLayout.NORTH);
            problemControls.add(descriptionScroll, BorderLayout.CENTER);
            heading.add(titleLabel, BorderLayout.NORTH);
            heading.add(problemControls, BorderLayout.CENTER);
            top.add(heading, BorderLayout.NORTH);
            top.add(metrics, BorderLayout.CENTER);
            top.add(controls, BorderLayout.SOUTH);

            panel.add(top, BorderLayout.NORTH);
            panel.add(legend, BorderLayout.SOUTH);
            return panel;
        }

        private void start() {
            if (runner == null || runner.isStopped()) {
                runner = new EvolutionRunner(problem, config, this::updateState);
                runnerThread = new Thread(runner, "neuro-evolution-visualizer");
                runnerThread.setDaemon(true);
                runnerThread.start();
            } else {
                runner.setPaused(false);
            }
        }

        private void pause() {
            if (runner != null) {
                runner.setPaused(true);
            }
        }

        private void reset() {
            stopRunner();
            clearState();
            start();
        }

        private void switchProblem(final String problemName) {
            if (problemName == null || problemName.isBlank()) {
                return;
            }
            NeuroEvolutionProblem selected = catalog.find(problemName, config.generalizationGridSize());
            if (selected.name().equals(problem.name())) {
                return;
            }
            boolean restart = runner != null && !runner.isStopped();
            stopRunner();
            problem = selected;
            setTitle(windowTitle(problem));
            titleLabel.setText(titleText(problem));
            problemDescriptionArea.setText(problem.description());
            problemDescriptionArea.setCaretPosition(0);
            clearState();
            if (restart) {
                start();
            }
        }

        private void clearState() {
            latestState = null;
            networkPanel.setVisualState(null);
            generationLabel.setText("Generation: 0");
            scoreLabel.setText("Champion score: waiting");
            bestScoreLabel.setText("Best score: waiting");
            accuracyLabel.setText("Best accuracy: waiting");
            mseLabel.setText("Best MSE: waiting");
            generalizationLabel.setText("Best generalization: waiting");
            jitterLabel.setText("Best jitter: waiting");
            smoothnessLabel.setText("Best smoothness: waiting");
            topologyLabel.setText("Best topology: waiting");
            populationLabel.setText("Population: waiting");
            parallelismLabel.setText("Parallelism: " + config.parallelism() + " workers");
            checkpointLabel.setText("Checkpoint: waiting");
            rateLabel.setText("Rate: waiting");
            ruleLabel.setText("Rule: waiting");
            if (detailsDialog != null) {
                detailsDialog.setVisualState(null);
            }
        }

        private void stopRunner() {
            if (runner != null) {
                runner.stop();
                runner = null;
            }
            runnerThread = null;
        }

        private void updateState(final VisualState state) {
            SwingUtilities.invokeLater(() -> {
                if (!state.snapshot().problem().name().equals(problem.name())) {
                    return;
                }
                latestState = state;
                generationLabel.setText("Generation: " + state.generation());
                scoreLabel.setText("Champion score: " + format(state.score().score()));
                bestScoreLabel.setText("Best score: " + format(state.bestScore().score()));
                accuracyLabel.setText("Best accuracy: "
                        + accuracyText(state.bestSnapshot().problem(), state.bestScore()));
                mseLabel.setText("Best MSE: " + format(state.bestScore().meanSquaredError()));
                generalizationLabel.setText(
                        "Best generalization: " + format(state.bestScore().generalizationMeanSquaredError()));
                jitterLabel.setText("Best jitter: " + format(state.bestScore().jitterMeanSquaredError()));
                smoothnessLabel.setText("Best smoothness: " + format(state.bestScore().smoothnessPenalty()));
                topologyLabel.setText("Best topology: " + state.bestSnapshot().nodes().size()
                        + " nodes, " + state.bestSnapshot().links().size() + " links");
                populationLabel.setText(state.populationSummary());
                rateLabel.setText("Rate: " + format(state.generationsPerSecond()) + " gen/s");
                EvolvableXorGenome genome = state.bestScore().genome();
                ruleLabel.setText("<html>Rule: " + genome.hiddenActivation()
                        + ", " + genome.inputRepresentation()
                        + ", " + genome.lossFunction()
                        + ", " + genome.learningSchedule() + "</html>");
                networkPanel.setVisualState(state);
                if (detailsDialog != null && detailsDialog.isVisible()) {
                    detailsDialog.setVisualState(state);
                }
            });
        }

        private void showBestDetails() {
            if (detailsDialog == null) {
                detailsDialog = new ChampionDetailsDialog(this);
            }
            detailsDialog.setVisualState(latestState);
            detailsDialog.setVisible(true);
            detailsDialog.toFront();
        }

        private void saveBestCheckpoint() {
            if (latestState == null) {
                checkpointLabel.setText("Checkpoint: no champion yet");
                return;
            }
            Path path = NeuroEvolutionChampionCheckpoint.defaultPath(problem);
            try {
                NeuroEvolutionChampionCheckpoint.save(path, problem, config, latestState.bestScore());
                checkpointLabel.setText("Checkpoint: saved " + path.getFileName());
            } catch (IOException exception) {
                checkpointLabel.setText("Checkpoint: save failed");
                System.err.println("Could not save champion checkpoint: " + exception.getMessage());
            }
        }

        private void loadBestCheckpoint() {
            Path path = NeuroEvolutionChampionCheckpoint.defaultPath(problem);
            if (!Files.exists(path)) {
                checkpointLabel.setText("Checkpoint: none for " + problem.key());
                return;
            }
            stopRunner();
            try {
                NeuroEvolutionChampionCheckpoint.LoadedChampion checkpoint =
                        NeuroEvolutionChampionCheckpoint.load(path, catalog);
                problem = checkpoint.problem();
                setTitle(windowTitle(problem));
                titleLabel.setText(titleText(problem));
                problemDescriptionArea.setText(problem.description());
                problemDescriptionArea.setCaretPosition(0);
                ChampionView champion = buildChampionView(problem, checkpoint.config(), checkpoint.score());
                updateState(new VisualState(
                        champion.score().generation(),
                        champion.score(),
                        champion.score(),
                        "Loaded checkpoint: " + path.getFileName(),
                        0.0,
                        champion.snapshot(),
                        champion.snapshot()));
                checkpointLabel.setText("Checkpoint: loaded " + path.getFileName());
            } catch (IOException | IllegalArgumentException exception) {
                checkpointLabel.setText("Checkpoint: load failed");
                System.err.println("Could not load champion checkpoint: " + exception.getMessage());
            }
        }

        private static JLabel valueLabel(final String text) {
            JLabel label = new JLabel(text, SwingConstants.LEFT);
            label.setFont(label.getFont().deriveFont(13.0f));
            return label;
        }

        private static JTextArea descriptionArea(final NeuroEvolutionProblem problem) {
            JTextArea area = new JTextArea(problem.description());
            area.setEditable(false);
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
            area.setOpaque(true);
            area.setBackground(new Color(250, 251, 252));
            area.setForeground(new Color(54, 61, 71));
            area.setFont(area.getFont().deriveFont(12.0f));
            area.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            return area;
        }

        private static String windowTitle(final NeuroEvolutionProblem problem) {
            return Objects.requireNonNull(problem, "Problem cannot be null.").name()
                    + " Neuro Evolution Visualizer";
        }

        private static String titleText(final NeuroEvolutionProblem problem) {
            return Objects.requireNonNull(problem, "Problem cannot be null.").name() + " Evolution";
        }

    }

    private static ChampionView buildChampionView(
            final NeuroEvolutionProblem problem,
            final XorNeuroEvolution.EvolutionConfig config,
            final XorNeuroEvolution.CandidateScore bestScore) {
        RichEvolvedProblemLearner learner = new RichEvolvedProblemLearner(
                problem,
                bestScore.genome(),
                championRandom(config, bestScore));
        learner.train(config.maxEpochs(), config.targetMeanSquaredError());
        return new ChampionView(bestScore.withOutputGroupScores(learner.outputGroupScores()), learner.snapshot());
    }

    private static Random championRandom(
            final XorNeuroEvolution.EvolutionConfig config,
            final XorNeuroEvolution.CandidateScore bestScore) {
        return new Random(config.seed() + 97_531L * (bestScore.generation() + 1L) + 17L);
    }

    private static final class EvolutionRunner implements Runnable {

        private final NeuroEvolutionProblem problem;
        private final XorNeuroEvolution.EvolutionConfig config;
        private final StateListener listener;
        private volatile boolean stopped;
        private volatile boolean paused;
        private XorNeuroEvolution.CandidateScore cachedBestScore;
        private XorNeuroEvolution.CandidateScore cachedBestScoreWithDiagnostics;
        private NetworkSnapshot cachedBestSnapshot;

        EvolutionRunner(
                final NeuroEvolutionProblem problem,
                final XorNeuroEvolution.EvolutionConfig config,
                final StateListener listener) {
            this.problem = Objects.requireNonNull(problem, "Problem cannot be null.");
            this.config = Objects.requireNonNull(config, "Config cannot be null.");
            this.listener = Objects.requireNonNull(listener, "Listener cannot be null.");
        }

        @Override
        public void run() {
            Random random = new Random(config.seed());
            List<EvolvableXorGenome> population = initialPopulation(random);
            XorNeuroEvolution.CandidateScore best = null;
            long startNanos = System.nanoTime();
            long lastVisualUpdateNanos = startNanos - VISUAL_UPDATE_INTERVAL_NANOS;
            ForkJoinPool evaluationPool =
                    NeuroEvolutionParallelism.newPool(config.parallelism(), config.populationSize());
            try {
                for (int generation = 0; generation < config.generations() && !stopped; generation++) {
                    waitWhilePaused();
                    if (stopped) {
                        break;
                    }
                    List<XorNeuroEvolution.CandidateScore> scored =
                            scorePopulation(population, generation, evaluationPool);
                    XorNeuroEvolution.CandidateScore champion = scored.get(0);
                    if (best == null || champion.score() < best.score()) {
                        best = champion;
                        saveBestCheckpoint(best);
                    }
                    long now = System.nanoTime();
                    if (shouldUpdateVisual(generation, now, lastVisualUpdateNanos)) {
                        listener.accept(visualState(generation, champion, best, scored, startNanos, now));
                        lastVisualUpdateNanos = System.nanoTime();
                    }
                    population = nextGeneration(scored, random);
                }
            } finally {
                NeuroEvolutionParallelism.shutdown(evaluationPool);
            }
        }

        boolean isStopped() {
            return stopped;
        }

        void stop() {
            stopped = true;
        }

        void setPaused(final boolean paused) {
            this.paused = paused;
        }

        private List<EvolvableXorGenome> initialPopulation(final Random random) {
            List<EvolvableXorGenome> population = new ArrayList<>(config.populationSize());
            for (int i = 0; i < config.populationSize(); i++) {
                population.add(EvolvableXorGenome.random(random));
            }
            return population;
        }

        private List<XorNeuroEvolution.CandidateScore> scorePopulation(
                final List<EvolvableXorGenome> population,
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

        private List<EvolvableXorGenome> nextGeneration(
                final List<XorNeuroEvolution.CandidateScore> scored,
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

        private XorNeuroEvolution.CandidateScore tournament(
                final List<XorNeuroEvolution.CandidateScore> scored,
                final Random random) {
            XorNeuroEvolution.CandidateScore best = null;
            int tournamentSize = Math.min(5, scored.size());
            for (int i = 0; i < tournamentSize; i++) {
                XorNeuroEvolution.CandidateScore candidate = scored.get(random.nextInt(scored.size()));
                if (best == null || candidate.score() < best.score()) {
                    best = candidate;
                }
            }
            return best;
        }

        private VisualState visualState(
                final int generation,
                final XorNeuroEvolution.CandidateScore score,
                final XorNeuroEvolution.CandidateScore bestScore,
                final List<XorNeuroEvolution.CandidateScore> scored,
                final long startNanos,
                final long nowNanos) {
            ChampionView bestView = championView(bestScore);
            double generationsPerSecond = generationsPerSecond(generation, startNanos, nowNanos);
            return new VisualState(
                    generation,
                    score,
                    bestView.score(),
                    populationSummary(scored),
                    generationsPerSecond,
                    bestView.snapshot(),
                    bestView.snapshot());
        }

        private ChampionView championView(final XorNeuroEvolution.CandidateScore bestScore) {
            if (bestScore.equals(cachedBestScore)
                    && cachedBestScoreWithDiagnostics != null
                    && cachedBestSnapshot != null) {
                return new ChampionView(cachedBestScoreWithDiagnostics, cachedBestSnapshot);
            }
            cachedBestScore = bestScore;
            ChampionView champion = buildChampionView(problem, config, bestScore);
            cachedBestScoreWithDiagnostics = champion.score();
            cachedBestSnapshot = champion.snapshot();
            return new ChampionView(cachedBestScoreWithDiagnostics, cachedBestSnapshot);
        }

        private void saveBestCheckpoint(final XorNeuroEvolution.CandidateScore bestScore) {
            try {
                NeuroEvolutionChampionCheckpoint.save(
                        NeuroEvolutionChampionCheckpoint.defaultPath(problem),
                        problem,
                        config,
                        bestScore);
            } catch (IOException exception) {
                System.err.println("Could not auto-save champion checkpoint: " + exception.getMessage());
            }
        }

        private boolean shouldUpdateVisual(
                final int generation,
                final long nowNanos,
                final long lastVisualUpdateNanos) {
            return generation == 0
                    || generation == config.generations() - 1
                    || nowNanos - lastVisualUpdateNanos >= VISUAL_UPDATE_INTERVAL_NANOS;
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

    private static final class NetworkPanel extends JPanel {

        private static final Color BACKGROUND = new Color(250, 251, 252);
        private static final Color GRID = new Color(226, 231, 237);
        private static final Color INPUT = new Color(68, 122, 201);
        private static final Color MEMORY = new Color(128, 91, 177);
        private static final Color HIDDEN = new Color(43, 153, 139);
        private static final Color BIAS = new Color(110, 120, 130);
        private static final Color OUTPUT = new Color(225, 143, 51);
        private static final Color POSITIVE = new Color(24, 143, 128);
        private static final Color NEGATIVE = new Color(206, 87, 79);
        private static final Color RECURRENT = new Color(111, 86, 174);
        private static final Color BIAS_LINK = new Color(60, 67, 78);

        private VisualState state;

        NetworkPanel() {
            setBackground(BACKGROUND);
            setPreferredSize(new Dimension(670, 680));
        }

        void setVisualState(final VisualState state) {
            this.state = state;
            repaint();
        }

        @Override
        protected void paintComponent(final Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            try {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                paintGrid(g);
                if (state == null) {
                    paintEmptyState(g);
                    return;
                }
                Map<String, Point2D.Double> positions = layoutNodes(state.bestSnapshot());
                paintLinks(g, state.bestSnapshot(), positions);
                paintNodes(g, state.bestSnapshot(), positions);
                paintGenomeStrip(g, state.bestScore().genome());
            } finally {
                g.dispose();
            }
        }

        private void paintGrid(final Graphics2D g) {
            g.setColor(BACKGROUND);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(GRID);
            for (int x = 36; x < getWidth(); x += 36) {
                g.drawLine(x, 0, x, getHeight());
            }
            for (int y = 36; y < getHeight(); y += 36) {
                g.drawLine(0, y, getWidth(), y);
            }
        }

        private void paintEmptyState(final Graphics2D g) {
            String text = "Press Start to evolve and visualize learners";
            g.setColor(new Color(70, 77, 87));
            g.setFont(g.getFont().deriveFont(Font.BOLD, 18.0f));
            FontMetrics metrics = g.getFontMetrics();
            g.drawString(text, (getWidth() - metrics.stringWidth(text)) / 2, getHeight() / 2);
        }

        private Map<String, Point2D.Double> layoutNodes(final NetworkSnapshot snapshot) {
            Map<VisualNodeLayer, List<VisualNode>> grouped = new EnumMap<>(VisualNodeLayer.class);
            for (VisualNodeLayer layer : VisualNodeLayer.values()) {
                grouped.put(layer, new ArrayList<>());
            }
            Map<Integer, List<VisualNode>> hiddenByDepth = new HashMap<>();
            Map<Integer, List<VisualNode>> biasByDepth = new HashMap<>();
            for (VisualNode node : snapshot.nodes()) {
                if (node.layer() == VisualNodeLayer.HIDDEN) {
                    hiddenByDepth.computeIfAbsent(node.depth(), ignored -> new ArrayList<>()).add(node);
                } else if (node.layer() == VisualNodeLayer.BIAS) {
                    biasByDepth.computeIfAbsent(node.depth(), ignored -> new ArrayList<>()).add(node);
                } else {
                    grouped.get(node.layer()).add(node);
                }
            }
            Map<String, Point2D.Double> positions = new HashMap<>();
            int hiddenLayers = snapshot.genome().hiddenLayers();
            double outputX = Math.max(470.0, getWidth() - 80.0);
            double hiddenStart = grouped.get(VisualNodeLayer.MEMORY).isEmpty() ? 235.0 : 285.0;
            double hiddenEnd = Math.max(hiddenStart, outputX - 145.0);
            placeColumn(positions, grouped.get(VisualNodeLayer.INPUT), 72.0);
            placeColumn(positions, grouped.get(VisualNodeLayer.MEMORY), 190.0);
            for (int layer = 0; layer < hiddenLayers; layer++) {
                double x = hiddenColumnX(layer, hiddenLayers, hiddenStart, hiddenEnd);
                placeColumn(positions, hiddenByDepth.getOrDefault(layer, List.of()), x);
                placeBiasNodes(positions, biasByDepth.getOrDefault(layer, List.of()), x);
            }
            placeColumn(positions, biasByDepth.getOrDefault(hiddenLayers, List.of()), Math.max(hiddenEnd + 55.0, outputX - 105.0));
            placeColumn(positions, grouped.get(VisualNodeLayer.OUTPUT), outputX);
            return positions;
        }

        private static double hiddenColumnX(
                final int layer,
                final int hiddenLayers,
                final double hiddenStart,
                final double hiddenEnd) {
            if (hiddenLayers <= 1) {
                return (hiddenStart + hiddenEnd) * 0.5;
            }
            return hiddenStart + (hiddenEnd - hiddenStart) * layer / (hiddenLayers - 1.0);
        }

        private void placeBiasNodes(
                final Map<String, Point2D.Double> positions,
                final List<VisualNode> nodes,
                final double x) {
            for (int i = 0; i < nodes.size(); i++) {
                positions.put(nodes.get(i).id(), new Point2D.Double(x, 44.0 + i * 34.0));
            }
        }

        private void placeColumn(
                final Map<String, Point2D.Double> positions,
                final List<VisualNode> nodes,
                final double x) {
            if (nodes.isEmpty()) {
                return;
            }
            double top = 80.0;
            double bottom = Math.max(top + 1.0, getHeight() - 110.0);
            double spacing = nodes.size() == 1 ? 0.0 : (bottom - top) / (nodes.size() - 1.0);
            for (int i = 0; i < nodes.size(); i++) {
                double y = nodes.size() == 1 ? (top + bottom) * 0.5 : top + spacing * i;
                positions.put(nodes.get(i).id(), new Point2D.Double(x, y));
            }
        }

        private void paintLinks(
                final Graphics2D g,
                final NetworkSnapshot snapshot,
                final Map<String, Point2D.Double> positions) {
            for (VisualLink link : snapshot.links()) {
                Point2D.Double from = positions.get(link.fromId());
                Point2D.Double to = positions.get(link.toId());
                if (from == null || to == null) {
                    continue;
                }
                float weight = (float) Math.min(1.0, Math.abs(link.weight()) / 3.0);
                Color base = linkColor(link);
                int alpha = link.bias()
                        ? 85 + Math.round(95.0f * weight)
                        : 70 + Math.round(150.0f * weight);
                g.setColor(withAlpha(base, alpha));
                Stroke oldStroke = g.getStroke();
                float strokeWidth = 0.8f + weight * 4.0f;
                if (link.bias()) {
                    g.setStroke(new BasicStroke(
                            strokeWidth,
                            BasicStroke.CAP_ROUND,
                            BasicStroke.JOIN_ROUND,
                            10.0f,
                            new float[] {7.0f, 7.0f},
                            0.0f));
                } else {
                    g.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                }
                if (link.recurrent()) {
                    double controlY = Math.min(from.y, to.y) - 46.0;
                    double controlX = (from.x + to.x) * 0.5;
                    g.draw(new QuadCurve2D.Double(from.x, from.y, controlX, controlY, to.x, to.y));
                } else {
                    g.drawLine((int) from.x, (int) from.y, (int) to.x, (int) to.y);
                }
                g.setStroke(oldStroke);
            }
        }

        private static Color linkColor(final VisualLink link) {
            if (link.recurrent()) {
                return RECURRENT;
            }
            if (link.bias()) {
                return BIAS_LINK;
            }
            return link.weight() >= 0.0 ? POSITIVE : NEGATIVE;
        }

        private void paintNodes(
                final Graphics2D g,
                final NetworkSnapshot snapshot,
                final Map<String, Point2D.Double> positions) {
            for (VisualNode node : snapshot.nodes()) {
                Point2D.Double point = positions.get(node.id());
                if (point == null) {
                    continue;
                }
                int radius = switch (node.layer()) {
                    case INPUT -> 11;
                    case MEMORY, BIAS -> 12;
                    case HIDDEN -> 16;
                    case OUTPUT -> 20;
                };
                Color color = nodeColor(node.layer());
                g.setColor(withAlpha(color, 52));
                g.fillOval((int) point.x - radius - 7, (int) point.y - radius - 7, (radius + 7) * 2, (radius + 7) * 2);
                g.setColor(color);
                g.fillOval((int) point.x - radius, (int) point.y - radius, radius * 2, radius * 2);
                g.setColor(Color.WHITE);
                g.setFont(g.getFont().deriveFont(Font.BOLD, node.layer() == VisualNodeLayer.OUTPUT ? 13.0f : 10.0f));
                FontMetrics metrics = g.getFontMetrics();
                g.drawString(
                        node.label(),
                        (int) point.x - metrics.stringWidth(node.label()) / 2,
                        (int) point.y + metrics.getAscent() / 2 - 2);
            }
        }

        private void paintGenomeStrip(final Graphics2D g, final EvolvableXorGenome genome) {
            int x = 22;
            int y = getHeight() - 50;
            String text = "hidden=" + genome.hiddenNeurons() + "x" + genome.hiddenLayers()
                    + " recurrent=" + genome.recurrentConnections()
                    + " memory=" + genome.memoryCells()
                    + " hebbian=" + genome.hebbianUpdate()
                    + " grad=" + genome.gradientUpdate()
                    + " norm=" + genome.normalization();
            g.setColor(new Color(255, 255, 255, 220));
            g.fillRoundRect(x - 10, y - 24, Math.min(getWidth() - 28, 600), 34, 10, 10);
            g.setColor(new Color(60, 67, 78));
            g.setFont(g.getFont().deriveFont(12.0f));
            g.drawString(text, x, y - 2);
        }

        private Color nodeColor(final VisualNodeLayer layer) {
            return switch (layer) {
                case INPUT -> INPUT;
                case MEMORY -> MEMORY;
                case BIAS -> BIAS;
                case HIDDEN -> HIDDEN;
                case OUTPUT -> OUTPUT;
            };
        }

        private static Color withAlpha(final Color color, final int alpha) {
            return new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(0, Math.min(255, alpha)));
        }
    }

    private static final class ChampionDetailsDialog extends JDialog {

        private final JTextArea detailsArea;

        ChampionDetailsDialog(final JFrame owner) {
            super(owner, "Best Champion Details", false);
            this.detailsArea = new JTextArea(waitingDetails());
            detailsArea.setEditable(false);
            detailsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            detailsArea.setLineWrap(false);
            setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
            setMinimumSize(new Dimension(620, 720));
            setLayout(new BorderLayout());
            add(new JScrollPane(detailsArea), BorderLayout.CENTER);
            pack();
            setLocationRelativeTo(owner);
        }

        void setVisualState(final VisualState state) {
            detailsArea.setText(state == null ? waitingDetails() : bestChampionDetails(state));
            detailsArea.setCaretPosition(0);
        }
    }

    private static String bestChampionDetails(final VisualState state) {
        XorNeuroEvolution.CandidateScore score = state.bestScore();
        EvolvableXorGenome genome = score.genome();
        NetworkSnapshot snapshot = state.bestSnapshot();
        NeuroEvolutionProblem problem = snapshot.problem();

        StringBuilder details = new StringBuilder(6_000);
        details.append("BEST CHAMPION\n");
        details.append("=============\n\n");
        appendLine(details, "Problem", problem.name());
        appendLine(details, "Mode", problem.classification() ? "classification" : "regression");
        appendLine(details, "Input dimensions", Integer.toString(problem.inputDimensions()));
        appendLine(details, "Output dimensions", Integer.toString(problem.outputDimensions()));
        appendLine(details, "Training samples", Integer.toString(problem.trainingSamples().size()));
        appendLine(details, "Generalization samples", Integer.toString(problem.generalizationSamples().size()));
        appendLine(details, "Sample state policy", problem.statefulSamples() ? "stateful sequence" : "stateless samples");
        appendLine(details, "Current GUI generation", Integer.toString(state.generation()));
        appendLine(details, "Best discovered generation", Integer.toString(score.generation()));
        details.append('\n');

        details.append("PROBLEM DESCRIPTION\n");
        details.append("-------------------\n");
        details.append(problem.description());
        details.append("\n\n");

        details.append("SCORE\n");
        details.append("-----\n");
        appendLine(details, "Overall score", format(score.score()));
        appendLine(details, "Training MSE", format(score.meanSquaredError()));
        appendLine(details, "Configured loss", format(score.configuredLoss()));
        appendLine(details, "Accuracy", accuracyText(problem, score));
        appendLine(details, "Generalization MSE", format(score.generalizationMeanSquaredError()));
        appendLine(details, "Baseline generalization MSE", format(problem.baselineGeneralizationMeanSquaredError()));
        appendLine(details, "Gen improvement vs baseline", baselineImprovementText(problem, score));
        appendLine(details, "Group-relative gen error", format(score.groupRelativeGeneralizationError()));
        appendLine(details, "Jitter MSE", format(score.jitterMeanSquaredError()));
        appendLine(details, "Smoothness penalty", format(score.smoothnessPenalty()));
        appendLine(details, "Complexity", format(score.complexity()));
        appendLine(details, "Complexity pressure", format(XorNeuroEvolution.normalizedComplexity(problem, score.complexity())));
        details.append('\n');

        if (!score.outputGroupScores().isEmpty() && problem.outputDimensions() > 1) {
            details.append("OUTPUT GROUP DIAGNOSTICS\n");
            details.append("------------------------\n");
            for (NeuroEvolutionOutputGroupScore groupScore : score.outputGroupScores()) {
                details.append("  ")
                        .append(padRight(groupScore.group().name(), 24))
                        .append(" train=")
                        .append(padRight(format(groupScore.trainingMeanSquaredError()), 10))
                        .append(" gen=")
                        .append(padRight(format(groupScore.generalizationMeanSquaredError()), 10))
                        .append(" baseline-gen=")
                        .append(padRight(format(groupScore.baselineGeneralizationMeanSquaredError()), 10))
                        .append(" improvement=")
                        .append(padRight(formatPercent(groupScore.generalizationImprovementOverBaseline()), 8))
                        .append(" weight=")
                        .append(format(groupScore.group().objectiveWeight()))
                        .append('\n');
            }
            details.append('\n');
        }

        details.append("TOPOLOGY\n");
        details.append("--------\n");
        appendLine(details, "Input feature width", Integer.toString(genome.inputFeatureSize(problem)));
        appendLine(details, "Full input width", Integer.toString(genome.inputSize(problem)));
        appendLine(details, "Hidden layers", Integer.toString(genome.hiddenLayers()));
        appendLine(details, "Hidden neurons per layer", Integer.toString(genome.hiddenNeurons()));
        appendLine(details, "Total hidden neurons", Integer.toString(genome.totalHiddenNeurons()));
        appendLine(details, "Memory cells", Integer.toString(genome.memoryCells()));
        appendLine(details, "Output nodes", Integer.toString(problem.outputDimensions()));
        appendLine(details, "Rendered nodes", Integer.toString(snapshot.nodes().size()));
        appendLine(details, "Rendered links", Integer.toString(snapshot.links().size()));
        appendLine(details, "Connection density gene", format(genome.connectionDensity()));
        appendLine(details, "Recurrent connection gene", Integer.toString(genome.recurrentConnections()));
        appendLine(details, "Actual recurrent links", Integer.toString(countRecurrentLinks(snapshot)));
        appendLine(details, "Positive / negative links", positiveNegativeLinkSummary(snapshot));
        appendLine(details, "Mean abs link weight", format(meanAbsoluteWeight(snapshot)));
        details.append('\n');

        details.append("TRANSFER AND REPRESENTATION\n");
        details.append("---------------------------\n");
        appendLine(details, "Hidden transfer function", genome.hiddenActivation().name());
        appendLine(details, "Output transfer function", genome.outputActivation().name());
        appendLine(details, "Activation slope", format(genome.activationSlope()));
        appendLine(details, "Input representation", genome.inputRepresentation().name());
        appendLine(details, "Phase / complex encoding", yesNo(genome.phaseEncoding()));
        appendLine(details, "Kernel memory encoding", yesNo(genome.kernelMemory()));
        appendLine(details, "Kernel sharpness", format(genome.kernelSharpness()));
        appendLine(details, "Loss function", genome.lossFunction().name());
        appendLine(details, "Learning schedule", genome.learningSchedule().name());
        details.append('\n');

        details.append("LOCAL UPDATE RULES\n");
        details.append("------------------\n");
        appendLine(details, "Gradient-like update", yesNo(genome.gradientUpdate()));
        appendLine(details, "Hebbian update", yesNo(genome.hebbianUpdate()));
        appendLine(details, "Normalization", yesNo(genome.normalization()));
        appendLine(details, "Second-derivative estimate", yesNo(genome.secondDerivativeEstimate()));
        appendLine(details, "Input learning rate", format(genome.inputLearningRate()));
        appendLine(details, "Output learning rate", format(genome.outputLearningRate()));
        appendLine(details, "Recurrent learning rate", format(genome.recurrentLearningRate()));
        appendLine(details, "Hebbian learning rate", format(genome.hebbianLearningRate()));
        appendLine(details, "Memory learning rate", format(genome.memoryLearningRate()));
        appendLine(details, "Momentum", format(genome.momentum()));
        appendLine(details, "Weight decay", format(genome.weightDecay()));
        appendLine(details, "Normalization strength", format(genome.normalizationStrength()));
        appendLine(details, "Error clip", format(genome.errorClip()));
        details.append('\n');

        details.append("NODES\n");
        details.append("-----\n");
        for (VisualNode node : sortedNodes(snapshot)) {
            details.append("  ")
                    .append(padRight(node.id(), 16))
                    .append(" layer=")
                    .append(padRight(node.layer().name(), 7))
                    .append(" depth=")
                    .append(node.depth())
                    .append(" label=")
                    .append(padRight(node.label(), 6))
                    .append(" index=")
                    .append(node.index())
                    .append('\n');
        }
        details.append('\n');

        details.append("LINKS BY ABSOLUTE WEIGHT\n");
        details.append("------------------------\n");
        for (VisualLink link : sortedLinks(snapshot)) {
            details.append("  ")
                    .append(padRight(link.fromId(), 16))
                    .append(link.recurrent() ? " ~> " : " -> ")
                    .append(padRight(link.toId(), 12))
                    .append(" weight=")
                    .append(format(link.weight()))
                    .append(link.bias() ? " bias" : "")
                    .append(link.recurrent() ? " recurrent" : "")
                    .append('\n');
        }
        return details.toString();
    }

    private static String waitingDetails() {
        return "Best champion details will appear after the first visual update.\n\n"
                + "Press Start in the main window to begin evolving.";
    }

    private static void appendLine(final StringBuilder builder, final String label, final String value) {
        builder.append("  ")
                .append(padRight(label, 28))
                .append(value)
                .append('\n');
    }

    private static List<VisualNode> sortedNodes(final NetworkSnapshot snapshot) {
        List<VisualNode> nodes = new ArrayList<>(snapshot.nodes());
        nodes.sort(Comparator.comparing(VisualNode::layer)
                .thenComparingInt(VisualNode::depth)
                .thenComparingInt(VisualNode::index));
        return nodes;
    }

    private static List<VisualLink> sortedLinks(final NetworkSnapshot snapshot) {
        List<VisualLink> links = new ArrayList<>(snapshot.links());
        links.sort(Comparator.comparingDouble((VisualLink link) -> Math.abs(link.weight())).reversed()
                .thenComparing(VisualLink::fromId)
                .thenComparing(VisualLink::toId));
        return links;
    }

    private static int countRecurrentLinks(final NetworkSnapshot snapshot) {
        int recurrent = 0;
        for (VisualLink link : snapshot.links()) {
            if (link.recurrent()) {
                recurrent++;
            }
        }
        return recurrent;
    }

    private static String positiveNegativeLinkSummary(final NetworkSnapshot snapshot) {
        int positive = 0;
        int negative = 0;
        int zero = 0;
        for (VisualLink link : snapshot.links()) {
            if (link.weight() > 0.0) {
                positive++;
            } else if (link.weight() < 0.0) {
                negative++;
            } else {
                zero++;
            }
        }
        return positive + " / " + negative + (zero == 0 ? "" : " / " + zero + " zero");
    }

    private static double meanAbsoluteWeight(final NetworkSnapshot snapshot) {
        if (snapshot.links().isEmpty()) {
            return 0.0;
        }
        double total = 0.0;
        for (VisualLink link : snapshot.links()) {
            total += Math.abs(link.weight());
        }
        return total / snapshot.links().size();
    }

    private static String padRight(final String text, final int width) {
        if (text.length() >= width) {
            return text + " ";
        }
        return text + " ".repeat(width - text.length());
    }

    private static String yesNo(final boolean value) {
        return value ? "yes" : "no";
    }

    private record ChampionView(
            XorNeuroEvolution.CandidateScore score,
            NetworkSnapshot snapshot) {

        private ChampionView {
            Objects.requireNonNull(score, "Score cannot be null.");
            Objects.requireNonNull(snapshot, "Snapshot cannot be null.");
        }
    }

    private record VisualState(
            int generation,
            XorNeuroEvolution.CandidateScore score,
            XorNeuroEvolution.CandidateScore bestScore,
            String populationSummary,
            double generationsPerSecond,
            NetworkSnapshot snapshot,
            NetworkSnapshot bestSnapshot) {

        private VisualState {
            Objects.requireNonNull(score, "Score cannot be null.");
            Objects.requireNonNull(bestScore, "Best score cannot be null.");
            Objects.requireNonNull(populationSummary, "Population summary cannot be null.");
            if (!Double.isFinite(generationsPerSecond) || generationsPerSecond < 0.0) {
                throw new IllegalArgumentException("Generation rate must be finite and non-negative.");
            }
            Objects.requireNonNull(snapshot, "Snapshot cannot be null.");
            Objects.requireNonNull(bestSnapshot, "Best snapshot cannot be null.");
        }
    }

    @FunctionalInterface
    private interface StateListener {
        void accept(VisualState state);
    }

    private static String format(final double value) {
        if (!Double.isFinite(value)) {
            return String.valueOf(value);
        }
        if (Math.abs(value) >= 100.0 || Math.abs(value) < 0.001 && value != 0.0) {
            return String.format("%.3e", value);
        }
        return String.format("%.4f", value);
    }

    private static String formatPercent(final double value) {
        if (!Double.isFinite(value)) {
            return String.valueOf(value);
        }
        return String.format("%.1f%%", value * 100.0);
    }

    private static String accuracyText(
            final NeuroEvolutionProblem problem,
            final XorNeuroEvolution.CandidateScore score) {
        return problem.classification() ? formatPercent(score.accuracy()) : "n/a";
    }

    private static String baselineImprovementText(
            final NeuroEvolutionProblem problem,
            final XorNeuroEvolution.CandidateScore score) {
        double baseline = problem.baselineGeneralizationMeanSquaredError();
        if (!Double.isFinite(baseline) || baseline <= 1.0e-12) {
            return "n/a";
        }
        return formatPercent(1.0 - score.generalizationMeanSquaredError() / baseline);
    }
}
