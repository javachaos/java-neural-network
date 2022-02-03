/*******************************************************************************
 * Copyright (c) 2014 Fred.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *     Fred - initial API and implementation
 ******************************************************************************/
package com.neuralnetwork.shared.neurons;

import com.neuralnetwork.shared.network.Network;

/**
 * Represents a SOM Neuron.
 * 
 * 
 *
 */
public interface SOMNeuron extends Neuron {

    /**
     * Get the network for this neuron.
     * @return the network for this neuron
     */
    Network getNeuralNetwork();

    /**
     * Set the x co-ordinate.
     * 
     * @param xPosition
     *      the x position
     */
    void setX(final int xPosition);
    
    /**
     * Set the y co-ordinate.
     * 
     * @param yPosition
     *      the y position
     */
    void setY(final int yPosition);
    
    /**
     * Returns the x co-ordinate of this neuron.
     * 
     * @return
     *      the x position of this neuron within
     *      the lattice
     */
    int getX();
    
    /**
     * Returns the y co-ordinate of this neuron.
     * 
     * @return
     *      the y position of this neuron within
     *      the lattice
     */
    int getY();
    
    /**
     * Calculate the distance to the other neuron.
     * 
     * @param n
     *      the other neuron
     *      
     * @return
     *      the square of the distance
     */
    double distanceTo(final SOMNeuron n);
    
    /**
     * Set the weight for the weight at w.
     * 
     * @param w
     *      the index of the weight to set
     *      
     * @param value
     *      the new value of the weight at index w
     */
    void setWeight(final int w, final double value);
    
    /**
     * Return the weight at index w.
     * 
     * @param w
     *      the index to the weight to get
     *      
     * @return
     *      the weight at index w
     */
    double getWeight(final int w);
    
    /**
     * Return the weight vector.
     * 
     * @return
     *      the weight vector
     */
     SOMLayerImpl getWeights();
    
    /**
     * Update the weights of the neuron based on the weights of the SOM Layer.
     * 
     * @param input
     *      the input value vector
     *      
     * @param learningRate
     *      the rate at which to learn the input
     *      
     * @param distanceFalloff
     *      the threshold fall off value
     */
     void updateWeights(final SOMLayer input,
             final double learningRate, final double distanceFalloff);

     /**
      * The type of neuron.
      * @return
      *      the type of neuron.
      */
     NeuronType getType();
}
