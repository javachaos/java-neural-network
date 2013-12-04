package com.neuralnetwork.shared.tests.exceptions;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neuralnetwork.shared.exceptions.OutOfBoundsException;

/**
 * OutOfBoundsException Test.
 * @author fred
 *
 */
public class OutOfBoundsExceptionTest {
    
    /**
     * Logger instance.
     */
    private static final Logger LOGGER = 
            LoggerFactory.getLogger(OutOfBoundsExceptionTest.class);
    
    /**
     * Exception message used for testing exception.
     */
    private static final String EXCEPTION_MSG = "Test exception.";
    
    /**
     * Test the out of bounds exception.
     */
    @Test
    public final void test() {
        try {
            throw new OutOfBoundsException(EXCEPTION_MSG);
        } catch (OutOfBoundsException e) {
            LOGGER.debug(e.getMessage());
            assertEquals(e.getMessage(), EXCEPTION_MSG);
        }
    }
    
}
