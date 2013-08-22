package com.neuralnetwork.shared.values;

/**
 * Represents the value zero.
 * 
 * @author fredladeroute
 *
 */
public class ZeroValue extends GenericValue<Double> implements IValue<Double> {
    
    /**
     * Constructor for a zero value,
     * initialized with value 0.
     */
    public ZeroValue() {
        super();
        this.setSign(POSITIVE);
        this.setValue(0.0);
    }
    
    /**
     * Constructor for a zero value,
     * with initialized value to initialValue.
     * Note: this variation of the constructor is not implemented and
     * will not be implemented since the ZeroValue is a constant value.
     * 
     * @param initialValue
     *      the initial value for this DoubleValue
     */
    public ZeroValue(final double initialValue) {
        //Unused.
    }

    @Override
    public final void setSign(final boolean s) {
        //unused.
    }

    @Override
    public final boolean getSign() {
        return getValue() > 0;
    }

    @Override
    public final void updateValue(final IValue<?> v) {
        //unused.
    }


    @Override
    public final String toString() {
        return "0";
    }
}
