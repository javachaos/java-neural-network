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

import java.util.ArrayList;

import com.github.javachaos.javaneuralnetwork.shared.layers.InputLayer;
import com.github.javachaos.javaneuralnetwork.shared.layers.InputNeuronLayer;
import com.github.javachaos.javaneuralnetwork.shared.layers.OutputLayer;
import com.github.javachaos.javaneuralnetwork.shared.network.*;
import com.github.javachaos.javaneuralnetwork.shared.tests.values.TestConstants;
import com.github.javachaos.javaneuralnetwork.shared.util.MathTools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

class InputNeuronImplLayerTest {

    private static final Logger LOGGER = 
            LoggerFactory.getLogger(InputNeuronImplLayerTest.class);
    private static final Double NN_INPUT_VALUE = 0.0123;

    @Test
    final void testInputLayer() {
        InputLayer l = new InputNeuronLayer(1);
        assertNotNull(l);
        assertEquals(LayerType.INPUT, l.getLayerType());
    }

    @Test
    final void testBuild() {
        InputLayer l = new InputNeuronLayer(1);
        l.build();
        int size = l.getSize();
        assertEquals(1, size);
    }

    @Test
    final void testAddValue() {
        InputLayer l = new InputNeuronLayer(1);
        l.build();
        l.addValue(1.0, 0);
        assertEquals(1.0, l.getNeuron(0).getValue(), TestConstants.DELTA);
    }

    @Test
    final void testAddValues() {
        InputLayer l = new InputNeuronLayer(1);
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
        Network n = new NeuralNetwork(5, 5,
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
        InputLayer l = new InputNeuronLayer(5);
        l.addValues(values);
        n.setInputLayer(l);
        NeuralNetContext nnctx = new NetworkContext(n);
        assertFalse(nnctx.isRunning());
        OutputLayer ol = l.propagate(nnctx);
        double end = MathTools.sum(ol.getOutputValues());
        assertNotEquals(0.0, end);
        LOGGER.debug("Total input: " + start);
        LOGGER.debug("Total output: " + end);
    }    
}
