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
 * Represents a double value.
 * 
 * @author fredladeroute
 *
 */
public class DoubleValue extends GenericValue<Double> implements IValue<Double> {
    
    /**
     * Constructor for a double value,
     * initialized with value 0.
     */
    public DoubleValue() {
        super();
        this.setSign(POSITIVE);
        this.setValue(0.0);
    }
    
    /**
     * Constructor for a double value,
     * with initialized value to initialValue.
     * 
     * @param initialValue
     *      the initial value for this DoubleValue
     */
    public DoubleValue(final double initialValue) {
        super();
        this.setSign(POSITIVE);
        this.setValue(initialValue);
    }

    @Override
    public final void setSign(final boolean s) {
        if (s && getValue() < 0 || !s && getValue() > 0) {
            this.setValue(getValue() * -1.0);
        }
    }

    @Override
    public final boolean getSign() {
        return getValue() > 0;
    }

    @Override
    public final void updateValue(final IValue<?> v) {
        this.setValue(this.getValue() + v.getValue().doubleValue());
    }

}
