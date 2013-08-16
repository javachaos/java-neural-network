package com.neuralnetwork.shared.values;


/**
 * A random value.
 * @author fredladeroute
 * 
 * @param <T>
 *      the type of random value to get
 *
 */
public class RandomValue<T extends Number> extends GenericValue<T> {

    /**
     * The value of type T of this random value.
     */
    private Number value;
    
    /**
     * Construct a new random value.
     * from [0,1].
     */
    @SuppressWarnings("unchecked")
    public RandomValue() {
        this.value = Math.random();
        this.setValue((T) value);
    }

    @Override
    public final void updateValue(final IValue<T> v) {
        this.setValue(v.getValue());
    }

    @Override
    public final boolean getSign() {
        return POSITIVE;
    }

    @Override
    public void setSign(final boolean sign) { }
}
