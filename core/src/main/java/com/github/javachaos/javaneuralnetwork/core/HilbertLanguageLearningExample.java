package com.github.javachaos.javaneuralnetwork.core;

import java.util.List;

import com.github.javachaos.javaneuralnetwork.shared.knowledge.GoalSubspace;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.LanguageState;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.LanguageTemporalLearner;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.LexicalSemanticSpace;

/**
 * Tiny language-as-semantic-transition demonstration.
 */
public final class HilbertLanguageLearningExample {

    private HilbertLanguageLearningExample() {
    }

    /**
     * Result of the language learning demo.
     *
     * @param semanticSpace lexical semantic space
     * @param learner temporal phrase learner
     * @param question initial question state
     * @param rankedPhrases phrase ranking from the question state
     * @param predictedAnswer predicted state after the best answer phrase
     * @param warmStatement warm concept state
     * @param negatedWarmPrediction predicted state after applying "not"
     * @param answerAlignmentBefore signed answer alignment before answering
     * @param answerAlignmentAfter signed answer alignment after answering
     * @param warmAlignmentBefore signed warm alignment before negation
     * @param warmAlignmentAfter signed warm alignment after negation
     */
    public record LanguageDemoResult(
            LexicalSemanticSpace semanticSpace,
            LanguageTemporalLearner learner,
            LanguageState question,
            List<LanguageTemporalLearner.PhraseScore> rankedPhrases,
            LanguageState predictedAnswer,
            LanguageState warmStatement,
            LanguageState negatedWarmPrediction,
            double answerAlignmentBefore,
            double answerAlignmentAfter,
            double warmAlignmentBefore,
            double warmAlignmentAfter) {
        public LanguageDemoResult {
            rankedPhrases = List.copyOf(rankedPhrases);
        }
    }

    /**
     * Runs a compact language learning scenario.
     *
     * @return demo result
     */
    public static LanguageDemoResult run() {
        LexicalSemanticSpace space = semanticSpace();
        LanguageTemporalLearner learner = LanguageTemporalLearner.of("toy-language", space, 1.0);
        LanguageState question = space.encode("question", "weather question unknown");
        LanguageState warmAnswer = space.encode("warm answer", "weather answer warm known");
        LanguageState coldAnswer = space.encode("cold answer", "weather answer cold known");
        LanguageState uncertain = space.encode("uncertain", "weather question unknown");
        LanguageState warmStatement = space.encode("warm", "temperature warm");
        LanguageState notWarm = space.encode("not warm", "temperature not warm");

        learner.observe(question, "it is warm", warmAnswer, 1.0);
        learner.observe(question, "it is cold", coldAnswer, 0.35);
        learner.observe(question, "i do not know", uncertain, -0.5);
        learner.observe(warmStatement, "not", notWarm, 0.25);

        GoalSubspace answerGoal = GoalSubspace.approach("seek-answer", space.concept("answer"));
        List<LanguageTemporalLearner.PhraseScore> ranked = learner.rankPhrases(
                question,
                List.of("i do not know", "it is cold", "it is warm"),
                answerGoal);
        LanguageState predictedAnswer = learner.predict(question, ranked.get(0).phrase());
        LanguageState negatedWarmPrediction = learner.predict(warmStatement, "not");

        return new LanguageDemoResult(
                space,
                learner,
                question,
                ranked,
                predictedAnswer,
                warmStatement,
                negatedWarmPrediction,
                space.alignment(question, "answer"),
                space.alignment(predictedAnswer, "answer"),
                space.alignment(warmStatement, "warm"),
                space.alignment(negatedWarmPrediction, "warm"));
    }

    /**
     * Runs the demo and prints the learned language behavior.
     *
     * @param args ignored
     */
    public static void main(final String[] args) {
        LanguageDemoResult result = run();
        System.out.println("Language temporal updates: " + result.learner().updates());
        System.out.println("Best phrase: " + result.rankedPhrases().get(0).phrase());
        System.out.println("Answer alignment before: " + result.answerAlignmentBefore());
        System.out.println("Answer alignment after: " + result.answerAlignmentAfter());
        System.out.println("Warm alignment before not: " + result.warmAlignmentBefore());
        System.out.println("Warm alignment after not: " + result.warmAlignmentAfter());
    }

    private static LexicalSemanticSpace semanticSpace() {
        return LexicalSemanticSpace.of("toy-language", List.of(
                "question",
                "answer",
                "warm",
                "cold",
                "known",
                "unknown",
                "safe",
                "unsafe"))
                .withToken("question", 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
                .withToken("answer", 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
                .withToken("warm", 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0)
                .withToken("hot", 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0)
                .withToken("cold", 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0)
                .withToken("known", 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0)
                .withToken("unknown", 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0)
                .withToken("safe", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0)
                .withToken("unsafe", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0);
    }
}
