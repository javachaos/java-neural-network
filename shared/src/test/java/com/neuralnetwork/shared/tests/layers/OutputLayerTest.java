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
