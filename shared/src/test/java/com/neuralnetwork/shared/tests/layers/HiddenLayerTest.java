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

import com.neuralnetwork.shared.layers.HiddenLayer;
import com.neuralnetwork.shared.layers.IHiddenLayer;
import com.neuralnetwork.shared.layers.ILayer;
import com.neuralnetwork.shared.network.LayerType;
import com.neuralnetwork.shared.values.IValue;
import com.neuralnetwork.shared.values.OneValue;

/**
 * @author fred
 *
 */
public class HiddenLayerTest {
    
    /**
     * Test method for {@link com.neuralnetwork
     * .shared.layers.HiddenLayer#HiddenLayer(int)}.
     */
    @Test
    public final void testHiddenLayer() {
        IHiddenLayer h = new HiddenLayer(1);
        assertNotNull(h);
        assertEquals(h.getLayerType(), LayerType.HIDDEN);
    }
    
    /**
     * Test method for {@link com.neuralnetwork
     * .shared.layers.HiddenLayer#getBiasNeuron()}.
     */
    @Test
    public final void testGetBiasNeuron() {
        IHiddenLayer h = new HiddenLayer(1);
        IValue<?> v = h.getBiasNeuron().getValue();
        assertEquals(v, new OneValue());
    }
    
    /**
     * Test method for {@link com.neuralnetwork
     * .shared.layers.HiddenLayer#propagate(
     * com.neuralnetwork.shared.network.INeuralNetContext)}.
     */
    @Test
    public final void testPropagate() {
        IHiddenLayer h = new HiddenLayer(1);
        h.propagate(null);
    }
    
    /**
     * Test method for {@link com.neuralnetwork
     * .shared.layers.HiddenLayer#build()}.
     */
    @Test
    public final void testBuild() {
        ILayer<?> h = new HiddenLayer(1);
        ((IHiddenLayer) h).build();
        int size = ((Vector<?>) h).size();
        assertEquals(size, 1);
    }
}
