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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Learns action-conditioned operators over a Hilbert-space world state.
 *
 * <p>This gives the knowledge model a compact notion of time: each action owns
 * a learned transition operator, a bounded trace memory, and a reward estimate.
 * The learner can then rank actions by predicted next state, remembered reward,
 * optional goal progress, and model confidence.
 */
public final class TemporalOperatorLearner {

    private static final int DEFAULT_MEMORY_LIMIT_PER_ACTION = 128;
    private static final double DEFAULT_REWARD_WEIGHT = 0.65;
    private static final double DEFAULT_GOAL_WEIGHT = 0.25;
    private static final double DEFAULT_CONFIDENCE_WEIGHT = 0.10;

    private final String name;
    private final int dimension;
    private final double learningRate;
    private final int memoryLimitPerAction;
    private final Map<String, ActionDynamics> dynamics = new LinkedHashMap<>();
    private int updates;

    private TemporalOperatorLearner(
            final String name,
            final int dimension,
            final double learningRate,
            final int memoryLimitPerAction) {
        this.name = requireName(name, "Learner name");
        this.dimension = requirePositiveDimension(dimension);
        this.learningRate = requirePositiveFinite(learningRate, "Learning rate");
        if (memoryLimitPerAction <= 0) {
            throw new IllegalArgumentException("Memory limit per action must be positive.");
        }
        this.memoryLimitPerAction = memoryLimitPerAction;
    }

    /**
     * Result of one temporal update.
     *
     * @param transition observed transition
     * @param prediction pre-update prediction
     * @param rewardEstimate updated action reward estimate
     * @param meanNormalizedError updated action prediction error estimate
     * @param actionUpdates number of updates for this action
     * @param totalUpdates total updates in this learner
     */
    public record LearningStep(
            TemporalTransition transition,
            StatePrediction prediction,
            double rewardEstimate,
            double meanNormalizedError,
            int actionUpdates,
            int totalUpdates) {
        public LearningStep {
            Objects.requireNonNull(transition, "Transition cannot be null.");
            Objects.requireNonNull(prediction, "Prediction cannot be null.");
            requireFinite(rewardEstimate, "Reward estimate");
            requireNonNegativeFinite(meanNormalizedError, "Mean normalized error");
            if (actionUpdates <= 0) {
                throw new IllegalArgumentException("Action updates must be positive.");
            }
            if (totalUpdates <= 0) {
                throw new IllegalArgumentException("Total updates must be positive.");
            }
        }
    }

    /**
     * Ranked action view for one state.
     *
     * @param action action label
     * @param predicted predicted next state
     * @param visits number of observed transitions for this action
     * @param recallScore similarity between the cue and remembered action contexts
     * @param expectedReward context-sensitive reward estimate
     * @param goalProgress predicted progress toward the requested goal
     * @param modelConfidence confidence from visits and prediction error
     * @param score combined ranking score
     */
    public record ActionScore(
            String action,
            WorldState predicted,
            int visits,
            double recallScore,
            double expectedReward,
            double goalProgress,
            double modelConfidence,
            double score) {
        public ActionScore {
            action = requireName(action, "Action");
            Objects.requireNonNull(predicted, "Predicted state cannot be null.");
            if (visits < 0) {
                throw new IllegalArgumentException("Visits cannot be negative.");
            }
            requireNonNegativeFinite(recallScore, "Recall score");
            requireFinite(expectedReward, "Expected reward");
            requireFinite(goalProgress, "Goal progress");
            requireNonNegativeFinite(modelConfidence, "Model confidence");
            requireFinite(score, "Score");
        }
    }

    /**
     * Creates a temporal learner with a default bounded memory per action.
     *
     * @param name learner name
     * @param dimension world-state dimension
     * @param learningRate update rate for each action operator
     * @return a temporal learner
     */
    public static TemporalOperatorLearner of(
            final String name,
            final int dimension,
            final double learningRate) {
        return of(name, dimension, learningRate, DEFAULT_MEMORY_LIMIT_PER_ACTION);
    }

    /**
     * Creates a temporal learner.
     *
     * @param name learner name
     * @param dimension world-state dimension
     * @param learningRate update rate for each action operator
     * @param memoryLimitPerAction retained trace count per action
     * @return a temporal learner
     */
    public static TemporalOperatorLearner of(
            final String name,
            final int dimension,
            final double learningRate,
            final int memoryLimitPerAction) {
        return new TemporalOperatorLearner(name, dimension, learningRate, memoryLimitPerAction);
    }

    /**
     * Learns one action-conditioned transition.
     *
     * @param transition observed temporal transition
     * @return update metadata
     */
    public LearningStep learn(final TemporalTransition transition) {
        requireTransition(transition);
        ActionDynamics actionDynamics = dynamics.computeIfAbsent(
                transition.action(), action -> new ActionDynamics(action));
        LearningStep step = actionDynamics.learn(transition);
        updates++;
        return new LearningStep(
                transition,
                step.prediction(),
                step.rewardEstimate(),
                step.meanNormalizedError(),
                step.actionUpdates(),
                updates);
    }

    /**
     * Predicts a next state for a known action.
     *
     * @param state current state
     * @param action action label
     * @return predicted next state
     */
    public WorldState predict(final WorldState state, final String action) {
        requireState(state);
        ActionDynamics actionDynamics = dynamics.get(requireName(action, "Action"));
        if (actionDynamics == null) {
            throw new IllegalArgumentException("Action has not been observed: " + action);
        }
        return actionDynamics.predict(state);
    }

    /**
     * Checks whether this learner has dynamics for the action and state shape.
     *
     * @param state current state
     * @param action action label
     * @return true when prediction can be produced
     */
    public boolean canPredict(final WorldState state, final String action) {
        return state != null
                && state.dimension() == dimension
                && action != null
                && dynamics.containsKey(action.trim());
    }

    /**
     * Ranks actions without an explicit goal.
     *
     * @param state current state
     * @param actions candidate action labels
     * @return ranked actions, best first
     */
    public List<ActionScore> rankActions(
            final WorldState state,
            final Collection<String> actions) {
        return rankActions(state, actions, null);
    }

    /**
     * Ranks actions with optional goal progress.
     *
     * @param state current state
     * @param actions candidate action labels
     * @param goal optional goal subspace
     * @return ranked actions, best first
     */
    public List<ActionScore> rankActions(
            final WorldState state,
            final Collection<String> actions,
            final GoalSubspace goal) {
        return rankActions(state, actions, goal,
                DEFAULT_REWARD_WEIGHT,
                DEFAULT_GOAL_WEIGHT,
                DEFAULT_CONFIDENCE_WEIGHT);
    }

    /**
     * Ranks actions with explicit score weights.
     *
     * @param state current state
     * @param actions candidate action labels
     * @param goal optional goal subspace
     * @param rewardWeight reward score weight
     * @param goalWeight goal-progress score weight
     * @param confidenceWeight model-confidence score weight
     * @return ranked actions, best first
     */
    public List<ActionScore> rankActions(
            final WorldState state,
            final Collection<String> actions,
            final GoalSubspace goal,
            final double rewardWeight,
            final double goalWeight,
            final double confidenceWeight) {
        requireState(state);
        Objects.requireNonNull(actions, "Actions cannot be null.");
        requireNonNegativeFinite(rewardWeight, "Reward weight");
        requireNonNegativeFinite(goalWeight, "Goal weight");
        requireNonNegativeFinite(confidenceWeight, "Confidence weight");
        if (goal != null && goal.dimension() != dimension) {
            throw new IllegalArgumentException("Goal dimension does not match temporal learner dimension.");
        }

        List<ActionScore> scores = new ArrayList<>(actions.size());
        for (String action : actions) {
            String cleanAction = requireName(action, "Action");
            ActionDynamics actionDynamics = dynamics.get(cleanAction);
            ActionScore score = actionDynamics == null
                    ? unobservedScore(state, cleanAction)
                    : actionDynamics.score(state, goal, rewardWeight, goalWeight, confidenceWeight);
            scores.add(score);
        }
        return scores.stream()
                .sorted(Comparator.comparingDouble(ActionScore::score).reversed()
                        .thenComparing(ActionScore::action))
                .toList();
    }

    /**
     * Returns the best action from a candidate set, when any are supplied.
     *
     * @param state current state
     * @param actions candidate action labels
     * @return best action score
     */
    public Optional<ActionScore> bestAction(
            final WorldState state,
            final Collection<String> actions) {
        return rankActions(state, actions).stream().findFirst();
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
        return dimension;
    }

    /**
     * @return action-operator learning rate
     */
    public double learningRate() {
        return learningRate;
    }

    /**
     * @return retained memory limit per action
     */
    public int memoryLimitPerAction() {
        return memoryLimitPerAction;
    }

    /**
     * @return total observed temporal transitions
     */
    public int updates() {
        return updates;
    }

    /**
     * @return number of observed action labels
     */
    public int actionCount() {
        return dynamics.size();
    }

    /**
     * @return observed actions in insertion order
     */
    public Set<String> observedActions() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(dynamics.keySet()));
    }

    /**
     * Counts retained traces across all actions.
     *
     * @return retained trace count
     */
    public int memorySize() {
        int size = 0;
        for (ActionDynamics actionDynamics : dynamics.values()) {
            size += actionDynamics.memory.size();
        }
        return size;
    }

    /**
     * Counts updates for one action.
     *
     * @param action action label
     * @return action visits
     */
    public int visits(final String action) {
        ActionDynamics actionDynamics = dynamics.get(requireName(action, "Action"));
        return actionDynamics == null ? 0 : actionDynamics.visits;
    }

    private ActionScore unobservedScore(final WorldState state, final String action) {
        return new ActionScore(
                action,
                WorldState.of(state.name() + " -> unknown " + action, state.state()),
                0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0);
    }

    private void requireTransition(final TemporalTransition transition) {
        Objects.requireNonNull(transition, "Transition cannot be null.");
        if (transition.before().dimension() != dimension || transition.after().dimension() != dimension) {
            throw new IllegalArgumentException("Transition dimension does not match temporal learner dimension.");
        }
    }

    private void requireState(final WorldState state) {
        Objects.requireNonNull(state, "State cannot be null.");
        if (state.dimension() != dimension) {
            throw new IllegalArgumentException("State dimension does not match temporal learner dimension.");
        }
    }

    private static String requireName(final String value, final String label) {
        Objects.requireNonNull(value, label + " cannot be null.");
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(label + " cannot be blank.");
        }
        return trimmed;
    }

    private static int requirePositiveDimension(final int value) {
        if (value <= 0) {
            throw new IllegalArgumentException("Dimension must be positive.");
        }
        return value;
    }

    private static double requirePositiveFinite(final double value, final String label) {
        requireFinite(value, label);
        if (value <= 0.0) {
            throw new IllegalArgumentException(label + " must be positive.");
        }
        return value;
    }

    private static void requireNonNegativeFinite(final double value, final String label) {
        requireFinite(value, label);
        if (value < 0.0) {
            throw new IllegalArgumentException(label + " cannot be negative.");
        }
    }

    private static void requireFinite(final double value, final String label) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(label + " must be finite.");
        }
    }

    private final class ActionDynamics {
        private final String action;
        private final HilbertStateTransitionLearner model;
        private final Deque<TemporalTransition> memory = new ArrayDeque<>();
        private int visits;
        private double rewardEstimate;
        private double meanNormalizedError;

        private ActionDynamics(final String action) {
            this.action = action;
            this.model = HilbertStateTransitionLearner.identityInitialized(
                    name + " " + action,
                    dimension,
                    learningRate);
        }

        private LearningStep learn(final TemporalTransition transition) {
            HilbertStateTransitionLearner.LearningStep learning = model.learn(
                    transition.before(),
                    transition.after());
            visits++;
            rewardEstimate += (transition.reward() - rewardEstimate) / visits;
            meanNormalizedError += (learning.normalizedError() - meanNormalizedError) / visits;
            memory.addLast(transition);
            while (memory.size() > memoryLimitPerAction) {
                memory.removeFirst();
            }
            return new LearningStep(
                    transition,
                    learning.prediction(),
                    rewardEstimate,
                    meanNormalizedError,
                    visits,
                    visits);
        }

        private WorldState predict(final WorldState state) {
            return model.predict(state);
        }

        private ActionScore score(
                final WorldState state,
                final GoalSubspace goal,
                final double rewardWeight,
                final double goalWeight,
                final double confidenceWeight) {
            WorldState predicted = predict(state);
            double recall = recallScore(state);
            double expectedReward = contextReward(state);
            double goalProgress = goal == null ? 0.0 : goal.progress(state, predicted);
            double confidence = modelConfidence();
            double score = rewardWeight * expectedReward
                    + goalWeight * goalProgress
                    + confidenceWeight * confidence;
            return new ActionScore(action, predicted, visits, recall, expectedReward,
                    goalProgress, confidence, score);
        }

        private double recallScore(final WorldState state) {
            double best = 0.0;
            for (TemporalTransition transition : memory) {
                best = Math.max(best, recallFit(state, transition.before()));
            }
            return best;
        }

        private double contextReward(final WorldState state) {
            if (memory.isEmpty()) {
                return rewardEstimate;
            }
            double weightedReward = 0.0;
            double totalWeight = 0.0;
            for (TemporalTransition transition : memory) {
                double weight = recallFit(state, transition.before());
                weightedReward += weight * transition.reward();
                totalWeight += weight;
            }
            if (totalWeight == 0.0) {
                return rewardEstimate;
            }
            return weightedReward / totalWeight;
        }

        private double modelConfidence() {
            double experience = visits / (visits + 4.0);
            double reliability = 1.0 / (1.0 + meanNormalizedError);
            return experience * reliability;
        }

        private double recallFit(final WorldState left, final WorldState right) {
            return 1.0 / (1.0 + left.distanceTo(right));
        }
    }
}
