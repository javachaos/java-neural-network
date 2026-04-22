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
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.github.javachaos.javaneuralnetwork.shared.hilbert.HilbertVector;
import com.github.javachaos.javaneuralnetwork.shared.hilbert.LinearOperator;

/**
 * Online developmental loop for learning from a stream of world states.
 *
 * <p>The learner predicts the next observation before it updates, treats the
 * pre-update residual as surprise, stores the transition as a memory trace, and
 * keeps a compressed set of BMU prototypes for recurring states.
 */
public final class DevelopmentalLearner {

    private static final double DEFAULT_NOVELTY_THRESHOLD = 0.25;
    private static final double DEFAULT_PROTOTYPE_LEARNING_RATE = 0.20;

    private final String name;
    private final HilbertStateTransitionLearner transitionLearner;
    private final double noveltyThreshold;
    private final double prototypeLearningRate;

    private WorldModel worldModel;
    private List<WorldState> prototypes;
    private WorldState previousObservation;
    private int observations;

    private DevelopmentalLearner(
            final String name,
            final HilbertStateTransitionLearner transitionLearner,
            final double noveltyThreshold,
            final double prototypeLearningRate) {
        this.name = requireName(name);
        this.transitionLearner =
                Objects.requireNonNull(transitionLearner, "Transition learner cannot be null.");
        this.noveltyThreshold = requireNonNegativeFinite(noveltyThreshold, "Novelty threshold");
        this.prototypeLearningRate =
                requireLearningRate(prototypeLearningRate, "Prototype learning rate");
        if (transitionLearner.inputDimension() != transitionLearner.outputDimension()) {
            throw new IllegalArgumentException("Developmental learning requires a square transition learner.");
        }
        this.worldModel = WorldModel.empty();
        this.prototypes = List.of();
    }

    /**
     * Result of observing one state in the developmental stream.
     *
     * @param observation observed world state
     * @param prediction pre-update prediction metadata, absent for the first observation
     * @param learning pre-update residual and operator update, absent for the first observation
     * @param trace remembered transition, absent for the first observation
     * @param bestMatch nearest prototype before the prototype memory was updated
     * @param surprise prediction error norm before learning
     * @param normalizedSurprise prediction error normalized by observed-state norm
     * @param novel true when the observation created a new prototype
     * @param observations total observations after this step
     * @param updates total transition-learner updates after this step
     * @param prototypeCount number of compressed state prototypes after this step
     */
    public record ObservationStep(
            WorldState observation,
            Optional<StatePrediction> prediction,
            Optional<HilbertStateTransitionLearner.LearningStep> learning,
            Optional<MemoryTrace> trace,
            Optional<BestMatchingUnit.Match> bestMatch,
            double surprise,
            double normalizedSurprise,
            boolean novel,
            int observations,
            int updates,
            int prototypeCount) {
        public ObservationStep {
            Objects.requireNonNull(observation, "Observation cannot be null.");
            prediction = requireOptional(prediction, "Prediction");
            learning = requireOptional(learning, "Learning");
            trace = requireOptional(trace, "Trace");
            bestMatch = requireOptional(bestMatch, "Best match");
            requireNonNegativeFinite(surprise, "Surprise");
            requireNonNegativeFinite(normalizedSurprise, "Normalized surprise");
            if (observations <= 0) {
                throw new IllegalArgumentException("Observations must be positive.");
            }
            if (updates < 0) {
                throw new IllegalArgumentException("Updates cannot be negative.");
            }
            if (prototypeCount <= 0) {
                throw new IllegalArgumentException("Prototype count must be positive.");
            }
        }

        /**
         * @return true when this step predicted a next state before observing it
         */
        public boolean hasPrediction() {
            return prediction.isPresent();
        }
    }

    /**
     * Creates a developmental learner with a zero transition operator.
     *
     * @param name learner name
     * @param dimension world-state dimension
     * @param learningRate transition learning rate
     * @return developmental learner
     */
    public static DevelopmentalLearner zeroInitialized(
            final String name,
            final int dimension,
            final double learningRate) {
        return zeroInitialized(name, dimension, learningRate,
                DEFAULT_NOVELTY_THRESHOLD, DEFAULT_PROTOTYPE_LEARNING_RATE);
    }

    /**
     * Creates a developmental learner with a zero transition operator.
     *
     * @param name learner name
     * @param dimension world-state dimension
     * @param learningRate transition learning rate
     * @param noveltyThreshold maximum BMU distance before a state is novel
     * @param prototypeLearningRate adaptation rate for an existing prototype
     * @return developmental learner
     */
    public static DevelopmentalLearner zeroInitialized(
            final String name,
            final int dimension,
            final double learningRate,
            final double noveltyThreshold,
            final double prototypeLearningRate) {
        return new DevelopmentalLearner(
                name,
                HilbertStateTransitionLearner.zeroInitialized(name + "-transition",
                        dimension, dimension, learningRate),
                noveltyThreshold,
                prototypeLearningRate);
    }

    /**
     * Creates a developmental learner from an existing square transition operator.
     *
     * @param name learner name
     * @param initialOperator starting transition operator
     * @param learningRate transition learning rate
     * @param noveltyThreshold maximum BMU distance before a state is novel
     * @param prototypeLearningRate adaptation rate for an existing prototype
     * @return developmental learner
     */
    public static DevelopmentalLearner of(
            final String name,
            final LinearOperator initialOperator,
            final double learningRate,
            final double noveltyThreshold,
            final double prototypeLearningRate) {
        Objects.requireNonNull(initialOperator, "Initial operator cannot be null.");
        if (initialOperator.rows() != initialOperator.columns()) {
            throw new IllegalArgumentException("Initial operator must be square.");
        }
        return new DevelopmentalLearner(
                name,
                HilbertStateTransitionLearner.of(name + "-transition",
                        initialOperator, learningRate),
                noveltyThreshold,
                prototypeLearningRate);
    }

    /**
     * Observes one world state, predicts before learning, and then updates.
     *
     * @param observation observed world state
     * @return observation metadata
     */
    public ObservationStep observe(final WorldState observation) {
        requireObservation(observation);
        Optional<BestMatchingUnit.Match> bestMatch = BestMatchingUnit.find(observation, prototypes);
        boolean novel = bestMatch.isEmpty() || bestMatch.orElseThrow().isNovel(noveltyThreshold);
        updatePrototypes(observation, bestMatch, novel);
        observations++;
        worldModel = worldModel.observe(observation);

        if (previousObservation == null) {
            previousObservation = observation;
            return new ObservationStep(
                    observation,
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    bestMatch,
                    0.0,
                    0.0,
                    novel,
                    observations,
                    transitionLearner.updates(),
                    prototypes.size());
        }

        HilbertStateTransitionLearner.LearningStep learning =
                transitionLearner.learn(previousObservation, observation);
        MemoryTrace trace = MemoryTrace.of(
                name + " trace " + transitionLearner.updates(),
                previousObservation,
                learning.previousOperator(),
                observation);
        worldModel = worldModel.remember(trace);
        previousObservation = observation;
        return new ObservationStep(
                observation,
                Optional.of(learning.prediction()),
                Optional.of(learning),
                Optional.of(trace),
                bestMatch,
                learning.prediction().errorNorm(),
                learning.normalizedError(),
                novel,
                observations,
                transitionLearner.updates(),
                prototypes.size());
    }

    /**
     * Observes a sequence of world states.
     *
     * @param stream ordered observations
     * @return observation metadata for every state
     */
    public List<ObservationStep> observeAll(final Collection<WorldState> stream) {
        Objects.requireNonNull(stream, "Observation stream cannot be null.");
        List<ObservationStep> steps = new ArrayList<>(stream.size());
        for (WorldState observation : stream) {
            steps.add(observe(observation));
        }
        return List.copyOf(steps);
    }

    /**
     * Predicts the next state from the most recent observation.
     *
     * @return next-state prediction, when at least one observation exists
     */
    public Optional<WorldState> predictNext() {
        if (previousObservation == null) {
            return Optional.empty();
        }
        return Optional.of(transitionLearner.predict(previousObservation));
    }

    /**
     * Predicts the next state from an explicit cue.
     *
     * @param cue current state
     * @return next-state prediction
     */
    public WorldState predictNext(final WorldState cue) {
        requireObservation(cue);
        return transitionLearner.predict(cue);
    }

    /**
     * @return learner name
     */
    public String name() {
        return name;
    }

    /**
     * @return world-state dimension
     */
    public int dimension() {
        return transitionLearner.inputDimension();
    }

    /**
     * @return online transition learner
     */
    public HilbertStateTransitionLearner transitionLearner() {
        return transitionLearner;
    }

    /**
     * @return current world model assembled from observations and traces
     */
    public WorldModel worldModel() {
        return worldModel;
    }

    /**
     * @return compressed BMU prototypes
     */
    public List<WorldState> prototypes() {
        return prototypes;
    }

    /**
     * @return most recent observation, when present
     */
    public Optional<WorldState> previousObservation() {
        return Optional.ofNullable(previousObservation);
    }

    /**
     * @return total observations processed
     */
    public int observations() {
        return observations;
    }

    /**
     * @return novelty threshold used by BMU compression
     */
    public double noveltyThreshold() {
        return noveltyThreshold;
    }

    /**
     * @return adaptation rate for matched prototypes
     */
    public double prototypeLearningRate() {
        return prototypeLearningRate;
    }

    /**
     * @return prototypes divided by observations, or 1 for an empty stream
     */
    public double prototypeCompressionRatio() {
        if (observations == 0) {
            return 1.0;
        }
        return (double) prototypes.size() / observations;
    }

    private void updatePrototypes(
            final WorldState observation,
            final Optional<BestMatchingUnit.Match> bestMatch,
            final boolean novel) {
        List<WorldState> next = new ArrayList<>(prototypes);
        if (novel) {
            next.add(WorldState.of("prototype-" + (next.size() + 1) + " " + observation.name(),
                    observation.state()));
        } else {
            BestMatchingUnit.Match match = bestMatch.orElseThrow();
            int index = next.indexOf(match.prototype());
            if (index < 0) {
                throw new IllegalStateException("Best matching prototype was not found.");
            }
            WorldState prototype = next.get(index);
            HilbertVector adapted = prototype.state().scale(1.0 - prototypeLearningRate)
                    .add(observation.state().scale(prototypeLearningRate));
            next.set(index, WorldState.of(prototype.name(), adapted));
        }
        prototypes = List.copyOf(next);
    }

    private void requireObservation(final WorldState observation) {
        Objects.requireNonNull(observation, "Observation cannot be null.");
        if (observation.dimension() != dimension()) {
            throw new IllegalArgumentException("Observation dimension does not match learner dimension.");
        }
    }

    private static String requireName(final String name) {
        Objects.requireNonNull(name, "Learner name cannot be null.");
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Learner name cannot be blank.");
        }
        return trimmed;
    }

    private static double requireLearningRate(final double value, final String label) {
        requireNonNegativeFinite(value, label);
        if (value > 1.0) {
            throw new IllegalArgumentException(label + " cannot exceed 1.");
        }
        return value;
    }

    private static double requireNonNegativeFinite(final double value, final String label) {
        if (!Double.isFinite(value) || value < 0.0) {
            throw new IllegalArgumentException(label + " must be finite and non-negative.");
        }
        return value;
    }

    private static <T> Optional<T> requireOptional(final Optional<T> value, final String label) {
        return Objects.requireNonNull(value, label + " cannot be null.");
    }
}
