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
 * One plain-text transition sample for language training.
 *
 * @param beforeText text describing the semantic context before the phrase
 * @param phrase phrase/action to learn
 * @param afterText text describing the semantic context after the phrase
 * @param reward scalar reward for the transition
 * @param terminal true when this transition ends an episode
 */
public record TextTrainingSample(
        String beforeText,
        String phrase,
        String afterText,
        double reward,
        boolean terminal) {

    public TextTrainingSample {
        beforeText = requireText(beforeText, "Before text");
        phrase = requireText(phrase, "Phrase");
        afterText = requireText(afterText, "After text");
        if (!Double.isFinite(reward)) {
            throw new IllegalArgumentException("Reward must be finite.");
        }
    }

    /**
     * Creates a non-terminal training sample.
     *
     * @param beforeText text describing the semantic context before the phrase
     * @param phrase phrase/action to learn
     * @param afterText text describing the semantic context after the phrase
     * @param reward scalar reward for the transition
     * @return a text training sample
     */
    public static TextTrainingSample of(
            final String beforeText,
            final String phrase,
            final String afterText,
            final double reward) {
        return new TextTrainingSample(beforeText, phrase, afterText, reward, false);
    }

    private static String requireText(final String text, final String label) {
        Objects.requireNonNull(text, label + " cannot be null.");
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(label + " cannot be blank.");
        }
        return trimmed;
    }
}
