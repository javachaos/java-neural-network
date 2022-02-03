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
 * Neural network context, stores a reference to the network.
 * Intended to be pass around during training.
 * @author fred
 *
 */
public class NetworkContext implements  NeuralNetContext {
    
    /**
     * The network reference.
     */
    private final Network network;
    
    /**
     * True if the network is processing.
     */
    private boolean isRunning;
    
    /**
     * Create a new neuralnet context with network n.
     * @param n
     *      the network for this context.
     */
    public NetworkContext(final Network n) {
        this.network = n;
    }

    @Override
    public final Network getNetwork() {
        return network;
    }

	@Override
	public final boolean isRunning() {
		return isRunning;
	}

	@Override
	public final void setRunning(final boolean running) {
		this.isRunning = running;
	}
}
