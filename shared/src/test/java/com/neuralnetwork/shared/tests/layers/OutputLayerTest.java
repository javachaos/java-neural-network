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

import com.neuralnetwork.shared.layers.IOutputLayer;
import com.neuralnetwork.shared.layers.OutputLayer;
import com.neuralnetwork.shared.network.LayerType;

/**
 * @author fred
 *
 */
public class OutputLayerTest {
    
    /**
     * Test method for {@link com.neuralnetwork
     * .shared.layers.OutputLayer#OutputLayer(int)}.
     */
    @Test
    public final void testOutputLayer() {
        IOutputLayer o = new OutputLayer(1);
        assertNotNull(o);
        assertEquals(o.getLayerType(), LayerType.OUTPUT);
    }
    
    /**
     * Test method for {@link com.neuralnetwork
     * .shared.layers.OutputLayer#build()}.
     */
    @Test
    public final void testBuild() {
        IOutputLayer l = new OutputLayer(1);
        ((IOutputLayer) l).build();
        int size = ((Vector<?>) l).size();
        assertEquals(size, 1);
    }
    
}
