/*******************************************************************************
 * Copyright (c) 2014 Fred .
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *     Fred  - initial API and implementation
 ******************************************************************************/
package com.neuralnetwork.shared.util;

import java.util.Iterator;

import com.neuralnetwork.shared.layers.Layer;
import com.neuralnetwork.shared.neurons.Neuron;

/**
 * Utility class to create connection between layers.
 */
public final class Connections {

	private static final Connections INSTANCE = new Connections();

	private Connections() {
	}
    
    /**
     * Create a full connection between layers l1 and l2.
     * 
     * @param l1
     *      the parent layer.
     *      
     * @param l2
     *      the child layer.
     *      
     * @param <T1>
     *      the type of the parent layer
     * 
     * @param <T2>
     *      the type of the child layer
     */
    public <T1 extends Neuron, T2 extends Neuron>
            void create(final Layer<T1> l1, final Layer<T2> l2) {
        Iterator<T1> iterator = l1.iterator();
        Iterator<T2> iterator1 = l2.iterator();
        T1 tmp;
        while (iterator.hasNext()) {
            tmp = iterator.next();
            while (iterator1.hasNext()) {
                tmp.addOutputLink(iterator1.next());
            }
        }
    }

    /**
     * Get an instance of the Connections class.
     * @return
     *      an instance of the Connections class.
     */
	public static Connections getInstance() {
		return INSTANCE;
	}
}
