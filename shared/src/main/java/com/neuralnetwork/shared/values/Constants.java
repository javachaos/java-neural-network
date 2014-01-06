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
package com.neuralnetwork.shared.values;

/**
 * Class filled with constant values.
 * @author fred
 *
 */
public final class Constants {
    
    static {
        new Constants();
    }
    
    /**
     * Unused ctor.
     */
    private Constants() {
    }
    
    /**
     * Test if two numbers are equal.
     * @param n
     *      the first number.
     * @param n1
     *      the second number.
     * @return
     *      true if they are equal
     */
    public static boolean isEqual(final Number n, final Number n1) {
        return n.intValue() == n1.intValue();
    }
    
    /**
     * Constant double value for one.
     */
    public static final double ONE_D = 1.0;
    
    /**
     * Constant double value for two.
     */
    public static final double TWO_D = 2.0;
    
    /**
     * Constant double value for three.
     */
    public static final double THREE_D = 3.0;
    
    /**
     * Constant double value for four.
     */
    public static final double FOUR_D = 4.0;
    
    /**
     * Constant double value for five.
     */
    public static final double FIVE_D = 5.0;
    
    /**
     * Constant double value for ten.
     */
    public static final double TEN_D = 10.0;
    
    /**
     * Constant double value for eleven.
     */
    public static final double ELEVEN_D = 11.0;
    
    /**
     * Constant double value for one.
     */
    public static final int ONE = 1;
    
    /**
     * Constant double value for two.
     */
    public static final int TWO = 2;
    
    /**
     * Constant double value for three.
     */
    public static final int THREE = 3;
    
    /**
     * Constant double value for four.
     */
    public static final int FOUR = 4;
    
    /**
     * Constant double value for five.
     */
    public static final int FIVE = 5;
    
    /**
     * Constant double value for ten.
     */
    public static final int TEN = 10;
}
