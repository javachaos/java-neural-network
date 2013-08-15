package com.neuralnetwork.shared.exceptions;

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
    public OutOfBoundsException(String msg) {
        super(msg);
    }

}
