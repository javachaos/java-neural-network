package com.github.javachaos.javaneuralnetwork.test;

import java.util.List;
import java.util.Random;

import com.github.javachaos.javaneuralnetwork.core.ConnectFourLearningExample;
import com.github.javachaos.javaneuralnetwork.core.ConnectFourLearningExample.GameLearningResult;
import com.github.javachaos.javaneuralnetwork.core.ConnectFourLearningExample.PlayedMove;
import com.github.javachaos.javaneuralnetwork.core.ConnectFourLearningExample.SelfPlayResult;
import com.github.javachaos.javaneuralnetwork.core.ConnectFourLearningExample.TrainingResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConnectFourLearningExampleTest {

    @Test
    final void testRulesApplyGravityAndDetectWins() {
        int[] board = new int[ConnectFourLearningExample.CELLS];

        assertEquals(ConnectFourLearningExample.RED, ConnectFourLearningExample.currentPlayer(board));
        assertEquals(ConnectFourLearningExample.ROWS - 1, ConnectFourLearningExample.rowForMove(board, 3));

        board = ConnectFourLearningExample.playMove(board, 3);
        assertEquals(ConnectFourLearningExample.RED,
                board[ConnectFourLearningExample.index(ConnectFourLearningExample.ROWS - 1, 3)]);
        assertEquals(ConnectFourLearningExample.YELLOW, ConnectFourLearningExample.currentPlayer(board));

        board = ConnectFourLearningExample.playMove(board, 3);
        assertEquals(ConnectFourLearningExample.YELLOW,
                board[ConnectFourLearningExample.index(ConnectFourLearningExample.ROWS - 2, 3)]);
        assertTrue(ConnectFourLearningExample.isLegalMove(board, 3));

        for (int i = 0; i < 4; i++) {
            board = ConnectFourLearningExample.playMove(board, 3);
        }
        assertFalse(ConnectFourLearningExample.isLegalMove(board, 3));

        int[] horizontalWin = new int[ConnectFourLearningExample.CELLS];
        for (int column = 0; column < 4; column++) {
            horizontalWin[ConnectFourLearningExample.index(ConnectFourLearningExample.ROWS - 1, column)] =
                    ConnectFourLearningExample.RED;
        }
        assertEquals(ConnectFourLearningExample.RED, ConnectFourLearningExample.winner(horizontalWin));
    }

    @Test
    final void testBootstrapLearnerKnowsLegalTactics() {
        TrainingResult result = ConnectFourLearningExample.trainPolicy(180);

        assertTrue(result.demonstrations() > 50);
        assertTrue(result.demonstrations() <= 180);
        assertEquals(result.demonstrations(), result.observations());
        assertEquals(result.demonstrations(), result.temporalUpdates());
        assertTrue(result.temporalLearner().actionCount() > 1);
        assertEquals(result.demonstrations(), result.legalMoves());
        assertEquals(1.0, result.legalMoveRate());
        assertEquals(3, result.emptyBoard().column());
        assertEquals(3, result.winningMove().column());
        assertEquals(3, result.blockingMove().column());
        assertTrue(result.winningMove().tactical());
        assertTrue(result.blockingMove().tactical());
    }

    @Test
    final void testLearnerReinforcesWinningPlayedMoves() {
        TrainingResult result = ConnectFourLearningExample.trainPolicy(120);
        int observationsBefore = result.learner().observations();
        int[] board = new int[ConnectFourLearningExample.CELLS];
        PlayedMove move = ConnectFourLearningExample.playedMove(board, 3);

        GameLearningResult learning = ConnectFourLearningExample.learnFromGame(
                result.learner(),
                List.of(move),
                ConnectFourLearningExample.RED);

        assertEquals(1, learning.reinforcedMoves());
        assertTrue(learning.reinforcementObservations() > 1);
        assertEquals(observationsBefore + learning.reinforcementObservations(),
                result.learner().observations());
        assertEquals(0, learning.temporalUpdates());
    }

    @Test
    final void testSelfPlayProducesTerminalGameAndLearns() {
        TrainingResult result = ConnectFourLearningExample.trainPolicy(120);
        int observationsBefore = result.learner().observations();
        int temporalUpdatesBefore = result.temporalLearner().updates();

        SelfPlayResult selfPlay = ConnectFourLearningExample.selfPlayGame(
                result.learner(),
                result.temporalLearner(),
                0.10,
                new Random(7L));

        assertTrue(selfPlay.moves() > 0);
        assertEquals(selfPlay.moves(), selfPlay.history().size());
        assertTrue(selfPlay.winner() != ConnectFourLearningExample.EMPTY
                || ConnectFourLearningExample.isFull(selfPlay.finalBoard()));
        assertTrue(result.learner().observations() > observationsBefore);
        assertEquals(temporalUpdatesBefore + selfPlay.moves(), result.temporalLearner().updates());
        assertEquals(result.temporalLearner().updates(), selfPlay.learning().temporalUpdates());
        assertFalse(ConnectFourLearningExample.boardSignature(selfPlay.finalBoard()).isBlank());
    }
}
