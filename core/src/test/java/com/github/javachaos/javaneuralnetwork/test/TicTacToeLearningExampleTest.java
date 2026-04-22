package com.github.javachaos.javaneuralnetwork.test;

import com.github.javachaos.javaneuralnetwork.core.TicTacToeLearningExample;
import com.github.javachaos.javaneuralnetwork.core.TicTacToeLearningExample.TrainingResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TicTacToeLearningExampleTest {

    private static TrainingResult cachedResult;

    @Test
    final void testLearnerImitatesMinimaxTicTacToePolicy() {
        TrainingResult result = result();

        assertTrue(result.demonstrations() > 500);
        assertEquals(result.demonstrations(), result.observations());
        assertEquals(result.demonstrations(), result.exactMatches());
        assertEquals(result.demonstrations(), result.legalMoves());
        assertEquals(1.0, result.exactMatchRate());
        assertEquals(1.0, result.legalMoveRate());
    }

    @Test
    final void testLearnerChoosesStrategicMoves() {
        TrainingResult result = result();

        assertEquals(4, result.emptyBoard().move());
        assertEquals(2, result.winningMove().move());
        assertEquals(2, result.blockingMove().move());
        assertTrue(result.emptyBoard().legal());
        assertTrue(result.winningMove().legal());
        assertTrue(result.blockingMove().legal());
    }

    @Test
    final void testTrainingBuildsPrototypeMemory() {
        TrainingResult result = result();

        assertTrue(result.prototypeCount() >= result.demonstrations());
        assertTrue(result.transitionCount() >= result.demonstrations());
    }

    @Test
    final void testRuleHelpersIdentifyTurnsAndTerminalBoards() {
        int[] empty = new int[TicTacToeLearningExample.CELLS];

        assertEquals(TicTacToeLearningExample.X, TicTacToeLearningExample.currentPlayer(empty));
        assertEquals(TicTacToeLearningExample.CELLS, TicTacToeLearningExample.legalMoves(empty).size());
        assertTrue(TicTacToeLearningExample.isLegalMove(empty, 4));

        int[] xWin = {
                TicTacToeLearningExample.X, TicTacToeLearningExample.X, TicTacToeLearningExample.X,
                TicTacToeLearningExample.O, TicTacToeLearningExample.O, TicTacToeLearningExample.EMPTY,
                TicTacToeLearningExample.EMPTY, TicTacToeLearningExample.EMPTY, TicTacToeLearningExample.EMPTY
        };

        assertEquals(TicTacToeLearningExample.X, TicTacToeLearningExample.winner(xWin));
        assertFalse(TicTacToeLearningExample.isFull(xWin));
        assertFalse(TicTacToeLearningExample.isLegalMove(xWin, 0));

        int[] draw = {
                TicTacToeLearningExample.X, TicTacToeLearningExample.O, TicTacToeLearningExample.X,
                TicTacToeLearningExample.X, TicTacToeLearningExample.O, TicTacToeLearningExample.O,
                TicTacToeLearningExample.O, TicTacToeLearningExample.X, TicTacToeLearningExample.X
        };

        assertEquals(TicTacToeLearningExample.EMPTY, TicTacToeLearningExample.winner(draw));
        assertTrue(TicTacToeLearningExample.isFull(draw));
    }

    private static TrainingResult result() {
        if (cachedResult == null) {
            cachedResult = TicTacToeLearningExample.trainPolicy();
        }
        return cachedResult;
    }
}
