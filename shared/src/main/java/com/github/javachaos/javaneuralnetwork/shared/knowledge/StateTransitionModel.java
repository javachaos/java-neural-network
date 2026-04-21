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

import java.util.Objects;

/**
 * Model that transforms a Hilbert-space world state into another world state.
 */
public interface StateTransitionModel {

    /**
     * @return the model name
     */
    String name();

    /**
     * @return expected input state dimension
     */
    int inputDimension();

    /**
     * @return produced output state dimension
     */
    int outputDimension();

    /**
     * Predicts the next state from a current state.
     *
     * @param state the current world state
     * @return the predicted world state
     */
    WorldState predict(WorldState state);

    /**
     * Checks whether this model can consume a state.
     *
     * @param state the candidate input state
     * @return true when the input dimension matches
     */
    default boolean canPredict(final WorldState state) {
        return state != null && state.dimension() == inputDimension();
    }

    /**
     * Creates an unobserved prediction record.
     *
     * @param state the state to forecast from
     * @return a prediction record
     */
    default StatePrediction forecast(final WorldState state) {
        return forecast(name() + " forecast", state);
    }

    /**
     * Creates a named unobserved prediction record.
     *
     * @param predictionName the prediction name
     * @param state the state to forecast from
     * @return a prediction record
     */
    default StatePrediction forecast(final String predictionName, final WorldState state) {
        Objects.requireNonNull(state, "State cannot be null.");
        return StatePrediction.forecast(predictionName, state, predict(state));
    }

    /**
     * Predicts from a state and compares the prediction to an observed state.
     *
     * @param predictionName the prediction name
     * @param before the state before prediction
     * @param observed the observed next state
     * @return an observed prediction record
     */
    default StatePrediction compare(
            final String predictionName,
            final WorldState before,
            final WorldState observed) {
        Objects.requireNonNull(before, "Before state cannot be null.");
        Objects.requireNonNull(observed, "Observed state cannot be null.");
        return StatePrediction.observed(predictionName, before, predict(before), observed);
    }
}
