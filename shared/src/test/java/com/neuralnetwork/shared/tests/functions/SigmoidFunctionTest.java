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
package com.neuralnetwork.shared.tests.functions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neuralnetwork.shared.functions.FunctionType;
import com.neuralnetwork.shared.functions.IActivationFunction;
import com.neuralnetwork.shared.functions.SigmoidFunction;
import com.neuralnetwork.shared.values.DoubleValue;

/**
 * @author fred
 *
 */
public class SigmoidFunctionTest {
    
    /**
     * Logger instance.
     */
    private static final Logger LOGGER = 
            LoggerFactory.getLogger(SigmoidFunctionTest.class);
    
    /**
     * The degree of accuracy between two double values.
     */
    private static final double DELTA = 10;
    
    /**
     * Expected value for the sigmoid function given zero.
     */
    private static final double EXPECTED_VALUE_ZERO = 0.5;
    
    /**
     * Expected value for the sigmoid function given one.
     */
    private static final double EXPECTED_VALUE_ONE = 0.7310585786300049;
    
    /**
     * Expected value for the sigmoin function given two.
     */
    private static final double EXPECTED_VALUE_TWO = 0.8807970779778823;
    
    /**
     * Expected value for the sigmoid function given zero.
     */
    private static final double EXPECTED_DVALUE_ZERO = 0.25;
    
    /**
     * Expected value for the sigmoid function given one.
     */
    private static final double EXPECTED_DVALUE_ONE = 0.19661193324148185;
    
    /**
     * Expected value for the sigmoin function given two.
     */
    private static final double EXPECTED_DVALUE_TWO = 0.10499358540350662;
    
    /**
     * Test method for 
     * {@link com.neuralnetwork.shared.functions
     * .SigmoidFunction#activate(double)}.
     */
    @Test
    public final void testActivate() {
        testValue(0, EXPECTED_VALUE_ZERO);
        testValue(1.0, EXPECTED_VALUE_ONE);
        testValue(2.0, EXPECTED_VALUE_TWO);
    }
    
    /**
     * Test method for 
     * {@link com.neuralnetwork.shared.functions
     * .SigmoidFunction#derivative(double)}.
     */
    @Test
    public final void testDerivative() {
        testDerivativeValue(0, EXPECTED_DVALUE_ZERO);
        testDerivativeValue(1.0, EXPECTED_DVALUE_ONE);
        testDerivativeValue(2.0, EXPECTED_DVALUE_TWO);
    }
    
    /**
     * Test method for {@link com.neuralnetwork.shared.functions
     * .SigmoidFunction#getFunctionType()}.
     */
    @Test
    public final void testGetFunctionType() {
        IActivationFunction f = new SigmoidFunction();
        FunctionType t = f.getFunctionType();
        assertEquals(t, FunctionType.SIGMOID);
    }
    
    /**
     * Test method for {@link com.neuralnetwork.shared.functions
     * .SigmoidFunction#equals()}.
     */
    @Test
    public final void testEquals() {
        IActivationFunction f = new SigmoidFunction();
        IActivationFunction f1 = new SigmoidFunction();
        
        assertEquals(f, f1);
        assertEquals(f, f);
        
        f = new SigmoidFunction();
        f1 = null;
        
        assertFalse(f.equals(f1));
        assertFalse(f.equals(new DoubleValue(0.0)));
    }
    
    /**
     * Test method for {@link com.neuralnetwork.shared.functions
     * .SigmoidFunction#hashCode()}.
     */
    @Test
    public final void testHashCode() {
        IActivationFunction f = new SigmoidFunction();
        IActivationFunction f1 = new SigmoidFunction();
        
        assertEquals(f.hashCode(), f1.hashCode());
        assertEquals(f.hashCode(), f.hashCode());
    }
    
    /**
     * Test a value on the sigmoid function.
     * 
     * @param input
     *      the input value to the sigmoid function
     * @param expected
     *      the expected output from the sigmoid function
     */
    private void testValue(final double input, final double expected) {
        LOGGER.debug("===== Sigmoid Function Test. =====");
        IActivationFunction f = new SigmoidFunction();
        double v = f.activate(input);
        LOGGER.debug(" Input: " + input);
        LOGGER.debug(" Value: " + v);
        LOGGER.debug(" Expected: " + expected);
        assertEquals(v, expected , DELTA * Math.ulp(expected));
        LOGGER.debug("==================================");
    }
    
    /**
     * Test a value on the derivative sigmoid function.
     * 
     * @param input
     *      the input value to the sigmoid function
     * @param expected
     *      the expected output from the sigmoid function
     */
    private void testDerivativeValue(final double input, final double expected) {
        LOGGER.debug("===== Sigmoid Derivative Test. =====");
        IActivationFunction f = new SigmoidFunction();
        double v = f.derivative(input);
        LOGGER.debug(" Input: " + input);
        LOGGER.debug(" Value: " + v);
        LOGGER.debug(" Expected: " + expected);
        assertEquals(v, expected , DELTA * Math.ulp(expected));
        LOGGER.debug("====================================");
    }
    
}
