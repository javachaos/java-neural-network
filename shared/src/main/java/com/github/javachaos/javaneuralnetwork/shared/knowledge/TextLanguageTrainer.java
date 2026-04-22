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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Trains the language temporal learner directly from plain text samples.
 */
public final class TextLanguageTrainer {

    private static final double DEFAULT_LEARNING_RATE = 1.0;

    private TextLanguageTrainer() {
    }

    /**
     * Mutable trained text model.
     *
     * @param semanticSpace generated lexical semantic space
     * @param learner phrase-transition learner
     * @param samples number of samples used to create this result
     * @param vocabularySize number of token axes in the generated space
     */
    public record TrainingResult(
            LexicalSemanticSpace semanticSpace,
            LanguageTemporalLearner learner,
            int samples,
            int vocabularySize) {
        public TrainingResult {
            Objects.requireNonNull(semanticSpace, "Semantic space cannot be null.");
            Objects.requireNonNull(learner, "Learner cannot be null.");
            if (samples < 0) {
                throw new IllegalArgumentException("Samples cannot be negative.");
            }
            if (vocabularySize <= 0) {
                throw new IllegalArgumentException("Vocabulary size must be positive.");
            }
        }

        /**
         * Encodes text with the generated semantic space.
         *
         * @param stateName state name
         * @param text text to encode
         * @return encoded language state
         */
        public LanguageState encode(final String stateName, final String text) {
            return semanticSpace.encode(stateName, text);
        }

        /**
         * Applies one more online text sample to the learner.
         *
         * @param sample sample to learn
         * @return observation metadata
         */
        public LanguageTemporalLearner.Observation observe(final TextTrainingSample sample) {
            Objects.requireNonNull(sample, "Sample cannot be null.");
            return learner.observe(
                    encode("before " + (learner.updates() + 1), sample.beforeText()),
                    sample.phrase(),
                    encode("after " + (learner.updates() + 1), sample.afterText()),
                    sample.reward(),
                    sample.terminal());
        }

        /**
         * Predicts the semantic result of a phrase from plain text context.
         *
         * @param beforeText context text
         * @param phrase phrase/action
         * @return predicted semantic state
         */
        public LanguageState predict(final String beforeText, final String phrase) {
            return learner.predict(encode("query", beforeText), phrase);
        }

        /**
         * Ranks phrase candidates from plain text context.
         *
         * @param beforeText context text
         * @param candidatePhrases phrase candidates
         * @return ranked phrases
         */
        public List<LanguageTemporalLearner.PhraseScore> rank(
                final String beforeText,
                final Collection<String> candidatePhrases) {
            return learner.rankPhrases(encode("query", beforeText), candidatePhrases);
        }

        /**
         * Ranks phrase candidates from plain text context with a concept goal.
         *
         * @param beforeText context text
         * @param candidatePhrases phrase candidates
         * @param goalConcept desired concept token
         * @return ranked phrases
         */
        public List<LanguageTemporalLearner.PhraseScore> rankToward(
                final String beforeText,
                final Collection<String> candidatePhrases,
                final String goalConcept) {
            GoalSubspace goal = GoalSubspace.approach(
                    "seek-" + goalConcept,
                    semanticSpace.concept(goalConcept));
            return learner.rankPhrases(encode("query", beforeText), candidatePhrases, goal);
        }
    }

    /**
     * Trains from samples using an automatically generated vocabulary.
     *
     * @param name learner name
     * @param samples text training samples
     * @return trained text model
     */
    public static TrainingResult train(
            final String name,
            final Collection<TextTrainingSample> samples) {
        return train(name, samples, DEFAULT_LEARNING_RATE);
    }

    /**
     * Trains from samples using an automatically generated vocabulary.
     *
     * @param name learner name
     * @param samples text training samples
     * @param learningRate temporal learning rate
     * @return trained text model
     */
    public static TrainingResult train(
            final String name,
            final Collection<TextTrainingSample> samples,
            final double learningRate) {
        String cleanName = requireName(name, "Trainer name");
        List<TextTrainingSample> trainingSamples = requireSamples(samples);
        LexicalSemanticSpace semanticSpace = semanticSpace(cleanName + "-space", trainingSamples);
        LanguageTemporalLearner learner = LanguageTemporalLearner.of(cleanName, semanticSpace, learningRate);
        TrainingResult result = new TrainingResult(
                semanticSpace,
                learner,
                trainingSamples.size(),
                semanticSpace.dimension());
        for (TextTrainingSample sample : trainingSamples) {
            result.observe(sample);
        }
        return result;
    }

    /**
     * Builds a one-token-one-axis lexical semantic space from text samples.
     *
     * @param name semantic space name
     * @param samples text samples
     * @return lexical semantic space
     */
    public static LexicalSemanticSpace semanticSpace(
            final String name,
            final Collection<TextTrainingSample> samples) {
        String cleanName = requireName(name, "Semantic space name");
        List<TextTrainingSample> trainingSamples = requireSamples(samples);
        Set<String> vocabulary = new LinkedHashSet<>();
        LexicalSemanticSpace tokenizer = LexicalSemanticSpace.of(cleanName + "-tokenizer", List.of("placeholder"))
                .withToken("placeholder", 1.0);
        for (TextTrainingSample sample : trainingSamples) {
            vocabulary.addAll(tokenizer.tokenize(sample.beforeText()));
            vocabulary.addAll(tokenizer.tokenize(sample.phrase()));
            vocabulary.addAll(tokenizer.tokenize(sample.afterText()));
        }
        if (vocabulary.isEmpty()) {
            throw new IllegalArgumentException("Training samples must contain at least one token.");
        }
        List<String> concepts = List.copyOf(vocabulary);
        LexicalSemanticSpace semanticSpace = LexicalSemanticSpace.of(cleanName, concepts);
        for (int i = 0; i < concepts.size(); i++) {
            double[] values = new double[concepts.size()];
            values[i] = 1.0;
            semanticSpace = semanticSpace.withToken(concepts.get(i), values);
        }
        return semanticSpace;
    }

    private static List<TextTrainingSample> requireSamples(
            final Collection<TextTrainingSample> samples) {
        Objects.requireNonNull(samples, "Samples cannot be null.");
        if (samples.isEmpty()) {
            throw new IllegalArgumentException("Samples cannot be empty.");
        }
        List<TextTrainingSample> copy = new ArrayList<>(samples.size());
        for (TextTrainingSample sample : samples) {
            copy.add(Objects.requireNonNull(sample, "Sample cannot be null."));
        }
        return List.copyOf(copy);
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
