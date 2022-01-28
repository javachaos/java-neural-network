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
package com.neuralnetwork.shared.tests.exceptions;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neuralnetwork.shared.exceptions.OutOfBoundsException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * OutOfBoundsException Test.
 * @author fred
 *
 */
class OutOfBoundsExceptionTest {
    
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
    final void test() {
        try {
            throw new OutOfBoundsException(EXCEPTION_MSG);
        } catch (OutOfBoundsException e) {
            LOGGER.debug(e.getMessage());
            assertEquals(EXCEPTION_MSG, e.getMessage());
        }
    }
    
}
