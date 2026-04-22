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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.github.javachaos.javaneuralnetwork.shared.hilbert.HilbertVector;
import com.github.javachaos.javaneuralnetwork.shared.hilbert.LinearOperator;

/**
 * Online "monkey see, monkey do" learner over Hilbert-space world states.
 *
 * <p>The learner keeps a compact prototype memory instead of raw observations.
 * A new transition is assigned to best-matching source and target prototypes.
 * Close observations adapt their prototypes, while distant observations create
 * new prototypes. Prediction follows the strongest remembered transition from
 * the source BMU and carries the source residual to the target prototype.
 */
public final class HilbertImitationLearner implements StateTransitionModel {

    private static final double DEFAULT_LEARNING_RATE = 0.35;
    private static final double DEFAULT_NOVELTY_THRESHOLD = 0.25;

    private final String name;
    private final int dimension;
    private final double learningRate;
    private final double noveltyThreshold;
    private final List<Prototype> prototypes = new ArrayList<>();
    private final Map<TransitionKey, Integer> transitionSupports = new LinkedHashMap<>();
    private int observations;

    private HilbertImitationLearner(
            final String name,
            final int dimension,
            final double learningRate,
            final double noveltyThreshold) {
        this.name = requireName(name);
        this.dimension = requirePositiveDimension(dimension);
        this.learningRate = requireProbability(learningRate, "Learning rate");
        this.noveltyThreshold = requireNonNegativeFinite(noveltyThreshold, "Novelty threshold");
    }

    /**
     * Assignment of an observed state to a prototype.
     *
     * @param input observed state
     * @param prototype prototype after any online adaptation
     * @param prototypeIndex stable prototype index
     * @param residual input minus the assigned prototype
     * @param distance assignment distance after adaptation
     * @param similarity inverse-distance similarity after adaptation
     * @param created true when a novel prototype was created
     * @param observations number of observations assigned to the prototype
     */
    public record PrototypeAssignment(
            WorldState input,
            WorldState prototype,
            int prototypeIndex,
            HilbertVector residual,
            double distance,
            double similarity,
            boolean created,
            int observations) {
        public PrototypeAssignment {
            Objects.requireNonNull(input, "Input state cannot be null.");
            Objects.requireNonNull(prototype, "Prototype state cannot be null.");
            Objects.requireNonNull(residual, "Residual cannot be null.");
            if (prototypeIndex < 0) {
                throw new IllegalArgumentException("Prototype index cannot be negative.");
            }
            requireNonNegativeFinite(distance, "Distance");
            requirePositiveFinite(similarity, "Similarity");
            if (observations <= 0) {
                throw new IllegalArgumentException("Prototype observations must be positive.");
            }
        }
    }

    /**
     * Remembered transition between two prototypes.
     *
     * @param sourceIndex source prototype index
     * @param targetIndex target prototype index
     * @param sourcePrototype source prototype state
     * @param targetPrototype target prototype state
     * @param support number of observations assigned to this transition
     */
    public record TransitionMemory(
            int sourceIndex,
            int targetIndex,
            WorldState sourcePrototype,
            WorldState targetPrototype,
            int support) {
        public TransitionMemory {
            if (sourceIndex < 0 || targetIndex < 0) {
                throw new IllegalArgumentException("Transition indexes cannot be negative.");
            }
            Objects.requireNonNull(sourcePrototype, "Source prototype cannot be null.");
            Objects.requireNonNull(targetPrototype, "Target prototype cannot be null.");
            if (support <= 0) {
                throw new IllegalArgumentException("Transition support must be positive.");
            }
        }
    }

    /**
     * Result of one observed transition update.
     *
     * @param before assigned source prototype
     * @param after assigned target prototype
     * @param transition updated transition memory
     * @param observations total transition observations seen by the learner
     */
    public record Observation(
            PrototypeAssignment before,
            PrototypeAssignment after,
            TransitionMemory transition,
            int observations) {
        public Observation {
            Objects.requireNonNull(before, "Before assignment cannot be null.");
            Objects.requireNonNull(after, "After assignment cannot be null.");
            Objects.requireNonNull(transition, "Transition memory cannot be null.");
            if (observations <= 0) {
                throw new IllegalArgumentException("Observation count must be positive.");
            }
        }
    }

    /**
     * Forecast produced by imitating the strongest source-prototype transition.
     *
     * @param cue input cue state
     * @param sourceMatch source best-matching unit
     * @param transition remembered transition being imitated
     * @param carriedResidual cue minus source prototype
     * @param predicted predicted next world state
     * @param confidence source similarity times transition support ratio
     */
    public record Imitation(
            WorldState cue,
            BestMatchingUnit.Match sourceMatch,
            TransitionMemory transition,
            HilbertVector carriedResidual,
            WorldState predicted,
            double confidence) {
        public Imitation {
            Objects.requireNonNull(cue, "Cue state cannot be null.");
            Objects.requireNonNull(sourceMatch, "Source match cannot be null.");
            Objects.requireNonNull(transition, "Transition memory cannot be null.");
            Objects.requireNonNull(carriedResidual, "Carried residual cannot be null.");
            Objects.requireNonNull(predicted, "Predicted state cannot be null.");
            requireNonNegativeFinite(confidence, "Confidence");
        }
    }

    private record Prototype(WorldState state, int observations) {
        private Prototype {
            Objects.requireNonNull(state, "Prototype state cannot be null.");
            if (observations <= 0) {
                throw new IllegalArgumentException("Prototype observations must be positive.");
            }
        }
    }

    private record TransitionKey(int sourceIndex, int targetIndex) {
    }

    /**
     * Creates a learner with conservative defaults.
     *
     * @param name learner name
     * @param dimension state dimension
     * @return an empty learner
     */
    public static HilbertImitationLearner empty(final String name, final int dimension) {
        return of(name, dimension, DEFAULT_LEARNING_RATE, DEFAULT_NOVELTY_THRESHOLD);
    }

    /**
     * Creates a learner.
     *
     * @param name learner name
     * @param dimension state dimension
     * @param learningRate prototype adaptation rate in {@code [0, 1]}
     * @param noveltyThreshold maximum BMU distance before creating a prototype
     * @return an empty learner
     */
    public static HilbertImitationLearner of(
            final String name,
            final int dimension,
            final double learningRate,
            final double noveltyThreshold) {
        return new HilbertImitationLearner(name, dimension, learningRate, noveltyThreshold);
    }

    /**
     * Observes and learns one before/after transition.
     *
     * @param before state before the transition
     * @param after state after the transition
     * @return assignment and transition metadata
     */
    public Observation observe(final WorldState before, final WorldState after) {
        requireState(before, "Before state");
        requireState(after, "After state");
        PrototypeAssignment source = assignPrototype(before);
        PrototypeAssignment target = assignPrototype(after);
        TransitionKey key = new TransitionKey(source.prototypeIndex(), target.prototypeIndex());
        int support = transitionSupports.getOrDefault(key, 0) + 1;
        transitionSupports.put(key, support);
        observations++;
        return new Observation(source, target, transitionMemory(key, support), observations);
    }

    /**
     * Learns from one memory trace.
     *
     * @param trace the observed trace
     * @return assignment and transition metadata
     */
    public Observation learn(final MemoryTrace trace) {
        Objects.requireNonNull(trace, "Trace cannot be null.");
        return observe(trace.before(), trace.after());
    }

    /**
     * Learns from a collection of memory traces in iteration order.
     *
     * @param traces traces to learn
     * @return number of learned traces
     */
    public int learnAll(final Collection<MemoryTrace> traces) {
        Objects.requireNonNull(traces, "Traces cannot be null.");
        int learned = 0;
        for (MemoryTrace trace : traces) {
            learn(Objects.requireNonNull(trace, "Trace cannot be null."));
            learned++;
        }
        return learned;
    }

    /**
     * Produces a rich imitation forecast.
     *
     * @param state cue state
     * @return imitation result
     */
    public Imitation imitate(final WorldState state) {
        requireState(state, "State");
        BestMatchingUnit.Match sourceMatch = BestMatchingUnit.find(state, prototypeStates())
                .orElseThrow(() -> new IllegalStateException("No prototype exists for imitation."));
        int sourceIndex = prototypeIndex(sourceMatch.prototype());
        TransitionMemory transition = strongestTransition(sourceIndex)
                .orElseThrow(() -> new IllegalStateException("No transition exists for the source prototype."));
        HilbertVector carriedResidual = sourceMatch.residual();
        HilbertVector predictedVector = transition.targetPrototype().state().add(carriedResidual);
        WorldState predicted = WorldState.of(state.name() + " -> " + name + " imitation", predictedVector);
        double confidence = sourceMatch.similarity()
                * transition.support() / (double) outgoingSupport(sourceIndex);
        return new Imitation(state, sourceMatch, transition, carriedResidual, predicted, confidence);
    }

    @Override
    public WorldState predict(final WorldState state) {
        return imitate(state).predicted();
    }

    @Override
    public boolean canPredict(final WorldState state) {
        if (state == null || state.dimension() != dimension || prototypes.isEmpty()) {
            return false;
        }
        return BestMatchingUnit.find(state, prototypeStates())
                .map(match -> hasOutgoingTransition(prototypeIndex(match.prototype())))
                .orElse(false);
    }

    /**
     * Computes mean squared prediction error over traces.
     *
     * @param traces traces to score
     * @return mean squared error per output coordinate
     */
    public double meanSquaredError(final Collection<MemoryTrace> traces) {
        Objects.requireNonNull(traces, "Traces cannot be null.");
        if (traces.isEmpty()) {
            throw new IllegalArgumentException("Traces cannot be empty.");
        }
        double sum = 0.0;
        int count = 0;
        for (MemoryTrace trace : traces) {
            Objects.requireNonNull(trace, "Trace cannot be null.");
            requireState(trace.before(), "Trace before state");
            requireState(trace.after(), "Trace after state");
            sum += trace.after().state().subtract(predict(trace.before()).state()).normSquared() / dimension;
            count++;
        }
        return sum / count;
    }

    /**
     * Builds a world model view from the learned prototypes and transitions.
     *
     * @return world model containing prototype states and prototype transitions
     */
    public WorldModel worldModel() {
        WorldModel model = WorldModel.empty();
        for (Prototype prototype : prototypes) {
            model = model.observe(prototype.state());
        }
        for (TransitionMemory transition : transitions()) {
            model = model.remember(MemoryTrace.of(
                    name + "-transition-" + transition.sourceIndex() + "-" + transition.targetIndex(),
                    transition.sourcePrototype(),
                    transitionOperator(transition.sourcePrototype(), transition.targetPrototype()),
                    transition.targetPrototype()));
        }
        return model;
    }

    /**
     * @return immutable prototype states
     */
    public List<WorldState> prototypes() {
        return prototypeStates();
    }

    /**
     * @return remembered transitions in insertion order
     */
    public List<TransitionMemory> transitions() {
        return transitionSupports.entrySet().stream()
                .map(entry -> transitionMemory(entry.getKey(), entry.getValue()))
                .toList();
    }

    /**
     * @return number of prototypes
     */
    public int prototypeCount() {
        return prototypes.size();
    }

    /**
     * @return number of unique remembered transitions
     */
    public int transitionCount() {
        return transitionSupports.size();
    }

    /**
     * @return number of observed transitions
     */
    public int observations() {
        return observations;
    }

    /**
     * @return prototype adaptation rate
     */
    public double learningRate() {
        return learningRate;
    }

    /**
     * @return maximum distance before a new prototype is created
     */
    public double noveltyThreshold() {
        return noveltyThreshold;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public int inputDimension() {
        return dimension;
    }

    @Override
    public int outputDimension() {
        return dimension;
    }

    private PrototypeAssignment assignPrototype(final WorldState input) {
        Optional<BestMatchingUnit.Match> nearest = BestMatchingUnit.find(input, prototypeStates());
        if (nearest.isEmpty() || nearest.orElseThrow().isNovel(noveltyThreshold)) {
            int index = prototypes.size();
            WorldState prototype = copyAsPrototype(input, index);
            prototypes.add(new Prototype(prototype, 1));
            return assignment(input, prototype, index, true);
        }

        BestMatchingUnit.Match match = nearest.orElseThrow();
        int index = prototypeIndex(match.prototype());
        Prototype existing = prototypes.get(index);
        WorldState adapted = adapt(existing.state(), input);
        Prototype updated = new Prototype(adapted, existing.observations() + 1);
        prototypes.set(index, updated);
        return assignment(input, adapted, index, false);
    }

    private PrototypeAssignment assignment(
            final WorldState input,
            final WorldState prototype,
            final int index,
            final boolean created) {
        BestMatchingUnit.Match match = BestMatchingUnit.match(input, prototype);
        return new PrototypeAssignment(input, prototype, index, match.residual(),
                match.distance(), match.similarity(), created, prototypes.get(index).observations());
    }

    private WorldState copyAsPrototype(final WorldState input, final int index) {
        WorldState prototype = WorldState.of(name + "-prototype-" + index, input.state());
        for (Map.Entry<String, HilbertVector> component : input.components().entrySet()) {
            prototype = prototype.withComponent(component.getKey(), component.getValue());
        }
        return prototype;
    }

    private WorldState adapt(final WorldState prototype, final WorldState input) {
        HilbertVector state = blend(prototype.state(), input.state());
        WorldState adapted = WorldState.of(prototype.name(), state);
        for (Map.Entry<String, HilbertVector> component : prototype.components().entrySet()) {
            adapted = adapted.withComponent(component.getKey(), component.getValue());
        }
        for (Map.Entry<String, HilbertVector> component : input.components().entrySet()) {
            HilbertVector current = prototype.component(component.getKey())
                    .orElse(HilbertVector.zero(dimension));
            adapted = adapted.withComponent(component.getKey(), blend(current, component.getValue()));
        }
        return adapted;
    }

    private HilbertVector blend(final HilbertVector existing, final HilbertVector observed) {
        return existing.scale(1.0 - learningRate).add(observed.scale(learningRate));
    }

    private List<WorldState> prototypeStates() {
        return prototypes.stream()
                .map(Prototype::state)
                .toList();
    }

    private int prototypeIndex(final WorldState prototype) {
        for (int i = 0; i < prototypes.size(); i++) {
            if (prototypes.get(i).state() == prototype) {
                return i;
            }
        }
        throw new IllegalStateException("Prototype is not owned by this learner.");
    }

    private Optional<TransitionMemory> strongestTransition(final int sourceIndex) {
        return transitionSupports.entrySet().stream()
                .filter(entry -> entry.getKey().sourceIndex() == sourceIndex)
                .map(entry -> transitionMemory(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparingInt(TransitionMemory::support).reversed()
                        .thenComparing(memory -> memory.targetPrototype().name()))
                .findFirst();
    }

    private boolean hasOutgoingTransition(final int sourceIndex) {
        return transitionSupports.keySet().stream()
                .anyMatch(key -> key.sourceIndex() == sourceIndex);
    }

    private int outgoingSupport(final int sourceIndex) {
        int support = 0;
        for (Map.Entry<TransitionKey, Integer> entry : transitionSupports.entrySet()) {
            if (entry.getKey().sourceIndex() == sourceIndex) {
                support += entry.getValue();
            }
        }
        if (support <= 0) {
            throw new IllegalStateException("Source prototype has no outgoing support.");
        }
        return support;
    }

    private TransitionMemory transitionMemory(final TransitionKey key, final int support) {
        return new TransitionMemory(
                key.sourceIndex(),
                key.targetIndex(),
                prototypes.get(key.sourceIndex()).state(),
                prototypes.get(key.targetIndex()).state(),
                support);
    }

    private LinearOperator transitionOperator(final WorldState source, final WorldState target) {
        double sourceNormSquared = source.state().normSquared();
        if (sourceNormSquared == 0.0) {
            return LinearOperator.identity(dimension);
        }
        double[][] matrix = new double[dimension][dimension];
        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                matrix[i][j] = target.state().get(i) * source.state().get(j) / sourceNormSquared;
            }
        }
        return LinearOperator.of(matrix);
    }

    private void requireState(final WorldState state, final String label) {
        Objects.requireNonNull(state, label + " cannot be null.");
        if (state.dimension() != dimension) {
            throw new IllegalArgumentException(label + " dimension does not match learner dimension.");
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

    private static int requirePositiveDimension(final int dimension) {
        if (dimension <= 0) {
            throw new IllegalArgumentException("Dimension must be positive.");
        }
        return dimension;
    }

    private static double requireProbability(final double value, final String label) {
        if (!Double.isFinite(value) || value < 0.0 || value > 1.0) {
            throw new IllegalArgumentException(label + " must be finite and in [0, 1].");
        }
        return value;
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
