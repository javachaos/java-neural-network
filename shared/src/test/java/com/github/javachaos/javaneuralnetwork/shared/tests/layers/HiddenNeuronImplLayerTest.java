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
package com.github.javachaos.javaneuralnetwork.shared.tests.layers;

import com.github.javachaos.javaneuralnetwork.shared.layers.HiddenLayer;
import com.github.javachaos.javaneuralnetwork.shared.layers.HiddenNeuronLayer;
import com.github.javachaos.javaneuralnetwork.shared.network.LayerType;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author fred
 *
 */
class HiddenNeuronImplLayerTest {
    
    /**
     * Test method for {@link com.neuralnetwork
     * .shared.layers.HiddenLayer#HiddenLayer(int)}.
     */
    @Test
    final void testHiddenLayer() {
        HiddenLayer h = new HiddenNeuronLayer(1, 0);
        assertNotNull(h);
        assertEquals(LayerType.HIDDEN, h.getLayerType());
    }
    
    /**
     * Test method for {@link com.neuralnetwork
     * .shared.layers.HiddenLayer#build()}.
     */
    @Test
    final void testBuild() {
        HiddenNeuronLayer h = new HiddenNeuronLayer(1, 0);
        ((HiddenLayer) h).build();
        int size = h.getSize();
        assertEquals(1, size);
    }
}
