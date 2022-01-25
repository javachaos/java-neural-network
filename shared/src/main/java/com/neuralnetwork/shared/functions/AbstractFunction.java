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
package com.neuralnetwork.shared.functions;

/**
 * Abstract Function class.
 * @author fred
 *
 */
public abstract class AbstractFunction implements
    IActivationFunction {
    
    /**
     * Default function id is Sigmoid.
     */
    private FunctionType defaultFunctionId = FunctionType.SIGMOID;
    
    /**
     * Create a new AbstractFunction.
     * @param f
     *      the function type to create.
     */
    public AbstractFunction(final FunctionType f) {
        this.defaultFunctionId = f;
    }
    
    @Override
    public final void changeFunction(final FunctionType f) {
    	this.defaultFunctionId = f;
    }
    
    @Override
    public final double activate(final double v) {
        switch(defaultFunctionId) {
            case LINEAR:
                return v;
            case SIGMOID:
                return 1 / (1 + Math.exp(-v));
            default:
            	return 0;
        }
    }
    
    @Override
    public final double derivative(final double v) {
        switch(defaultFunctionId) {
            case LINEAR:
                return 1;
            case SIGMOID:
                return activate(v) * (1.0 - activate(v));
            default:
            	return 0;
        }
    }
    
    @Override
    public final FunctionType getFunctionType() {
        return defaultFunctionId;
    }
    
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + defaultFunctionId.hashCode();
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        return obj instanceof SigmoidFunction;
    }
    
}
