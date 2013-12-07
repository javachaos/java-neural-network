/*******************************************************************************
 * Copyright (c) 2013 Fred Laderoute.
 * All rights reserved. This program and the accompanying
 * materials are made available under the terms of the GNU 
 * Public License v3.0 which accompanies this distribution, 
 * and is available at http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *      Fred Laderoute - initial API and implementation
 *******************************************************************************/
package com.neuralnetwork.shared.values;

/**
 * Represents a value of one.
 * 
 * @author fredladeroute
 *
 */
public class OneValue extends DoubleValue implements IValue<Double> {

    /**
     * Constructor for a double value,
     * initialized with value 0.
     */
    public OneValue() {
    	super(1.0);
    }
}
