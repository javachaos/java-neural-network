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
package com.github.javachaos.javaneuralnetwork.shared.knowledge;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.github.javachaos.javaneuralnetwork.shared.hilbert.HilbertVector;
import com.github.javachaos.javaneuralnetwork.shared.network.Network;

/**
 * Adapts a feed-forward network into a Hilbert-space state-transition model.
 */
public final class NeuralStateTransitionModel implements StateTransitionModel {

    private final String name;
    private final Network network;
    private final int inputDimension;
    private final int outputDimension;

    private NeuralStateTransitionModel(
            final String name,
            final Network network,
            final int inputDimension,
            final int outputDimension) {
        this.name = requireName(name);
        this.network = Objects.requireNonNull(network, "Network cannot be null.");
        this.inputDimension = requirePositiveDimension(inputDimension, "Input dimension");
        this.outputDimension = requirePositiveDimension(outputDimension, "Output dimension");
    }

    /**
     * Creates an adapter around an existing neural network.
     *
     * @param name the model name
     * @param network the neural network to adapt
     * @param inputDimension the expected input world-state dimension
     * @param outputDimension the produced output world-state dimension
     * @return a state-transition model
     */
    public static NeuralStateTransitionModel of(
            final String name,
            final Network network,
            final int inputDimension,
            final int outputDimension) {
        return new NeuralStateTransitionModel(name, network, inputDimension, outputDimension);
    }

    @Override
    public WorldState predict(final WorldState state) {
        Objects.requireNonNull(state, "State cannot be null.");
        if (state.dimension() != inputDimension) {
            throw new IllegalArgumentException("State dimension does not match model input dimension.");
        }
        List<Double> output = network.runInputs(toList(state.state()));
        if (output.size() != outputDimension) {
            throw new IllegalStateException("Network output dimension does not match model output dimension.");
        }
        return WorldState.of(state.name() + " -> " + name, HilbertVector.of(toArray(output)));
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public int inputDimension() {
        return inputDimension;
    }

    @Override
    public int outputDimension() {
        return outputDimension;
    }

    /**
     * @return the adapted network
     */
    public Network network() {
        return network;
    }

    private List<Double> toList(final HilbertVector vector) {
        List<Double> values = new ArrayList<>(vector.dimension());
        for (int i = 0; i < vector.dimension(); i++) {
            values.add(vector.get(i));
        }
        return values;
    }

    private double[] toArray(final List<Double> output) {
        double[] values = new double[output.size()];
        for (int i = 0; i < output.size(); i++) {
            values[i] = Objects.requireNonNull(output.get(i), "Network output cannot contain null values.");
        }
        return values;
    }

    private static String requireName(final String name) {
        Objects.requireNonNull(name, "Model name cannot be null.");
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Model name cannot be blank.");
        }
        return trimmed;
    }

    private static int requirePositiveDimension(final int dimension, final String label) {
        if (dimension <= 0) {
            throw new IllegalArgumentException(label + " must be positive.");
        }
        return dimension;
    }
}
