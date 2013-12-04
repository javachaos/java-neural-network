/*******************************************************************************
 * Copyright (c) 2013 Fred Laderoute.
 * All rights reserved. This program and the accompanying
 * materials are made available under the terms of the GNU 
 * Public License v3.0 which accompanies this distribution, 
 * and is available at http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *      Fred Laderoute - initial API and implementation
 *******************************************************************************/
package com.neuralnetwork.shared.nodes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neuralnetwork.shared.network.INetwork;

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
     * Neural network associated with this SOM Neuron.
     */
    private INetwork neuralNetwork;
    
    /**
     * The x and y co ordinates of this Neuron within the Lattice.
     */
    private int xPos, yPos;
    
    /**
     * Creates a new SOM Neuron, initializing the 
     * underlying neural network associated with this neuron.
     */
    private SOMNeuron() {
        //neuralNetwork = new Network();
        //TODO Fix network creation.
        // And come up with a way to store network configs
        // for later use. For example store 
        // a predefined network config for every neuron.
    }
    
    /**
     * Creates a new SOM Neuron.
     * 
     * @param numWeights
     *      the number of weights attached to this neuron
     */
    public SOMNeuron(final int numWeights) {
    	this();
        weights = new SOMLayer();
        for (int x = 0; x < numWeights; x++) {
            weights.addElement(new Double(Math.random()));
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
    	this();
        weights = new SOMLayer();
        for (int i = 0; i < numWeights; i++) {
            weights.addElement(new Double(Math.random()));
        }
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
            LOGGER.warn("Weight index out of bounds.");
            return;
        }
        weights.setElementAt(new Double(value), wIndex);
    }
    
    @Override
    public final double getWeight(final int wIndex) {
        if (wIndex >= weights.size()) {
            LOGGER.warn("Weight index out of bounds. "
                    + "Returning (index MOD size).");
            return wIndex % weights.size();
        }
        
        return (Double) weights.elementAt(wIndex);
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
            wt = (Double) weights.elementAt(i);
            vw = (Double) input.elementAt(i);
            wt += distanceFalloff * learningRate * (vw - wt);
            weights.setElementAt(new Double(wt), i);
        }
    }

    /**
     * @return the neuralNetwork
     */
    public final INetwork getNeuralnetwork() {
        return neuralNetwork;
    }
}
