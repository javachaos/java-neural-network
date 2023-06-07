package com.github.javachaos.javaneuralnetwork.jbrain.exceptions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Simple exception wrapper.
 */
public class JbrainException extends RuntimeException {

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 6112395321058090701L;

    /**
     * Logger instance.
     */
    public static final Logger LOGGER =
            LogManager.getLogger(JbrainException.class);
	
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
