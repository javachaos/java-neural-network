package com.github.javachaos.javaneuralnetwork.examples;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.javachaos.javaneuralnetwork.shared.hilbert.HilbertVector;
import com.github.javachaos.javaneuralnetwork.shared.hilbert.LinearOperator;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.ConceptSubspace;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.HilbertStateTransitionLearner;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.MemoryTrace;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.WorldState;

/**
 * Simulated environment for training a Hilbert-state transition learner.
 */
public final class HilbertToyEnvironmentExample {

    private static final int DIMENSION = 4;
    private static final LinearOperator TRUE_ENVIRONMENT = LinearOperator.of(new double[][]{
            {1.0, 0.35, 0.0, 0.0},
            {0.0, 0.90, 0.0, 0.45},
            {0.0, 0.0, 1.0, 0.0},
            {0.0, 0.10, 0.0, 0.80}
    });

    private HilbertToyEnvironmentExample() {
    }

    /**
     * Summary of one deterministic simulation run.
     *
     * @param trainingSamples number of training traces
     * @param validationSamples number of validation traces
     * @param initialTrainingMse training MSE before learning
     * @param finalTrainingMse training MSE after learning
     * @param initialValidationMse validation MSE before learning
     * @param finalValidationMse validation MSE after learning
     * @param learnedOperator learned transition operator
     * @param mixedObservation held-out mixed observation
     * @param mixedPrediction prediction for the mixed observation
     * @param mixedExpected expected target for the mixed observation
     * @param mixedDangerRelevance predicted danger relevance
     * @param mixedCuriosityRelevance predicted curiosity relevance
     */
    public record SimulationResult(
            int trainingSamples,
            int validationSamples,
            double initialTrainingMse,
            double finalTrainingMse,
            double initialValidationMse,
            double finalValidationMse,
            LinearOperator learnedOperator,
            WorldState mixedObservation,
            WorldState mixedPrediction,
            WorldState mixedExpected,
            double mixedDangerRelevance,
            double mixedCuriosityRelevance) {
    }

    /**
     * Runs the deterministic toy environment simulation.
     *
     * @return simulation metrics and example prediction
     */
    public static SimulationResult runSimulation() {
        List<MemoryTrace> training = trainingSet();
        List<MemoryTrace> validation = validationSet();
        HilbertStateTransitionLearner learner =
                HilbertStateTransitionLearner.zeroInitialized("toy-environment", DIMENSION, DIMENSION, 1.0);
        double initialTrainingMse = learner.meanSquaredError(training);
        double initialValidationMse = learner.meanSquaredError(validation);
        learner.train(training, 1);
        double finalTrainingMse = learner.meanSquaredError(training);
        double finalValidationMse = learner.meanSquaredError(validation);
        WorldState mixedObservation = observation("smoke + question", 1.0, 0.0, 1.0, 0.0);
        WorldState mixedPrediction = learner.predict(mixedObservation);
        WorldState mixedExpected = environmentResponse("expected smoke + question", mixedObservation.state());
        ConceptSubspace danger = ConceptSubspace.of("danger", HilbertVector.basis(DIMENSION, 0));
        ConceptSubspace curiosity = ConceptSubspace.of("curiosity", HilbertVector.basis(DIMENSION, 2));

        return new SimulationResult(
                training.size(),
                validation.size(),
                initialTrainingMse,
                finalTrainingMse,
                initialValidationMse,
                finalValidationMse,
                learner.operator(),
                mixedObservation,
                mixedPrediction,
                mixedExpected,
                danger.relevance(mixedPrediction.state()),
                curiosity.relevance(mixedPrediction.state()));
    }

    /**
     * Runs the simulation and prints its metrics.
     *
     * @param args ignored
     */
    public static void main(final String[] args) {
        SimulationResult result = runSimulation();
        System.out.println("Training samples: " + result.trainingSamples());
        System.out.println("Validation samples: " + result.validationSamples());
        System.out.println("Initial training MSE: " + result.initialTrainingMse());
        System.out.println("Final training MSE: " + result.finalTrainingMse());
        System.out.println("Initial validation MSE: " + result.initialValidationMse());
        System.out.println("Final validation MSE: " + result.finalValidationMse());
        System.out.println("Learned operator: " + result.learnedOperator());
        System.out.println("Held-out observation: " + format(result.mixedObservation().state()));
        System.out.println("Prediction: " + format(result.mixedPrediction().state()));
        System.out.println("Expected: " + format(result.mixedExpected().state()));
        System.out.println("Prediction danger relevance: " + result.mixedDangerRelevance());
        System.out.println("Prediction curiosity relevance: " + result.mixedCuriosityRelevance());
    }

    private static List<MemoryTrace> trainingSet() {
        return List.of(
                trace("smoke", observation("smoke", 1.0, 0.0, 0.0, 0.0)),
                trace("warmth", observation("warmth", 0.0, 1.0, 0.0, 0.0)),
                trace("question", observation("question", 0.0, 0.0, 1.0, 0.0)),
                trace("silence", observation("silence", 0.0, 0.0, 0.0, 1.0)));
    }

    private static List<MemoryTrace> validationSet() {
        List<MemoryTrace> validation = new ArrayList<>();
        validation.add(trace("smoke + question", observation("smoke + question", 1.0, 0.0, 1.0, 0.0)));
        validation.add(trace("warmth + silence", observation("warmth + silence", 0.0, 1.0, 0.0, 1.0)));
        validation.add(trace("mixed-1", observation("mixed-1", 0.4, 0.8, 0.4, 0.0)));
        validation.add(trace("mixed-2", observation("mixed-2", 0.0, 0.35, 0.0, 0.94)));
        validation.add(trace("mixed-3", observation("mixed-3", 0.2, 0.3, 0.8, 0.4)));
        return validation;
    }

    private static MemoryTrace trace(final String name, final WorldState input) {
        return MemoryTrace.observe(name, input, environmentResponse(name + " response", input.state()));
    }

    private static WorldState observation(
            final String name,
            final double smoke,
            final double warmth,
            final double question,
            final double silence) {
        return WorldState.of(name, HilbertVector.of(smoke, warmth, question, silence).normalized());
    }

    private static WorldState environmentResponse(final String name, final HilbertVector input) {
        return WorldState.of(name, TRUE_ENVIRONMENT.apply(input));
    }

    private static String format(final HilbertVector vector) {
        return Arrays.toString(vector.toArray());
    }
}
