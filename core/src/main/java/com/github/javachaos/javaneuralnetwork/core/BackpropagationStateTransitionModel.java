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
package com.github.javachaos.javaneuralnetwork.core;

import java.util.Objects;

import com.github.javachaos.javaneuralnetwork.shared.hilbert.HilbertVector;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.StatePrediction;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.StateTransitionModel;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.WorldState;

/**
 * Treats the core backpropagation network as a learned world-state transition.
 */
public final class BackpropagationStateTransitionModel implements StateTransitionModel {

    private final String name;
    private final BackpropagationNetwork network;
    private final int inputDimension;
    private final int outputDimension;

    private BackpropagationStateTransitionModel(
            final String name,
            final BackpropagationNetwork network,
            final int inputDimension,
            final int outputDimension) {
        this.name = requireName(name);
        this.network = Objects.requireNonNull(network, "Network cannot be null.");
        this.inputDimension = requirePositiveDimension(inputDimension, "Input dimension");
        this.outputDimension = requirePositiveDimension(outputDimension, "Output dimension");
    }

    /**
     * Creates a state-transition adapter around a backpropagation network.
     *
     * @param name the model name
     * @param network the core backpropagation network
     * @param inputDimension expected input world-state dimension
     * @param outputDimension produced output world-state dimension
     * @return a trainable state-transition model
     */
    public static BackpropagationStateTransitionModel of(
            final String name,
            final BackpropagationNetwork network,
            final int inputDimension,
            final int outputDimension) {
        return new BackpropagationStateTransitionModel(name, network, inputDimension, outputDimension);
    }

    @Override
    public WorldState predict(final WorldState state) {
        Objects.requireNonNull(state, "State cannot be null.");
        if (state.dimension() != inputDimension) {
            throw new IllegalArgumentException("State dimension does not match model input dimension.");
        }
        double[] output = network.run(state.state().toArray());
        if (output.length != outputDimension) {
            throw new IllegalStateException("Network output dimension does not match model output dimension.");
        }
        return WorldState.of(state.name() + " -> " + name, HilbertVector.of(output));
    }

    /**
     * Trains the underlying network on one world-state transition.
     *
     * @param before the input world state
     * @param after the desired output world state
     * @param trainingRate the training rate
     * @param momentum the momentum term
     * @return squared error reported by the network
     */
    public double train(
            final WorldState before,
            final WorldState after,
            final double trainingRate,
            final double momentum) {
        requireTrainingPair(before, after);
        requireNonNegativeFinite(trainingRate, "Training rate");
        requireNonNegativeFinite(momentum, "Momentum");
        return network.train(before.state().toArray(), after.state().toArray(), trainingRate, momentum);
    }

    /**
     * Compares the current prediction to an observation, then trains on it.
     *
     * @param predictionName the prediction name
     * @param before the input world state
     * @param observed the observed output world state
     * @param trainingRate the training rate
     * @param momentum the momentum term
     * @return the pre-training prediction record
     */
    public StatePrediction observe(
            final String predictionName,
            final WorldState before,
            final WorldState observed,
            final double trainingRate,
            final double momentum) {
        StatePrediction prediction = compare(predictionName, before, observed);
        train(before, observed, trainingRate, momentum);
        return prediction;
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
     * @return the adapted backpropagation network
     */
    public BackpropagationNetwork network() {
        return network;
    }

    private void requireTrainingPair(final WorldState before, final WorldState after) {
        Objects.requireNonNull(before, "Before state cannot be null.");
        Objects.requireNonNull(after, "After state cannot be null.");
        if (before.dimension() != inputDimension) {
            throw new IllegalArgumentException("Before state dimension does not match model input dimension.");
        }
        if (after.dimension() != outputDimension) {
            throw new IllegalArgumentException("After state dimension does not match model output dimension.");
        }
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

    private static void requireNonNegativeFinite(final double value, final String label) {
        if (!Double.isFinite(value) || value < 0.0) {
            throw new IllegalArgumentException(label + " must be finite and non-negative.");
        }
    }
}
