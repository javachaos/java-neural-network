/*******************************************************************************
 * Copyright (c) 2014 Fred Laderoute.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *     Fred Laderoute - initial API and implementation
 *******************************************************************************/
package com.neuralnetwork.shared.functions;

/**
 * Linear activation function.
 * @author fredladeroute
 *
 */
public final class LinearFunction extends 
        AbstractFunction implements IActivationFunction {

    /**
     * Construct a new Sigmoid function.
     */
    public LinearFunction() {
        super(FunctionType.LINEAR);
    }
}