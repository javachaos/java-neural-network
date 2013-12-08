package com.neuralnetwork.shared.tests.values;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.neuralnetwork.shared.values.Constants;

/**
 * Test the constants class.
 * @author fred
 *
 */
public final class ConstantsTest {
    
    /**
     * Test the Constants class.
     */
    @Test
    public void testConstants() {
        assertEquals(Constants.ONE, 1);
        assertNotNull(Constants.class);
        assertTrue(Constants.isEqual(Constants.ONE, Constants.ONE_D));
        assertFalse(Constants.isEqual(Constants.ONE, Constants.TWO));
    }
}
