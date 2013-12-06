package com.neuralnetwork.shared.functions;

/**
 * Sigmoid activation function.
 * @author fredladeroute
 *
 */
public final class SigmoidFunction implements IActivationFunction {

	private final FunctionType functionId = FunctionType.SIGMOID;
	
	@Override
    public double activate(final double x) {
        return 1 / (1 + Math.exp(-x));
    }

    @Override
    public double derivative(final double x) {
        return activate(x) * (1.0 - activate(x));
    }

    @Override
	public FunctionType getFunctionType() {
		return functionId;
	}
    
    /* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
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
	public boolean equals(Object obj) {
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
