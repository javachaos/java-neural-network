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
package com.neuralnetwork.shared.exceptions;

/**
 * NeuronLinkException runtime exception thrown when
 * there is an error linking two neurons.
 *  
 * @author fredladeroute
 *
 */
public class NeuronLinkException extends RuntimeException {

    /**
     * Serial Version UID.
     */
    private static final long serialVersionUID = -8321102153473257324L;
 
    /**
     * Construct a new NeuronLinkException.
     * 
     * @param msg
     *      the message to display to the user.
     */
    public NeuronLinkException(final String msg) {
        super(msg);
    }

}