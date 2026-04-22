package com.github.javachaos.javaneuralnetwork.core;

import java.util.List;

import com.github.javachaos.javaneuralnetwork.shared.knowledge.LanguageState;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.LanguageTemporalLearner;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.TextLanguageTrainer;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.TextTrainingSample;

/**
 * Plain-text training demo for the Hilbert language learner.
 */
public final class HilbertTextTrainingExample {

    private HilbertTextTrainingExample() {
    }

    /**
     * Text training demo result.
     *
     * @param training trained text model
     * @param rankedPhrases ranked phrase candidates
     * @param predictedBest predicted state after the best phrase
     * @param answerCoordinate answer coordinate of the prediction
     * @param warmCoordinate warm coordinate of the prediction
     * @param knownCoordinate known coordinate of the prediction
     */
    public record TextTrainingDemoResult(
            TextLanguageTrainer.TrainingResult training,
            List<LanguageTemporalLearner.PhraseScore> rankedPhrases,
            LanguageState predictedBest,
            double answerCoordinate,
            double warmCoordinate,
            double knownCoordinate) {
        public TextTrainingDemoResult {
            rankedPhrases = List.copyOf(rankedPhrases);
        }
    }

    /**
     * Runs the text training demo.
     *
     * @return demo result
     */
    public static TextTrainingDemoResult run() {
        TextLanguageTrainer.TrainingResult training = TextLanguageTrainer.train(
                "weather-text",
                trainingSamples(),
                1.0);
        List<String> candidates = List.of("i do not know", "it is cold", "it is warm");
        List<LanguageTemporalLearner.PhraseScore> ranked = training.rankToward(
                "weather question unknown",
                candidates,
                "answer");
        LanguageState predictedBest = training.predict(
                "weather question unknown",
                ranked.get(0).phrase());
        return new TextTrainingDemoResult(
                training,
                ranked,
                predictedBest,
                training.semanticSpace().coordinate(predictedBest, "answer"),
                training.semanticSpace().coordinate(predictedBest, "warm"),
                training.semanticSpace().coordinate(predictedBest, "known"));
    }

    /**
     * Runs the demo and prints the result.
     *
     * @param args ignored
     */
    public static void main(final String[] args) {
        TextTrainingDemoResult result = run();
        System.out.println("Text samples: " + result.training().samples());
        System.out.println("Vocabulary size: " + result.training().vocabularySize());
        System.out.println("Text temporal updates: " + result.training().learner().updates());
        System.out.println("Best phrase: " + result.rankedPhrases().get(0).phrase());
        System.out.println("Answer coordinate: " + result.answerCoordinate());
        System.out.println("Warm coordinate: " + result.warmCoordinate());
        System.out.println("Known coordinate: " + result.knownCoordinate());
    }

    private static List<TextTrainingSample> trainingSamples() {
        return List.of(
                TextTrainingSample.of(
                        "weather question unknown",
                        "it is warm",
                        "weather answer warm known",
                        1.0),
                TextTrainingSample.of(
                        "weather question unknown",
                        "it is cold",
                        "weather answer cold known",
                        0.25),
                TextTrainingSample.of(
                        "weather question unknown",
                        "i do not know",
                        "weather question unknown",
                        -0.5),
                TextTrainingSample.of(
                        "temperature warm",
                        "not",
                        "temperature not warm",
                        0.25));
    }
}
