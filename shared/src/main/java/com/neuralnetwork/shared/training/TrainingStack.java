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
package com.neuralnetwork.shared.training;

import java.util.Stack;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private Stack<Vector<Double>> data;

    /**
     * The number of features for each sample of this training stack.
     */
	private int featureSize;
    
    /**
     * Construct a training set.
     * 
     * @param numFeatures
     *      the initial size of the training stack.
     */
    public TrainingStack(final int numFeatures) {
    	this.featureSize = numFeatures;
        data = new Stack<Vector<Double>>();
    }
    
    /**
     * Add a training sample to this TrainingSet.
     * 
     * @param rawData
     *      the raw data of this training sample
     */
    public final void addTrainingSample(
            final Vector<Double> rawData) {
        if (rawData != null
        		&& rawData.size() == featureSize) {
            data.add(rawData);
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
    public final Vector<Double> popSample() {
        return data.pop();
    }
    
    /**
     * Get the next training sample and remove it from the set.
     * 
     * @return
     *      the next training sample, null if set is empty
     */
    public final Vector<Double> peekSample() {
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
    public final Stack<Vector<Double>> getData() {
        return data;
    }

    /**
     * Return the size of each sample vector.
     * 
     * @return
     * 		the size of each sample vector
     */
	public final int getNumFeatures() {
		return featureSize;
	}

}
