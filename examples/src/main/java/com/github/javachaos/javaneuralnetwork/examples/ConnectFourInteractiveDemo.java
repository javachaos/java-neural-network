package com.github.javachaos.javaneuralnetwork.examples;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

/**
 * Swing front-end for playing Connect 4 against the online learner.
 */
public final class ConnectFourInteractiveDemo extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final int HUMAN = ConnectFourLearningExample.RED;
    private static final int AI = ConnectFourLearningExample.YELLOW;
    private static final int SELF_PLAY_UPDATE_INTERVAL = 25;
    private static final double SELF_PLAY_EXPLORATION_RATE = 0.08;

    private int[] board = new int[ConnectFourLearningExample.CELLS];
    private final List<ConnectFourLearningExample.PlayedMove> history = new ArrayList<>();
    private final JButton[] columns = new JButton[ConnectFourLearningExample.COLUMNS];
    private final JLabel[] cells = new JLabel[ConnectFourLearningExample.CELLS];
    private final JLabel statusLabel = new JLabel("Training Connect 4 model...", SwingConstants.CENTER);
    private final JLabel metricsLabel = new JLabel("Learning rules, tactics, and bootstrap play.",
            SwingConstants.CENTER);
    private final JProgressBar progressBar = new JProgressBar();
    private final JButton newGameButton = new JButton("New Game");
    private final JButton retrainButton = new JButton("Retrain");
    private final JButton selfPlayButton = new JButton("Self-Play Fast");

    private ConnectFourLearningExample.TrainingResult trainingResult;
    private SwingWorker<SelfPlaySummary, SelfPlaySnapshot> selfPlayWorker;
    private volatile boolean selfPlayStopRequested;
    private boolean gameOver = true;
    private int learnedGames;
    private int selfPlayGames;
    private int selfPlayRedWins;
    private int selfPlayYellowWins;
    private int selfPlayDraws;
    private int selfPlayUniqueTerminalBoards;
    private final Set<String> selfPlayTerminalBoards = new LinkedHashSet<>();

    private record SelfPlaySnapshot(
            int[] finalBoard,
            int games,
            int redWins,
            int yellowWins,
            int draws,
            int uniqueTerminalBoards,
            int observations,
            int prototypes) {
        private SelfPlaySnapshot {
            finalBoard = Arrays.copyOf(finalBoard, finalBoard.length);
        }
    }

    private record SelfPlaySummary(
            int games,
            int redWins,
            int yellowWins,
            int draws,
            Set<String> terminalBoards,
            int observations,
            int prototypes) {
        private SelfPlaySummary {
            terminalBoards = Set.copyOf(terminalBoards);
        }
    }

    private ConnectFourInteractiveDemo() {
        super("Hilbert Connect 4");
        configureWindow();
        startTraining();
    }

    /**
     * Launches the Connect 4 demo.
     *
     * @param args ignored
     */
    public static void main(final String[] args) {
        quietTrainingLogs();
        SwingUtilities.invokeLater(() -> {
            setSystemLookAndFeel();
            new ConnectFourInteractiveDemo().setVisible(true);
        });
    }

    private void configureWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout(0, 8));
        header.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, 16.0f));
        metricsLabel.setFont(metricsLabel.getFont().deriveFont(12.0f));
        progressBar.setIndeterminate(true);
        header.add(statusLabel, BorderLayout.NORTH);
        header.add(metricsLabel, BorderLayout.CENTER);
        header.add(progressBar, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        JPanel boardPanel = new JPanel(new BorderLayout(0, 8));
        boardPanel.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        JPanel columnPanel = new JPanel(new GridLayout(1, ConnectFourLearningExample.COLUMNS, 6, 0));
        for (int column = 0; column < columns.length; column++) {
            columns[column] = createColumnButton(column);
            columnPanel.add(columns[column]);
        }
        boardPanel.add(columnPanel, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(
                ConnectFourLearningExample.ROWS,
                ConnectFourLearningExample.COLUMNS,
                6,
                6));
        for (int row = 0; row < ConnectFourLearningExample.ROWS; row++) {
            for (int column = 0; column < ConnectFourLearningExample.COLUMNS; column++) {
                int index = ConnectFourLearningExample.index(row, column);
                cells[index] = createCellLabel();
                grid.add(cells[index]);
            }
        }
        boardPanel.add(grid, BorderLayout.CENTER);
        add(boardPanel, BorderLayout.CENTER);

        JPanel controls = new JPanel(new GridLayout(1, 3, 8, 0));
        controls.setBorder(BorderFactory.createEmptyBorder(8, 16, 16, 16));
        newGameButton.addActionListener(this::newGame);
        retrainButton.addActionListener(this::retrain);
        selfPlayButton.addActionListener(this::toggleSelfPlay);
        newGameButton.setEnabled(false);
        retrainButton.setEnabled(false);
        selfPlayButton.setEnabled(false);
        controls.add(newGameButton);
        controls.add(selfPlayButton);
        controls.add(retrainButton);
        add(controls, BorderLayout.SOUTH);

        setMinimumSize(new Dimension(620, 620));
        pack();
        setLocationRelativeTo(null);
        refreshBoard();
    }

    private JButton createColumnButton(final int column) {
        JButton button = new JButton(String.valueOf(column + 1));
        button.setFocusPainted(false);
        button.addActionListener(event -> playHumanMove(column));
        return button;
    }

    private JLabel createCellLabel() {
        JLabel label = new JLabel("", SwingConstants.CENTER);
        label.setOpaque(true);
        label.setPreferredSize(new Dimension(70, 70));
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        label.setBorder(BorderFactory.createLineBorder(new Color(58, 83, 125), 2));
        return label;
    }

    private void startTraining() {
        cancelSelfPlay();
        trainingResult = null;
        gameOver = true;
        learnedGames = 0;
        selfPlayGames = 0;
        selfPlayRedWins = 0;
        selfPlayYellowWins = 0;
        selfPlayDraws = 0;
        selfPlayUniqueTerminalBoards = 0;
        selfPlayTerminalBoards.clear();
        Arrays.fill(board, ConnectFourLearningExample.EMPTY);
        history.clear();
        refreshBoard();
        statusLabel.setText("Training Connect 4 model...");
        metricsLabel.setText("Learning rules, tactics, and bootstrap play.");
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        newGameButton.setEnabled(false);
        retrainButton.setEnabled(false);
        selfPlayButton.setEnabled(false);

        SwingWorker<ConnectFourLearningExample.TrainingResult, Void> worker =
                new SwingWorker<>() {
                    @Override
                    protected ConnectFourLearningExample.TrainingResult doInBackground() {
                        return ConnectFourLearningExample.trainPolicy();
                    }

                    @Override
                    protected void done() {
                        finishTraining(this);
                    }
                };
        worker.execute();
    }

    private void finishTraining(
            final SwingWorker<ConnectFourLearningExample.TrainingResult, Void> worker) {
        try {
            trainingResult = worker.get();
            progressBar.setVisible(false);
            newGameButton.setEnabled(true);
            retrainButton.setEnabled(true);
            selfPlayButton.setEnabled(true);
            resetBoard();
            metricsLabel.setText(metricsText());
            statusLabel.setText("Training complete. You are red. Choose a column.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            showTrainingFailure(e);
        } catch (ExecutionException e) {
            showTrainingFailure(e.getCause() == null ? e : e.getCause());
        }
    }

    private void showTrainingFailure(final Throwable throwable) {
        progressBar.setVisible(false);
        retrainButton.setEnabled(true);
        selfPlayButton.setEnabled(false);
        statusLabel.setText("Training failed.");
        metricsLabel.setText(throwable.getMessage() == null ? throwable.getClass().getSimpleName()
                : throwable.getMessage());
        JOptionPane.showMessageDialog(this, metricsLabel.getText(), "Training failed", JOptionPane.ERROR_MESSAGE);
    }

    private void newGame(final ActionEvent event) {
        resetBoard();
        statusLabel.setText("New game. You are red. Choose a column.");
    }

    private void retrain(final ActionEvent event) {
        startTraining();
    }

    private void toggleSelfPlay(final ActionEvent event) {
        if (isSelfPlayRunning()) {
            stopSelfPlay();
        } else {
            startSelfPlay();
        }
    }

    private void resetBoard() {
        board = new int[ConnectFourLearningExample.CELLS];
        history.clear();
        gameOver = false;
        refreshBoard();
    }

    private void playHumanMove(final int column) {
        if (trainingResult == null || gameOver || !isHumanTurn()
                || !ConnectFourLearningExample.isLegalMove(board, column)) {
            return;
        }
        applyMove(column);
        if (finishGameIfNeeded()) {
            return;
        }
        statusLabel.setText("AI thinking...");
        setColumnsEnabled(false);
        SwingUtilities.invokeLater(this::playAiMove);
    }

    private void playAiMove() {
        try {
            ConnectFourLearningExample.MovePrediction prediction =
                    ConnectFourLearningExample.predictMove(
                            trainingResult.learner(),
                            trainingResult.temporalLearner(),
                            board);
            applyMove(prediction.column());
            if (!finishGameIfNeeded()) {
                statusLabel.setText("AI played column " + (prediction.column() + 1) + ". Your turn.");
            }
        } catch (RuntimeException e) {
            gameOver = true;
            setColumnsEnabled(false);
            statusLabel.setText("AI could not choose a move.");
            metricsLabel.setText(e.getMessage());
        }
    }

    private void startSelfPlay() {
        if (trainingResult == null || isSelfPlayRunning()) {
            return;
        }
        gameOver = true;
        history.clear();
        refreshBoard();
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        statusLabel.setText("Self-play running fast...");
        newGameButton.setEnabled(false);
        retrainButton.setEnabled(false);
        selfPlayButton.setText("Stop Self-Play");
        setColumnsEnabled(false);

        int baseGames = selfPlayGames;
        int baseRedWins = selfPlayRedWins;
        int baseYellowWins = selfPlayYellowWins;
        int baseDraws = selfPlayDraws;
        Set<String> baseTerminalBoards = new LinkedHashSet<>(selfPlayTerminalBoards);
        selfPlayStopRequested = false;
        SwingWorker<SelfPlaySummary, SelfPlaySnapshot> worker =
                new SwingWorker<>() {
                    @Override
                    protected SelfPlaySummary doInBackground() {
                        Random random = new Random(System.nanoTime());
                        int games = 0;
                        int redWins = 0;
                        int yellowWins = 0;
                        int draws = 0;
                        Set<String> terminalBoards = new LinkedHashSet<>(baseTerminalBoards);
                        int observations = trainingResult.learner().observations();
                        int prototypes = trainingResult.learner().imitationMemory().prototypeCount();
                        ConnectFourLearningExample.SelfPlayResult lastResult = null;
                        while (!selfPlayStopRequested && !isCancelled()) {
                            ConnectFourLearningExample.SelfPlayResult result =
                                    ConnectFourLearningExample.selfPlayGame(
                                            trainingResult.learner(),
                                            trainingResult.temporalLearner(),
                                            SELF_PLAY_EXPLORATION_RATE,
                                            random);
                            lastResult = result;
                            games++;
                            terminalBoards.add(ConnectFourLearningExample.boardSignature(result.finalBoard()));
                            if (result.winner() == ConnectFourLearningExample.RED) {
                                redWins++;
                            } else if (result.winner() == ConnectFourLearningExample.YELLOW) {
                                yellowWins++;
                            } else {
                                draws++;
                            }
                            observations = result.learning().observations();
                            prototypes = result.learning().prototypeCount();
                            if (games % SELF_PLAY_UPDATE_INTERVAL == 0) {
                                publish(new SelfPlaySnapshot(
                                        result.finalBoard(),
                                        baseGames + games,
                                        baseRedWins + redWins,
                                        baseYellowWins + yellowWins,
                                        baseDraws + draws,
                                        terminalBoards.size(),
                                        observations,
                                        prototypes));
                            }
                        }
                        if (lastResult != null) {
                            publish(new SelfPlaySnapshot(
                                    lastResult.finalBoard(),
                                    baseGames + games,
                                    baseRedWins + redWins,
                                    baseYellowWins + yellowWins,
                                    baseDraws + draws,
                                    terminalBoards.size(),
                                    observations,
                                    prototypes));
                        }
                        return new SelfPlaySummary(
                                games,
                                redWins,
                                yellowWins,
                                draws,
                                terminalBoards,
                                observations,
                                prototypes);
                    }

                    @Override
                    protected void process(final List<SelfPlaySnapshot> chunks) {
                        updateSelfPlaySnapshot(chunks.get(chunks.size() - 1));
                    }

                    @Override
                    protected void done() {
                        finishSelfPlay(this, baseGames, baseRedWins, baseYellowWins, baseDraws);
                    }
                };
        selfPlayWorker = worker;
        worker.execute();
    }

    private void stopSelfPlay() {
        if (selfPlayWorker != null) {
            selfPlayStopRequested = true;
            statusLabel.setText("Stopping self-play...");
            selfPlayButton.setEnabled(false);
        }
    }

    private void cancelSelfPlay() {
        if (selfPlayWorker != null) {
            selfPlayStopRequested = true;
            selfPlayWorker.cancel(true);
            selfPlayWorker = null;
        }
    }

    private void updateSelfPlaySnapshot(final SelfPlaySnapshot snapshot) {
        board = Arrays.copyOf(snapshot.finalBoard(), snapshot.finalBoard().length);
        selfPlayGames = snapshot.games();
        selfPlayRedWins = snapshot.redWins();
        selfPlayYellowWins = snapshot.yellowWins();
        selfPlayDraws = snapshot.draws();
        selfPlayUniqueTerminalBoards = snapshot.uniqueTerminalBoards();
        metricsLabel.setText(metricsText());
        statusLabel.setText("Self-play running fast: " + selfPlayGames + " games.");
        refreshBoard();
    }

    private void finishSelfPlay(
            final SwingWorker<SelfPlaySummary, SelfPlaySnapshot> worker,
            final int baseGames,
            final int baseRedWins,
            final int baseYellowWins,
            final int baseDraws) {
        try {
            SelfPlaySummary summary = worker.get();
            selfPlayGames = baseGames + summary.games();
            selfPlayRedWins = baseRedWins + summary.redWins();
            selfPlayYellowWins = baseYellowWins + summary.yellowWins();
            selfPlayDraws = baseDraws + summary.draws();
            selfPlayTerminalBoards.clear();
            selfPlayTerminalBoards.addAll(summary.terminalBoards());
            selfPlayUniqueTerminalBoards = selfPlayTerminalBoards.size();
            statusLabel.setText((selfPlayStopRequested ? "Self-play stopped: " : "Self-play complete: ")
                    + selfPlayGames + " games.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            statusLabel.setText("Self-play interrupted.");
        } catch (CancellationException e) {
            statusLabel.setText("Self-play stopped: " + selfPlayGames + " games.");
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            statusLabel.setText("Self-play failed.");
            metricsLabel.setText(cause.getMessage());
        } finally {
            selfPlayWorker = null;
            progressBar.setVisible(false);
            selfPlayButton.setText("Self-Play Fast");
            selfPlayButton.setEnabled(trainingResult != null);
            newGameButton.setEnabled(trainingResult != null);
            retrainButton.setEnabled(trainingResult != null);
            selfPlayStopRequested = false;
            metricsLabel.setText(metricsText());
            refreshBoard();
        }
    }

    private void applyMove(final int column) {
        history.add(ConnectFourLearningExample.playedMove(board, column));
        board = ConnectFourLearningExample.playMove(board, column);
        refreshBoard();
    }

    private boolean finishGameIfNeeded() {
        int winner = ConnectFourLearningExample.winner(board);
        if (winner == HUMAN) {
            finishGame("You win.", winner);
            return true;
        }
        if (winner == AI) {
            finishGame("AI wins.", winner);
            return true;
        }
        if (ConnectFourLearningExample.isFull(board)) {
            finishGame("Draw.", ConnectFourLearningExample.EMPTY);
            return true;
        }
        refreshBoard();
        return false;
    }

    private void finishGame(final String message, final int winner) {
        gameOver = true;
        setColumnsEnabled(false);
        statusLabel.setText(message + " Learning from this game...");
        newGameButton.setEnabled(false);
        retrainButton.setEnabled(false);
        learnFromCompletedGame(winner, message);
    }

    private void learnFromCompletedGame(final int winner, final String finishedMessage) {
        List<ConnectFourLearningExample.PlayedMove> completedHistory = List.copyOf(history);
        SwingWorker<ConnectFourLearningExample.GameLearningResult, Void> worker =
                new SwingWorker<>() {
                    @Override
                    protected ConnectFourLearningExample.GameLearningResult doInBackground() {
                        return ConnectFourLearningExample.learnFromGame(
                                trainingResult.learner(),
                                trainingResult.temporalLearner(),
                                completedHistory,
                                winner);
                    }

                    @Override
                    protected void done() {
                        finishGameLearning(this, finishedMessage);
                    }
                };
        worker.execute();
    }

    private void finishGameLearning(
            final SwingWorker<ConnectFourLearningExample.GameLearningResult, Void> worker,
            final String finishedMessage) {
        try {
            ConnectFourLearningExample.GameLearningResult result = worker.get();
            learnedGames++;
            metricsLabel.setText(metricsText());
            statusLabel.setText(finishedMessage + " Reinforced "
                    + result.reinforcedMoves() + " moves.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            statusLabel.setText("Game finished, but learning was interrupted.");
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            statusLabel.setText("Game finished, but learning failed.");
            metricsLabel.setText(cause.getMessage());
        } finally {
            newGameButton.setEnabled(true);
            retrainButton.setEnabled(true);
            selfPlayButton.setEnabled(true);
            refreshBoard();
        }
    }

    private boolean isHumanTurn() {
        return ConnectFourLearningExample.currentPlayer(board) == HUMAN;
    }

    private void refreshBoard() {
        for (int row = 0; row < ConnectFourLearningExample.ROWS; row++) {
            for (int column = 0; column < ConnectFourLearningExample.COLUMNS; column++) {
                int index = ConnectFourLearningExample.index(row, column);
                JLabel label = cells[index];
                label.setText(mark(board[index]));
                label.setForeground(markForeground(board[index]));
                label.setBackground(markBackground(board[index]));
            }
        }
        boolean canPlay = trainingResult != null && !gameOver && !isSelfPlayRunning() && isHumanTurn();
        for (int column = 0; column < columns.length; column++) {
            columns[column].setEnabled(canPlay && ConnectFourLearningExample.isLegalMove(board, column));
        }
    }

    private void setColumnsEnabled(final boolean enabled) {
        for (int column = 0; column < columns.length; column++) {
            columns[column].setEnabled(enabled && ConnectFourLearningExample.isLegalMove(board, column));
        }
    }

    private String metricsText() {
        return String.format("Demos: %d | Obs: %d | Time: %d | Human: %d | Self: %d | Unique: %d | R/Y/D %d/%d/%d",
                trainingResult.demonstrations(),
                trainingResult.learner().observations(),
                trainingResult.temporalLearner().updates(),
                learnedGames,
                selfPlayGames,
                selfPlayUniqueTerminalBoards,
                selfPlayRedWins,
                selfPlayYellowWins,
                selfPlayDraws);
    }

    private boolean isSelfPlayRunning() {
        return selfPlayWorker != null && !selfPlayWorker.isDone();
    }

    private static String mark(final int cell) {
        if (cell == HUMAN) {
            return "R";
        }
        if (cell == AI) {
            return "Y";
        }
        return "";
    }

    private static Color markForeground(final int cell) {
        if (cell == HUMAN) {
            return Color.WHITE;
        }
        if (cell == AI) {
            return new Color(45, 45, 45);
        }
        return new Color(45, 45, 45);
    }

    private static Color markBackground(final int cell) {
        if (cell == HUMAN) {
            return new Color(197, 57, 57);
        }
        if (cell == AI) {
            return new Color(240, 205, 66);
        }
        return new Color(239, 242, 246);
    }

    private static void setSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // The default look and feel is fine when the system one is unavailable.
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void quietTrainingLogs() {
        try {
            Class<?> levelType = Class.forName("org.apache.logging.log4j.Level");
            Object warn = Enum.valueOf((Class<Enum>) levelType.asSubclass(Enum.class), "WARN");
            Class<?> configuratorType = Class.forName("org.apache.logging.log4j.core.config.Configurator");
            configuratorType.getMethod("setRootLevel", levelType).invoke(null, warn);
            configuratorType.getMethod("setAllLevels", String.class, levelType)
                    .invoke(null, "com.github.javachaos.javaneuralnetwork", warn);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            // Console logging is harmless when Log4j Core is not on the compile classpath.
        }
    }
}
