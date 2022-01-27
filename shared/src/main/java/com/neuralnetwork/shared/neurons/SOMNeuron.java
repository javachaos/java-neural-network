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
    
    /**
     * Logger instance.
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(SOMNeuron.class);
    
    /**
     * The weights for this Neuron.
     */
    private SOMLayer weights;
    
    /**
     * The type of this neuron.
     */
    private final NeuronType type = NeuronType.SOM;
    
    /**
     * Neural network associated with this SOM Neuron.
     */
    private final INetwork neuralNetwork;
    
    /**
     * The x and y co ordinates of this Neuron within the Lattice.
     */
    private int xPos, yPos;
    
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
            weights.addElement(Double.valueOf(Math.random()));
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
            weights.addElement(Double.valueOf(Math.random()));
        }
        setX(x);
        setY(y);
    }
    
    @Override
    public final void setX(final int xpos) {
        xPos = xpos;
    }

    @Override
    public final void setY(final int ypos) {
        yPos = ypos;
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
        int x, y;
        x = getX() - n.getX();
        x = (int) Math.pow(x, 2);
        y = getY() - n.getY();
        y = (int) Math.pow(x, 2);
        return x + y;
    }
    
    @Override
    public final void setWeight(final int wIndex, final double value) {
        if (wIndex >= weights.size()) {
            throw new IndexOutOfBoundsException(
            		"Weight index was out of bounds.");
        }
        weights.setElementAt(Double.valueOf(value), wIndex);
    }
    
    @Override
    public final double getWeight(final int wIndex) {
        if (wIndex >= weights.size()) {
            LOGGER.warn("Weight index out of bounds. "
                    + "Returning (index MOD size).");
            return weights.elementAt(wIndex % weights.size());
        }
        
        return weights.elementAt(wIndex);
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
        double wt, vw;
        for (int i = 0; i < weights.size(); i++) {
            wt = weights.elementAt(i);
            vw = input.elementAt(i);
            wt += distanceFalloff * learningRate * (vw - wt);
            weights.setElementAt(Double.valueOf(wt), i);
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
		return type;
	}
}
