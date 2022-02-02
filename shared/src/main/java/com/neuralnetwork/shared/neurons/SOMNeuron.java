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
package com.neuralnetwork.shared.neurons;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neuralnetwork.shared.network.INetwork;
import com.neuralnetwork.shared.util.NetworkConfig;
import com.neuralnetwork.shared.util.NeuralNetBuilder;
import com.neuralnetwork.shared.util.SimpleNetworkConfigs;

/**
 * Represents a SOM Neuron.
 * 
 * @author fredladeroute
 *
 */
public class SOMNeuron implements ISOMNeuron {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(SOMNeuron.class);

    private SOMLayer weights;
    private static final NeuronType TYPE = NeuronType.SOM;
    private final INetwork neuralNetwork;
    private int xPos;
    private int yPos;
    
    /**
     * Creates a new SOM Neuron, initializing the 
     * underlying neural network associated with this neuron.
     * 
     * @param configuration
     *      the configuration to be used in constructing the new network.
     *      
     */
    private SOMNeuron(final NetworkConfig configuration) {
    	NeuralNetBuilder b = 
    			new NeuralNetBuilder(configuration);
    	neuralNetwork = b.build();
    }
    
    /**
     * Creates a new SOM Neuron.
     * 
     * @param numWeights
     *      the number of weights attached to this neuron
     */
    public SOMNeuron(final int numWeights) {
    	this(SimpleNetworkConfigs.CONFIG_5_4_3_4_5);
        weights = new SOMLayer();
        for (int x = 0; x < numWeights; x++) {
            weights.add(Math.random());
        }
    }
    
    /**
     * Creates a new SOM Neuron.
     * 
     * @param numWeights
     *      the number of weights attached to this neuron
     * 
     * @param x
     *      the x co-ordinate of this neuron within the lattice
     *      
     * @param y
     *      the y co-ordinate of this neuron within the lattice
     */
    public SOMNeuron(final int numWeights, final int x, final int y) {
    	this(SimpleNetworkConfigs.CONFIG_5_4_3_4_5);
        weights = new SOMLayer();
        for (int i = 0; i < numWeights; i++) {
            weights.add(Math.random());
        }
        setX(x);
        setY(y);
    }
    
    @Override
    public final void setX(final int xPosition) {
        xPos = xPosition;
    }

    @Override
    public final void setY(final int yPosition) {
        yPos = yPosition;
    }
    
    @Override
    public final int getX() {
        return xPos;
    }
    
    @Override
    public final int getY() {
        return yPos;
    }
    
    @Override
    public final double distanceTo(final SOMNeuron n) {
        double x;
        double y;
        x = getX() - (double) n.getX();
        x = (int) Math.pow(x, 2);
        y = getY() - (double) n.getY();
        y = (int) Math.pow(y, 2);
        return x + y;
    }
    
    @Override
    public final void setWeight(final int wIndex, final double value) {
        if (wIndex >= weights.size()) {
            throw new IndexOutOfBoundsException(
            		"Weight index was out of bounds.");
        }
        weights.set(wIndex, value);
    }
    
    @Override
    public final double getWeight(final int wIndex) {
        if (wIndex >= weights.size()) {
            LOGGER.warn("Weight index out of bounds. "
                    + "Returning (index MOD size).");
            return weights.get(wIndex % weights.size());
        }
        return weights.get(wIndex);
    }
    
    @Override
    public final SOMLayer getWeights() {
        return weights;
    }
    
    @Override
    public final void updateWeights(
            final SOMLayer input, 
            final double learningRate,
            final double distanceFalloff) {
        double wt;
        double vw;
        for (int i = 0; i < weights.size(); i++) {
            wt = weights.get(i);
            vw = input.get(i);
            wt += distanceFalloff * learningRate * (vw - wt);
            weights.set(i, wt);
        }
    }

    /**
     * @return the neuralNetwork
     */
    public final INetwork getNeuralnetwork() {
        return neuralNetwork;
    }

    @Override
	public final NeuronType getType() {
		return TYPE;
	}
}
