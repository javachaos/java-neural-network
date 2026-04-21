/*******************************************************************************
 * Copyright (c) 2014 Fred .
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *     Fred  - initial API and implementation
 ******************************************************************************/
package com.github.javachaos.javaneuralnetwork.shared.tests.knowledge;

import com.github.javachaos.javaneuralnetwork.shared.hilbert.HilbertVector;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.ConceptSubspace;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.GoalSubspace;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.StatePrediction;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.WorldState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatePredictionTest {

    private static final double EPSILON = 1.0e-10;

    @Test
    final void testForecastHasNoObservation() {
        WorldState before = WorldState.of("before", HilbertVector.of(1.0, 0.0));
        WorldState predicted = WorldState.of("predicted", HilbertVector.of(0.5, 0.5));
        StatePrediction prediction = StatePrediction.forecast("forecast", before, predicted);

        assertEquals("forecast", prediction.name());
        assertEquals(before, prediction.before());
        assertEquals(predicted, prediction.predicted());
        assertFalse(prediction.hasObserved());
        assertFalse(prediction.observed().isPresent());
        assertFalse(prediction.residual().isPresent());
        assertThrows(IllegalStateException.class, prediction::errorNorm);
    }

    @Test
    final void testObservedPredictionMeasuresResidualAndGoalFit() {
        WorldState before = WorldState.of("before", HilbertVector.of(1.0, 0.0));
        WorldState predicted = WorldState.of("predicted", HilbertVector.of(0.5, 0.5));
        WorldState observed = WorldState.of("observed", HilbertVector.of(0.0, 1.0));
        StatePrediction prediction = StatePrediction.observed("observed-step", before, predicted, observed);
        GoalSubspace goal = GoalSubspace.approach("seek-y",
                ConceptSubspace.of("y", HilbertVector.basis(2, 1)));

        assertTrue(prediction.hasObserved());
        assertArrayEquals(new double[]{-0.5, 0.5}, prediction.residual().orElseThrow().toArray(), EPSILON);
        assertEquals(0.5, prediction.errorSquared(), EPSILON);
        assertEquals(Math.sqrt(0.5), prediction.errorNorm(), EPSILON);
        assertEquals(Math.sqrt(0.5), prediction.normalizedError(), EPSILON);
        assertEquals(0.5, prediction.predictedSatisfaction(goal), EPSILON);
        assertEquals(1.0, prediction.observedSatisfaction(goal), EPSILON);
        assertEquals(0.5, prediction.predictedProgress(goal), EPSILON);
        assertEquals(1.0, prediction.observedProgress(goal), EPSILON);
    }

    @Test
    final void testRejectsInvalidPredictions() {
        WorldState before = WorldState.of("before", HilbertVector.of(1.0, 0.0));
        WorldState predicted = WorldState.of("predicted", HilbertVector.of(0.5, 0.5));
        WorldState wrongDimension = WorldState.of("wrong", HilbertVector.of(1.0, 2.0, 3.0));

        assertThrows(IllegalArgumentException.class,
                () -> StatePrediction.forecast(" ", before, predicted));
        assertThrows(IllegalArgumentException.class,
                () -> StatePrediction.observed("bad", before, predicted, wrongDimension));
        assertThrows(NullPointerException.class,
                () -> StatePrediction.forecast("bad", null, predicted));
    }
}
