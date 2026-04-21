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
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable state-space memory graph for Hilbert-space world learning.
 *
 * <p>World states are graph nodes, memory traces are directed edges, and
 * concepts/goals are projective query surfaces over the graph.
 */
public final class WorldModel {

    private final List<WorldState> states;
    private final PredictiveMemory memory;
    private final List<ConceptSubspace> concepts;
    private final List<GoalSubspace> goals;

    private WorldModel(
            final List<WorldState> states,
            final PredictiveMemory memory,
            final List<ConceptSubspace> concepts,
            final List<GoalSubspace> goals) {
        this.states = List.copyOf(states);
        this.memory = Objects.requireNonNull(memory, "Predictive memory cannot be null.");
        this.concepts = List.copyOf(concepts);
        this.goals = List.copyOf(goals);
    }

    /**
     * Scored view of a state in the graph.
     *
     * @param state the state being scored
     * @param distance Hilbert-space distance to a cue, or zero when unused
     * @param relevance concept relevance, or zero when unused
     * @param score ranking score for the query
     */
    public record StateScore(
            WorldState state,
            double distance,
            double relevance,
            double score) {
        public StateScore {
            Objects.requireNonNull(state, "State cannot be null.");
            requireNonNegativeFinite(distance, "Distance");
            requireNonNegativeFinite(relevance, "Relevance");
            requireFinite(score, "Score");
        }
    }

    /**
     * Scored view of a goal against one state.
     *
     * @param goal the scored goal
     * @param satisfaction goal satisfaction for the state
     */
    public record GoalScore(GoalSubspace goal, double satisfaction) {
        public GoalScore {
            Objects.requireNonNull(goal, "Goal cannot be null.");
            requireFinite(satisfaction, "Satisfaction");
        }
    }

    /**
     * @return an empty world model
     */
    public static WorldModel empty() {
        return new WorldModel(List.of(), PredictiveMemory.empty(), List.of(), List.of());
    }

    /**
     * Creates a world model from states, traces, concepts, and goals.
     *
     * @param states world states to include
     * @param traces memory traces to include
     * @param concepts concept subspaces to include
     * @param goals goal subspaces to include
     * @return a world model
     */
    public static WorldModel of(
            final Collection<WorldState> states,
            final Collection<MemoryTrace> traces,
            final Collection<ConceptSubspace> concepts,
            final Collection<GoalSubspace> goals) {
        Objects.requireNonNull(states, "States cannot be null.");
        Objects.requireNonNull(traces, "Traces cannot be null.");
        Objects.requireNonNull(concepts, "Concepts cannot be null.");
        Objects.requireNonNull(goals, "Goals cannot be null.");
        WorldModel model = empty();
        for (WorldState state : states) {
            model = model.observe(state);
        }
        for (MemoryTrace trace : traces) {
            model = model.remember(trace);
        }
        for (ConceptSubspace concept : concepts) {
            model = model.withConcept(concept);
        }
        for (GoalSubspace goal : goals) {
            model = model.withGoal(goal);
        }
        return model;
    }

    /**
     * Adds one state node.
     *
     * @param state the observed state
     * @return a new world model
     */
    public WorldModel observe(final WorldState state) {
        Objects.requireNonNull(state, "State cannot be null.");
        List<WorldState> nextStates = new ArrayList<>(states);
        appendIfAbsent(nextStates, state);
        return new WorldModel(nextStates, memory, concepts, goals);
    }

    /**
     * Adds one directed memory edge and its endpoint states.
     *
     * @param trace the memory trace
     * @return a new world model
     */
    public WorldModel remember(final MemoryTrace trace) {
        Objects.requireNonNull(trace, "Trace cannot be null.");
        List<WorldState> nextStates = new ArrayList<>(states);
        appendIfAbsent(nextStates, trace.before());
        appendIfAbsent(nextStates, trace.after());
        return new WorldModel(nextStates, memory.remember(trace), concepts, goals);
    }

    /**
     * Adds one concept query surface.
     *
     * @param concept the concept subspace
     * @return a new world model
     */
    public WorldModel withConcept(final ConceptSubspace concept) {
        Objects.requireNonNull(concept, "Concept cannot be null.");
        List<ConceptSubspace> nextConcepts = new ArrayList<>(concepts);
        nextConcepts.add(concept);
        return new WorldModel(states, memory, nextConcepts, goals);
    }

    /**
     * Adds one goal query surface.
     *
     * @param goal the goal subspace
     * @return a new world model
     */
    public WorldModel withGoal(final GoalSubspace goal) {
        Objects.requireNonNull(goal, "Goal cannot be null.");
        List<GoalSubspace> nextGoals = new ArrayList<>(goals);
        nextGoals.add(goal);
        return new WorldModel(states, memory, concepts, nextGoals);
    }

    /**
     * Ranks compatible states by geometric similarity to a cue.
     *
     * @param cue the cue state
     * @return states in descending similarity order
     */
    public List<StateScore> rankStatesBySimilarity(final WorldState cue) {
        Objects.requireNonNull(cue, "Cue state cannot be null.");
        return states.stream()
                .filter(state -> state.dimension() == cue.dimension())
                .map(state -> {
                    double distance = state.distanceTo(cue);
                    return new StateScore(state, distance, 0.0, 1.0 / (1.0 + distance));
                })
                .sorted(descendingStateScore())
                .toList();
    }

    /**
     * Ranks compatible states by relevance to a concept subspace.
     *
     * @param concept the concept to query
     * @return states in descending concept relevance order
     */
    public List<StateScore> rankStatesByRelevance(final ConceptSubspace concept) {
        Objects.requireNonNull(concept, "Concept cannot be null.");
        return states.stream()
                .filter(state -> state.dimension() == concept.dimension())
                .filter(state -> state.state().normSquared() > 0.0)
                .map(state -> {
                    double relevance = state.relevance(concept);
                    return new StateScore(state, 0.0, relevance, relevance);
                })
                .sorted(descendingStateScore())
                .toList();
    }

    /**
     * Ranks compatible goals by current satisfaction.
     *
     * @param state the state to score
     * @return goals in descending satisfaction order
     */
    public List<GoalScore> rankGoals(final WorldState state) {
        Objects.requireNonNull(state, "State cannot be null.");
        if (state.state().normSquared() == 0.0) {
            return List.of();
        }
        return goals.stream()
                .filter(goal -> goal.dimension() == state.dimension())
                .map(goal -> new GoalScore(goal, goal.satisfaction(state)))
                .sorted(Comparator.comparingDouble(GoalScore::satisfaction).reversed()
                        .thenComparing(score -> score.goal().name()))
                .toList();
    }

    /**
     * Ranks traces by recall from a cue state.
     *
     * @param cue the cue state
     * @return scored traces
     */
    public List<PredictiveMemory.TraceScore> rankTracesByRecall(final WorldState cue) {
        return memory.rankByRecall(cue);
    }

    /**
     * Ranks traces by residual relevance to a concept.
     *
     * @param concept the concept to query
     * @return scored traces
     */
    public List<PredictiveMemory.TraceScore> rankTracesByResidualRelevance(final ConceptSubspace concept) {
        return memory.rankByResidualRelevance(concept);
    }

    /**
     * Ranks traces by recall, reliability, and progress toward a goal.
     *
     * @param cue the cue state
     * @param goal the goal to approach
     * @return scored traces
     */
    public List<PredictiveMemory.TraceScore> rankTracesForGoal(
            final WorldState cue,
            final GoalSubspace goal) {
        return memory.rankForGoal(cue, goal);
    }

    /**
     * Returns the best goal-directed trace when any compatible trace exists.
     *
     * @param cue the cue state
     * @param goal the goal to approach
     * @return best scored trace
     */
    public Optional<PredictiveMemory.TraceScore> bestTraceForGoal(
            final WorldState cue,
            final GoalSubspace goal) {
        return memory.bestForGoal(cue, goal);
    }

    /**
     * @return immutable state nodes
     */
    public List<WorldState> states() {
        return states;
    }

    /**
     * @return immutable memory traces
     */
    public List<MemoryTrace> traces() {
        return memory.traces();
    }

    /**
     * @return immutable concepts
     */
    public List<ConceptSubspace> concepts() {
        return concepts;
    }

    /**
     * @return immutable goals
     */
    public List<GoalSubspace> goals() {
        return goals;
    }

    /**
     * @return predictive memory view of graph edges
     */
    public PredictiveMemory predictiveMemory() {
        return memory;
    }

    /**
     * @return number of state nodes
     */
    public int stateCount() {
        return states.size();
    }

    /**
     * @return number of memory edges
     */
    public int traceCount() {
        return memory.size();
    }

    private void appendIfAbsent(final List<WorldState> destination, final WorldState state) {
        if (!destination.contains(state)) {
            destination.add(state);
        }
    }

    private Comparator<StateScore> descendingStateScore() {
        return Comparator.comparingDouble(StateScore::score).reversed()
                .thenComparing(score -> score.state().name())
                .thenComparingLong(score -> score.state().revision());
    }

    private static void requireFinite(final double value, final String label) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(label + " must be finite.");
        }
    }

    private static void requireNonNegativeFinite(final double value, final String label) {
        requireFinite(value, label);
        if (value < 0.0) {
            throw new IllegalArgumentException(label + " must be non-negative.");
        }
    }
}
