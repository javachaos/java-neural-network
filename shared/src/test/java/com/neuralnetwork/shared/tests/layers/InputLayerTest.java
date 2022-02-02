/*******************************************************************************
 * Copyright (c) 2014 Fred Laderoute.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *     Fred Laderoute - initial API and implementation
 ******************************************************************************/
package com.neuralnetwork.shared.tests.layers;

import java.util.ArrayList;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neuralnetwork.shared.layers.IInputLayer;
import com.neuralnetwork.shared.layers.IOutputLayer;
import com.neuralnetwork.shared.layers.InputLayer;
import com.neuralnetwork.shared.network.INetwork;
import com.neuralnetwork.shared.network.INeuralNetContext;
import com.neuralnetwork.shared.network.LayerType;
import com.neuralnetwork.shared.network.Network;
import com.neuralnetwork.shared.network.NeuralNetContext;
import com.neuralnetwork.shared.tests.values.TestConstants;
import com.neuralnetwork.shared.util.MathTools;

import static org.junit.jupiter.api.Assertions.*;

class InputLayerTest {

    private static final Logger LOGGER = 
            LoggerFactory.getLogger(InputLayerTest.class);
    private static final Double NN_INPUT_VALUE = 0.0123;

    @Test
    final void testInputLayer() {
        IInputLayer l = new InputLayer(1);
        assertNotNull(l);
        assertEquals(LayerType.INPUT, l.getLayerType());
    }

    @Test
    final void testBuild() {
        IInputLayer l = new InputLayer(1);
        l.build();
        int size = l.getSize();
        assertEquals(1, size);
    }

    @Test
    final void testAddValue() {
        IInputLayer l = new InputLayer(1);
        l.build();
        l.addValue(1.0, 0);
        assertEquals(1.0, l.getNeuron(0).getValue(), TestConstants.DELTA);
    }

    @Test
    final void testAddValues() {
        IInputLayer l = new InputLayer(1);
        ArrayList<Double> empty = new ArrayList<>();
        Assertions.assertThrows(NullPointerException.class, () -> l.addValues(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> l.addValues(empty));

        ArrayList<Double> d = new ArrayList<>();
        d.add(1.0);
        l.addValues(d);
        l.build();
        assertEquals(1.0, l.getNeuron(0).getValue(), TestConstants.DELTA);
    }

    @Test
    final void testPropagate() {
        INetwork n = new Network(5, 5,
                                 3,
                                 new int[] {4,3,4});
        n.build();
        ArrayList<Double> values = new ArrayList<>();
        values.add(NN_INPUT_VALUE);
        values.add(NN_INPUT_VALUE);
        values.add(NN_INPUT_VALUE);
        values.add(NN_INPUT_VALUE);
        values.add(NN_INPUT_VALUE);
        double start = MathTools.sum(values);
        assertNotEquals(0.0, start);
        IInputLayer l = new InputLayer(5);
        l.addValues(values);
        n.setInputLayer(l);
        INeuralNetContext nnctx = new NeuralNetContext(n);
        assertFalse(nnctx.isRunning());
        IOutputLayer ol = l.propagate(nnctx);
        double end = MathTools.sum(ol.getOutputValues());
        assertNotEquals(0.0, end);
        LOGGER.debug("Total input: " + start);
        LOGGER.debug("Total output: " + end);
    }    
}
