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
package com.neuralnetwork.shared.functions;

/**
 * Linear activation function.
 */
public final class LinearFunction extends BaseFunction {

    public LinearFunction() {
        super(new Function(), FunctionType.LINEAR);
    }

    @Override
    public double derivative(double v) {
        return 1.0;
    }

    @Override
    public double activate(double v) {
        return v;
    }

}