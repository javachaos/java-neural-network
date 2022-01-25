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
 * Defines the type of layer.
 * 
 * @author fredladeroute
 *
 */
public enum LayerType {
    
    /**
     * Hidden layer.
     */
    HIDDEN,
    
    /**
     * Input layer.
     */
    INPUT,
    
    /**
     * Output layer.
     */
    OUTPUT
}
