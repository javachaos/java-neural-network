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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConceptSubspaceTest {

    private static final double EPSILON = 1.0e-10;

    @Test
    final void testConceptProjectsAndScoresState() {
        ConceptSubspace spatial = ConceptSubspace.of("spatial",
                HilbertVector.basis(3, 0),
                HilbertVector.basis(3, 1));
        HilbertVector state = HilbertVector.of(3.0, 4.0, 12.0);

        assertEquals("spatial", spatial.name());
        assertEquals(3, spatial.dimension());
        assertArrayEquals(new double[]{3.0, 4.0, 0.0}, spatial.project(state).toArray(), EPSILON);
        assertEquals(25.0 / 169.0, spatial.relevance(state), EPSILON);
        assertTrue(spatial.projector().isIdempotent(EPSILON));
    }

    @Test
    final void testConceptRejectsInvalidInputs() {
        assertThrows(IllegalArgumentException.class,
                () -> ConceptSubspace.of(" ", HilbertVector.basis(2, 0)));
        assertThrows(IllegalArgumentException.class,
                () -> ConceptSubspace.of("bad", HilbertVector.of(2.0, 0.0)));
        assertThrows(IllegalArgumentException.class,
                () -> ConceptSubspace.of("axis", HilbertVector.basis(2, 0)).relevance(HilbertVector.zero(2)));
    }
}
