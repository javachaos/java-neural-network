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
import com.neuralnetwork.shared.network.LayerType;
import com.neuralnetwork.shared.values.IValue;
import com.neuralnetwork.shared.values.OneValue;

/**
 * @author fred
 *
 */
public class InputLayerTest {
    
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
     * .shared.layers.InputLayer#getBiasNeuron()}.
     */
    @Test
    public final void testGetBiasNeuron() {
        InputLayer l = new InputLayer(1);
        IValue<?> v = l.getBiasNeuron().getValue();
        assertEquals(v, new OneValue());
    }
    
    /**
     * Test method for {@link com.neuralnetwork
     * .shared.layers.InputLayer#build()}.
     */
    @Test
    public final void testBuild() {
        IInputLayer l = new InputLayer(1);
        ((IInputLayer) l).build();
        int size = ((Vector<?>) l).size();
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
        IInputLayer l = new InputLayer(1);
        l.propagate(null);
    }    
}
