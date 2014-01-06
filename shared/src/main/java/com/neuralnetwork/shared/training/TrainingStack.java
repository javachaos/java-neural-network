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
package com.neuralnetwork.shared.training;

import java.util.Stack;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neuralnetwork.shared.util.Pair;

/**
 * Represents a Training data set.
 * 
 * @author fredladeroute
 *
 */
public class TrainingStack {

    /**
     * Logger instance.
     */
    public static final Logger LOGGER = 
            LoggerFactory.getLogger(TrainingStack.class);
    
    /**
     * The raw data values.
     */
    private Stack<Pair<Vector<Double>, Vector<Double>>> data;

    /**
     * The sample size of this training stack.
     */
	private int sampleSize;
    
    /**
     * Construct a training set.
     * 
     * @param size
     *      the initial size of the training stack.
     */
    public TrainingStack(final int size) {
    	this.sampleSize = size;
        data = new Stack<Pair<Vector<Double>, Vector<Double>>>();
    }
    
    /**
     * Add a training sample to this TrainingSet.
     * 
     * @param rawData
     *      the raw data of this training sample
     *      
     * @param expectedData
     *      the expected results vector of this training sample
     */
    public final void addTrainingSample(
            final Vector<Double> rawData, final Vector<Double> expectedData) {
        if (rawData != null
        		&& expectedData != null
        		&& rawData.size() == sampleSize
        		&& expectedData.size() == sampleSize) {
            Pair<Vector<Double>, Vector<Double>> pair =
                    new Pair<Vector<Double>,
                    Vector<Double>>(rawData, expectedData);
            data.add(pair);
        } else {
            LOGGER.error("Cannot add null training samples.");
        }
    }
    
    /**
     * Get the next training sample and remove it from the set.
     * 
     * @return
     *      the next training sample, null if set is empty
     */
    public final Pair<Vector<Double>, Vector<Double>> popSample() {
        return data.pop();
    }
    
    /**
     * Get the next training sample and remove it from the set.
     * 
     * @return
     *      the next training sample, null if set is empty
     */
    public final Pair<Vector<Double>, Vector<Double>> peekSample() {
        return data.peek();
    }
    
    /**
     * Return the set of training samples from this training set. Were the 
     * first item in each pair of the set is the raw new data
     * and the second item is the expected data.
     * 
     * @return
     *      the data from this training set.
     */
    public final Stack<Pair<Vector<Double>, Vector<Double>>> getData() {
        return data;
    }

    /**
     * Return the size of each sample vector.
     * 
     * @return
     * 		the size of each sample vector
     */
	public final int getSampleSize() {
		return sampleSize;
	}

}
