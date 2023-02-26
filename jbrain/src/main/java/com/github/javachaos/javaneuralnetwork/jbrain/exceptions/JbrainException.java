package com.github.javachaos.javaneuralnetwork.jbrain.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JbrainException extends RuntimeException {

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 6112395321058090701L;

    /**
     * Logger instance.
     */
    public static final Logger LOGGER = 
            LoggerFactory.getLogger(JbrainException.class);
	
	/**
	 * Default ctor.
	 */
	public JbrainException() {
		super();
		LOGGER.error("A runtime exception occured.");
	}
	
	/**
	 * Jbrain runtime exception constructor.
	 * 
	 * @param message
	 * 		the message of the exception.
	 */
	public JbrainException(String message) {
		super(message);
		LOGGER.error(message);
	}

}
