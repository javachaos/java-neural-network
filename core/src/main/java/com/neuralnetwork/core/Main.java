/*******************************************************************************
 * Copyright (c) 2013 Fred Laderoute.
 * All rights reserved. This program and the accompanying
 * materials are made available under the terms of the GNU
 * Public License v3.0 which accompanies this distribution,
 * and is available at http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *      Fred Laderoute - initial API and implementation
 ******************************************************************************/
package com.neuralnetwork.core;

import com.neuralnetwork.core.BackpropagationNetwork.TransferFunction;

/**
 * Main class.
 *
 * @author fredladeroute
 *
 */
public final class Main {

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

        int[] layerSizes = new int[] { 100, 50, 25, 50, 100 };
        TransferFunction[] tFuncs = new TransferFunction[] {
                TransferFunction.None,
                TransferFunction.Sigmoid,
                TransferFunction.Sigmoid,
                TransferFunction.Sigmoid,
                TransferFunction.Sigmoid };
        BackpropagationNetwork bpn = new BackpropagationNetwork(
                layerSizes, tFuncs);
        double[][] input = new double[][] {
                { 0.4, 0.2, 0.1, 0.3, 0.8, 0.4, 0.2, 0.1, 0.3, 0.8},
                { 0.1, 0.6, 0.2, 0.2, 0.3, 0.7, 0.4, 0.3, 0.65, 0.32},
                { 0.4, 0.2, 0.6, 0.6, 0.8, 0.4, 0.2, 0.1, 0.3, 0.6},
                { 0.1, 0.6, 0.2, 0.2, 0.3, 0.7, 0.4, 0.3, 0.65, 0.32},
                { 0.4, 0.2, 0.1, 0.3, 0.8, 0.4, 0.2, 0.1, 0.3, 0.8},
                { 0.1, 0.6, 0.2, 0.2, 0.3, 0.7, 0.4, 0.3, 0.65, 0.32},
                { 0.4, 0.2, 0.1, 0.3, 0.8, 0.4, 0.2, 0.1, 0.3, 0.8},
                { 0.6, 0.6, 0.2, 0.2, 0.3, 0.7, 0.4, 0.3, 0.65, 0.32},
                { 0.4, 0.6, 0.1, 0.3, 0.8, 0.4, 0.6, 0.1, 0.3, 0.8},
                { 0.4, 0.2, 0.1, 0.3, 0.8, 0.4, 0.2, 0.1, 0.3, 0.8}};
        double[][] desired = new double[][] {
                { 0.1, 0.6, 0.2, 0.2, 0.3, 0.7, 0.4, 0.3, 0.65, 0.32},
                { 0.4, 0.2, 0.1, 0.3, 0.8, 0.4, 0.2, 0.1, 0.3, 0.8},
                { 0.1, 0.6, 0.6, 0.2, 0.3, 0.7, 0.4, 0.3, 0.65, 0.32},
                { 0.4, 0.2, 0.1, 0.3, 0.8, 0.4, 0.2, 0.1, 0.3, 0.8},
                { 0.1, 0.6, 0.2, 0.2, 0.3, 0.7, 0.4, 0.3, 0.65, 0.32},
                { 0.1, 0.6, 0.2, 0.2, 0.3, 0.6, 0.4, 0.3, 0.65, 0.32},
                { 0.4, 0.2, 0.6, 0.3, 0.8, 0.4, 0.2, 0.1, 0.3, 0.8},
                { 0.4, 0.2, 0.1, 0.6, 0.8, 0.4, 0.2, 0.1, 0.3, 0.6},
                { 0.1, 0.6, 0.2, 0.2, 0.3, 0.7, 0.4, 0.3, 0.65, 0.32},
                { 0.4, 0.2, 0.1, 0.3, 0.8, 0.4, 0.2, 0.1, 0.3, 0.8}};
        double[] output = new double[1];

        double error = 0.0;
        double desiredError = 0.0000000000000000001;
        int i = 0;
        int updateEvery = 10000;
        double learnRate = 0.61803398875;
        bpn.train(input, desired, learnRate + 1, learnRate, desiredError);
        bpn.saveNet("/home/alfred/Documents/main.net");
    }
}
