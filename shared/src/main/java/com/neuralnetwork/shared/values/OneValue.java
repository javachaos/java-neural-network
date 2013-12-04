package com.neuralnetwork.shared.values;

/**
 * Represents a value of one.
 * 
 * @author fredladeroute
 *
 */
public class OneValue extends GenericValue<Double> implements IValue<Double> {

    /**
     * Constructor for a double value,
     * initialized with value 0.
     */
    public OneValue() {
        super();
        this.setSign(POSITIVE);
        this.setValue(1.0);
    }
    
    /**
     * Constructor for a one value,
     * with initialized value to initialValue.
     * Note: this variation of the constructor is not implemented and
     * will not be implemented since the OneValue is a constant value.
     * 
     * @param initialValue
     *      the initial value for this DoubleValue
     */
    public OneValue(final double initialValue) {
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
        return "1";
    }

}
