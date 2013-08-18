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
 * This class represents a GenericValue.
 * 
 * @author fredladeroute
 *
 * @param <T>
 */
public abstract class GenericValue<T extends Number> implements IValue<T> {

    /**
     * Defines a positive value.
     */
    protected static final boolean POSITIVE = true;
    
    /**
     * Defines a negative value.
     */
    protected static final boolean NEGATIVE = false;
    /**
     * The value of this generic value.
     */
    private T value;
    
    @Override
    public final void setValue(final T t) {
        this.value = t;
    }

    @Override
    public final T getValue() {
        return value;
    }

}
