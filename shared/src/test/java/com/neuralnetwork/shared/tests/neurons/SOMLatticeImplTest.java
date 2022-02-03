/*******************************************************************************
 * Copyright (c) 2014 Fred .
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *     Fred  - initial API and implementation
 ******************************************************************************/
package com.neuralnetwork.shared.tests.neurons;

import com.neuralnetwork.shared.neurons.NeuronType;
import com.neuralnetwork.shared.neurons.SOMLatticeImpl;
import com.neuralnetwork.shared.neurons.SOMLayerImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Fred
 *
 */
class SOMLatticeImplTest {

	@Test
	final void testSOMLattice() {
		SOMLatticeImpl s = new SOMLatticeImpl(5,
		                              5, 5);
		assertEquals(5, s.getNeuron(0, 0).getWeights().size());
	}

	@Test
	final void testGetNeuron() {
		SOMLatticeImpl s = new SOMLatticeImpl(5,5, 5);
		assertEquals(NeuronType.SOM, s.getNeuron(1,4).getType());
	}

	@Test
	final void testGetWidth() {
		SOMLatticeImpl s = new SOMLatticeImpl(5, 5, 5);
		assertEquals(5, s.getWidth());
	}

	@Test
	final void testGetHeight() {
	    SOMLatticeImpl s = new SOMLatticeImpl(5,
	                                  5, 5);
		assertEquals(5, s.getHeight());
	}

	@Test
	final void testGetBMU() {
	    SOMLatticeImpl s = new SOMLatticeImpl(5, 5, 5);
		SOMLayerImpl i = new SOMLayerImpl();
		i.add(0.0);
		i.add(0.0);
		i.add(0.0);
		i.add(0.0);
		i.add(0.0);
		assertEquals(NeuronType.SOM, s.getBMU(i).getType());
	}

}
