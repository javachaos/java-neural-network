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
package com.neuralnetwork.shared.tests.functions;

import com.neuralnetwork.shared.functions.FunctionType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Fred
 *
 */
public class FunctionTypeTest {

    /**
     * Tests the FunctionType class.
     */
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
