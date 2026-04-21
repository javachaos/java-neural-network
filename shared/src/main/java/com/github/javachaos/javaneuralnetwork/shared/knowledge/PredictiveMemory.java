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
 * Small immutable memory bank for ranking predictive transition traces.
 */
public final class PredictiveMemory {

    private static final double RECALL_WEIGHT = 0.45;
    private static final double RELIABILITY_WEIGHT = 0.25;
    private static final double GOAL_WEIGHT = 0.30;

    private final List<MemoryTrace> traces;

    private PredictiveMemory(final List<MemoryTrace> traces) {
        this.traces = List.copyOf(traces);
    }

    /**
     * Scored view of a remembered trace.
     *
     * @param trace the remembered trace
     * @param recallScore fit between cue and trace before-state
     * @param reliabilityScore inverse prediction error
     * @param goalProgress goal progress when replayed from the cue
     * @param residualRelevance relevance of residual error to a concept
     * @param score combined ranking score for the current query
     */
    public record TraceScore(
            MemoryTrace trace,
            double recallScore,
            double reliabilityScore,
            double goalProgress,
            double residualRelevance,
            double score) {
        public TraceScore {
            Objects.requireNonNull(trace, "Trace cannot be null.");
        }
    }

    /**
     * @return an empty predictive memory
     */
    public static PredictiveMemory empty() {
        return new PredictiveMemory(List.of());
    }

    /**
     * Creates memory from traces.
     *
     * @param traces the traces to remember
     * @return a predictive memory
     */
    public static PredictiveMemory of(final Collection<MemoryTrace> traces) {
        Objects.requireNonNull(traces, "Traces cannot be null.");
        List<MemoryTrace> copy = new ArrayList<>(traces.size());
        for (MemoryTrace trace : traces) {
            copy.add(Objects.requireNonNull(trace, "Trace cannot be null."));
        }
        return new PredictiveMemory(copy);
    }

    /**
     * Remembers one more trace.
     *
     * @param trace the trace to remember
     * @return a new predictive memory
     */
    public PredictiveMemory remember(final MemoryTrace trace) {
        Objects.requireNonNull(trace, "Trace cannot be null.");
        List<MemoryTrace> next = new ArrayList<>(traces);
        next.add(trace);
        return new PredictiveMemory(next);
    }

    /**
     * Ranks traces by how closely their before-state matches a cue.
     *
     * @param cue the current world state
     * @return scored traces in descending recall order
     */
    public List<TraceScore> rankByRecall(final WorldState cue) {
        Objects.requireNonNull(cue, "Cue state cannot be null.");
        return traces.stream()
                .filter(trace -> canReplay(trace, cue))
                .map(trace -> recallScore(trace, cue))
                .sorted(descendingScore())
                .toList();
    }

    /**
     * Ranks traces by their historical prediction reliability.
     *
     * @return scored traces in descending reliability order
     */
    public List<TraceScore> rankByReliability() {
        return traces.stream()
                .map(trace -> new TraceScore(trace, 0.0, reliabilityScore(trace), 0.0, 0.0, reliabilityScore(trace)))
                .sorted(descendingScore())
                .toList();
    }

    /**
     * Ranks traces by how much of their prediction residual lies in a concept.
     *
     * @param concept the concept to inspect
     * @return scored traces in descending residual relevance order
     */
    public List<TraceScore> rankByResidualRelevance(final ConceptSubspace concept) {
        Objects.requireNonNull(concept, "Concept cannot be null.");
        return traces.stream()
                .filter(trace -> trace.after().dimension() == concept.dimension())
                .map(trace -> {
                    double residualRelevance = trace.residualRelevance(concept);
                    return new TraceScore(trace, 0.0, reliabilityScore(trace), 0.0,
                            residualRelevance, residualRelevance);
                })
                .sorted(descendingScore())
                .toList();
    }

    /**
     * Ranks traces by recall, reliability, and replayed progress toward a goal.
     *
     * @param cue the current world state
     * @param goal the goal to move toward
     * @return scored traces in descending goal-directed order
     */
    public List<TraceScore> rankForGoal(final WorldState cue, final GoalSubspace goal) {
        Objects.requireNonNull(cue, "Cue state cannot be null.");
        Objects.requireNonNull(goal, "Goal cannot be null.");
        if (cue.dimension() != goal.dimension()) {
            throw new IllegalArgumentException("Cue state dimension does not match goal dimension.");
        }
        return traces.stream()
                .filter(trace -> canReplay(trace, cue) && trace.action().rows() == goal.dimension())
                .map(trace -> goalScore(trace, cue, goal))
                .sorted(descendingScore())
                .toList();
    }

    /**
     * Returns the best goal-directed trace when any compatible trace exists.
     *
     * @param cue the current world state
     * @param goal the goal to move toward
     * @return the best scored trace
     */
    public Optional<TraceScore> bestForGoal(final WorldState cue, final GoalSubspace goal) {
        return rankForGoal(cue, goal).stream().findFirst();
    }

    /**
     * @return the number of remembered traces
     */
    public int size() {
        return traces.size();
    }

    /**
     * @return true when there are no remembered traces
     */
    public boolean isEmpty() {
        return traces.isEmpty();
    }

    /**
     * @return immutable traces
     */
    public List<MemoryTrace> traces() {
        return traces;
    }

    private TraceScore recallScore(final MemoryTrace trace, final WorldState cue) {
        double recall = replayFit(trace, cue);
        return new TraceScore(trace, recall, reliabilityScore(trace), 0.0, 0.0, recall);
    }

    private TraceScore goalScore(
            final MemoryTrace trace,
            final WorldState cue,
            final GoalSubspace goal) {
        double recall = replayFit(trace, cue);
        double reliability = reliabilityScore(trace);
        double progress = goal.progress(cue, trace.replay(cue));
        double score = RECALL_WEIGHT * recall
                + RELIABILITY_WEIGHT * reliability
                + GOAL_WEIGHT * progress;
        return new TraceScore(trace, recall, reliability, progress, 0.0, score);
    }

    private double replayFit(final MemoryTrace trace, final WorldState cue) {
        double distance = cue.distanceTo(trace.before());
        return 1.0 / (1.0 + distance);
    }

    private double reliabilityScore(final MemoryTrace trace) {
        return 1.0 / (1.0 + trace.normalizedError());
    }

    private boolean canReplay(final MemoryTrace trace, final WorldState cue) {
        return trace.before().dimension() == cue.dimension()
                && trace.action().columns() == cue.dimension();
    }

    private Comparator<TraceScore> descendingScore() {
        return Comparator.comparingDouble(TraceScore::score).reversed()
                .thenComparing(score -> score.trace().name());
    }
}
