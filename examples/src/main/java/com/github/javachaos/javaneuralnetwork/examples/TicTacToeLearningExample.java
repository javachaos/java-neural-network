package com.github.javachaos.javaneuralnetwork.examples;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.github.javachaos.javaneuralnetwork.core.BackpropagationNetwork;
import com.github.javachaos.javaneuralnetwork.core.BackpropagationStateTransitionModel;
import com.github.javachaos.javaneuralnetwork.core.TransferFunctions;
import com.github.javachaos.javaneuralnetwork.shared.hilbert.HilbertVector;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.HilbertImitationLearner;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.NeuralImitationLearner;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.WorldState;

/**
 * Trains the Hilbert/neural imitation loop to play tic-tac-toe from expert
 * demonstrations.
 */
public final class TicTacToeLearningExample {

    public static final int CELLS = 9;
    public static final int DIMENSION = CELLS * 2;
    public static final int EMPTY = 0;
    public static final int X = 1;
    public static final int O = -1;
    private static final int[] MOVE_ORDER = {4, 0, 2, 6, 8, 1, 3, 5, 7};
    private static final int[][] LINES = {
            {0, 1, 2},
            {3, 4, 5},
            {6, 7, 8},
            {0, 3, 6},
            {1, 4, 7},
            {2, 5, 8},
            {0, 4, 8},
            {2, 4, 6}
    };

    private TicTacToeLearningExample() {
    }

    /**
     * One generated expert policy example.
     *
     * @param board absolute board values, X=1, O=-1, empty=0
     * @param player player to move
     * @param expertMove minimax move index
     * @param before encoded board state
     * @param after encoded target move state
     */
    public record Demonstration(
            int[] board,
            int player,
            int expertMove,
            WorldState before,
            WorldState after) {
        public Demonstration {
            board = Arrays.copyOf(board, board.length);
            Objects.requireNonNull(before, "Before state cannot be null.");
            Objects.requireNonNull(after, "After state cannot be null.");
        }
    }

    /**
     * One move chosen by the trained learner.
     *
     * @param board absolute board values
     * @param player player to move
     * @param move selected legal move
     * @param expertMove minimax move for comparison
     * @param legal true when the selected move is legal
     * @param moveScores raw predicted action scores
     */
    public record MovePrediction(
            int[] board,
            int player,
            int move,
            int expertMove,
            boolean legal,
            double[] moveScores) {
        public MovePrediction {
            board = Arrays.copyOf(board, board.length);
            moveScores = Arrays.copyOf(moveScores, moveScores.length);
        }
    }

    /**
     * Result of training the policy model.
     *
     * @param demonstrations number of generated demonstrations
     * @param prototypeCount learned prototype count
     * @param transitionCount learned prototype-transition count
     * @param observations observed transition count
     * @param exactMatches demonstrations where the learner chose the expert move
     * @param legalMoves demonstrations where the learner chose any legal move
     * @param exactMatchRate exact expert-match rate
     * @param legalMoveRate legal move rate
     * @param emptyBoard prediction for an empty board
     * @param winningMove prediction for an immediate winning board
     * @param blockingMove prediction for an immediate blocking board
     * @param learner trained hybrid learner
     */
    public record TrainingResult(
            int demonstrations,
            int prototypeCount,
            int transitionCount,
            int observations,
            int exactMatches,
            int legalMoves,
            double exactMatchRate,
            double legalMoveRate,
            MovePrediction emptyBoard,
            MovePrediction winningMove,
            MovePrediction blockingMove,
            NeuralImitationLearner learner) {
    }

    /**
     * Trains the tic-tac-toe policy example.
     *
     * @return training metrics and sample predictions
     */
    public static TrainingResult trainPolicy() {
        List<Demonstration> demonstrations = demonstrations();
        BackpropagationStateTransitionModel neuralModel = BackpropagationStateTransitionModel.of(
                "tic-tac-toe-neural-policy",
                new BackpropagationNetwork(
                        new int[]{DIMENSION, 36, DIMENSION},
                        new TransferFunctions.TransferFunction[]{
                                TransferFunctions.TransferFunction.NONE,
                                TransferFunctions.TransferFunction.SIGMOID,
                                TransferFunctions.TransferFunction.SIGMOID
                        }),
                DIMENSION,
                DIMENSION);
        HilbertImitationLearner memory =
                HilbertImitationLearner.of("tic-tac-toe-memory", DIMENSION, 1.0, 0.0);
        NeuralImitationLearner learner = NeuralImitationLearner.of(
                "tic-tac-toe-policy",
                memory,
                neuralModel,
                (predictionName, before, observed) ->
                        neuralModel.observe(predictionName, before, observed, 0.20, 0.0),
                0.05);

        for (Demonstration demonstration : demonstrations) {
            learner.observe(demonstration.before(), demonstration.after());
        }

        int exactMatches = 0;
        int legalMoves = 0;
        for (Demonstration demonstration : demonstrations) {
            MovePrediction prediction = predictMove(learner, demonstration.board());
            if (prediction.legal()) {
                legalMoves++;
            }
            if (prediction.move() == demonstration.expertMove()) {
                exactMatches++;
            }
        }

        MovePrediction emptyBoard = predictMove(learner, emptyBoard());
        MovePrediction winningMove = predictMove(learner, board(
                X, X, EMPTY,
                O, O, EMPTY,
                EMPTY, EMPTY, EMPTY));
        MovePrediction blockingMove = predictMove(learner, board(
                O, O, EMPTY,
                X, EMPTY, EMPTY,
                EMPTY, X, EMPTY));

        return new TrainingResult(
                demonstrations.size(),
                memory.prototypeCount(),
                memory.transitionCount(),
                learner.observations(),
                exactMatches,
                legalMoves,
                exactMatches / (double) demonstrations.size(),
                legalMoves / (double) demonstrations.size(),
                emptyBoard,
                winningMove,
                blockingMove,
                learner);
    }

    /**
     * Predicts a legal tic-tac-toe move for a board.
     *
     * @param learner trained learner
     * @param board absolute board values
     * @return selected move and scores
     */
    public static MovePrediction predictMove(
            final NeuralImitationLearner learner,
            final int[] board) {
        Objects.requireNonNull(learner, "Learner cannot be null.");
        requireBoard(board);
        if (winner(board) != EMPTY || isFull(board)) {
            throw new IllegalArgumentException("Cannot predict a move for a finished board.");
        }
        int player = currentPlayer(board);
        WorldState prediction = learner.predict(boardState("query", board));
        double[] scores = actionScores(prediction);
        int selectedMove = chooseLegalMove(board, scores);
        return new MovePrediction(
                board,
                player,
                selectedMove,
                expertMove(board),
                isLegalMove(board, selectedMove),
                scores);
    }

    /**
     * Generates minimax demonstrations for every reachable nonterminal board.
     *
     * @return expert demonstrations
     */
    public static List<Demonstration> demonstrations() {
        LinkedHashMap<String, Demonstration> demonstrations = new LinkedHashMap<>();
        collectDemonstrations(emptyBoard(), demonstrations, new LinkedHashMap<>(), new LinkedHashMap<>());
        return List.copyOf(demonstrations.values());
    }

    /**
     * Runs the example and prints its metrics.
     *
     * @param args ignored
     */
    public static void main(final String[] args) {
        TrainingResult result = trainPolicy();
        System.out.println("Tic-tac-toe demonstrations: " + result.demonstrations());
        System.out.println("Prototype count: " + result.prototypeCount());
        System.out.println("Transition count: " + result.transitionCount());
        System.out.println("Exact expert matches: " + result.exactMatches()
                + " / " + result.demonstrations());
        System.out.println("Exact match rate: " + result.exactMatchRate());
        System.out.println("Legal move rate: " + result.legalMoveRate());
        System.out.println("Empty board move: " + result.emptyBoard().move());
        System.out.println("Winning move: " + result.winningMove().move());
        System.out.println("Blocking move: " + result.blockingMove().move());
    }

    private static void collectDemonstrations(
            final int[] board,
            final Map<String, Demonstration> demonstrations,
            final Map<String, Boolean> visitedBoards,
            final Map<String, Integer> minimaxMemo) {
        String absoluteKey = boardKey(board);
        if (visitedBoards.putIfAbsent(absoluteKey, Boolean.TRUE) != null) {
            return;
        }
        if (winner(board) != EMPTY || isFull(board)) {
            return;
        }

        int move = expertMove(board, minimaxMemo);
        Demonstration demonstration = demonstration(board, move);
        String perspectiveKey = vectorKey(demonstration.before().state());
        Demonstration existing = demonstrations.get(perspectiveKey);
        if (existing != null && existing.expertMove() != move) {
            throw new IllegalStateException("Conflicting expert demonstrations for equivalent board state.");
        }
        demonstrations.putIfAbsent(perspectiveKey, demonstration);

        int player = currentPlayer(board);
        for (int moveIndex : legalMoves(board)) {
            int[] nextBoard = Arrays.copyOf(board, board.length);
            nextBoard[moveIndex] = player;
            collectDemonstrations(nextBoard, demonstrations, visitedBoards, minimaxMemo);
        }
    }

    private static Demonstration demonstration(final int[] board, final int move) {
        int player = currentPlayer(board);
        return new Demonstration(
                board,
                player,
                move,
                boardState("board " + boardKey(board), board),
                moveState("move " + move, move));
    }

    private static int expertMove(final int[] board) {
        return expertMove(board, new LinkedHashMap<>());
    }

    private static int expertMove(final int[] board, final Map<String, Integer> minimaxMemo) {
        requireBoard(board);
        int player = currentPlayer(board);
        int bestMove = -1;
        int bestScore = player == X ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        for (int move : MOVE_ORDER) {
            if (!isLegalMove(board, move)) {
                continue;
            }
            int[] nextBoard = Arrays.copyOf(board, board.length);
            nextBoard[move] = player;
            int score = minimax(nextBoard, -player, minimaxMemo);
            if (player == X && score > bestScore || player == O && score < bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }
        if (bestMove < 0) {
            throw new IllegalArgumentException("Board has no legal move.");
        }
        return bestMove;
    }

    private static int minimax(
            final int[] board,
            final int player,
            final Map<String, Integer> memo) {
        int terminal = terminalScore(board);
        if (terminal != Integer.MIN_VALUE) {
            return terminal;
        }
        String key = boardKey(board) + ":" + player;
        Integer cached = memo.get(key);
        if (cached != null) {
            return cached;
        }

        int bestScore = player == X ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        for (int move : MOVE_ORDER) {
            if (!isLegalMove(board, move)) {
                continue;
            }
            int[] nextBoard = Arrays.copyOf(board, board.length);
            nextBoard[move] = player;
            int score = minimax(nextBoard, -player, memo);
            if (player == X) {
                bestScore = Math.max(bestScore, score);
            } else {
                bestScore = Math.min(bestScore, score);
            }
        }
        memo.put(key, bestScore);
        return bestScore;
    }

    private static int terminalScore(final int[] board) {
        int winner = winner(board);
        int played = playedCells(board);
        if (winner == X) {
            return 10 - played;
        }
        if (winner == O) {
            return played - 10;
        }
        if (played == CELLS) {
            return 0;
        }
        return Integer.MIN_VALUE;
    }

    private static WorldState boardState(final String name, final int[] board) {
        int player = currentPlayer(board);
        double[] values = new double[DIMENSION];
        for (int i = 0; i < CELLS; i++) {
            values[i] = board[i] == EMPTY ? 0.0 : (board[i] == player ? 1.0 : -1.0);
        }
        return WorldState.of(name, HilbertVector.of(values));
    }

    private static WorldState moveState(final String name, final int move) {
        if (move < 0 || move >= CELLS) {
            throw new IllegalArgumentException("Move index is outside the board.");
        }
        double[] values = new double[DIMENSION];
        values[CELLS + move] = 1.0;
        return WorldState.of(name, HilbertVector.of(values));
    }

    private static double[] actionScores(final WorldState prediction) {
        double[] values = prediction.state().toArray();
        return Arrays.copyOfRange(values, CELLS, DIMENSION);
    }

    private static int chooseLegalMove(final int[] board, final double[] scores) {
        int bestMove = -1;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (int move : MOVE_ORDER) {
            if (isLegalMove(board, move) && scores[move] > bestScore) {
                bestScore = scores[move];
                bestMove = move;
            }
        }
        if (bestMove < 0) {
            throw new IllegalArgumentException("Board has no legal move.");
        }
        return bestMove;
    }

    /**
     * Lists currently legal move indexes in board order.
     *
     * @param board absolute board values
     * @return legal cell indexes
     */
    public static List<Integer> legalMoves(final int[] board) {
        requireBoard(board);
        List<Integer> moves = new ArrayList<>();
        for (int i = 0; i < board.length; i++) {
            if (board[i] == EMPTY) {
                moves.add(i);
            }
        }
        return moves;
    }

    /**
     * Checks whether a move can be played on the current board.
     *
     * @param board absolute board values
     * @param move candidate cell index
     * @return true when the cell exists and is empty
     */
    public static boolean isLegalMove(final int[] board, final int move) {
        requireBoard(board);
        return move >= 0 && move < CELLS && board[move] == EMPTY;
    }

    /**
     * Computes the side to move from piece counts.
     *
     * @param board absolute board values
     * @return {@link #X} when X moves next, otherwise {@link #O}
     */
    public static int currentPlayer(final int[] board) {
        requireBoard(board);
        int xCount = 0;
        int oCount = 0;
        for (int cell : board) {
            if (cell == X) {
                xCount++;
            } else if (cell == O) {
                oCount++;
            }
        }
        if (xCount == oCount) {
            return X;
        }
        if (xCount == oCount + 1) {
            return O;
        }
        throw new IllegalArgumentException("Board does not have a legal side to move.");
    }

    /**
     * Computes the winner of a board.
     *
     * @param board absolute board values
     * @return {@link #X}, {@link #O}, or {@link #EMPTY} when no side has won
     */
    public static int winner(final int[] board) {
        requireBoard(board);
        for (int[] line : LINES) {
            int sum = board[line[0]] + board[line[1]] + board[line[2]];
            if (sum == X * 3) {
                return X;
            }
            if (sum == O * 3) {
                return O;
            }
        }
        return EMPTY;
    }

    /**
     * Checks whether all cells are occupied.
     *
     * @param board absolute board values
     * @return true when no empty cell remains
     */
    public static boolean isFull(final int[] board) {
        requireBoard(board);
        return playedCells(board) == CELLS;
    }

    private static int playedCells(final int[] board) {
        int played = 0;
        for (int cell : board) {
            if (cell != EMPTY) {
                played++;
            }
        }
        return played;
    }

    private static int[] emptyBoard() {
        return new int[CELLS];
    }

    private static int[] board(final int... cells) {
        requireBoard(cells);
        return Arrays.copyOf(cells, cells.length);
    }

    private static void requireBoard(final int[] board) {
        Objects.requireNonNull(board, "Board cannot be null.");
        if (board.length != CELLS) {
            throw new IllegalArgumentException("Board must have nine cells.");
        }
        for (int cell : board) {
            if (cell != X && cell != O && cell != EMPTY) {
                throw new IllegalArgumentException("Board cells must be X, O, or empty.");
            }
        }
    }

    private static String boardKey(final int[] board) {
        StringBuilder key = new StringBuilder(board.length);
        for (int cell : board) {
            key.append(switch (cell) {
                case X -> 'X';
                case O -> 'O';
                default -> '.';
            });
        }
        return key.toString();
    }

    private static String vectorKey(final HilbertVector vector) {
        return Arrays.toString(vector.toArray());
    }
}
