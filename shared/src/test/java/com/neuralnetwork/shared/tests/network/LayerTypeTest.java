/*******************************************************************************
 * Copyright (c) 2014 Fred Laderoute.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *     Fred Laderoute - initial API and implementation
 *******************************************************************************/
/**
 * 
 */
package com.neuralnetwork.shared.tests.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.neuralnetwork.shared.network.LayerType;

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
