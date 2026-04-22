package com.github.javachaos.javaneuralnetwork.core;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.Arrays;
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
 * Small Swing front-end for playing against the trained tic-tac-toe learner.
 */
public final class TicTacToeInteractiveDemo extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final int HUMAN = TicTacToeLearningExample.X;
    private static final int AI = TicTacToeLearningExample.O;

    private final int[] board = new int[TicTacToeLearningExample.CELLS];
    private final JButton[] cells = new JButton[TicTacToeLearningExample.CELLS];
    private final JLabel statusLabel = new JLabel("Training tic-tac-toe model...", SwingConstants.CENTER);
    private final JLabel metricsLabel = new JLabel("Building Hilbert memory and neural transition model.",
            SwingConstants.CENTER);
    private final JProgressBar progressBar = new JProgressBar();
    private final JButton newGameButton = new JButton("New Game");
    private final JButton retrainButton = new JButton("Retrain");

    private TicTacToeLearningExample.TrainingResult trainingResult;
    private boolean gameOver;

    private TicTacToeInteractiveDemo() {
        super("Hilbert Tic-Tac-Toe");
        configureWindow();
        startTraining();
    }

    /**
     * Launches the interactive demo.
     *
     * @param args ignored
     */
    public static void main(final String[] args) {
        quietTrainingLogs();
        SwingUtilities.invokeLater(() -> {
            setSystemLookAndFeel();
            new TicTacToeInteractiveDemo().setVisible(true);
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

        JPanel boardPanel = new JPanel(new GridLayout(3, 3, 6, 6));
        boardPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        for (int i = 0; i < cells.length; i++) {
            cells[i] = createCellButton(i);
            boardPanel.add(cells[i]);
        }
        add(boardPanel, BorderLayout.CENTER);

        JPanel controls = new JPanel(new GridLayout(1, 2, 8, 0));
        controls.setBorder(BorderFactory.createEmptyBorder(8, 16, 16, 16));
        newGameButton.addActionListener(this::newGame);
        retrainButton.addActionListener(this::retrain);
        newGameButton.setEnabled(false);
        retrainButton.setEnabled(false);
        controls.add(newGameButton);
        controls.add(retrainButton);
        add(controls, BorderLayout.SOUTH);

        setMinimumSize(new Dimension(420, 500));
        pack();
        setLocationRelativeTo(null);
        refreshBoard();
    }

    private JButton createCellButton(final int index) {
        JButton button = new JButton();
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 52));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBackground(new Color(245, 247, 250));
        button.addActionListener(event -> playHumanMove(index));
        return button;
    }

    private void startTraining() {
        trainingResult = null;
        gameOver = true;
        Arrays.fill(board, TicTacToeLearningExample.EMPTY);
        refreshBoard();
        statusLabel.setText("Training tic-tac-toe model...");
        metricsLabel.setText("Building Hilbert memory and neural transition model.");
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        newGameButton.setEnabled(false);
        retrainButton.setEnabled(false);

        SwingWorker<TicTacToeLearningExample.TrainingResult, Void> worker =
                new SwingWorker<>() {
                    @Override
                    protected TicTacToeLearningExample.TrainingResult doInBackground() {
                        return TicTacToeLearningExample.trainPolicy();
                    }

                    @Override
                    protected void done() {
                        finishTraining(this);
                    }
                };
        worker.execute();
    }

    private void finishTraining(final SwingWorker<TicTacToeLearningExample.TrainingResult, Void> worker) {
        try {
            trainingResult = worker.get();
            progressBar.setVisible(false);
            newGameButton.setEnabled(true);
            retrainButton.setEnabled(true);
            resetBoard();
            metricsLabel.setText(metricsText(trainingResult));
            statusLabel.setText("Training complete. You are X. Pick a square.");
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
        statusLabel.setText("Training failed.");
        metricsLabel.setText(throwable.getMessage() == null ? throwable.getClass().getSimpleName()
                : throwable.getMessage());
        JOptionPane.showMessageDialog(this, metricsLabel.getText(), "Training failed", JOptionPane.ERROR_MESSAGE);
    }

    private void newGame(final ActionEvent event) {
        resetBoard();
        statusLabel.setText("New game. You are X. Pick a square.");
    }

    private void retrain(final ActionEvent event) {
        startTraining();
    }

    private void resetBoard() {
        Arrays.fill(board, TicTacToeLearningExample.EMPTY);
        gameOver = false;
        refreshBoard();
    }

    private void playHumanMove(final int index) {
        if (trainingResult == null || gameOver || !isHumanTurn()
                || !TicTacToeLearningExample.isLegalMove(board, index)) {
            return;
        }

        board[index] = HUMAN;
        refreshBoard();
        if (finishGameIfNeeded()) {
            return;
        }

        statusLabel.setText("AI thinking...");
        setBoardEnabled(false);
        SwingUtilities.invokeLater(this::playAiMove);
    }

    private void playAiMove() {
        try {
            TicTacToeLearningExample.MovePrediction prediction =
                    TicTacToeLearningExample.predictMove(trainingResult.learner(), board);
            board[prediction.move()] = AI;
            refreshBoard();
            if (!finishGameIfNeeded()) {
                statusLabel.setText("AI played square " + prediction.move() + ". Your turn.");
            }
        } catch (RuntimeException e) {
            gameOver = true;
            setBoardEnabled(false);
            statusLabel.setText("AI could not choose a move.");
            metricsLabel.setText(e.getMessage());
        }
    }

    private boolean finishGameIfNeeded() {
        int winner = TicTacToeLearningExample.winner(board);
        if (winner == HUMAN) {
            gameOver = true;
            setBoardEnabled(false);
            statusLabel.setText("You win.");
            return true;
        }
        if (winner == AI) {
            gameOver = true;
            setBoardEnabled(false);
            statusLabel.setText("AI wins.");
            return true;
        }
        if (TicTacToeLearningExample.isFull(board)) {
            gameOver = true;
            setBoardEnabled(false);
            statusLabel.setText("Draw.");
            return true;
        }
        refreshBoard();
        return false;
    }

    private boolean isHumanTurn() {
        return TicTacToeLearningExample.currentPlayer(board) == HUMAN;
    }

    private void refreshBoard() {
        boolean canPlay = trainingResult != null && !gameOver && isHumanTurn();
        for (int i = 0; i < cells.length; i++) {
            JButton button = cells[i];
            button.setText(mark(board[i]));
            button.setForeground(markColor(board[i]));
            button.setEnabled(canPlay && TicTacToeLearningExample.isLegalMove(board, i));
        }
    }

    private void setBoardEnabled(final boolean enabled) {
        for (int i = 0; i < cells.length; i++) {
            cells[i].setEnabled(enabled && TicTacToeLearningExample.isLegalMove(board, i));
        }
    }

    private static String metricsText(final TicTacToeLearningExample.TrainingResult result) {
        return String.format("Demos: %d | Prototypes: %d | Exact: %.1f%% | Legal: %.1f%%",
                result.demonstrations(),
                result.prototypeCount(),
                result.exactMatchRate() * 100.0,
                result.legalMoveRate() * 100.0);
    }

    private static String mark(final int cell) {
        if (cell == HUMAN) {
            return "X";
        }
        if (cell == AI) {
            return "O";
        }
        return "";
    }

    private static Color markColor(final int cell) {
        if (cell == HUMAN) {
            return new Color(38, 84, 166);
        }
        if (cell == AI) {
            return new Color(166, 64, 64);
        }
        return Color.DARK_GRAY;
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
