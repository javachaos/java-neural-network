/**
 * 
 */
package com.neuralnetwork.shared.tests.neurons;

import static org.junit.Assert.*;

import org.junit.Test;

import com.neuralnetwork.shared.neurons.NeuronType;
import com.neuralnetwork.shared.neurons.SOMLattice;
import com.neuralnetwork.shared.neurons.SOMLayer;

/**
 * @author Fred
 *
 */
public class SOMLatticeTest {

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.SOMLattice#SOMLattice(int, int, int)}.
	 */
	@Test
	public final void testSOMLattice() {
		SOMLattice s = new SOMLattice(5, 5, 5);
		assertEquals(5, s.getHeight());
		assertEquals(5, s.getWidth());
		assertEquals(5, s.getNeuron(0, 0).getWeights().size());
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.SOMLattice#getNeuron(int, int)}.
	 */
	@Test
	public final void testGetNeuron() {
		SOMLattice s = new SOMLattice(5, 5, 5);
		assertEquals(s.getNeuron(1, 4).getType(), NeuronType.SOM);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.SOMLattice#getWidth()}.
	 */
	@Test
	public final void testGetWidth() {
		SOMLattice s = new SOMLattice(5, 5, 5);
		assertEquals(5, s.getHeight());
		assertEquals(5, s.getWidth());
		assertEquals(5, s.getNeuron(0, 0).getWeights().size());
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.SOMLattice#getHeight()}.
	 */
	@Test
	public final void testGetHeight() {
		SOMLattice s = new SOMLattice(5, 5, 5);
		assertEquals(5, s.getHeight());
		assertEquals(5, s.getWidth());
		assertEquals(5, s.getNeuron(0, 0).getWeights().size());
	}

	/**
	 * Test method for {@link com.neuralnetwork.shared
	 * .neurons.SOMLattice#getBMU(
	 * com.neuralnetwork.shared.neurons.SOMLayer)}.
	 */
	@Test
	public final void testGetBMU() {
		SOMLattice s = new SOMLattice(5, 5, 5);		
		SOMLayer i = new SOMLayer();
		i.add(0.0);
		i.add(0.0);
		i.add(0.0);
		i.add(0.0);
		i.add(0.0);
		assertEquals(s.getBMU(i).getType(), NeuronType.SOM);
	}

}
