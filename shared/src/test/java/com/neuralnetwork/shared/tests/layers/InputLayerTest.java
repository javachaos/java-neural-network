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
/**
 * 
 */
package com.neuralnetwork.shared.tests.layers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.Vector;

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
import com.neuralnetwork.shared.values.Constants;

/**
 * @author fred
 *
 */
class InputLayerTest {
	
    /**
     * Logger instance.
     */
    private static final Logger LOGGER = 
            LoggerFactory.getLogger(InputLayerTest.class);
    
    /**
     * Neural net input value.
     */
    private static final Double NN_INPUT_VALUE = 0.0123;

    /**
     * Test method for {@link com.neuralnetwork
     * .shared.layers.InputLayer#InputLayer(int)}.
     */
    @Test
    final void testInputLayer() {
        IInputLayer l = new InputLayer(1);
        assertNotNull(l);
        assertEquals(LayerType.INPUT, l.getLayerType());
    }
    
    /**
     * Test method for {@link com.neuralnetwork
     * .shared.layers.InputLayer#build()}.
     */
    @Test
    final void testBuild() {
        IInputLayer l = new InputLayer(1);
        l.build();
        int size = l.getSize();
        assertEquals(1, size);
    }
    
    /**
     * Test method for {@link com.neuralnetwork
     * .shared.layers.InputLayer#addValue(
     * com.neuralnetwork.shared.values.IValue, int)}.
     */
    @Test
    final void testAddValue() {
        IInputLayer l = new InputLayer(1);
        l.build();
        l.addValue(1.0, 0);
        assertEquals(1.0, l.getNeuron(0).getValue(), TestConstants.DELTA);
    }
    
    /**
     * Test method for {@link com.neuralnetwork
     * .shared.layers.InputLayer#addValues(Vector<Double> values)}.
     */
    @Test
    final void testAddValues() {
        IInputLayer l = new InputLayer(1);
        ArrayList<Double> empty = new ArrayList<Double>();
        Assertions.assertThrows(NullPointerException.class, () -> l.addValues(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> l.addValues(empty));

        ArrayList<Double> d = new ArrayList<Double>();
        d.add(1.0);
        l.addValues(d);
        l.build();
        assertEquals(1.0, l.getNeuron(0).getValue(), TestConstants.DELTA);
    }
    
    /**
     * Test method for {@link com.neuralnetwork
     * .shared.layers.InputLayer#propagate(
     * com.neuralnetwork.shared.network.INeuralNetContext)}.
     */
    @Test
    final void testPropagate() {
        INetwork n = new Network(Constants.FIVE, Constants.FIVE, 
                                 Constants.THREE, 
                                 new int[] {Constants.FOUR, 
                                            Constants.THREE,
                                            Constants.FOUR});
        n.build();
        ArrayList<Double> values = new ArrayList<Double>();
        values.add(NN_INPUT_VALUE);
        values.add(NN_INPUT_VALUE);
        values.add(NN_INPUT_VALUE);
        values.add(NN_INPUT_VALUE);
        values.add(NN_INPUT_VALUE);
        double start = MathTools.sum(values);
        assertNotEquals(0.0, start);
        IInputLayer l = new InputLayer(Constants.FIVE);
        l.addValues(values);
        n.setInputLayer(l);
        INeuralNetContext nnctx = new NeuralNetContext(n);
        IOutputLayer ol = l.propagate(nnctx);
        double end = MathTools.sum(ol.getOutputValues());
        assertNotEquals(0.0, end);
        LOGGER.debug("Total input: " + start);
        LOGGER.debug("Total output: " + end);
    }    
}
