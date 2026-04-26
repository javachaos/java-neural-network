package com.github.javachaos.javaneuralnetwork.examples;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import com.github.javachaos.javaneuralnetwork.core.BackpropagationNetwork;
import com.github.javachaos.javaneuralnetwork.core.BackpropagationStateTransitionModel;
import com.github.javachaos.javaneuralnetwork.core.TransferFunctions;
import com.github.javachaos.javaneuralnetwork.shared.hilbert.HilbertVector;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.HilbertImitationLearner;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.NeuralImitationLearner;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.TemporalOperatorLearner;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.TemporalTransition;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.WorldState;

/**
 * Rule-aware Connect 4 learner that starts from simple demonstrations and keeps
 * learning from completed games.
 */
public final class ConnectFourLearningExample {

    public static final int ROWS = 6;
    public static final int COLUMNS = 7;
    public static final int CELLS = ROWS * COLUMNS;
    public static final int DIMENSION = CELLS + COLUMNS;
    public static final int EMPTY = 0;
    public static final int RED = 1;
    public static final int YELLOW = -1;

    private static final int DEFAULT_BOOTSTRAP_LIMIT = 700;
    private static final int BOOTSTRAP_PLAYOUTS = 80;
    private static final int WIN_REINFORCEMENT = 4;
    private static final int DRAW_REINFORCEMENT = 1;
    private static final double TEMPORAL_LEARNING_RATE = 0.18;
    private static final int TEMPORAL_MEMORY_LIMIT_PER_ACTION = 384;
    private static final double TEMPORAL_DISCOUNT = 0.92;
    private static final double TEMPORAL_POLICY_WEIGHT = 0.35;
    private static final int[] CENTER_ORDER = {3, 2, 4, 1, 5, 0, 6};
    private static final int[][] DIRECTIONS = {
            {0, 1},
            {1, 0},
            {1, 1},
            {1, -1}
    };

    private ConnectFourLearningExample() {
    }

    /**
     * One rule-generated training example.
     *
     * @param board board before the move
     * @param player side to move
     * @param column chosen target column
     * @param before encoded board state
     * @param after encoded column target
     */
    public record Demonstration(
            int[] board,
            int player,
            int column,
            WorldState before,
            WorldState after) {
        public Demonstration {
            board = Arrays.copyOf(board, board.length);
            requireBoard(board);
            requirePlayer(player);
            requireColumn(column);
            Objects.requireNonNull(before, "Before state cannot be null.");
            Objects.requireNonNull(after, "After state cannot be null.");
            if (before.dimension() != DIMENSION || after.dimension() != DIMENSION) {
                throw new IllegalArgumentException("Demonstration states must use the Connect 4 dimension.");
            }
        }
    }

    /**
     * One move selected by the learner.
     *
     * @param board board before the move
     * @param player side to move
     * @param column selected legal column
     * @param legal true when the selected column can be played
     * @param tactical true when a hard win/block rule selected the move
     * @param usedLearner true when the Hilbert/neural learner supplied scores
     * @param columnScores raw action scores for each column
     */
    public record MovePrediction(
            int[] board,
            int player,
            int column,
            boolean legal,
            boolean tactical,
            boolean usedLearner,
            double[] columnScores) {
        public MovePrediction {
            board = Arrays.copyOf(board, board.length);
            columnScores = Arrays.copyOf(columnScores, columnScores.length);
            requireBoard(board);
            requirePlayer(player);
            requireColumn(column);
            if (columnScores.length != COLUMNS) {
                throw new IllegalArgumentException("Column score vector must have seven columns.");
            }
        }
    }

    /**
     * Training summary for the bootstrapped learner.
     *
     * @param demonstrations number of generated demonstrations
     * @param prototypeCount learned prototype count
     * @param transitionCount learned prototype transition count
     * @param observations total learner observations
     * @param temporalUpdates learned temporal transition observations
     * @param legalMoves number of bootstrap boards where prediction was legal
     * @param legalMoveRate legal prediction rate on bootstrap boards
     * @param emptyBoard prediction for an empty board
     * @param winningMove prediction for an immediate win
     * @param blockingMove prediction for an immediate block
     * @param learner trainable hybrid learner
     * @param temporalLearner action-conditioned transition learner
     */
    public record TrainingResult(
            int demonstrations,
            int prototypeCount,
            int transitionCount,
            int observations,
            int temporalUpdates,
            int legalMoves,
            double legalMoveRate,
            MovePrediction emptyBoard,
            MovePrediction winningMove,
            MovePrediction blockingMove,
            NeuralImitationLearner learner,
            TemporalOperatorLearner temporalLearner) {
    }

    /**
     * One played game move suitable for post-game reinforcement.
     *
     * @param boardBefore board before the move
     * @param player side that moved
     * @param column played column
     * @param before encoded board state
     * @param after encoded column target
     */
    public record PlayedMove(
            int[] boardBefore,
            int player,
            int column,
            WorldState before,
            WorldState after) {
        public PlayedMove {
            boardBefore = Arrays.copyOf(boardBefore, boardBefore.length);
            requireBoard(boardBefore);
            requirePlayer(player);
            requireColumn(column);
            Objects.requireNonNull(before, "Before state cannot be null.");
            Objects.requireNonNull(after, "After state cannot be null.");
            if (!isLegalMove(boardBefore, column)) {
                throw new IllegalArgumentException("Played move must be legal for the board.");
            }
            if (player != currentPlayer(boardBefore)) {
                throw new IllegalArgumentException("Played move player does not match the board turn.");
            }
        }
    }

    /**
     * Summary of online learning from one completed game.
     *
     * @param reinforcedMoves number of moves selected for reinforcement
     * @param reinforcementObservations total online observations added
     * @param winner game winner, or {@link #EMPTY} for a draw
     * @param observations learner observation count after the update
     * @param prototypeCount learner prototype count after the update
     * @param transitionCount learner transition count after the update
     * @param temporalUpdates temporal transition observations after the update
     */
    public record GameLearningResult(
            int reinforcedMoves,
            int reinforcementObservations,
            int winner,
            int observations,
            int prototypeCount,
            int transitionCount,
            int temporalUpdates) {
    }

    /**
     * Result of one fast self-play game.
     *
     * @param finalBoard terminal board
     * @param winner winner, or {@link #EMPTY} for a draw
     * @param moves number of moves played
     * @param history move history before learning
     * @param learning online learning summary from the completed game
     */
    public record SelfPlayResult(
            int[] finalBoard,
            int winner,
            int moves,
            List<PlayedMove> history,
            GameLearningResult learning) {
        public SelfPlayResult {
            finalBoard = Arrays.copyOf(finalBoard, finalBoard.length);
            history = List.copyOf(history);
            requireBoard(finalBoard);
            requireOutcome(winner);
            Objects.requireNonNull(learning, "Learning result cannot be null.");
            if (moves <= 0) {
                throw new IllegalArgumentException("Self-play game must include at least one move.");
            }
            if (history.size() != moves) {
                throw new IllegalArgumentException("Move count must match self-play history.");
            }
            if (ConnectFourLearningExample.winner(finalBoard) == EMPTY && !isFull(finalBoard)) {
                throw new IllegalArgumentException("Self-play result must end on a terminal board.");
            }
        }
    }

    /**
     * Trains the default Connect 4 bootstrap policy.
     *
     * @return training summary and trainable learner
     */
    public static TrainingResult trainPolicy() {
        return trainPolicy(DEFAULT_BOOTSTRAP_LIMIT);
    }

    /**
     * Trains a Connect 4 bootstrap policy with a bounded number of examples.
     *
     * @param maxDemonstrations maximum bootstrap demonstrations
     * @return training summary and trainable learner
     */
    public static TrainingResult trainPolicy(final int maxDemonstrations) {
        List<Demonstration> demonstrations = bootstrapDemonstrations(maxDemonstrations);
        BackpropagationStateTransitionModel neuralModel = BackpropagationStateTransitionModel.of(
                "connect-four-neural-policy",
                new BackpropagationNetwork(
                        new int[]{DIMENSION, 56, DIMENSION},
                        new TransferFunctions.TransferFunction[]{
                                TransferFunctions.TransferFunction.NONE,
                                TransferFunctions.TransferFunction.SIGMOID,
                                TransferFunctions.TransferFunction.SIGMOID
                        }),
                DIMENSION,
                DIMENSION);
        HilbertImitationLearner memory =
                HilbertImitationLearner.of("connect-four-memory", DIMENSION, 1.0, 0.0);
        TemporalOperatorLearner temporalLearner = TemporalOperatorLearner.of(
                "connect-four-time",
                DIMENSION,
                TEMPORAL_LEARNING_RATE,
                TEMPORAL_MEMORY_LIMIT_PER_ACTION);
        NeuralImitationLearner learner = NeuralImitationLearner.of(
                "connect-four-policy",
                memory,
                neuralModel,
                (predictionName, before, observed) ->
                        neuralModel.observe(predictionName, before, observed, 0.15, 0.0),
                0.08);

        for (Demonstration demonstration : demonstrations) {
            learner.observe(demonstration.before(), demonstration.after());
            learnTemporalMove(
                    temporalLearner,
                    demonstration.board(),
                    demonstration.column(),
                    immediateTemporalReward(demonstration.board(), demonstration.column()),
                    isTerminalAfterMove(demonstration.board(), demonstration.column()));
        }

        int legalMoves = 0;
        for (Demonstration demonstration : demonstrations) {
            if (predictMove(learner, demonstration.board()).legal()) {
                legalMoves++;
            }
        }

        MovePrediction emptyBoard = predictMove(learner, emptyBoard());
        MovePrediction winningMove = predictMove(learner, immediateWinningBoard());
        MovePrediction blockingMove = predictMove(learner, immediateBlockingBoard());

        return new TrainingResult(
                demonstrations.size(),
                memory.prototypeCount(),
                memory.transitionCount(),
                learner.observations(),
                temporalLearner.updates(),
                legalMoves,
                legalMoves / (double) demonstrations.size(),
                emptyBoard,
                winningMove,
                blockingMove,
                learner,
                temporalLearner);
    }

    /**
     * Generates a compact rule-based bootstrap corpus.
     *
     * @param maxDemonstrations maximum number of examples
     * @return demonstrations in deterministic order
     */
    public static List<Demonstration> bootstrapDemonstrations(final int maxDemonstrations) {
        if (maxDemonstrations <= 0) {
            throw new IllegalArgumentException("Demonstration limit must be positive.");
        }
        LinkedHashMap<String, Demonstration> demonstrations = new LinkedHashMap<>();
        for (int[] board : craftedBoards()) {
            addDemonstration(demonstrations, board, maxDemonstrations);
        }

        Random random = new Random(20260422L);
        for (int game = 0; game < BOOTSTRAP_PLAYOUTS && demonstrations.size() < maxDemonstrations; game++) {
            int[] board = emptyBoard();
            while (winner(board) == EMPTY && !isFull(board) && demonstrations.size() < maxDemonstrations) {
                addDemonstration(demonstrations, board, maxDemonstrations);
                int column = playoutColumn(board, random);
                board = playMove(board, column);
            }
        }
        return List.copyOf(demonstrations.values());
    }

    /**
     * Predicts a legal Connect 4 column. Immediate wins and blocks are enforced
     * as rules; the learner chooses among the remaining legal moves.
     *
     * @param learner trainable learner
     * @param board board before the move
     * @return selected move and scores
     */
    public static MovePrediction predictMove(
            final NeuralImitationLearner learner,
            final int[] board) {
        return predictMove(learner, null, board);
    }

    /**
     * Predicts a legal Connect 4 column with optional temporal action memory.
     *
     * @param learner trainable learner
     * @param temporalLearner action-conditioned transition learner, or null
     * @param board board before the move
     * @return selected move and scores
     */
    public static MovePrediction predictMove(
            final NeuralImitationLearner learner,
            final TemporalOperatorLearner temporalLearner,
            final int[] board) {
        Objects.requireNonNull(learner, "Learner cannot be null.");
        requirePlayableBoard(board);
        int player = currentPlayer(board);
        double[] scores = new double[COLUMNS];
        boolean usedLearner = false;
        WorldState query = boardState("query", board);
        if (learner.canPredict(query)) {
            scores = actionScores(learner.predict(query));
            usedLearner = true;
        }

        int tacticalColumn = immediateWinningColumn(board, player);
        if (tacticalColumn < 0) {
            tacticalColumn = immediateWinningColumn(board, -player);
        }
        if (tacticalColumn >= 0) {
            return new MovePrediction(board, player, tacticalColumn,
                    isLegalMove(board, tacticalColumn), true, usedLearner, scores);
        }

        int selectedColumn = temporalLearner == null
                ? (usedLearner ? chooseLegalColumn(board, scores) : heuristicMove(board))
                : chooseTemporalColumn(temporalLearner, board, scores, usedLearner);
        return new MovePrediction(board, player, selectedColumn,
                isLegalMove(board, selectedColumn), false, usedLearner, scores);
    }

    /**
     * Builds a post-game move record for online reinforcement.
     *
     * @param boardBefore board before the move
     * @param column played column
     * @return encoded move record
     */
    public static PlayedMove playedMove(final int[] boardBefore, final int column) {
        requirePlayableBoard(boardBefore);
        if (!isLegalMove(boardBefore, column)) {
            throw new IllegalArgumentException("Column cannot be played.");
        }
        int player = currentPlayer(boardBefore);
        return new PlayedMove(
                boardBefore,
                player,
                column,
                boardState("played board " + boardKey(boardBefore), boardBefore),
                columnState("played column " + column, column));
    }

    /**
     * Learns from a completed game. The winning side's moves are reinforced; a
     * draw reinforces both sides lightly. Because board encoding is relative to
     * the side to move, strong human moves become useful AI examples too.
     *
     * @param learner learner to update
     * @param history played moves in game order
     * @param winner winner, or {@link #EMPTY} for a draw
     * @return online learning summary
     */
    public static GameLearningResult learnFromGame(
            final NeuralImitationLearner learner,
            final List<PlayedMove> history,
            final int winner) {
        return learnFromGame(learner, null, history, winner);
    }

    /**
     * Learns from a completed game and also updates action-conditioned temporal
     * dynamics from every observed board transition.
     *
     * @param learner learner to update
     * @param temporalLearner temporal learner to update, or null
     * @param history played moves in game order
     * @param winner winner, or {@link #EMPTY} for a draw
     * @return online learning summary
     */
    public static GameLearningResult learnFromGame(
            final NeuralImitationLearner learner,
            final TemporalOperatorLearner temporalLearner,
            final List<PlayedMove> history,
            final int winner) {
        Objects.requireNonNull(learner, "Learner cannot be null.");
        Objects.requireNonNull(history, "Game history cannot be null.");
        requireOutcome(winner);

        int reinforcedMoves = 0;
        int reinforcementObservations = 0;
        for (PlayedMove move : history) {
            Objects.requireNonNull(move, "Played move cannot be null.");
            if (winner != EMPTY && move.player() != winner) {
                continue;
            }
            int repetitions = winner == EMPTY ? DRAW_REINFORCEMENT : WIN_REINFORCEMENT;
            reinforcedMoves++;
            for (int i = 0; i < repetitions; i++) {
                learner.observe(move.before(), move.after());
                reinforcementObservations++;
            }
        }
        if (temporalLearner != null) {
            learnTemporalFromGame(temporalLearner, history, winner);
        }

        return new GameLearningResult(
                reinforcedMoves,
                reinforcementObservations,
                winner,
                learner.observations(),
                learner.imitationMemory().prototypeCount(),
                learner.imitationMemory().transitionCount(),
                temporalLearner == null ? 0 : temporalLearner.updates());
    }

    private static int learnTemporalFromGame(
            final TemporalOperatorLearner temporalLearner,
            final List<PlayedMove> history,
            final int winner) {
        int updates = 0;
        for (int i = 0; i < history.size(); i++) {
            PlayedMove move = Objects.requireNonNull(history.get(i), "Played move cannot be null.");
            double reward = temporalReward(move.player(), winner, history.size() - i - 1);
            learnTemporalMove(temporalLearner, move.boardBefore(), move.column(), reward, i == history.size() - 1);
            updates++;
        }
        return updates;
    }

    private static void learnTemporalMove(
            final TemporalOperatorLearner temporalLearner,
            final int[] board,
            final int column,
            final double reward,
            final boolean terminal) {
        int[] nextBoard = playMove(board, column);
        temporalLearner.learn(new TemporalTransition(
                "connect-four " + boardKey(board) + " " + columnAction(column),
                boardState("before " + boardKey(board), board),
                columnAction(column),
                boardState("after " + boardKey(nextBoard), nextBoard),
                reward,
                terminal));
    }

    private static double immediateTemporalReward(final int[] board, final int column) {
        int player = currentPlayer(board);
        int[] nextBoard = playMove(board, column);
        if (winner(nextBoard) == player) {
            return 1.0;
        }
        return 0.0;
    }

    private static boolean isTerminalAfterMove(final int[] board, final int column) {
        int[] nextBoard = playMove(board, column);
        return winner(nextBoard) != EMPTY || isFull(nextBoard);
    }

    private static double temporalReward(
            final int player,
            final int winner,
            final int distanceFromTerminal) {
        requirePlayer(player);
        requireOutcome(winner);
        if (winner == EMPTY) {
            return 0.0;
        }
        double outcome = winner == player ? 1.0 : -1.0;
        return outcome * Math.pow(TEMPORAL_DISCOUNT, distanceFromTerminal);
    }

    /**
     * Plays one complete learner-vs-learner game at full speed and learns from
     * the result.
     *
     * @param learner learner that controls both sides and receives reinforcement
     * @param explorationRate chance to pick a random legal non-tactical move
     * @param random randomness source for exploration
     * @return terminal game and learning summary
     */
    public static SelfPlayResult selfPlayGame(
            final NeuralImitationLearner learner,
            final double explorationRate,
            final Random random) {
        return selfPlayGame(learner, null, explorationRate, random);
    }

    /**
     * Plays one complete learner-vs-learner game at full speed and learns from
     * both the final result and every temporal board transition.
     *
     * @param learner learner that controls both sides and receives reinforcement
     * @param temporalLearner temporal learner that receives transition updates, or null
     * @param explorationRate chance to pick a random legal non-tactical move
     * @param random randomness source for exploration
     * @return terminal game and learning summary
     */
    public static SelfPlayResult selfPlayGame(
            final NeuralImitationLearner learner,
            final TemporalOperatorLearner temporalLearner,
            final double explorationRate,
            final Random random) {
        Objects.requireNonNull(learner, "Learner cannot be null.");
        requireProbability(explorationRate, "Exploration rate");
        Objects.requireNonNull(random, "Random cannot be null.");

        int[] board = emptyBoard();
        List<PlayedMove> history = new ArrayList<>();
        while (winner(board) == EMPTY && !isFull(board)) {
            int column = selfPlayColumn(learner, temporalLearner, board, explorationRate, random);
            history.add(playedMove(board, column));
            board = playMove(board, column);
        }

        int outcome = winner(board);
        GameLearningResult learning = learnFromGame(learner, temporalLearner, history, outcome);
        return new SelfPlayResult(board, outcome, history.size(), history, learning);
    }

    /**
     * A small rule policy used only for bootstrapping and fallback.
     *
     * @param board board before the move
     * @return preferred legal column
     */
    public static int heuristicMove(final int[] board) {
        requirePlayableBoard(board);
        int player = currentPlayer(board);
        int win = immediateWinningColumn(board, player);
        if (win >= 0) {
            return win;
        }
        int block = immediateWinningColumn(board, -player);
        if (block >= 0) {
            return block;
        }

        int bestSafeColumn = -1;
        double bestSafeScore = Double.NEGATIVE_INFINITY;
        int bestColumn = -1;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (int column : CENTER_ORDER) {
            if (!isLegalMove(board, column)) {
                continue;
            }
            double score = moveScore(board, column, player);
            if (score > bestScore) {
                bestScore = score;
                bestColumn = column;
            }
            if (!allowsImmediateOpponentWin(board, column, player) && score > bestSafeScore) {
                bestSafeScore = score;
                bestSafeColumn = column;
            }
        }
        if (bestSafeColumn >= 0) {
            return bestSafeColumn;
        }
        if (bestColumn >= 0) {
            return bestColumn;
        }
        throw new IllegalArgumentException("Board has no legal moves.");
    }

    /**
     * Applies gravity and returns the board after the current player moves.
     *
     * @param board current board
     * @param column column to play
     * @return next board
     */
    public static int[] playMove(final int[] board, final int column) {
        requirePlayableBoard(board);
        if (!isLegalMove(board, column)) {
            throw new IllegalArgumentException("Column cannot be played.");
        }
        return dropPiece(board, column, currentPlayer(board));
    }

    /**
     * Lists legal columns in center-first order.
     *
     * @param board current board
     * @return playable columns
     */
    public static List<Integer> legalColumns(final int[] board) {
        requireBoard(board);
        if (winner(board) != EMPTY || isFull(board)) {
            return List.of();
        }
        List<Integer> columns = new ArrayList<>();
        for (int column : CENTER_ORDER) {
            if (isLegalMove(board, column)) {
                columns.add(column);
            }
        }
        return columns;
    }

    /**
     * Checks whether a column accepts another piece.
     *
     * @param board current board
     * @param column candidate column
     * @return true when the move is legal
     */
    public static boolean isLegalMove(final int[] board, final int column) {
        requireBoard(board);
        return column >= 0
                && column < COLUMNS
                && winner(board) == EMPTY
                && !isFull(board)
                && board[index(0, column)] == EMPTY;
    }

    /**
     * Finds the row where a move would land.
     *
     * @param board current board
     * @param column candidate column
     * @return row index, or -1 when the column is full
     */
    public static int rowForMove(final int[] board, final int column) {
        requireBoard(board);
        requireColumn(column);
        for (int row = ROWS - 1; row >= 0; row--) {
            if (board[index(row, column)] == EMPTY) {
                return row;
            }
        }
        return -1;
    }

    /**
     * Computes the side to move from piece counts.
     *
     * @param board current board
     * @return {@link #RED} first, then alternating with {@link #YELLOW}
     */
    public static int currentPlayer(final int[] board) {
        requireBoard(board);
        int redCount = 0;
        int yellowCount = 0;
        for (int cell : board) {
            if (cell == RED) {
                redCount++;
            } else if (cell == YELLOW) {
                yellowCount++;
            }
        }
        if (redCount == yellowCount) {
            return RED;
        }
        if (redCount == yellowCount + 1) {
            return YELLOW;
        }
        throw new IllegalArgumentException("Board does not have a legal side to move.");
    }

    /**
     * Returns the board winner.
     *
     * @param board current board
     * @return {@link #RED}, {@link #YELLOW}, or {@link #EMPTY}
     */
    public static int winner(final int[] board) {
        requireBoard(board);
        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                int cell = board[index(row, column)];
                if (cell == EMPTY) {
                    continue;
                }
                for (int[] direction : DIRECTIONS) {
                    if (countLine(board, row, column, direction[0], direction[1], cell) >= 4) {
                        return cell;
                    }
                }
            }
        }
        return EMPTY;
    }

    /**
     * Checks whether the board is full.
     *
     * @param board current board
     * @return true when no empty cell remains
     */
    public static boolean isFull(final int[] board) {
        requireBoard(board);
        for (int column = 0; column < COLUMNS; column++) {
            if (board[index(0, column)] == EMPTY) {
                return false;
            }
        }
        return true;
    }

    /**
     * Converts row/column coordinates to the board array index.
     *
     * @param row row index
     * @param column column index
     * @return row-major index
     */
    public static int index(final int row, final int column) {
        if (row < 0 || row >= ROWS || column < 0 || column >= COLUMNS) {
            throw new IllegalArgumentException("Board coordinate is outside the Connect 4 grid.");
        }
        return row * COLUMNS + column;
    }

    /**
     * Produces a stable compact board signature for diversity tracking.
     *
     * @param board board to encode
     * @return row-major board signature
     */
    public static String boardSignature(final int[] board) {
        requireBoard(board);
        return boardKey(board);
    }

    /**
     * Runs the Connect 4 example and prints bootstrap metrics.
     *
     * @param args ignored
     */
    public static void main(final String[] args) {
        TrainingResult result = trainPolicy();
        System.out.println("Connect 4 bootstrap demonstrations: " + result.demonstrations());
        System.out.println("Prototype count: " + result.prototypeCount());
        System.out.println("Transition count: " + result.transitionCount());
        System.out.println("Legal move rate: " + result.legalMoveRate());
        System.out.println("Empty board column: " + result.emptyBoard().column());
        System.out.println("Winning column: " + result.winningMove().column());
        System.out.println("Blocking column: " + result.blockingMove().column());
    }

    private static boolean addDemonstration(
            final Map<String, Demonstration> demonstrations,
            final int[] board,
            final int limit) {
        if (demonstrations.size() >= limit || winner(board) != EMPTY || isFull(board)) {
            return false;
        }
        int column = heuristicMove(board);
        Demonstration demonstration = demonstration(board, column);
        demonstrations.putIfAbsent(boardKey(board), demonstration);
        return demonstrations.size() < limit;
    }

    private static Demonstration demonstration(final int[] board, final int column) {
        int player = currentPlayer(board);
        return new Demonstration(
                board,
                player,
                column,
                boardState("board " + boardKey(board), board),
                columnState("column " + column, column));
    }

    private static int playoutColumn(final int[] board, final Random random) {
        int player = currentPlayer(board);
        int win = immediateWinningColumn(board, player);
        if (win >= 0) {
            return win;
        }
        int block = immediateWinningColumn(board, -player);
        if (block >= 0) {
            return block;
        }
        if (random.nextDouble() < 0.35) {
            return heuristicMove(board);
        }
        List<Integer> legal = legalColumns(board);
        return legal.get(random.nextInt(legal.size()));
    }

    private static int selfPlayColumn(
            final NeuralImitationLearner learner,
            final TemporalOperatorLearner temporalLearner,
            final int[] board,
            final double explorationRate,
            final Random random) {
        MovePrediction prediction = predictMove(learner, temporalLearner, board);
        if (prediction.tactical() || random.nextDouble() >= explorationRate) {
            return prediction.column();
        }
        List<Integer> legal = legalColumns(board);
        return legal.get(random.nextInt(legal.size()));
    }

    private static int chooseTemporalColumn(
            final TemporalOperatorLearner temporalLearner,
            final int[] board,
            final double[] policyScores,
            final boolean hasPolicyScores) {
        Objects.requireNonNull(temporalLearner, "Temporal learner cannot be null.");
        List<Integer> legalColumns = legalColumns(board);
        if (legalColumns.isEmpty()) {
            throw new IllegalArgumentException("Board has no legal moves.");
        }
        if (temporalLearner.updates() == 0) {
            return hasPolicyScores ? chooseLegalColumn(board, policyScores) : heuristicMove(board);
        }

        WorldState state = boardState("temporal query " + boardKey(board), board);
        List<TemporalOperatorLearner.ActionScore> ranked =
                temporalLearner.rankActions(state, legalColumnActions(board));
        int bestColumn = -1;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (TemporalOperatorLearner.ActionScore temporalScore : ranked) {
            int column = columnFromAction(temporalScore.action());
            double policyScore = hasPolicyScores && Double.isFinite(policyScores[column])
                    ? policyScores[column]
                    : moveScore(board, column, currentPlayer(board));
            double score = policyScore + TEMPORAL_POLICY_WEIGHT * temporalScore.score();
            if (score > bestScore) {
                bestScore = score;
                bestColumn = column;
            }
        }
        if (bestColumn >= 0) {
            return bestColumn;
        }
        return hasPolicyScores ? chooseLegalColumn(board, policyScores) : heuristicMove(board);
    }

    private static int immediateWinningColumn(final int[] board, final int player) {
        requirePlayer(player);
        for (int column : CENTER_ORDER) {
            if (!isLegalMove(board, column)) {
                continue;
            }
            int[] next = dropPiece(board, column, player);
            if (winner(next) == player) {
                return column;
            }
        }
        return -1;
    }

    private static boolean allowsImmediateOpponentWin(
            final int[] board,
            final int column,
            final int player) {
        int[] next = dropPiece(board, column, player);
        if (winner(next) == player) {
            return false;
        }
        return immediateWinningColumn(next, -player) >= 0;
    }

    private static double moveScore(final int[] board, final int column, final int player) {
        int[] next = dropPiece(board, column, player);
        double centerScore = COLUMNS - Math.abs(column - COLUMNS / 2.0);
        return centerScore
                + linePotential(next, player) * 3.0
                - linePotential(next, -player) * 1.4;
    }

    private static double linePotential(final int[] board, final int player) {
        double score = 0.0;
        int opponent = -player;
        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                for (int[] direction : DIRECTIONS) {
                    if (!windowInBounds(row, column, direction[0], direction[1])) {
                        continue;
                    }
                    int playerCount = 0;
                    int opponentCount = 0;
                    for (int i = 0; i < 4; i++) {
                        int cell = board[index(row + direction[0] * i, column + direction[1] * i)];
                        if (cell == player) {
                            playerCount++;
                        } else if (cell == opponent) {
                            opponentCount++;
                        }
                    }
                    if (opponentCount == 0 && playerCount > 0) {
                        score += switch (playerCount) {
                            case 1 -> 1.0;
                            case 2 -> 8.0;
                            case 3 -> 45.0;
                            default -> 1000.0;
                        };
                    }
                }
            }
        }
        return score;
    }

    private static boolean windowInBounds(
            final int row,
            final int column,
            final int rowStep,
            final int columnStep) {
        int endRow = row + rowStep * 3;
        int endColumn = column + columnStep * 3;
        return endRow >= 0 && endRow < ROWS && endColumn >= 0 && endColumn < COLUMNS;
    }

    private static int countLine(
            final int[] board,
            final int row,
            final int column,
            final int rowStep,
            final int columnStep,
            final int player) {
        int count = 0;
        int currentRow = row;
        int currentColumn = column;
        while (currentRow >= 0
                && currentRow < ROWS
                && currentColumn >= 0
                && currentColumn < COLUMNS
                && board[index(currentRow, currentColumn)] == player) {
            count++;
            currentRow += rowStep;
            currentColumn += columnStep;
        }
        return count;
    }

    private static int[] dropPiece(final int[] board, final int column, final int player) {
        requireBoard(board);
        requireColumn(column);
        requirePlayer(player);
        int row = rowForMove(board, column);
        if (row < 0) {
            throw new IllegalArgumentException("Column is full.");
        }
        int[] next = Arrays.copyOf(board, board.length);
        next[index(row, column)] = player;
        return next;
    }

    private static int chooseLegalColumn(final int[] board, final double[] scores) {
        if (scores.length != COLUMNS) {
            throw new IllegalArgumentException("Column score vector must have seven entries.");
        }
        int bestColumn = -1;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (int column : CENTER_ORDER) {
            if (!isLegalMove(board, column)) {
                continue;
            }
            double score = Double.isFinite(scores[column]) ? scores[column] : Double.NEGATIVE_INFINITY;
            if (score > bestScore) {
                bestScore = score;
                bestColumn = column;
            }
        }
        if (bestColumn < 0) {
            throw new IllegalArgumentException("Board has no legal moves.");
        }
        return bestColumn;
    }

    private static WorldState boardState(final String name, final int[] board) {
        int player = currentPlayer(board);
        double[] values = new double[DIMENSION];
        for (int i = 0; i < CELLS; i++) {
            values[i] = board[i] == EMPTY ? 0.0 : (board[i] == player ? 1.0 : -1.0);
        }
        return WorldState.of(name, HilbertVector.of(values));
    }

    private static WorldState columnState(final String name, final int column) {
        requireColumn(column);
        double[] values = new double[DIMENSION];
        values[CELLS + column] = 1.0;
        return WorldState.of(name, HilbertVector.of(values));
    }

    private static List<String> legalColumnActions(final int[] board) {
        List<Integer> columns = legalColumns(board);
        List<String> actions = new ArrayList<>(columns.size());
        for (int column : columns) {
            actions.add(columnAction(column));
        }
        return actions;
    }

    private static String columnAction(final int column) {
        requireColumn(column);
        return "column-" + column;
    }

    private static int columnFromAction(final String action) {
        Objects.requireNonNull(action, "Action cannot be null.");
        String prefix = "column-";
        if (!action.startsWith(prefix)) {
            throw new IllegalArgumentException("Connect 4 action is not a column action.");
        }
        try {
            int column = Integer.parseInt(action.substring(prefix.length()));
            requireColumn(column);
            return column;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Connect 4 action has an invalid column.", e);
        }
    }

    private static double[] actionScores(final WorldState prediction) {
        double[] values = prediction.state().toArray();
        return Arrays.copyOfRange(values, CELLS, DIMENSION);
    }

    private static List<int[]> craftedBoards() {
        List<int[]> boards = new ArrayList<>();
        boards.add(emptyBoard());
        boards.add(immediateWinningBoard());
        boards.add(immediateBlockingBoard());
        boards.add(verticalWinningBoard());
        boards.add(centerFightBoard());
        return boards;
    }

    private static int[] immediateWinningBoard() {
        int[] board = emptyBoard();
        board[index(ROWS - 1, 0)] = RED;
        board[index(ROWS - 1, 1)] = RED;
        board[index(ROWS - 1, 2)] = RED;
        board[index(ROWS - 1, 4)] = YELLOW;
        board[index(ROWS - 1, 5)] = YELLOW;
        board[index(ROWS - 1, 6)] = YELLOW;
        return board;
    }

    private static int[] immediateBlockingBoard() {
        int[] board = emptyBoard();
        board[index(ROWS - 1, 0)] = YELLOW;
        board[index(ROWS - 1, 1)] = YELLOW;
        board[index(ROWS - 1, 2)] = YELLOW;
        board[index(ROWS - 1, 4)] = RED;
        board[index(ROWS - 2, 4)] = RED;
        board[index(ROWS - 1, 5)] = RED;
        return board;
    }

    private static int[] verticalWinningBoard() {
        int[] board = emptyBoard();
        board[index(ROWS - 1, 0)] = RED;
        board[index(ROWS - 2, 0)] = RED;
        board[index(ROWS - 3, 0)] = RED;
        board[index(ROWS - 1, 1)] = YELLOW;
        board[index(ROWS - 1, 2)] = YELLOW;
        board[index(ROWS - 1, 3)] = YELLOW;
        return board;
    }

    private static int[] centerFightBoard() {
        int[] board = emptyBoard();
        board = playMove(board, 3);
        board = playMove(board, 3);
        board = playMove(board, 2);
        board = playMove(board, 4);
        return board;
    }

    private static int[] emptyBoard() {
        return new int[CELLS];
    }

    private static void requirePlayableBoard(final int[] board) {
        requireBoard(board);
        if (winner(board) != EMPTY || isFull(board)) {
            throw new IllegalArgumentException("Board has no playable moves.");
        }
        currentPlayer(board);
    }

    private static void requireBoard(final int[] board) {
        Objects.requireNonNull(board, "Board cannot be null.");
        if (board.length != CELLS) {
            throw new IllegalArgumentException("Connect 4 board must have 42 cells.");
        }
        for (int cell : board) {
            if (cell != RED && cell != YELLOW && cell != EMPTY) {
                throw new IllegalArgumentException("Board cells must be red, yellow, or empty.");
            }
        }
    }

    private static void requireColumn(final int column) {
        if (column < 0 || column >= COLUMNS) {
            throw new IllegalArgumentException("Column is outside the Connect 4 board.");
        }
    }

    private static void requirePlayer(final int player) {
        if (player != RED && player != YELLOW) {
            throw new IllegalArgumentException("Player must be red or yellow.");
        }
    }

    private static void requireOutcome(final int winner) {
        if (winner != RED && winner != YELLOW && winner != EMPTY) {
            throw new IllegalArgumentException("Winner must be red, yellow, or empty for draw.");
        }
    }

    private static void requireProbability(final double value, final String label) {
        if (!Double.isFinite(value) || value < 0.0 || value > 1.0) {
            throw new IllegalArgumentException(label + " must be finite and in [0, 1].");
        }
    }

    private static String boardKey(final int[] board) {
        StringBuilder key = new StringBuilder(board.length);
        for (int cell : board) {
            key.append(switch (cell) {
                case RED -> 'R';
                case YELLOW -> 'Y';
                default -> '.';
            });
        }
        return key.toString();
    }
}
