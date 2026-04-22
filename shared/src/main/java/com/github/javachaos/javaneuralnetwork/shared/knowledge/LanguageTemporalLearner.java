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

/**
 * Learns phrases as temporal operators over semantic language states.
 */
public final class LanguageTemporalLearner {

    private static final int DEFAULT_MEMORY_LIMIT_PER_PHRASE = 128;

    private final String name;
    private final LexicalSemanticSpace semanticSpace;
    private final TemporalOperatorLearner temporalLearner;

    private LanguageTemporalLearner(
            final String name,
            final LexicalSemanticSpace semanticSpace,
            final TemporalOperatorLearner temporalLearner) {
        this.name = requireName(name, "Learner name");
        this.semanticSpace = Objects.requireNonNull(semanticSpace, "Semantic space cannot be null.");
        this.temporalLearner = Objects.requireNonNull(temporalLearner, "Temporal learner cannot be null.");
        if (temporalLearner.dimension() != semanticSpace.dimension()) {
            throw new IllegalArgumentException("Temporal learner dimension must match semantic space.");
        }
    }

    /**
     * One language observation.
     *
     * @param transition temporal transition
     * @param learning temporal learning metadata
     */
    public record Observation(
            TemporalTransition transition,
            TemporalOperatorLearner.LearningStep learning) {
        public Observation {
            Objects.requireNonNull(transition, "Transition cannot be null.");
            Objects.requireNonNull(learning, "Learning step cannot be null.");
        }
    }

    /**
     * Ranked phrase candidate.
     *
     * @param phrase original phrase
     * @param predicted predicted semantic state after using the phrase
     * @param visits observed transition count for the phrase
     * @param recallScore similarity to known phrase contexts
     * @param expectedReward context-sensitive phrase reward
     * @param goalProgress predicted progress toward the requested goal
     * @param modelConfidence confidence from visits and prediction error
     * @param score combined ranking score
     */
    public record PhraseScore(
            String phrase,
            LanguageState predicted,
            int visits,
            double recallScore,
            double expectedReward,
            double goalProgress,
            double modelConfidence,
            double score) {
        public PhraseScore {
            phrase = requireName(phrase, "Phrase");
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
     * Creates a language temporal learner.
     *
     * @param name learner name
     * @param semanticSpace lexical semantic space
     * @param learningRate action-operator learning rate
     * @return a language temporal learner
     */
    public static LanguageTemporalLearner of(
            final String name,
            final LexicalSemanticSpace semanticSpace,
            final double learningRate) {
        Objects.requireNonNull(semanticSpace, "Semantic space cannot be null.");
        return new LanguageTemporalLearner(
                name,
                semanticSpace,
                TemporalOperatorLearner.of(
                        name + "-time",
                        semanticSpace.dimension(),
                        learningRate,
                        DEFAULT_MEMORY_LIMIT_PER_PHRASE));
    }

    /**
     * Learns one phrase transition.
     *
     * @param before state before the phrase
     * @param phrase phrase/action text
     * @param after state after the phrase
     * @param reward scalar reward
     * @return observation metadata
     */
    public Observation observe(
            final LanguageState before,
            final String phrase,
            final LanguageState after,
            final double reward) {
        return observe(before, phrase, after, reward, false);
    }

    /**
     * Learns one phrase transition.
     *
     * @param before state before the phrase
     * @param phrase phrase/action text
     * @param after state after the phrase
     * @param reward scalar reward
     * @param terminal true when the phrase ended an episode
     * @return observation metadata
     */
    public Observation observe(
            final LanguageState before,
            final String phrase,
            final LanguageState after,
            final double reward,
            final boolean terminal) {
        requireState(before);
        requireState(after);
        String action = actionFor(phrase);
        TemporalTransition transition = new TemporalTransition(
                name + " " + action + " " + (temporalLearner.updates() + 1),
                before.semanticState(),
                action,
                after.semanticState(),
                reward,
                terminal);
        return new Observation(transition, temporalLearner.learn(transition));
    }

    /**
     * Predicts how a phrase changes a semantic state.
     *
     * @param before state before the phrase
     * @param phrase phrase/action text
     * @return predicted language state
     */
    public LanguageState predict(final LanguageState before, final String phrase) {
        requireState(before);
        String action = actionFor(phrase);
        return LanguageState.of(
                before.text() + " | " + phrase,
                temporalLearner.predict(before.semanticState(), action),
                semanticSpace.tokenize(phrase));
    }

    /**
     * Ranks phrase candidates without an explicit goal.
     *
     * @param before current semantic state
     * @param phrases candidate phrases
     * @return ranked phrases, best first
     */
    public List<PhraseScore> rankPhrases(
            final LanguageState before,
            final Collection<String> phrases) {
        return rankPhrases(before, phrases, null);
    }

    /**
     * Ranks phrase candidates with optional goal progress.
     *
     * @param before current semantic state
     * @param phrases candidate phrases
     * @param goal optional goal
     * @return ranked phrases, best first
     */
    public List<PhraseScore> rankPhrases(
            final LanguageState before,
            final Collection<String> phrases,
            final GoalSubspace goal) {
        requireState(before);
        Objects.requireNonNull(phrases, "Phrases cannot be null.");
        List<String> phraseList = List.copyOf(phrases);
        List<String> actions = new ArrayList<>(phraseList.size());
        for (String phrase : phraseList) {
            actions.add(actionFor(phrase));
        }
        List<TemporalOperatorLearner.ActionScore> actionScores =
                temporalLearner.rankActions(before.semanticState(), actions, goal);
        List<PhraseScore> phraseScores = new ArrayList<>(actionScores.size());
        for (TemporalOperatorLearner.ActionScore actionScore : actionScores) {
            String phrase = phraseForAction(actionScore.action());
            phraseScores.add(new PhraseScore(
                    phrase,
                    LanguageState.of(before.text() + " | " + phrase,
                            actionScore.predicted(),
                            semanticSpace.tokenize(phrase)),
                    actionScore.visits(),
                    actionScore.recallScore(),
                    actionScore.expectedReward(),
                    actionScore.goalProgress(),
                    actionScore.modelConfidence(),
                    actionScore.score()));
        }
        return List.copyOf(phraseScores);
    }

    /**
     * Returns the best phrase candidate, when any candidates exist.
     *
     * @param before current semantic state
     * @param phrases candidate phrases
     * @param goal optional goal
     * @return best phrase score
     */
    public Optional<PhraseScore> bestPhrase(
            final LanguageState before,
            final Collection<String> phrases,
            final GoalSubspace goal) {
        return rankPhrases(before, phrases, goal).stream().findFirst();
    }

    /**
     * @return learner name
     */
    public String name() {
        return name;
    }

    /**
     * @return lexical semantic space
     */
    public LexicalSemanticSpace semanticSpace() {
        return semanticSpace;
    }

    /**
     * @return underlying temporal learner
     */
    public TemporalOperatorLearner temporalLearner() {
        return temporalLearner;
    }

    /**
     * @return number of observed phrase transitions
     */
    public int updates() {
        return temporalLearner.updates();
    }

    private void requireState(final LanguageState state) {
        Objects.requireNonNull(state, "Language state cannot be null.");
        if (state.dimension() != semanticSpace.dimension()) {
            throw new IllegalArgumentException("Language state dimension does not match semantic space.");
        }
    }

    private String actionFor(final String phrase) {
        String cleanPhrase = canonicalPhrase(phrase);
        return "phrase:" + cleanPhrase;
    }

    private String phraseForAction(final String action) {
        String prefix = "phrase:";
        if (!action.startsWith(prefix)) {
            throw new IllegalArgumentException("Action is not a phrase action.");
        }
        return action.substring(prefix.length()).replace('-', ' ');
    }

    private String canonicalPhrase(final String phrase) {
        List<String> tokens = semanticSpace.tokenize(requireName(phrase, "Phrase"));
        if (tokens.isEmpty()) {
            throw new IllegalArgumentException("Phrase must contain at least one token.");
        }
        return String.join("-", tokens);
    }

    private static String requireName(final String value, final String label) {
        Objects.requireNonNull(value, label + " cannot be null.");
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(label + " cannot be blank.");
        }
        return trimmed;
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
}
