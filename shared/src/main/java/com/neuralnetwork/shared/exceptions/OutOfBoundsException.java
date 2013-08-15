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
