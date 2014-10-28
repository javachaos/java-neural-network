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
package com.neuralnetwork.shared.values;

import java.util.Random;


/**
 * A random value.
 * @author fredladeroute
 *
 */
public class RandomValue extends DoubleValue implements IValue<Double> {

    /**
     * Minimum value.
     */
    private static final Random RAND = new Random();

    /**
     * Construct a new random value.
     * from [-1,1].
     */
    public RandomValue() {
        super((RAND.nextDouble() * 2) - 1);
    }

}
