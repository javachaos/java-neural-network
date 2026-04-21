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

import com.github.javachaos.javaneuralnetwork.shared.hilbert.Basis;
import com.github.javachaos.javaneuralnetwork.shared.hilbert.HilbertVector;
import com.github.javachaos.javaneuralnetwork.shared.hilbert.Measurement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BasisMeasurementTest {

    private static final double EPSILON = 1.0e-10;

    @Test
    final void testStandardBasisMeasurement() {
        Basis basis = Basis.standard(2);
        Measurement measurement = basis.measure(HilbertVector.of(3.0, 4.0));

        assertTrue(basis.isComplete());
        assertArrayEquals(new double[]{3.0, 4.0}, measurement.amplitudes().toArray(), EPSILON);
        assertEquals(9.0 / 25.0, measurement.probability(0), EPSILON);
        assertEquals(16.0 / 25.0, measurement.probability(1), EPSILON);
        assertEquals(1.0, measurement.totalProbability(), EPSILON);
        assertEquals(1, measurement.mostLikelyIndex());
    }

    @Test
    final void testSubspaceMeasurementCapturesPartialProbability() {
        Basis xAxis = Basis.orthonormal(HilbertVector.basis(2, 0));
        Measurement measurement = xAxis.measure(HilbertVector.of(3.0, 4.0));

        assertFalse(xAxis.isComplete());
        assertEquals(9.0 / 25.0, measurement.totalProbability(), EPSILON);
        assertArrayEquals(new double[]{3.0, 0.0}, xAxis.project(HilbertVector.of(3.0, 4.0)).toArray(), EPSILON);
    }

    @Test
    final void testRejectsNonOrthonormalBasis() {
        assertThrows(IllegalArgumentException.class,
                () -> Basis.orthonormal(HilbertVector.of(2.0, 0.0)));
        assertThrows(IllegalArgumentException.class,
                () -> Basis.orthonormal(HilbertVector.of(1.0, 0.0), HilbertVector.of(1.0, 0.0)));
        assertThrows(IllegalArgumentException.class,
                () -> Basis.standard(2).measure(HilbertVector.zero(2)));
    }
}
