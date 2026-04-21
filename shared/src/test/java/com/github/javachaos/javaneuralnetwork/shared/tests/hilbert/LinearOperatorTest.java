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
package com.github.javachaos.javaneuralnetwork.shared.tests.hilbert;

import com.github.javachaos.javaneuralnetwork.shared.hilbert.HilbertVector;
import com.github.javachaos.javaneuralnetwork.shared.hilbert.LinearOperator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LinearOperatorTest {

    private static final double EPSILON = 1.0e-10;

    @Test
    final void testIdentityOperator() {
        HilbertVector state = HilbertVector.of(2.0, -1.0, 0.5);

        assertArrayEquals(state.toArray(), LinearOperator.identity(3).apply(state).toArray(), EPSILON);
    }

    @Test
    final void testCompositionAppliesRightOperatorFirst() {
        LinearOperator scale = LinearOperator.of(new double[][]{
                {2.0, 0.0},
                {0.0, 3.0}
        });
        LinearOperator swap = LinearOperator.of(new double[][]{
                {0.0, 1.0},
                {1.0, 0.0}
        });

        assertArrayEquals(
                new double[]{10.0, 12.0},
                scale.compose(swap).apply(HilbertVector.of(4.0, 5.0)).toArray(),
                EPSILON);
    }

    @Test
    final void testProjectorIsSelfAdjointAndIdempotent() {
        HilbertVector diagonal = HilbertVector.of(1.0, 1.0);
        LinearOperator projector = LinearOperator.projector(diagonal);

        assertTrue(projector.isSelfAdjoint(EPSILON));
        assertTrue(projector.isIdempotent(EPSILON));
        assertArrayEquals(
                new double[]{1.0, 1.0},
                projector.apply(HilbertVector.of(2.0, 0.0)).toArray(),
                EPSILON);
    }

    @Test
    final void testTensorProductOperator() {
        LinearOperator left = LinearOperator.of(new double[][]{
                {1.0, 2.0}
        });
        LinearOperator right = LinearOperator.of(new double[][]{
                {3.0},
                {4.0}
        });
        LinearOperator product = left.tensorProduct(right);

        assertEquals(2, product.rows());
        assertEquals(2, product.columns());
        double[][] values = product.toArray();
        assertArrayEquals(new double[]{3.0, 6.0}, values[0], EPSILON);
        assertArrayEquals(new double[]{4.0, 8.0}, values[1], EPSILON);
    }

    @Test
    final void testRejectsInvalidOperators() {
        assertThrows(IllegalArgumentException.class, () -> LinearOperator.of(new double[][]{}));
        assertThrows(IllegalArgumentException.class, () -> LinearOperator.of(new double[][]{{1.0}, {1.0, 2.0}}));
        assertThrows(IllegalArgumentException.class, () -> LinearOperator.identity(0));
        assertThrows(IllegalArgumentException.class,
                () -> LinearOperator.identity(2).apply(HilbertVector.of(1.0, 2.0, 3.0)));
    }
}
