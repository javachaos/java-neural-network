package com.neuralnetwork.shared.functions;

/**
 * Abstract Function class.
 * @author fred
 *
 */
public abstract class AbstractFunction implements
    IActivationFunction {
    
    /**
     * The function id for this function.
     * Default function id is Sigmoid.
     */
    private FunctionType functionId = FunctionType.SIGMOID;
    
    /**
     * Create a new AbstractFunction.
     * @param f
     *      the function type to create.
     */
    public AbstractFunction(final FunctionType f) {
        this.functionId = f;
    }
    
    @Override
    public final void changeFunction(final FunctionType f) {
    	this.functionId = f;
    }
    
    @Override
    public final double activate(final double v) {
        switch(functionId) {
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
        switch(functionId) {
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
        return functionId;
    }
    
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + functionId.hashCode();
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
        if (!(obj instanceof SigmoidFunction)) {
            return false;
        }
        
        return true;
    }
    
}
