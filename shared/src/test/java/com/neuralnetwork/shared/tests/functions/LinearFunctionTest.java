/**
 * 
 */
package com.neuralnetwork.shared.tests.functions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.neuralnetwork.shared.functions.FunctionType;
import com.neuralnetwork.shared.functions.LinearFunction;

/**
 * @author Fred
 *
 */
public class LinearFunctionTest {

	/**
	 * Regression size.
	 */
	private static final int NUM_ITER = 1000;
	
	/**
	 * The amount of accuracy to be used.
	 */
	private static final int ACCUR = 10;
	
	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.functions.LinearFunction#LinearFunction()}.
	 */
	@Test
	public final void testLinearFunction() {
		LinearFunction f = new LinearFunction();
		assertNotNull(f);
		for (int i = 0; i < NUM_ITER; i++) {
			assertEquals(f.activate(i), i, ACCUR * Math.ulp(i));
			assertEquals(f.derivative(i), 1, ACCUR * Math.ulp(i));
		}

		f.changeFunction(FunctionType.NULL);
		
		for (int i = 0; i < NUM_ITER; i++) {
			assertEquals(f.activate(i), 0, ACCUR * Math.ulp(i));
			assertEquals(f.derivative(i), 0, ACCUR * Math.ulp(i));
		}
	}

}
