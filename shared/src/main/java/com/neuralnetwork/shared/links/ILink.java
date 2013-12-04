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
package com.neuralnetwork.shared.links;

import com.neuralnetwork.shared.nodes.INeuron;
import com.neuralnetwork.shared.values.IValue;

/**
 * Represents the link between to INode classes.
 * 
 * @author fredladeroute
 *
 */
public interface ILink {
    
    /**
     * Get the head of the link.
     * 
     * @return 
     *      the head of the link.
     */
    INeuron getHead();
    
    /**
     * Get the tail of the link.
     * 
     * @return 
     *      the tail of the link.
     */
    INeuron getTail();
    
    /**
     * Set the head INode of this ILink.
     *  
     * @param ihead
     *      the INode to set as head for this link
     * 
     */
    void setHead(INeuron ihead);
    
    /**
     * Set the tail INode of this ILink.
     *  
     * @param itail
     *      the INode to set as tail for this link
     */
    void setTail(INeuron itail);
    
    
    /**
     * Return the weight of this link
     * normalized to [0-1].
     * 
     * @return 
     *      the weight of this link.
     */
    IValue<?> getWeight();
    
    /**
     * Update the weight of this link. If the value pushes the weight beyond the
     * bounds of [0-1] the weight will be clamped to either 0 or 1.
     * 
     * @param value
     *      the amount to update the weight by.
     * 
     */
    void updateWeight(IValue<?> value);
    
    /**
     * Change the weight of this ILink to weightValue.
     * 
     * @param weightValue
     *      the value of the new weight for this link.
     */
    void setWeight(IValue<?> weightValue);
    
    /**
     * Returns the age of this link.
     * 
     * <pre>
     * age - The number of times this link is
     *      updated. During training if the age of a link
     *      is less than 3 for 100 training samples the
     *      link dies.
     * </pre>
     * @return 
     *      the age of this link
     */
    int getAge();

}
