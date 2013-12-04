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
 * A random value.
 * @author fredladeroute
 *
 */
public class RandomValue extends GenericValue<Double> implements IValue<Double> {

    /**
     * The value of type T of this random value.
     */
    private Double value;
    
    /**
     * Construct a new random value.
     * from [0,1].
     */
    public RandomValue() {
        this.value = Math.random();
    }

    @Override
    public final boolean getSign() {
        return POSITIVE;
    }

    @Override
    public final void setSign(final boolean sign) { 
        if (!sign && value < 0) {
            this.setValue(getValue() * -1.0);
        } else if (sign && value > 0) {
            this.setValue(getValue() * -1.0);
        }
    }

    @Override
    public final void updateValue(final IValue<?> v) {
        this.setValue(getValue() + v.getValue().doubleValue());
    }
    
    @Override
    public final String toString() {
        return "" + value;
    }
    
}
