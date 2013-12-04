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
package com.neuralnetwork.shared.training;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neuralnetwork.shared.util.Pair;

/**
 * Represents a Training data set.
 * 
 * @author fredladeroute
 *
 */
public class TrainingSet {

    /**
     * Logger instance.
     */
    public static final Logger LOGGER = 
            LoggerFactory.getLogger(TrainingSet.class);
    
    /**
     * The raw data values.
     */
    private Set<Pair<Double[], Double[]>> data;
    
    /**
     * Construct a training set.
     */
    public TrainingSet() {
        data = new HashSet<Pair<Double[], Double[]>>();
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
            final Double[] rawData, final Double[] expectedData) {
        if (rawData != null && expectedData != null) {
            Pair<Double[], Double[]> pair =
                    new Pair<Double[], Double[]>(rawData, expectedData);
            data.add(pair);
        } else {
            LOGGER.error("Cannot add null training samples.");
        }
    }
}
