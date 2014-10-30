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
package com.neuralnetwork.shared.values;

/**
 * Error Value.
 * 
 * @author fredladeroute
 *
 */
public class ErrorValue extends DoubleValue {

	/**
	 * Default error value.
	 */
	public static final ErrorValue E_0_00001 = new ErrorValue(0.00001);
	
	/**
	 * Default error value.
	 */
	public static final ErrorValue ZERO = new ErrorValue(0.0);
	
    /**
     * Construct an error value.
     * 
     * @param v
     *      the value to set for this error value
     */
    public ErrorValue(final double v) {
        super(v);
    }
}
