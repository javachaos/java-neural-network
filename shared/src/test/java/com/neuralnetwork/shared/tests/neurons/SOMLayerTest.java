/**
 * 
 */
package com.neuralnetwork.shared.tests.neurons;

import static org.junit.Assert.*;

import org.junit.Test;

import com.neuralnetwork.shared.neurons.SOMLayer;

/**
 * @author Fred
 *
 */
public class SOMLayerTest {

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.SOMLayer
	 * #SOMLayer()}.
	 */
	@Test
	public final void testSOMLayer() {
		SOMLayer l = new SOMLayer();

		assertNotNull(l);
		assertEquals(l.size(), 0);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.SOMLayer
	 * #SOMLayer(int)}.
	 */
	@Test
	public final void testSOMLayerInt() {
		SOMLayer l = new SOMLayer(10);

		assertNotNull(l);
		assertEquals(l.size(), 10);
	}

	/**
	 * Test method for {@link com.neuralnetwork
	 * .shared.neurons.SOMLayer
	 * #dist(com.neuralnetwork.shared.neurons.SOMLayer)}.
	 */
	@Test
	public final void testDist() {
		SOMLayer l = new SOMLayer(5);
		SOMLayer l1 = new SOMLayer(5);

		assertEquals(0.0, l.dist(l1), 10 * Math.ulp(0));
		
		l = new SOMLayer();
		l1 = new SOMLayer();
		
		l.add(10.0);
		l.add(10.0);
		l.add(10.0);
		l.add(10.0);
		l.add(10.0);
		l1.add(10.0);
		l1.add(10.0);
		l1.add(10.0);
		l1.add(10.0);
		l1.add(10.0);
		
		assertEquals(0.0, l.dist(l1), 10 * Math.ulp(0));
		
		l1.add(10.0);
		assertFalse(0.0 == l.dist(l1));
	}

}
