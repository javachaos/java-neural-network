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
/*
 * SOMLattice.java
 *
 * Created on December 13, 2002, 2:16 PM
 */

package com.neuralnetwork.shared.neurons;

import com.neuralnetwork.shared.layers.Layer;

/**
 * Represents a SOM Lattice.
 * 
 * 
 *
 */
public class SOMLatticeImpl implements SOMLattice {

	private final int width;
    private final int height;

	private final SOMNeuronImpl[][] lattice;
	
	/**
	 * Constructs a SOM Lattice.
	 * 
	 * @param w
	 *         the desired width of the lattice
	 *         
	 * @param h
	 *         the desired height of the lattice
	 *         
	 * @param vectorLength
	 *         the length of input vectors to this lattice
	 */
	public SOMLatticeImpl(final int w, final int h, final int vectorLength) {
		width = w;
		height = h;
		lattice = new SOMNeuronImpl[width][height];
		
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				lattice[x][y] = new SOMNeuronImpl(vectorLength);
				lattice[x][y].setX(x);
				lattice[x][y].setY(y);
			}
		}
	}
	
	@Override
	public final SOMNeuron getNeuron(final int x, final int y) {
		return lattice[x][y];
	}

	@Override
	public Layer<SOMNeuron> getLayer(int idx) {
		return null;
	}

	@Override
	public final int getWidth() {
		return width;
	}
	
	@Override
	public final int getHeight() {
		return height;
	}

	@Override
	public int getSize() {
		return 0;
	}

	@Override
	public final SOMNeuron getBMU(final SOMLayerImpl inputVector) {
	    
	    SOMNeuronImpl bmu = lattice[0][0];
		double bestDist = inputVector.dist(bmu.getWeights());
		double dist;
		
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				dist = inputVector.dist(lattice[x][y].getWeights());
				if (dist < bestDist) {
					bmu = lattice[x][y];
					bestDist = dist;
				}
			}
		}
		
		return bmu;
	}
	
}
