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
