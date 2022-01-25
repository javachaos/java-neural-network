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

import java.util.Vector;

import nl.jqno.equalsverifier.internal.util.Assert;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neuralnetwork.shared.layers.IInputLayer;
import com.neuralnetwork.shared.layers.InputLayer;
import com.neuralnetwork.shared.network.INetwork;
import com.neuralnetwork.shared.network.INeuralNetContext;
import com.neuralnetwork.shared.network.LayerType;
import com.neuralnetwork.shared.network.Network;
import com.neuralnetwork.shared.network.NeuralNetContext;
import com.neuralnetwork.shared.tests.util.TestConstants;
import com.neuralnetwork.shared.values.Constants;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author fred
 *
 */
public class InputLayerTest {
	
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
        l.build();
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
        l.addValue(1.0, 0);
        assertEquals(1.0, l.getNeuron(0).getValue(), TestConstants.DELTA);
    }
    
    /**
     * Test method for {@link com.neuralnetwork
     * .shared.layers.InputLayer#addValues(Vector<Double> values)}.
     */
    @Test
    public final void testAddValues() {
        IInputLayer l = new InputLayer(1);
        try {
        	l.addValues(null);
        	// Exception not thrown.
        	Assert.fail(null);
        } catch (Throwable t) {
        	//Success
        	LOGGER.info("Method InputLayer.addValues"
        			+ "successfully caught exception.");
        }

        try {
            l.addValues(new Vector<Double>());
        	// Exception not thrown. parameter size mismatch
        	Assert.fail(null);
        } catch (Throwable t) {
        	LOGGER.info("Method InputLayer.addValues"
        			+ "successfully caught exception.");
        }

        Vector<Double> d = new Vector<Double>();
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
