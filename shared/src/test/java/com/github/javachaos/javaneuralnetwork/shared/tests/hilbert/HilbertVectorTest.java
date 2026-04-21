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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HilbertVectorTest {

    private static final double EPSILON = 1.0e-10;

    @Test
    final void testVectorGeometry() {
        HilbertVector state = HilbertVector.of(3.0, 4.0);
        HilbertVector other = HilbertVector.of(1.0, 2.0);

        assertEquals(11.0, state.innerProduct(other), EPSILON);
        assertEquals(25.0, state.normSquared(), EPSILON);
        assertEquals(5.0, state.norm(), EPSILON);
        assertArrayEquals(new double[]{0.6, 0.8}, state.normalized().toArray(), EPSILON);
    }

    @Test
    final void testProjectionOntoBasisVector() {
        HilbertVector state = HilbertVector.of(3.0, 4.0);
        HilbertVector xAxis = HilbertVector.basis(2, 0);

        assertArrayEquals(new double[]{3.0, 0.0}, state.projectOnto(xAxis).toArray(), EPSILON);
    }

    @Test
    final void testReflectionPreservesAxisAndFlipsOrthogonalComponent() {
        HilbertVector state = HilbertVector.of(2.0, 3.0, -4.0);
        HilbertVector xAxis = HilbertVector.basis(3, 0);

        HilbertVector reflected = state.reflectAbout(xAxis);

        assertArrayEquals(new double[]{2.0, -3.0, 4.0}, reflected.toArray(), EPSILON);
        assertEquals(state.norm(), reflected.norm(), EPSILON);
        assertArrayEquals(state.toArray(), reflected.reflectAbout(xAxis).toArray(), EPSILON);
    }

    @Test
    final void testReflectionAboutDiagonalSwapsTwoDimensionalCoordinates() {
        HilbertVector state = HilbertVector.of(2.0, 0.0);
        HilbertVector diagonal = HilbertVector.of(1.0, 1.0);

        assertArrayEquals(new double[]{0.0, 2.0}, state.reflectAbout(diagonal).toArray(), EPSILON);
    }

    @Test
    final void testTensorProduct() {
        HilbertVector left = HilbertVector.of(1.0, 2.0);
        HilbertVector right = HilbertVector.of(3.0, 4.0, 5.0);

        assertArrayEquals(
                new double[]{3.0, 4.0, 5.0, 6.0, 8.0, 10.0},
                left.tensorProduct(right).toArray(),
                EPSILON);
    }

    @Test
    final void testRejectsInvalidVectors() {
        assertThrows(IllegalArgumentException.class, HilbertVector::of);
        assertThrows(IllegalArgumentException.class, () -> HilbertVector.of(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> HilbertVector.zero(0));
        assertThrows(IllegalArgumentException.class, () -> HilbertVector.basis(2, 2));
        assertThrows(IllegalArgumentException.class, () -> HilbertVector.zero(2).normalized());
        assertThrows(IllegalArgumentException.class,
                () -> HilbertVector.of(1.0, 0.0).reflectAbout(HilbertVector.zero(2)));
    }
}
