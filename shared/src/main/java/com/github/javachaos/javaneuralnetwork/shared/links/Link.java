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
package com.github.javachaos.javaneuralnetwork.shared.links;

import com.github.javachaos.javaneuralnetwork.shared.neurons.Neuron;

/**
 * Represents the link between two neuron classes.
 */
public interface Link {
    
    /**
     * Get the head of the link.
     * 
     * @return 
     *      the head of the link.
     */
    Neuron getHead();
    
    /**
     * Get the tail of the link.
     * 
     * @return 
     *      the tail of the link.
     */
    Neuron getTail();
    
    /**
     * Set the head neuron of this link.
     *  
     * @param head
     *      the neuron to set as head for this link
     * 
     */
    void setHead(Neuron head);
    
    /**
     * Set the tail neuron of this link.
     *  
     * @param tail
     *      the neuron to set as tail for this link
     */
    void setTail(Neuron tail);
    
    
    /**
     * Return the weight of this link
     * normalized to [0-1].
     * 
     * @return 
     *      the weight of this link.
     */
    double getWeight();
    
    /**
     * Update the weight of this link. If the value pushes the weight beyond the
     * bounds of [0-1] the weight will be clamped to either 0 or 1.
     * 
     * @param value
     *      the amount to update the weight by.
     * 
     */
    void updateWeight(double value);
    
    /**
     * Change the weight of this link to weightValue.
     * 
     * @param weightValue
     *      the value of the new weight for this link.
     */
    void setWeight(double weightValue);
    
    /**
     * Returns the age of this link.
     * 
     * <pre>
     * The number of times this link is
     * updated. During training if the age of a link
     * is greater than 10000 the link dies.
     * (loses its previous weight value a sort of amnesia factor)
     * </pre>
     * @return 
     *      the age of this link
     */
    int getAge();

}
