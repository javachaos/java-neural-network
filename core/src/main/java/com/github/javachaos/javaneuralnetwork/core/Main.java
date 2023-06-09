/*******************************************************************************
 * Copyright (c) 2013 Fred .
 * All rights reserved. This program and the accompanying
 * materials are made available under the terms of the GNU
 * Public License v3.0 which accompanies this distribution,
 * and is available at http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *      Fred  - initial API and implementation
 ******************************************************************************/
package com.github.javachaos.javaneuralnetwork.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.SplittableRandom;

/**
 * Main class.
 *
 * 
 *
 */
public final class Main {

    private static final Logger LOGGER = LogManager.getLogger(Main.class);

    private static final SplittableRandom rand = new SplittableRandom();
    /**
     * Unused ctor.
     */
    private Main() {
    }

    /**
     * Main method.
     * @param args
     *      command line args
     */
    public static void main(final String[] args) {

        int[] layerSizes = new int[] { 784, 128, 64, 128, 784 };//autoencoder

        TransferFunctions.TransferFunction[] tFuncs = new TransferFunctions.TransferFunction[] {
                TransferFunctions.TransferFunction.NONE,
                TransferFunctions.TransferFunction.RELU,
                TransferFunctions.TransferFunction.SIGMOID,
                TransferFunctions.TransferFunction.RELU,
                TransferFunctions.TransferFunction.SIGMOID};
        BackpropagationNetwork bpn = new BackpropagationNetwork(
                layerSizes, tFuncs);

        double desiredError = 0.1;
        double learnRate = 0.61803398875;
        MNISTDataReader dr = new MNISTDataReader("/train-images-idx3-ubyte");
        double[][] input = new double[dr.getNumImages()/2][dr.getNumCols() * dr.getNumRows()];

        int i = 0;
        while (!dr.getImages().isEmpty() && i < dr.getNumImages() / 2) {
            MNISTImage img = dr.getImages().remove(rand.nextInt(dr.getImages().size()));
            input[i++] = img.getNormilized();
        }

        LOGGER.debug("Desired Error: {}", desiredError);
        LOGGER.debug("Learning Rate: {}", learnRate);
        double error = bpn.train(input, input, learnRate + 1, desiredError);
        LOGGER.debug("Error: {}", error);
        bpn.saveNet(System.getProperty("user.home")+"/Documents/main.net");
    }
}
