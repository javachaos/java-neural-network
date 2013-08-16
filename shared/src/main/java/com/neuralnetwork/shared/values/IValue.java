package com.neuralnetwork.shared.values;

/**
 * Represents a value to be presented to an INode.
 * 
 * @author fredladeroute
 *
 * @param <T>
 *      the type of IValue this is
 */
public interface IValue<T extends Number> {

    /**
     * Set the value of this IValue.
     * 
     * @param t
     *      the value of type T to set 
     */
    void setValue(T t);
    
    /**
     * Return the actual value of type <T>
     * from this IValue.
     * 
     * @return
     *      the value of type <T>
     */
    T getValue();
    
    /**
     * Update this IValue by the amount t.
     * 
     * @param value
     *      the amount to add or subtract from this value
     */
    void updateValue(IValue<T> value);
    
    /**
     * Set the sign of this value.
     * True for positive, false for negative.
     * 
     * @param sign
     *      the sign to set this IValue to
     */
    void setSign(boolean sign);
    
    /**
     * Returns the sign of this IValue.
     * 
     * @return
     *      the sign of this IValue, true is
     *      positive and false is negative.
     */
    boolean getSign();
}