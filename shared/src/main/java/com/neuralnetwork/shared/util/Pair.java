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
package com.neuralnetwork.shared.util;

/**
 * Represents a pair of objects <T1> and <T2>.
 * 
 * @author fredladeroute
 *
 * @param <T1>
 *      the type of the first object
 *      
 * @param <T2>
 *      the type of the second object
 */
public class Pair<T1, T2> {

    /**
     * The first item in the pair.
     */
    private T1 first;
    
    /**
     * The second item in the pair.
     */
    private T2 second;
    
    /**
     * Constructs a pair.
     * 
     * @param f
     *      the first item in the pair
     *      
     * @param s
     *      the second item in the pair
     */
    public Pair(final T1 f, final T2 s) {
        this.first = f;
        this.second = s;
    }

    /**
     * @return the second
     */
    public final T2 getSecond() {
        return second;
    }

    /**
     * @param s the second to set
     */
    public final void setSecond(final T2 s) {
        this.second = s;
    }

    /**
     * @return the first
     */
    public final T1 getFirst() {
        return first;
    }

    /**
     * @param f the first to set
     */
    public final void setFirst(final T1 f) {
        this.first = f;
    }
}
