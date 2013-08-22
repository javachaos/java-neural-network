package com.neuralnetwork.core;

import com.neuralnetwork.shared.layers.HiddenLayer;
import com.neuralnetwork.shared.util.NeuralNetBuilder;

/**
 * Main class.
 * 
 * @author fredladeroute
 *
 */
public final class Main {
    
    /**
     * Size of input and output layer.
     */
    private static final int SIZE_N = 3;
    
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
        NeuralNetBuilder builder = new NeuralNetBuilder(SIZE_N, SIZE_N);
        builder.addHiddenLayer(new HiddenLayer(2));
        builder.addHiddenLayer(new HiddenLayer(1));
        builder.build();
    }
}
