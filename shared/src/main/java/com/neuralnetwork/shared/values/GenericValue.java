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
