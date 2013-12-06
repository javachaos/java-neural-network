/**
 * 
 */
package com.neuralnetwork.shared.tests.functions;

import static org.junit.Assert.*;

import org.junit.Test;

import com.neuralnetwork.shared.functions.FunctionType;

/**
 * @author Fred
 *
 */
public class FunctionTypeTest {

	@Test
	public final void test() {
		FunctionType[] t = FunctionType.values();
		
		for (FunctionType f : t) {
			assertNotNull(f);
		}
		
		assertEquals(FunctionType.valueOf("SIGMOID"),
				FunctionType.SIGMOID);
		
		assertEquals(FunctionType.valueOf("LINEAR"),
				FunctionType.LINEAR);
		
	}

}
