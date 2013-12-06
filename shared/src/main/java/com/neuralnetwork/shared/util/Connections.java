package com.neuralnetwork.shared.util;

import java.util.Iterator;

import com.neuralnetwork.shared.layers.ILayer;
import com.neuralnetwork.shared.neurons.INeuron;

/**
 * Utility class to create connection between layers.
 * 
 * @author fredladeroute
 *
 */
public final class Connections {
	
	/**
	 * Creates a new instance of the connections class.
	 */
	private static final Connections instance = new Connections();
	
	/**
	 * Unused Constructor.
	 */
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
    public <T1 extends INeuron, T2 extends INeuron> 
            void create(final ILayer<T1> l1, final ILayer<T2> l2) {
        Iterator<T1> iter1 = l1.iterator();
        Iterator<T2> iter2 = l2.iterator();
        T1 tmp = null;
        while (iter1.hasNext()) {
            tmp = iter1.next();
            while (iter2.hasNext()) {
                tmp.addOutputLink(iter2.next());
            }
        }
    }

	public static Connections getInstance() {
		return instance;
	}
}
