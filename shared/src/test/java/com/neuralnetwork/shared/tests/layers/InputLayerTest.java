/*******************************************************************************
 * Copyright (c) 2013 Fred Laderoute.
 * All rights reserved. This program and the accompanying
 * materials are made available under the terms of the GNU 
 * Public License v3.0 which accompanies this distribution, 
 * and is available at http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *      Fred Laderoute - initial API and implementation
 *******************************************************************************/
/**
 * 
 */
package com.neuralnetwork.shared.tests.layers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Vector;

import org.junit.Test;

import com.neuralnetwork.shared.layers.IInputLayer;
import com.neuralnetwork.shared.layers.InputLayer;
import com.neuralnetwork.shared.network.INetwork;
import com.neuralnetwork.shared.network.INeuralNetContext;
import com.neuralnetwork.shared.network.LayerType;
import com.neuralnetwork.shared.network.Network;
import com.neuralnetwork.shared.network.NeuralNetContext;
import com.neuralnetwork.shared.values.Constants;
import com.neuralnetwork.shared.values.OneValue;

/**
 * @author fred
 *
 */
public class InputLayerTest {
    
    /**
     * Neural net input value.
     */
    private static final Double NN_INPUT_VALUE = 0.0123;

    /**
     * Test method for {@link com.neuralnetwork
     * .shared.layers.InputLayer#InputLayer(int)}.
     */
    @Test
    public final void testInputLayer() {
        IInputLayer l = new InputLayer(1);
        assertNotNull(l);
        assertEquals(l.getLayerType(), LayerType.INPUT);
    }
    
    /**
     * Test method for {@link com.neuralnetwork
     * .shared.layers.InputLayer#build()}.
     */
    @Test
    public final void testBuild() {
        IInputLayer l = new InputLayer(1);
        ((IInputLayer) l).build();
        int size = l.getSize();
        assertEquals(size, 1);
    }
    
    /**
     * Test method for {@link com.neuralnetwork
     * .shared.layers.InputLayer#addValue(
     * com.neuralnetwork.shared.values.IValue, int)}.
     */
    @Test
    public final void testAddValue() {
        IInputLayer l = new InputLayer(1);
        l.build();
        l.addValue(new OneValue(), 0);
        assertEquals(l.getNeuron(0).getValue(), new OneValue());
    }
    
    /**
     * Test method for {@link com.neuralnetwork
     * .shared.layers.InputLayer#propagate(
     * com.neuralnetwork.shared.network.INeuralNetContext)}.
     */
    @Test
    public final void testPropagate() {
        INetwork n = new Network(Constants.FIVE, Constants.FIVE, 
                                 Constants.THREE, 
                                 new int[] {Constants.FOUR, 
                                            Constants.TWO, 
                                            Constants.FOUR});
        n.build();
        Vector<Double> values = new Vector<Double>();
        values.add(NN_INPUT_VALUE);
        values.add(NN_INPUT_VALUE);
        values.add(NN_INPUT_VALUE);
        values.add(NN_INPUT_VALUE);
        values.add(NN_INPUT_VALUE);
        
        IInputLayer l = new InputLayer(Constants.FIVE);
        l.addValues(values);
        n.setInputLayer(l);
        INeuralNetContext nnctx = new NeuralNetContext(n);
        l.propagate(nnctx);
    }    
}
