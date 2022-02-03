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
package com.neuralnetwork.shared.tests.neurons;

import com.neuralnetwork.shared.neurons.HiddenNeuronImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Fred
 *
 */
class HiddenNeuronImplTest {

	@Test
	final void testToString() {
		HiddenNeuronImpl h = new HiddenNeuronImpl();
		h.setValue(1.0);
		assertEquals("HN(1.0) ", h.toString());
	}

}
