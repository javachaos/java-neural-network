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
package com.github.javachaos.javaneuralnetwork.shared.training;

import java.util.Deque;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

import com.github.javachaos.javaneuralnetwork.shared.util.ListUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Represents a Training data set.
 * 
 * 
 *
 */
public class TrainingStack {

    /**
     * Logger instance.
     */
    public static final Logger LOGGER =
            LogManager.getLogger(TrainingStack.class);
    
    /**
     * The raw data values.
     */
    private final Deque<List<Double>> data;

    /**
     * The number of features for each sample of this training stack.
     */
	private final int featureSize;
    
    /**
     * Construct a training set.
     * 
     * @param numFeatures
     *      the initial size of the training stack.
     */
    public TrainingStack(final int numFeatures) {
    	this.featureSize = numFeatures;
        data = new LinkedBlockingDeque<>();
    }
    
    /**
     * Add a training sample to this TrainingSet.
     * 
     * @param rawData
     *      the raw data of this training sample
     */
    public final void addTrainingSample(
            final List<Double> rawData) {
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
    public final List<Double> popSample() {
        return data.pop();
    }
    
    /**
     * Get the next training sample and remove it from the set.
     * 
     * @return
     *      the next training sample, null if set is empty
     */
    public final List<Double> peekSample() {
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
    public final Deque<List<Double>> getData() {
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
	
	/**
	 * Generate random samples.
	 * 
	 * @param numberOfSamples
	 * 		the number of samples to generate.
	 */
	public final void generateRandomSamples(final int numberOfSamples) {
		for (int i = 0; i < numberOfSamples; i++) {
			addTrainingSample(ListUtils.getRandomVector(featureSize));
		}
	}

}
