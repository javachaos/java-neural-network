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
package com.neuralnetwork.shared.network;


/**
 * Represents a NeuralNetContext which stores a reference to the network.
 * 
 * @author fredladeroute
 * 
 */
public interface NeuralNetContext {

    /**
     * Get a reference to the network.
     * @return
     *      a reference to the network
     */
    Network getNetwork();
    
    /**
     * Return true if is processing.
     * 
     * @return
     * 		true if is running.
     */
    boolean isRunning();

    /**
     * Set isRunning marker.
     * 
     * @param b
     * 		the boolean value to set isRunning to.
     */
	void setRunning(boolean b);
}
