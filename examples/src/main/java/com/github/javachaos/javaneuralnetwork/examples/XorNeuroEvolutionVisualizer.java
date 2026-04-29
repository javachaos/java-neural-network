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
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.JTextArea;
import javax.swing.JTextField;
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
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Swing GUI that visualizes the current rich learner while evolution runs.
 */
public final class XorNeuroEvolutionVisualizer {

    private static final int GUI_GENERATION_LIMIT = Integer.MAX_VALUE;

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
        private NeuroEvolutionWorker worker;
        private final NetworkPanel networkPanel;
        private final JLabel titleLabel;
        private final JComboBox<String> problemSelector;
        private final JComboBox<WorkerMode> workerModeSelector;
        private final JComboBox<NeuroEvolutionDistributedConfig.ComputeBackend> workerBackendSelector;
        private final JTextField remoteHostField;
        private final JTextField remotePortField;
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
        private final JLabel strategyLabel;
        private final JLabel parallelismLabel;
        private final JLabel workerLabel;
        private final JLabel checkpointLabel;
        private final JLabel rateLabel;
        private final JLabel ruleLabel;
        private final JLabel frontierScoreLabel;
        private final JLabel frontierGeneralizationLabel;
        private final JLabel frontierGroupLabel;
        private final JLabel frontierAttentionLabel;
        private final JLabel frontierTradeoffLabel;
        private final JButton startButton;
        private final JButton pauseButton;
        private final JButton resetButton;
        private final JButton detailsButton;
        private final JButton saveCheckpointButton;
        private final JButton loadCheckpointButton;
        private final JButton applyWorkerButton;

        private NeuroEvolutionProblem problem;
        private NeuroEvolutionRunHandle runner;
        private VisualState latestState;
        private ChampionDetailsDialog detailsDialog;
        private final ParetoFrontierTracker frontierTracker = new ParetoFrontierTracker();

        VisualizerFrame(
                final NeuroEvolutionProblem problem,
                final XorNeuroEvolution.EvolutionConfig config,
                final NeuroEvolutionProblemCatalog catalog) {
            super(windowTitle(problem));
            this.problem = Objects.requireNonNull(problem, "Problem cannot be null.");
            this.config = Objects.requireNonNull(config, "Config cannot be null.");
            this.catalog = Objects.requireNonNull(catalog, "Problem catalog cannot be null.");
            WorkerMode initialWorkerMode = initialWorkerMode();
            NeuroEvolutionDistributedConfig.ComputeBackend initialBackend = initialBackend();
            this.worker = createWorker(initialWorkerMode, initialBackend, initialRemoteHost(), initialRemotePort());
            this.networkPanel = new NetworkPanel();
            this.titleLabel = new JLabel(titleText(problem));
            this.problemSelector = new JComboBox<>(catalog.problemKeys().toArray(String[]::new));
            this.workerModeSelector = new JComboBox<>(WorkerMode.values());
            this.workerBackendSelector = new JComboBox<>(NeuroEvolutionDistributedConfig.ComputeBackend.values());
            this.remoteHostField = new JTextField(initialRemoteHost());
            this.remotePortField = new JTextField(Integer.toString(initialRemotePort()));
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
            this.strategyLabel = valueLabel("Strategy: waiting");
            this.parallelismLabel = valueLabel("Parallelism: " + config.parallelism() + " workers");
            this.workerLabel = valueLabel("Worker: " + workerDescription(initialWorkerMode, initialBackend));
            this.checkpointLabel = valueLabel("Checkpoint: waiting");
            this.rateLabel = valueLabel("Rate: waiting");
            this.ruleLabel = valueLabel("Rule: waiting");
            this.frontierScoreLabel = valueLabel("Score lens: waiting");
            this.frontierGeneralizationLabel = valueLabel("Gen lens: waiting");
            this.frontierGroupLabel = valueLabel("Group lens: waiting");
            this.frontierAttentionLabel = valueLabel("Attention lens: waiting");
            this.frontierTradeoffLabel = valueLabel("Compact lens: waiting");
            this.startButton = new JButton("Start");
            this.pauseButton = new JButton("Pause");
            this.resetButton = new JButton("Reset");
            this.detailsButton = new JButton("Best Details");
            this.saveCheckpointButton = new JButton("Save Best");
            this.loadCheckpointButton = new JButton("Load Best");
            this.applyWorkerButton = new JButton("Apply Worker");

            titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18.0f));
            problemSelector.setSelectedItem(problem.key());
            workerModeSelector.setSelectedItem(initialWorkerMode);
            workerBackendSelector.setSelectedItem(initialBackend);
            updateWorkerControlEnablement();
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
            applyWorkerButton.addActionListener(event -> applyWorkerSelection());
            workerModeSelector.addActionListener(event -> updateWorkerControlEnablement());
            problemSelector.addActionListener(event -> switchProblem((String) problemSelector.getSelectedItem()));
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(final WindowEvent event) {
                    stopRunner();
                    worker.close();
                }
            });
        }

        private static WorkerMode initialWorkerMode() {
            if (Boolean.getBoolean("neuroEvolution.javaWorker")) {
                return WorkerMode.JAVA_LOCAL;
            }
            String configured = System.getProperty("neuroEvolution.workerMode", "RUST_LOCAL");
            try {
                return WorkerMode.valueOf(configured.trim().toUpperCase());
            } catch (IllegalArgumentException exception) {
                return WorkerMode.RUST_LOCAL;
            }
        }

        private static NeuroEvolutionDistributedConfig.ComputeBackend initialBackend() {
            String configured = System.getProperty("neuroEvolution.computeBackend", "CPU");
            try {
                return NeuroEvolutionDistributedConfig.ComputeBackend.valueOf(
                        configured.trim().toUpperCase());
            } catch (IllegalArgumentException exception) {
                return NeuroEvolutionDistributedConfig.ComputeBackend.CPU;
            }
        }

        private static String initialRemoteHost() {
            return System.getProperty("neuroEvolution.remoteHost", "127.0.0.1");
        }

        private static int initialRemotePort() {
            return parsePort(System.getProperty("neuroEvolution.remotePort", "50051"), 50051);
        }

        private static NeuroEvolutionWorker createWorker(
                final WorkerMode mode,
                final NeuroEvolutionDistributedConfig.ComputeBackend backend,
                final String remoteHost,
                final int remotePort) {
            NeuroEvolutionDistributedConfig distributedConfig =
                    NeuroEvolutionDistributedConfig.local().withComputeBackend(backend);
            return switch (mode) {
                case JAVA_LOCAL -> new LocalNeuroEvolutionWorker();
                case RUST_LOCAL -> new RustNeuroEvolutionWorker(distributedConfig);
                case RUST_REMOTE -> new RemoteRustNeuroEvolutionWorker(remoteHost, remotePort, distributedConfig);
            };
        }

        private static String workerDescription(
                final WorkerMode mode,
                final NeuroEvolutionDistributedConfig.ComputeBackend backend) {
            if (mode == WorkerMode.JAVA_LOCAL) {
                return mode.label();
            }
            return mode.label() + " / " + backend.name();
        }

        private static int parsePort(final String value, final int fallback) {
            try {
                int port = Integer.parseInt(value.trim());
                return port > 0 && port <= 65_535 ? port : fallback;
            } catch (NumberFormatException exception) {
                return fallback;
            }
        }

        private JPanel sidePanel() {
            final int sideInnerWidth = 274;
            final int metricRowHeight = 22;
            final int metricRowGap = 4;
            final int metricRows = 16;
            JPanel panel = new JPanel(new BorderLayout());
            panel.setPreferredSize(new Dimension(310, 680));
            panel.setBackground(new Color(242, 244, 247));
            JPanel content = new JPanel(new BorderLayout(0, 14));
            content.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
            content.setBackground(new Color(242, 244, 247));

            JPanel metrics = new JPanel(new GridLayout(0, 1, 0, metricRowGap));
            metrics.setOpaque(false);
            metrics.setPreferredSize(new Dimension(
                    sideInnerWidth,
                    metricRows * metricRowHeight + (metricRows - 1) * metricRowGap));
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
            metrics.add(strategyLabel);
            metrics.add(parallelismLabel);
            metrics.add(workerLabel);
            metrics.add(checkpointLabel);
            metrics.add(rateLabel);
            metrics.add(ruleLabel);

            JPanel frontier = new JPanel(new GridLayout(0, 1, 0, 6));
            frontier.setOpaque(false);
            frontier.add(valueLabel("Observed frontier"));
            frontier.add(frontierScoreLabel);
            frontier.add(frontierGeneralizationLabel);
            frontier.add(frontierGroupLabel);
            frontier.add(frontierAttentionLabel);
            frontier.add(frontierTradeoffLabel);

            JPanel controls = new JPanel(new GridLayout(0, 2, 8, 8));
            controls.setOpaque(false);
            controls.add(startButton);
            controls.add(pauseButton);
            controls.add(resetButton);
            controls.add(detailsButton);
            controls.add(saveCheckpointButton);
            controls.add(loadCheckpointButton);

            JPanel workerControls = new JPanel(new GridLayout(0, 1, 0, 6));
            workerControls.setOpaque(false);
            workerControls.add(valueLabel("Worker management"));
            workerControls.add(workerModeSelector);
            workerControls.add(workerBackendSelector);
            JPanel remoteControls = new JPanel(new GridLayout(1, 2, 6, 0));
            remoteControls.setOpaque(false);
            remoteControls.add(remoteHostField);
            remoteControls.add(remotePortField);
            workerControls.add(remoteControls);
            workerControls.add(applyWorkerButton);

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
            descriptionScroll.setPreferredSize(new Dimension(sideInnerWidth, 132));
            descriptionScroll.setBorder(BorderFactory.createLineBorder(new Color(212, 217, 224)));
            problemControls.add(problemSelector, BorderLayout.NORTH);
            problemControls.add(descriptionScroll, BorderLayout.CENTER);
            heading.add(titleLabel, BorderLayout.NORTH);
            heading.add(problemControls, BorderLayout.CENTER);
            top.add(heading, BorderLayout.NORTH);
            JPanel metricsAndFrontier = new JPanel(new BorderLayout(0, 12));
            metricsAndFrontier.setOpaque(false);
            metricsAndFrontier.add(metrics, BorderLayout.NORTH);
            metricsAndFrontier.add(frontier, BorderLayout.CENTER);
            top.add(metricsAndFrontier, BorderLayout.CENTER);
            JPanel lowerControls = new JPanel(new BorderLayout(0, 10));
            lowerControls.setOpaque(false);
            lowerControls.add(workerControls, BorderLayout.NORTH);
            lowerControls.add(controls, BorderLayout.SOUTH);
            top.add(lowerControls, BorderLayout.SOUTH);

            content.add(top, BorderLayout.NORTH);
            content.add(legend, BorderLayout.SOUTH);
            JScrollPane sideScroll = new JScrollPane(content);
            sideScroll.setBorder(null);
            sideScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            sideScroll.getVerticalScrollBar().setUnitIncrement(16);
            panel.add(sideScroll, BorderLayout.CENTER);
            return panel;
        }

        private void start() {
            if (runner == null || runner.isStopped()) {
                runner = worker.start(
                        new NeuroEvolutionRunRequest(problem, config),
                        new GuiRunListener());
            } else {
                runner.resume();
            }
        }

        private void applyWorkerSelection() {
            WorkerMode mode = (WorkerMode) workerModeSelector.getSelectedItem();
            NeuroEvolutionDistributedConfig.ComputeBackend backend =
                    (NeuroEvolutionDistributedConfig.ComputeBackend) workerBackendSelector.getSelectedItem();
            if (mode == null || backend == null) {
                workerLabel.setText("Worker: invalid selection");
                return;
            }
            int port = parsePort(remotePortField.getText(), initialRemotePort());
            String host = remoteHostField.getText().trim();
            if (host.isBlank()) {
                host = initialRemoteHost();
                remoteHostField.setText(host);
            }
            remotePortField.setText(Integer.toString(port));
            boolean restart = runner != null && !runner.isStopped();
            stopRunner();
            worker.close();
            worker = createWorker(mode, backend, host, port);
            workerLabel.setText("Worker: " + workerDescription(mode, backend));
            clearState();
            if (restart) {
                start();
            }
        }

        private void updateWorkerControlEnablement() {
            WorkerMode mode = (WorkerMode) workerModeSelector.getSelectedItem();
            boolean rustWorker = mode != WorkerMode.JAVA_LOCAL;
            boolean remoteWorker = mode == WorkerMode.RUST_REMOTE;
            workerBackendSelector.setEnabled(rustWorker);
            remoteHostField.setEnabled(remoteWorker);
            remotePortField.setEnabled(remoteWorker);
        }

        private void pause() {
            if (runner != null) {
                runner.pause();
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
            frontierTracker.clear();
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
            strategyLabel.setText("Strategy: waiting");
            parallelismLabel.setText("Parallelism: " + config.parallelism() + " workers");
            checkpointLabel.setText("Checkpoint: waiting");
            rateLabel.setText("Rate: waiting");
            ruleLabel.setText("Rule: waiting");
            setStableText(frontierScoreLabel, "Score lens: waiting");
            setStableText(frontierGeneralizationLabel, "Gen lens: waiting");
            setStableText(frontierGroupLabel, "Group lens: waiting");
            setStableText(frontierAttentionLabel, "Attention lens: waiting");
            setStableText(frontierTradeoffLabel, "Compact lens: waiting");
            if (detailsDialog != null) {
                detailsDialog.setVisualState(null);
            }
        }

        private void stopRunner() {
            if (runner != null) {
                runner.stop();
                runner = null;
            }
        }

        private void updateProgress(final NeuroEvolutionRunProgress progress) {
            updateState(new VisualState(
                    progress.generation(),
                    progress.champion(),
                    progress.best(),
                    progress.populationSummary(),
                    progress.strategySummary(),
                    progress.generationsPerSecond(),
                    progress.snapshot(),
                    progress.bestSnapshot()));
        }

        private void updateState(final VisualState state) {
            SwingUtilities.invokeLater(() -> {
                if (!state.snapshot().problem().name().equals(problem.name())) {
                    return;
                }
                latestState = state;
                frontierTracker.observe(problem, state.score());
                frontierTracker.observe(problem, state.bestScore());
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
                setStableText(populationLabel, state.populationSummary());
                setStableText(strategyLabel, state.strategySummary());
                rateLabel.setText("Rate: " + format(state.generationsPerSecond()) + " gen/s");
                EvolvableXorGenome genome = state.bestScore().genome();
                setStableText(ruleLabel, "Rule: " + genome.hiddenActivation()
                        + ", " + genome.inputRepresentation()
                        + ", " + genome.lossFunction()
                        + ", " + genome.learningSchedule());
                updateFrontierLabels(problem);
                networkPanel.setVisualState(state);
                if (detailsDialog != null && detailsDialog.isVisible()) {
                    detailsDialog.setVisualState(state);
                }
            });
        }

        private void updateFrontierLabels(final NeuroEvolutionProblem currentProblem) {
            setStableText(
                    frontierScoreLabel,
                    frontierText("Score lens", frontierTracker.bestByScore(), frontierTracker.scoreValue()));
            setStableText(
                    frontierGeneralizationLabel,
                    frontierText(
                            "Gen lens",
                            frontierTracker.bestByGeneralization(),
                            frontierTracker.generalizationValue()));
            setStableText(
                    frontierGroupLabel,
                    frontierText("Group lens", frontierTracker.bestByGroup(), frontierTracker.groupValue()));
            setStableText(
                    frontierAttentionLabel,
                    frontierPercentText(
                            "Attention lens",
                            frontierTracker.bestByAttention(),
                            frontierTracker.attentionValue()));
            setStableText(
                    frontierTradeoffLabel,
                    frontierText(
                            "Compact lens",
                            frontierTracker.bestByTradeoff(),
                            frontierTracker.tradeoffValue(currentProblem)));
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
                NeuroEvolutionChampionView champion =
                        NeuroEvolutionChampionViews.build(problem, checkpoint.config(), checkpoint.score());
                updateState(new VisualState(
                        champion.score().generation(),
                        champion.score(),
                        champion.score(),
                        "Loaded checkpoint: " + path.getFileName(),
                        "Strategy: checkpoint",
                        0.0,
                        champion.snapshot(),
                        champion.snapshot()));
                checkpointLabel.setText("Checkpoint: loaded " + path.getFileName());
            } catch (IOException | IllegalArgumentException exception) {
                checkpointLabel.setText("Checkpoint: load failed");
                System.err.println("Could not load champion checkpoint: " + exception.getMessage());
            }
        }

        private final class GuiRunListener implements NeuroEvolutionRunListener {

            @Override
            public void onProgress(final NeuroEvolutionRunProgress progress) {
                updateProgress(progress);
            }

            @Override
            public void onCheckpoint(final NeuroEvolutionCheckpointEvent checkpoint) {
                SwingUtilities.invokeLater(() -> {
                    if (!checkpoint.problem().name().equals(problem.name())) {
                        return;
                    }
                    if (checkpoint.saved()) {
                        checkpointLabel.setText("Checkpoint: auto-saved " + checkpoint.path().getFileName());
                    } else {
                        checkpointLabel.setText("Checkpoint: auto-save failed");
                        System.err.println("Could not auto-save champion checkpoint: " + checkpoint.message());
                    }
                });
            }

            @Override
            public void onFailure(final Throwable failure) {
                SwingUtilities.invokeLater(() -> {
                    checkpointLabel.setText("Worker: failed");
                    System.err.println("Neuro-evolution worker failed: " + failure.getMessage());
                });
            }
        }

        private static JLabel valueLabel(final String text) {
            JLabel label = new JLabel(text, SwingConstants.LEFT);
            label.setFont(label.getFont().deriveFont(13.0f));
            label.setPreferredSize(new Dimension(274, 22));
            label.setMinimumSize(new Dimension(274, 22));
            label.setMaximumSize(new Dimension(274, 22));
            label.setToolTipText(text);
            return label;
        }

        private static void setStableText(final JLabel label, final String text) {
            label.setText(text);
            label.setToolTipText(text);
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

    private enum WorkerMode {
        JAVA_LOCAL("Java local"),
        RUST_LOCAL("Rust local"),
        RUST_REMOTE("Rust gRPC");

        private final String label;

        WorkerMode(final String label) {
            this.label = label;
        }

        String label() {
            return label;
        }

        @Override
        public String toString() {
            return label;
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
        private static final int STABLE_HIDDEN_LAYER_SLOTS = 4;
        private static final int STABLE_HIDDEN_NEURON_SLOTS = 12;
        private static final int STABLE_MEMORY_SLOTS = 4;

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
            double outputX = Math.max(470.0, getWidth() - 80.0);
            double hiddenStart = 285.0;
            double hiddenEnd = Math.max(hiddenStart, outputX - 145.0);
            placeIndexedColumn(positions, grouped.get(VisualNodeLayer.INPUT), 72.0, stableInputSlots(snapshot));
            placeIndexedColumn(positions, grouped.get(VisualNodeLayer.MEMORY), 190.0, STABLE_MEMORY_SLOTS);
            int hiddenLayers = snapshot.genome().hiddenLayers();
            for (int layer = 0; layer < hiddenLayers; layer++) {
                double x = hiddenColumnX(layer, STABLE_HIDDEN_LAYER_SLOTS, hiddenStart, hiddenEnd);
                placeIndexedColumn(
                        positions,
                        hiddenByDepth.getOrDefault(layer, List.of()),
                        x,
                        STABLE_HIDDEN_NEURON_SLOTS);
                placeBiasNodes(positions, biasByDepth.getOrDefault(layer, List.of()), x);
            }
            placeIndexedColumn(
                    positions,
                    biasByDepth.getOrDefault(hiddenLayers, List.of()),
                    Math.max(hiddenEnd + 55.0, outputX - 105.0),
                    1);
            placeIndexedColumn(
                    positions,
                    grouped.get(VisualNodeLayer.OUTPUT),
                    outputX,
                    Math.max(1, snapshot.problem().outputDimensions()));
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

        private void placeIndexedColumn(
                final Map<String, Point2D.Double> positions,
                final List<VisualNode> nodes,
                final double x,
                final int slotCount) {
            if (nodes.isEmpty()) {
                return;
            }
            int slots = Math.max(1, Math.max(slotCount, highestNodeIndex(nodes) + 1));
            for (VisualNode node : nodes) {
                int slot = Math.max(0, Math.min(slots - 1, node.index()));
                positions.put(node.id(), new Point2D.Double(x, yForSlot(slot, slots)));
            }
        }

        private double yForSlot(final int slot, final int slotCount) {
            double top = 80.0;
            double bottom = Math.max(top + 1.0, getHeight() - 110.0);
            if (slotCount <= 1) {
                return (top + bottom) * 0.5;
            }
            return top + (bottom - top) * slot / (slotCount - 1.0);
        }

        private static int highestNodeIndex(final List<VisualNode> nodes) {
            int highest = 0;
            for (VisualNode node : nodes) {
                highest = Math.max(highest, node.index());
            }
            return highest;
        }

        private static int stableInputSlots(final NetworkSnapshot snapshot) {
            int slots = 0;
            for (XorInputRepresentation representation : XorInputRepresentation.values()) {
                slots = Math.max(
                        slots,
                        representation.encode(
                                new double[snapshot.problem().inputDimensions()],
                                snapshot.problem().kernelCenters(),
                                true,
                                true,
                                snapshot.genome().kernelSharpness()).length);
            }
            return Math.max(slots, snapshot.genome().inputFeatureSize(snapshot.problem()));
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
                int radius = nodeRadius(snapshot, node);
                Color color = nodeColor(node.layer());
                g.setColor(withAlpha(color, 52));
                g.fillOval((int) point.x - radius - 7, (int) point.y - radius - 7, (radius + 7) * 2, (radius + 7) * 2);
                g.setColor(color);
                g.fillOval((int) point.x - radius, (int) point.y - radius, radius * 2, radius * 2);
                if (!shouldDrawNodeLabel(snapshot, node, radius)) {
                    continue;
                }
                g.setColor(Color.WHITE);
                g.setFont(g.getFont().deriveFont(Font.BOLD, node.layer() == VisualNodeLayer.OUTPUT ? 13.0f : 10.0f));
                FontMetrics metrics = g.getFontMetrics();
                g.drawString(
                        node.label(),
                        (int) point.x - metrics.stringWidth(node.label()) / 2,
                        (int) point.y + metrics.getAscent() / 2 - 2);
            }
        }

        private int nodeRadius(final NetworkSnapshot snapshot, final VisualNode node) {
            int maxRadius = switch (node.layer()) {
                case INPUT -> 11;
                case MEMORY, BIAS -> 12;
                case HIDDEN -> 16;
                case OUTPUT -> 18;
            };
            int slots = stableSlotsFor(snapshot, node);
            if (slots <= 1) {
                return maxRadius;
            }
            double spacing = Math.abs(yForSlot(1, slots) - yForSlot(0, slots));
            return Math.max(4, Math.min(maxRadius, (int) Math.floor(spacing * 0.38)));
        }

        private boolean shouldDrawNodeLabel(
                final NetworkSnapshot snapshot,
                final VisualNode node,
                final int radius) {
            if (node.layer() == VisualNodeLayer.BIAS || node.layer() == VisualNodeLayer.HIDDEN) {
                return true;
            }
            return radius >= 8 && stableSlotsFor(snapshot, node) <= 24;
        }

        private static int stableSlotsFor(final NetworkSnapshot snapshot, final VisualNode node) {
            return switch (node.layer()) {
                case INPUT -> stableInputSlots(snapshot);
                case MEMORY -> STABLE_MEMORY_SLOTS;
                case BIAS -> 1;
                case HIDDEN -> STABLE_HIDDEN_NEURON_SLOTS;
                case OUTPUT -> Math.max(1, snapshot.problem().outputDimensions());
            };
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

        details.append("CURRENT SEARCH STATE\n");
        details.append("--------------------\n");
        appendLine(details, "Strategy", state.strategySummary());
        appendLine(details, "Population", state.populationSummary());
        appendLine(details, "Generation rate", format(state.generationsPerSecond()) + " gen/s");
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

        if (problem.freeEnergyProfile().enabled()) {
            NeuroEvolutionFreeEnergyProfile freeEnergyProfile = problem.freeEnergyProfile();
            details.append("PREDICTIVE CODING\n");
            details.append("-----------------\n");
            appendLine(details, "Predictive free energy", format(score.predictiveFreeEnergy()));
            appendLine(details, "Sensory prediction energy", format(score.sensoryPredictionEnergy()));
            appendLine(details, "Latent prediction energy", format(score.latentPredictionEnergy()));
            appendLine(details, "Complexity prior energy", format(score.complexityPriorEnergy()));
            appendLine(details, "Objective weight", format(freeEnergyProfile.objectiveWeight()));
            appendLine(details, "Sensory / latent / prior weights",
                    format(freeEnergyProfile.sensoryPredictionWeight())
                            + " / " + format(freeEnergyProfile.latentPredictionWeight())
                            + " / " + format(freeEnergyProfile.complexityPriorWeight()));
            details.append('\n');
        }

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

    private static String frontierText(
            final String label,
            final XorNeuroEvolution.CandidateScore score,
            final double value) {
        if (score == null || !Double.isFinite(value)) {
            return label + ": waiting";
        }
        return label + ": g" + score.generation() + " / " + format(value);
    }

    private static String frontierPercentText(
            final String label,
            final XorNeuroEvolution.CandidateScore score,
            final double value) {
        if (score == null || !Double.isFinite(value)) {
            return label + ": waiting";
        }
        return label + ": g" + score.generation() + " / " + formatPercent(value);
    }

    private static double averageAttentionImprovement(final XorNeuroEvolution.CandidateScore score) {
        double total = 0.0;
        int count = 0;
        for (NeuroEvolutionOutputGroupScore groupScore : score.outputGroupScores()) {
            String groupName = groupScore.group().name().toLowerCase(Locale.ROOT);
            if (groupName.startsWith("attention")) {
                total += groupScore.generalizationImprovementOverBaseline();
                count++;
            }
        }
        return count == 0 ? Double.NaN : total / count;
    }

    private static double compactTradeoff(
            final NeuroEvolutionProblem problem,
            final XorNeuroEvolution.CandidateScore score) {
        if (score == null) {
            return Double.NaN;
        }
        double groupRelativeError = XorNeuroEvolution.boundedGroupRelativeGeneralizationError(
                score.groupRelativeGeneralizationError());
        return score.generalizationMeanSquaredError()
                + 0.01 * groupRelativeError
                + 0.001 * XorNeuroEvolution.normalizedComplexity(problem, score.complexity());
    }

    private static final class ParetoFrontierTracker {

        private XorNeuroEvolution.CandidateScore bestByScore;
        private XorNeuroEvolution.CandidateScore bestByGeneralization;
        private XorNeuroEvolution.CandidateScore bestByGroup;
        private XorNeuroEvolution.CandidateScore bestByAttention;
        private XorNeuroEvolution.CandidateScore bestByTradeoff;

        void clear() {
            bestByScore = null;
            bestByGeneralization = null;
            bestByGroup = null;
            bestByAttention = null;
            bestByTradeoff = null;
        }

        void observe(
                final NeuroEvolutionProblem problem,
                final XorNeuroEvolution.CandidateScore score) {
            Objects.requireNonNull(problem, "Problem cannot be null.");
            Objects.requireNonNull(score, "Score cannot be null.");
            if (bestByScore == null || score.score() < bestByScore.score()) {
                bestByScore = score;
            }
            if (Double.isFinite(score.generalizationMeanSquaredError())
                    && (bestByGeneralization == null
                    || score.generalizationMeanSquaredError()
                    < bestByGeneralization.generalizationMeanSquaredError())) {
                bestByGeneralization = score;
            }
            if (Double.isFinite(score.groupRelativeGeneralizationError())
                    && (bestByGroup == null
                    || score.groupRelativeGeneralizationError()
                    < bestByGroup.groupRelativeGeneralizationError())) {
                bestByGroup = score;
            }
            double attention = averageAttentionImprovement(score);
            if (Double.isFinite(attention)
                    && (bestByAttention == null
                    || attention > averageAttentionImprovement(bestByAttention))) {
                bestByAttention = score;
            }
            double tradeoff = compactTradeoff(problem, score);
            if (Double.isFinite(tradeoff)
                    && (bestByTradeoff == null
                    || tradeoff < compactTradeoff(problem, bestByTradeoff))) {
                bestByTradeoff = score;
            }
        }

        XorNeuroEvolution.CandidateScore bestByScore() {
            return bestByScore;
        }

        double scoreValue() {
            return bestByScore == null ? Double.NaN : bestByScore.score();
        }

        XorNeuroEvolution.CandidateScore bestByGeneralization() {
            return bestByGeneralization;
        }

        double generalizationValue() {
            return bestByGeneralization == null
                    ? Double.NaN
                    : bestByGeneralization.generalizationMeanSquaredError();
        }

        XorNeuroEvolution.CandidateScore bestByGroup() {
            return bestByGroup;
        }

        double groupValue() {
            return bestByGroup == null ? Double.NaN : bestByGroup.groupRelativeGeneralizationError();
        }

        XorNeuroEvolution.CandidateScore bestByAttention() {
            return bestByAttention;
        }

        double attentionValue() {
            return bestByAttention == null ? Double.NaN : averageAttentionImprovement(bestByAttention);
        }

        XorNeuroEvolution.CandidateScore bestByTradeoff() {
            return bestByTradeoff;
        }

        double tradeoffValue(final NeuroEvolutionProblem problem) {
            return compactTradeoff(problem, bestByTradeoff);
        }
    }

    private record VisualState(
            int generation,
            XorNeuroEvolution.CandidateScore score,
            XorNeuroEvolution.CandidateScore bestScore,
            String populationSummary,
            String strategySummary,
            double generationsPerSecond,
            NetworkSnapshot snapshot,
            NetworkSnapshot bestSnapshot) {

        private VisualState {
            Objects.requireNonNull(score, "Score cannot be null.");
            Objects.requireNonNull(bestScore, "Best score cannot be null.");
            Objects.requireNonNull(populationSummary, "Population summary cannot be null.");
            Objects.requireNonNull(strategySummary, "Strategy summary cannot be null.");
            if (!Double.isFinite(generationsPerSecond) || generationsPerSecond < 0.0) {
                throw new IllegalArgumentException("Generation rate must be finite and non-negative.");
            }
            Objects.requireNonNull(snapshot, "Snapshot cannot be null.");
            Objects.requireNonNull(bestSnapshot, "Best snapshot cannot be null.");
        }
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
