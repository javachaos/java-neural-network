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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.github.javachaos.javaneuralnetwork.shared.hilbert.HilbertVector;

/**
 * A tiny lexical bridge from text into a finite semantic Hilbert space.
 *
 * <p>This is deliberately not a tokenizer-as-mind model. Text is treated as an
 * observation that perturbs named semantic axes. Negators such as "not" are
 * cheap operators that invert the next known lexical vector.
 */
public final class LexicalSemanticSpace {

    private static final Set<String> NEGATORS = Set.of("not", "no", "never", "without");

    private final String name;
    private final List<String> concepts;
    private final Map<String, Integer> conceptIndex;
    private final Map<String, HilbertVector> tokenVectors;

    private LexicalSemanticSpace(
            final String name,
            final List<String> concepts,
            final Map<String, HilbertVector> tokenVectors) {
        this.name = requireName(name, "Semantic space name");
        if (concepts.isEmpty()) {
            throw new IllegalArgumentException("Semantic space must have at least one concept.");
        }
        this.concepts = List.copyOf(concepts);
        this.conceptIndex = indexConcepts(concepts);
        this.tokenVectors = Map.copyOf(tokenVectors);
        requireTokenDimensions();
    }

    /**
     * Creates an empty lexical semantic space with named concept axes.
     *
     * @param name semantic space name
     * @param concepts concept axis names in coordinate order
     * @return a lexical semantic space
     */
    public static LexicalSemanticSpace of(final String name, final List<String> concepts) {
        Objects.requireNonNull(concepts, "Concepts cannot be null.");
        return new LexicalSemanticSpace(name, cleanConcepts(concepts), Map.of());
    }

    /**
     * Adds or replaces a token vector.
     *
     * @param token token text
     * @param coordinates semantic coordinates
     * @return a new lexical semantic space
     */
    public LexicalSemanticSpace withToken(final String token, final double... coordinates) {
        Objects.requireNonNull(coordinates, "Coordinates cannot be null.");
        return withToken(token, HilbertVector.of(Arrays.copyOf(coordinates, coordinates.length)));
    }

    /**
     * Adds or replaces a token vector.
     *
     * @param token token text
     * @param vector semantic vector
     * @return a new lexical semantic space
     */
    public LexicalSemanticSpace withToken(final String token, final HilbertVector vector) {
        String cleanToken = normalizeToken(token);
        Objects.requireNonNull(vector, "Vector cannot be null.");
        if (vector.dimension() != dimension()) {
            throw new IllegalArgumentException("Token vector dimension does not match semantic space.");
        }
        Map<String, HilbertVector> next = new LinkedHashMap<>(tokenVectors);
        next.put(cleanToken, vector);
        return new LexicalSemanticSpace(name, concepts, next);
    }

    /**
     * Encodes text into a semantic language state.
     *
     * @param stateName world-state name
     * @param text text to encode
     * @return language state
     */
    public LanguageState encode(final String stateName, final String text) {
        String cleanStateName = requireName(stateName, "State name");
        List<String> tokens = tokenize(text);
        double[] values = new double[dimension()];
        boolean negateNextKnownToken = false;
        for (String token : tokens) {
            if (NEGATORS.contains(token)) {
                negateNextKnownToken = !negateNextKnownToken;
                continue;
            }
            HilbertVector vector = tokenVectors.get(token);
            if (vector == null) {
                continue;
            }
            double scale = negateNextKnownToken ? -1.0 : 1.0;
            for (int i = 0; i < values.length; i++) {
                values[i] += scale * vector.get(i);
            }
            negateNextKnownToken = false;
        }
        return LanguageState.of(text, WorldState.of(cleanStateName, HilbertVector.of(values)), tokens);
    }

    /**
     * Tokenizes text into lowercase alphanumeric/hyphen terms.
     *
     * @param text text to tokenize
     * @return normalized tokens
     */
    public List<String> tokenize(final String text) {
        Objects.requireNonNull(text, "Text cannot be null.");
        String[] parts = text.toLowerCase(Locale.ROOT).split("[^a-z0-9-]+");
        List<String> tokens = new ArrayList<>();
        for (String part : parts) {
            if (!part.isBlank()) {
                tokens.add(part);
            }
        }
        return List.copyOf(tokens);
    }

    /**
     * Builds a one-dimensional concept subspace for a named axis.
     *
     * @param concept concept name
     * @return concept subspace
     */
    public ConceptSubspace concept(final String concept) {
        return ConceptSubspace.of(requireConcept(concept), axis(concept));
    }

    /**
     * Returns the basis vector for a concept axis.
     *
     * @param concept concept name
     * @return basis vector
     */
    public HilbertVector axis(final String concept) {
        return HilbertVector.basis(dimension(), conceptIndex(requireConcept(concept)));
    }

    /**
     * Reads a raw coordinate from a language state.
     *
     * @param state language state
     * @param concept concept name
     * @return coordinate value
     */
    public double coordinate(final LanguageState state, final String concept) {
        Objects.requireNonNull(state, "Language state cannot be null.");
        requireDimension(state);
        return state.vector().get(conceptIndex(requireConcept(concept)));
    }

    /**
     * Scores signed alignment to a concept axis.
     *
     * @param state language state
     * @param concept concept name
     * @return signed alignment in [-1, 1], or 0 for the zero vector
     */
    public double alignment(final LanguageState state, final String concept) {
        Objects.requireNonNull(state, "Language state cannot be null.");
        requireDimension(state);
        double norm = state.vector().norm();
        if (norm == 0.0) {
            return 0.0;
        }
        return coordinate(state, concept) / norm;
    }

    /**
     * @return semantic space name
     */
    public String name() {
        return name;
    }

    /**
     * @return semantic dimension
     */
    public int dimension() {
        return concepts.size();
    }

    /**
     * @return concept names in coordinate order
     */
    public List<String> concepts() {
        return concepts;
    }

    /**
     * @return known token vocabulary
     */
    public Set<String> vocabulary() {
        return Set.copyOf(tokenVectors.keySet());
    }

    private void requireDimension(final LanguageState state) {
        if (state.dimension() != dimension()) {
            throw new IllegalArgumentException("Language state dimension does not match semantic space.");
        }
    }

    private String requireConcept(final String concept) {
        String cleanConcept = normalizeToken(concept);
        if (!conceptIndex.containsKey(cleanConcept)) {
            throw new IllegalArgumentException("Unknown concept: " + concept);
        }
        return cleanConcept;
    }

    private int conceptIndex(final String concept) {
        return conceptIndex.get(concept);
    }

    private void requireTokenDimensions() {
        for (HilbertVector vector : tokenVectors.values()) {
            if (vector.dimension() != dimension()) {
                throw new IllegalArgumentException("Token vector dimension does not match semantic space.");
            }
        }
    }

    private static List<String> cleanConcepts(final List<String> concepts) {
        List<String> cleanConcepts = new ArrayList<>(concepts.size());
        Set<String> seen = new LinkedHashSet<>();
        for (String concept : concepts) {
            String cleanConcept = normalizeToken(concept);
            if (!seen.add(cleanConcept)) {
                throw new IllegalArgumentException("Duplicate concept: " + concept);
            }
            cleanConcepts.add(cleanConcept);
        }
        return cleanConcepts;
    }

    private static Map<String, Integer> indexConcepts(final List<String> concepts) {
        Map<String, Integer> index = new LinkedHashMap<>();
        for (int i = 0; i < concepts.size(); i++) {
            index.put(concepts.get(i), i);
        }
        return Map.copyOf(index);
    }

    private static String normalizeToken(final String token) {
        return requireName(token, "Token").toLowerCase(Locale.ROOT);
    }

    private static String requireName(final String value, final String label) {
        Objects.requireNonNull(value, label + " cannot be null.");
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(label + " cannot be blank.");
        }
        return trimmed;
    }
}
