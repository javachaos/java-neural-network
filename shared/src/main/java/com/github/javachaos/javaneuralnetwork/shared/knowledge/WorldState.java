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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.github.javachaos.javaneuralnetwork.shared.hilbert.Basis;
import com.github.javachaos.javaneuralnetwork.shared.hilbert.HilbertVector;
import com.github.javachaos.javaneuralnetwork.shared.hilbert.LinearOperator;
import com.github.javachaos.javaneuralnetwork.shared.hilbert.Measurement;

/**
 * Immutable snapshot of a world represented in a finite Hilbert space.
 */
public final class WorldState {

    private final String name;
    private final HilbertVector state;
    private final Map<String, HilbertVector> components;
    private final long revision;

    private WorldState(
            final String name,
            final HilbertVector state,
            final Map<String, HilbertVector> components,
            final long revision) {
        this.name = name;
        this.state = state;
        this.components = Map.copyOf(components);
        this.revision = revision;
    }

    /**
     * Creates a named world state with no named components.
     *
     * @param name the world state name
     * @param state the global state vector
     * @return a world state
     */
    public static WorldState of(final String name, final HilbertVector state) {
        return new WorldState(requireName(name), requireState(state), Map.of(), 0L);
    }

    /**
     * Creates an unnamed world state with no named components.
     *
     * @param state the global state vector
     * @return a world state
     */
    public static WorldState of(final HilbertVector state) {
        return of("world", state);
    }

    /**
     * @return the state name
     */
    public String name() {
        return name;
    }

    /**
     * @return the global state vector
     */
    public HilbertVector state() {
        return state;
    }

    /**
     * @return the state dimension
     */
    public int dimension() {
        return state.dimension();
    }

    /**
     * @return the immutable revision counter for this snapshot lineage
     */
    public long revision() {
        return revision;
    }

    /**
     * Adds or replaces a named component vector.
     *
     * @param componentName the component name
     * @param component the component vector
     * @return a new world state
     */
    public WorldState withComponent(final String componentName, final HilbertVector component) {
        String cleanName = requireName(componentName);
        requireComponentDimension(component);
        Map<String, HilbertVector> next = new LinkedHashMap<>(components);
        next.put(cleanName, component);
        return new WorldState(name, state, next, revision + 1L);
    }

    /**
     * Removes a named component vector.
     *
     * @param componentName the component name
     * @return a new world state
     */
    public WorldState withoutComponent(final String componentName) {
        String cleanName = requireName(componentName);
        if (!components.containsKey(cleanName)) {
            return this;
        }
        Map<String, HilbertVector> next = new LinkedHashMap<>(components);
        next.remove(cleanName);
        return new WorldState(name, state, next, revision + 1L);
    }

    /**
     * Reads a named component.
     *
     * @param componentName the component name
     * @return the component, when present
     */
    public Optional<HilbertVector> component(final String componentName) {
        return Optional.ofNullable(components.get(requireName(componentName)));
    }

    /**
     * @param componentName the component name
     * @return true when the named component exists
     */
    public boolean hasComponent(final String componentName) {
        return components.containsKey(requireName(componentName));
    }

    /**
     * @return immutable component names
     */
    public Set<String> componentNames() {
        return components.keySet();
    }

    /**
     * @return immutable component map
     */
    public Map<String, HilbertVector> components() {
        return components;
    }

    /**
     * Applies a linear operator to the global state and all component states.
     *
     * @param nextName the name of the transformed state
     * @param operator the operator to apply
     * @return a transformed world state
     */
    public WorldState transform(final String nextName, final LinearOperator operator) {
        Objects.requireNonNull(operator, "Operator cannot be null.");
        HilbertVector nextState = operator.apply(state);
        Map<String, HilbertVector> nextComponents = new LinkedHashMap<>();
        for (Map.Entry<String, HilbertVector> entry : components.entrySet()) {
            nextComponents.put(entry.getKey(), operator.apply(entry.getValue()));
        }
        return new WorldState(requireName(nextName), nextState, nextComponents, revision + 1L);
    }

    /**
     * Creates a weighted superposition with another state.
     *
     * @param nextName the name of the new state
     * @param other the other state
     * @param thisWeight the weight for this state
     * @param otherWeight the weight for the other state
     * @return the superposed state
     */
    public WorldState superpose(
            final String nextName,
            final WorldState other,
            final double thisWeight,
            final double otherWeight) {
        requireComparable(other);
        requireFinite(thisWeight);
        requireFinite(otherWeight);
        HilbertVector nextState = state.scale(thisWeight).add(other.state.scale(otherWeight));
        return new WorldState(requireName(nextName), nextState, Map.of(), revision + 1L);
    }

    /**
     * Projects the global state into a concept subspace.
     *
     * @param concept the concept subspace
     * @return the concept projection
     */
    public HilbertVector project(final ConceptSubspace concept) {
        Objects.requireNonNull(concept, "Concept cannot be null.");
        return concept.project(state);
    }

    /**
     * Scores how relevant a concept subspace is to this global state.
     *
     * @param concept the concept subspace
     * @return a value in the interval [0, 1]
     */
    public double relevance(final ConceptSubspace concept) {
        Objects.requireNonNull(concept, "Concept cannot be null.");
        return concept.relevance(state);
    }

    /**
     * Measures this state in a basis.
     *
     * @param basis the basis to measure against
     * @return the measurement result
     */
    public Measurement measure(final Basis basis) {
        Objects.requireNonNull(basis, "Basis cannot be null.");
        return basis.measure(state);
    }

    /**
     * Computes the vector difference from another state to this state.
     *
     * @param previous the previous state
     * @return this state minus the previous state
     */
    public HilbertVector differenceFrom(final WorldState previous) {
        requireComparable(previous);
        return state.subtract(previous.state);
    }

    /**
     * Computes squared Hilbert-space distance to another state.
     *
     * @param other the other state
     * @return squared distance
     */
    public double squaredDistanceTo(final WorldState other) {
        return differenceFrom(other).normSquared();
    }

    /**
     * Computes Hilbert-space distance to another state.
     *
     * @param other the other state
     * @return distance
     */
    public double distanceTo(final WorldState other) {
        return Math.sqrt(squaredDistanceTo(other));
    }

    private void requireComparable(final WorldState other) {
        Objects.requireNonNull(other, "World state cannot be null.");
        if (dimension() != other.dimension()) {
            throw new IllegalArgumentException("World state dimensions do not match.");
        }
    }

    private void requireComponentDimension(final HilbertVector component) {
        requireState(component);
        if (component.dimension() != dimension()) {
            throw new IllegalArgumentException("Component dimension does not match world dimension.");
        }
    }

    private static HilbertVector requireState(final HilbertVector state) {
        return Objects.requireNonNull(state, "State cannot be null.");
    }

    private static String requireName(final String name) {
        Objects.requireNonNull(name, "Name cannot be null.");
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be blank.");
        }
        return trimmed;
    }

    private static void requireFinite(final double value) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("Weights must be finite.");
        }
    }
}
