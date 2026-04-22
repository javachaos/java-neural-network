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
import java.util.Optional;

import com.github.javachaos.javaneuralnetwork.shared.hilbert.HilbertVector;

/**
 * Hybrid learner that combines prototype imitation with a neural transition
 * model.
 *
 * <p>The imitation learner acts as compact episodic memory. The neural model
 * acts as a smooth transition estimator. Online observation updates both layers
 * when a trainer is supplied, and prediction blends both signals when both are
 * available.
 */
public final class NeuralImitationLearner implements StateTransitionModel {

    private final String name;
    private final HilbertImitationLearner imitationMemory;
    private final StateTransitionModel neuralModel;
    private final Trainer trainer;
    private final double neuralWeight;
    private int observations;

    private NeuralImitationLearner(
            final String name,
            final HilbertImitationLearner imitationMemory,
            final StateTransitionModel neuralModel,
            final Trainer trainer,
            final double neuralWeight) {
        this.name = requireName(name);
        this.imitationMemory = Objects.requireNonNull(imitationMemory, "Imitation memory cannot be null.");
        this.neuralModel = Objects.requireNonNull(neuralModel, "Neural model cannot be null.");
        this.trainer = trainer;
        this.neuralWeight = requirePositiveFinite(neuralWeight, "Neural weight");
        requireCompatibleModels();
    }

    /**
     * Training hook for the neural side of the hybrid loop.
     */
    @FunctionalInterface
    public interface Trainer {

        /**
         * Compares the current neural prediction to an observation, then updates
         * the neural model.
         *
         * @param predictionName prediction/update name
         * @param before input state
         * @param observed observed output state
         * @return pre-training neural prediction
         */
        StatePrediction observe(String predictionName, WorldState before, WorldState observed);
    }

    /**
     * Detailed hybrid forecast.
     *
     * @param cue input cue state
     * @param imitation prototype-memory forecast, when available
     * @param modelPrediction neural forecast, when available
     * @param predicted blended forecast
     * @param imitationWeight weight assigned to prototype memory
     * @param neuralWeight weight assigned to the neural model
     * @param confidence normalized combined confidence
     */
    public record HybridPrediction(
            WorldState cue,
            Optional<HilbertImitationLearner.Imitation> imitation,
            Optional<WorldState> modelPrediction,
            WorldState predicted,
            double imitationWeight,
            double neuralWeight,
            double confidence) {
        public HybridPrediction {
            Objects.requireNonNull(cue, "Cue state cannot be null.");
            imitation = Objects.requireNonNull(imitation, "Imitation optional cannot be null.");
            modelPrediction = Objects.requireNonNull(modelPrediction, "Model prediction optional cannot be null.");
            Objects.requireNonNull(predicted, "Predicted state cannot be null.");
            requireNonNegativeFinite(imitationWeight, "Imitation weight");
            requireNonNegativeFinite(neuralWeight, "Neural weight");
            requireNonNegativeFinite(confidence, "Confidence");
        }
    }

    /**
     * One online hybrid update.
     *
     * @param prediction pre-update hybrid prediction, when possible
     * @param neuralLearning pre-update neural prediction, when a trainer exists
     * @param imitationLearning prototype-memory update
     * @param observations total observed transitions
     */
    public record LearningStep(
            Optional<HybridPrediction> prediction,
            Optional<StatePrediction> neuralLearning,
            HilbertImitationLearner.Observation imitationLearning,
            int observations) {
        public LearningStep {
            prediction = Objects.requireNonNull(prediction, "Prediction optional cannot be null.");
            neuralLearning = Objects.requireNonNull(neuralLearning, "Neural learning optional cannot be null.");
            Objects.requireNonNull(imitationLearning, "Imitation learning cannot be null.");
            if (observations <= 0) {
                throw new IllegalArgumentException("Observations must be positive.");
            }
        }
    }

    /**
     * Creates a trainable hybrid learner.
     *
     * @param name learner name
     * @param imitationMemory compact prototype memory
     * @param neuralModel neural transition model
     * @param trainer neural update hook
     * @param neuralWeight blend weight for neural predictions
     * @return hybrid learner
     */
    public static NeuralImitationLearner of(
            final String name,
            final HilbertImitationLearner imitationMemory,
            final StateTransitionModel neuralModel,
            final Trainer trainer,
            final double neuralWeight) {
        Objects.requireNonNull(trainer, "Trainer cannot be null.");
        return new NeuralImitationLearner(name, imitationMemory, neuralModel, trainer, neuralWeight);
    }

    /**
     * Creates a predict-only hybrid around a pre-trained neural model.
     *
     * @param name learner name
     * @param imitationMemory compact prototype memory
     * @param neuralModel neural transition model
     * @param neuralWeight blend weight for neural predictions
     * @return hybrid learner
     */
    public static NeuralImitationLearner predictOnly(
            final String name,
            final HilbertImitationLearner imitationMemory,
            final StateTransitionModel neuralModel,
            final double neuralWeight) {
        return new NeuralImitationLearner(name, imitationMemory, neuralModel, null, neuralWeight);
    }

    /**
     * Observes one transition and updates both layers.
     *
     * @param before input state
     * @param after observed output state
     * @return online update metadata
     */
    public LearningStep observe(final WorldState before, final WorldState after) {
        requireInput(before);
        requireOutput(after);
        Optional<HybridPrediction> preUpdatePrediction =
                canPredict(before) ? Optional.of(forecastDetailed(before)) : Optional.empty();
        Optional<StatePrediction> neuralLearning = Optional.empty();
        if (trainer != null) {
            neuralLearning = Optional.of(trainer.observe(
                    name + " neural update " + (observations + 1), before, after));
        }
        HilbertImitationLearner.Observation imitationLearning = imitationMemory.observe(before, after);
        observations++;
        return new LearningStep(preUpdatePrediction, neuralLearning, imitationLearning, observations);
    }

    /**
     * Produces a detailed hybrid forecast.
     *
     * @param state cue state
     * @return blended forecast
     */
    public HybridPrediction forecastDetailed(final WorldState state) {
        requireInput(state);
        Optional<HilbertImitationLearner.Imitation> imitation = Optional.empty();
        if (imitationMemory.canPredict(state)) {
            imitation = Optional.of(imitationMemory.imitate(state));
        }
        Optional<WorldState> modelPrediction = Optional.empty();
        if (neuralModel.canPredict(state)) {
            modelPrediction = Optional.of(neuralModel.predict(state));
        }
        if (imitation.isEmpty() && modelPrediction.isEmpty()) {
            throw new IllegalStateException("Neither imitation memory nor neural model can predict this state.");
        }
        double imitationWeight = imitation
                .map(HilbertImitationLearner.Imitation::confidence)
                .orElse(0.0);
        double modelWeight = modelPrediction.isPresent() ? neuralWeight : 0.0;
        double totalWeight = imitationWeight + modelWeight;
        HilbertVector predictedVector = HilbertVector.zero(outputDimension());
        if (imitation.isPresent()) {
            predictedVector = predictedVector.add(
                    imitation.orElseThrow().predicted().state().scale(imitationWeight));
        }
        if (modelPrediction.isPresent()) {
            predictedVector = predictedVector.add(
                    requireModelOutput(modelPrediction.orElseThrow()).state().scale(modelWeight));
        }
        predictedVector = predictedVector.scale(1.0 / totalWeight);
        WorldState predicted = WorldState.of(state.name() + " -> " + name, predictedVector);
        double confidence = totalWeight / (1.0 + neuralWeight);
        return new HybridPrediction(state, imitation, modelPrediction, predicted,
                imitationWeight, modelWeight, confidence);
    }

    @Override
    public WorldState predict(final WorldState state) {
        return forecastDetailed(state).predicted();
    }

    @Override
    public boolean canPredict(final WorldState state) {
        return state != null
                && state.dimension() == inputDimension()
                && (imitationMemory.canPredict(state) || neuralModel.canPredict(state));
    }

    /**
     * @return compact imitation memory
     */
    public HilbertImitationLearner imitationMemory() {
        return imitationMemory;
    }

    /**
     * @return neural transition model
     */
    public StateTransitionModel neuralModel() {
        return neuralModel;
    }

    /**
     * @return configured neural blend weight
     */
    public double neuralWeight() {
        return neuralWeight;
    }

    /**
     * @return number of observed transitions
     */
    public int observations() {
        return observations;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public int inputDimension() {
        return imitationMemory.inputDimension();
    }

    @Override
    public int outputDimension() {
        return imitationMemory.outputDimension();
    }

    private void requireCompatibleModels() {
        if (imitationMemory.inputDimension() != neuralModel.inputDimension()) {
            throw new IllegalArgumentException("Imitation and neural input dimensions must match.");
        }
        if (imitationMemory.outputDimension() != neuralModel.outputDimension()) {
            throw new IllegalArgumentException("Imitation and neural output dimensions must match.");
        }
    }

    private void requireInput(final WorldState state) {
        Objects.requireNonNull(state, "Input state cannot be null.");
        if (state.dimension() != inputDimension()) {
            throw new IllegalArgumentException("Input state dimension does not match hybrid input dimension.");
        }
    }

    private void requireOutput(final WorldState state) {
        Objects.requireNonNull(state, "Output state cannot be null.");
        if (state.dimension() != outputDimension()) {
            throw new IllegalArgumentException("Output state dimension does not match hybrid output dimension.");
        }
    }

    private WorldState requireModelOutput(final WorldState state) {
        if (state.dimension() != outputDimension()) {
            throw new IllegalStateException("Neural model output dimension does not match hybrid output dimension.");
        }
        return state;
    }

    private static String requireName(final String name) {
        Objects.requireNonNull(name, "Learner name cannot be null.");
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Learner name cannot be blank.");
        }
        return trimmed;
    }

    private static double requirePositiveFinite(final double value, final String label) {
        if (!Double.isFinite(value) || value <= 0.0) {
            throw new IllegalArgumentException(label + " must be finite and positive.");
        }
        return value;
    }

    private static double requireNonNegativeFinite(final double value, final String label) {
        if (!Double.isFinite(value) || value < 0.0) {
            throw new IllegalArgumentException(label + " must be finite and non-negative.");
        }
        return value;
    }
}
