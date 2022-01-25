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
package com.neuralnetwork.shared.tests.network;

import com.neuralnetwork.shared.network.LayerType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Fred
 *
 */
public class LayerTypeTest {

    /**
     * Test the LayerType class.
     */
	@Test
	public final void test() {
		LayerType[] t = LayerType.values();
		
		for (LayerType l : t) {
			assertNotNull(l);
		}
		
		assertEquals(LayerType.HIDDEN, LayerType.valueOf("HIDDEN"));
		assertEquals(LayerType.INPUT, LayerType.valueOf("INPUT"));
		assertEquals(LayerType.OUTPUT, LayerType.valueOf("OUTPUT"));
	}

}
