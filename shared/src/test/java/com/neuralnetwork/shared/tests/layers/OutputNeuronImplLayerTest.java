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
package com.neuralnetwork.shared.tests.layers;

import com.neuralnetwork.shared.layers.OutputLayer;
import com.neuralnetwork.shared.layers.OutputNeuronLayer;
import com.neuralnetwork.shared.network.LayerType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author fred
 *
 */
class OutputNeuronImplLayerTest {

    @Test
    final void testOutputLayer() {
        OutputLayer o = new OutputNeuronLayer(1);
        assertNotNull(o);
        assertEquals(LayerType.OUTPUT, o.getLayerType());
    }

    @Test
    final void testBuild() {
        OutputLayer l = new OutputNeuronLayer(1);
        l.build();
        int size = l.getSize();
        assertEquals(1, size);
    }
    
}
