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
package com.neuralnetwork.shared.layers;

import com.neuralnetwork.shared.neurons.HiddenNeuron;

/**
 * Interface for an IHiddenLayer.
 * @author fredladeroute
 *
 */
public interface HiddenLayer extends Layer<HiddenNeuron>, Buildable, Comparable<HiddenLayer> {
    int getIndex();
}
