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

import java.util.List;
import java.util.Objects;

import com.github.javachaos.javaneuralnetwork.shared.hilbert.HilbertVector;

/**
 * Text paired with its current semantic world-state representation.
 *
 * @param text source text or semantic label
 * @param semanticState Hilbert-space semantic state
 * @param tokens normalized tokens used by the encoder
 */
public record LanguageState(
        String text,
        WorldState semanticState,
        List<String> tokens) {

    public LanguageState {
        text = requireText(text);
        semanticState = Objects.requireNonNull(semanticState, "Semantic state cannot be null.");
        tokens = List.copyOf(Objects.requireNonNull(tokens, "Tokens cannot be null."));
    }

    /**
     * Creates a language state.
     *
     * @param text source text or semantic label
     * @param semanticState Hilbert-space semantic state
     * @param tokens normalized tokens used by the encoder
     * @return a language state
     */
    public static LanguageState of(
            final String text,
            final WorldState semanticState,
            final List<String> tokens) {
        return new LanguageState(text, semanticState, tokens);
    }

    /**
     * @return semantic vector
     */
    public HilbertVector vector() {
        return semanticState.state();
    }

    /**
     * @return semantic dimension
     */
    public int dimension() {
        return semanticState.dimension();
    }

    private static String requireText(final String text) {
        Objects.requireNonNull(text, "Text cannot be null.");
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Text cannot be blank.");
        }
        return trimmed;
    }
}
