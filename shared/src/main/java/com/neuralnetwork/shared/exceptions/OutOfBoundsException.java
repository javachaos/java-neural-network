/*******************************************************************************
 * Copyright (c) 2014 Fred Laderoute.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *     Fred Laderoute - initial API and implementation
 *******************************************************************************/
package com.neuralnetwork.shared.exceptions;

/**
 * OutOfBoundsException runtime exception thrown when
 * an input value is found to be out of bounds.
 *
 * @author fredladeroute
 *
 */
public class OutOfBoundsException extends RuntimeException {

    /**
     * Serial Version UID.
     */
    private static final long serialVersionUID = -8321102153473257324L;

    /**
     * Construct a new OutOfBoundsException.
     *
     * @param msg
     *      the message to display to the user.
     */
    public OutOfBoundsException(final String msg) {
        super(msg);
    }

}
