package com.github.javachaos.javaneuralnetwork.core;

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
 * Nonlinear simulated environment for Hilbert-state learning.
 */
public final class HilbertNonlinearEnvironmentExample {

    private static final int INPUT_DIMENSION = 4;
    private static final int OUTPUT_DIMENSION = 4;
    private static final int LIFTED_DIMENSION =
            INPUT_DIMENSION + (INPUT_DIMENSION * (INPUT_DIMENSION - 1) / 2);
    private static final int TRAINING_EPOCHS = 1_200;

    private HilbertNonlinearEnvironmentExample() {
    }

    /**
     * Summary of a nonlinear simulation run.
     *
     * @param trainingSamples number of training traces
     * @param validationSamples number of held-out validation traces
     * @param rawDimension input dimension seen by the raw learner
     * @param liftedDimension input dimension seen by the lifted learner
     * @param rawInitialTrainingMse raw training MSE before learning
     * @param rawFinalTrainingMse raw training MSE after learning
     * @param rawInitialValidationMse raw validation MSE before learning
     * @param rawFinalValidationMse raw validation MSE after learning
     * @param liftedInitialTrainingMse lifted training MSE before learning
     * @param liftedFinalTrainingMse lifted training MSE after learning
     * @param liftedInitialValidationMse lifted validation MSE before learning
     * @param liftedFinalValidationMse lifted validation MSE after learning
     * @param mixedObservation held-out nonlinear observation
     * @param mixedExpected expected environment response
     * @param rawMixedPrediction raw learner prediction
     * @param liftedMixedPrediction lifted learner prediction
     * @param rawMixedErrorSquared raw prediction squared error on the held-out observation
     * @param liftedMixedErrorSquared lifted prediction squared error on the held-out observation
     * @param rawDangerRelevance raw prediction danger relevance
     * @param liftedDangerRelevance lifted prediction danger relevance
     */
    public record NonlinearSimulationResult(
            int trainingSamples,
            int validationSamples,
            int rawDimension,
            int liftedDimension,
            double rawInitialTrainingMse,
            double rawFinalTrainingMse,
            double rawInitialValidationMse,
            double rawFinalValidationMse,
            double liftedInitialTrainingMse,
            double liftedFinalTrainingMse,
            double liftedInitialValidationMse,
            double liftedFinalValidationMse,
            WorldState mixedObservation,
            WorldState mixedExpected,
            WorldState rawMixedPrediction,
            WorldState liftedMixedPrediction,
            double rawMixedErrorSquared,
            double liftedMixedErrorSquared,
            double rawDangerRelevance,
            double liftedDangerRelevance) {
    }

    /**
     * Runs the nonlinear environment simulation.
     *
     * @return nonlinear simulation metrics
     */
    public static NonlinearSimulationResult runSimulation() {
        List<WorldState> trainingObservations = trainingObservations();
        List<WorldState> validationObservations = validationObservations();
        List<MemoryTrace> rawTraining = rawTraces(trainingObservations);
        List<MemoryTrace> rawValidation = rawTraces(validationObservations);
        List<MemoryTrace> liftedTraining = liftedTraces(trainingObservations);
        List<MemoryTrace> liftedValidation = liftedTraces(validationObservations);

        HilbertStateTransitionLearner rawLearner =
                HilbertStateTransitionLearner.zeroInitialized("raw-nonlinear",
                        INPUT_DIMENSION, OUTPUT_DIMENSION, 0.25);
        double rawInitialTrainingMse = rawLearner.meanSquaredError(rawTraining);
        double rawInitialValidationMse = rawLearner.meanSquaredError(rawValidation);
        rawLearner.train(rawTraining, TRAINING_EPOCHS);

        HilbertStateTransitionLearner liftedLearner =
                HilbertStateTransitionLearner.zeroInitialized("lifted-nonlinear",
                        LIFTED_DIMENSION, OUTPUT_DIMENSION, 0.45);
        double liftedInitialTrainingMse = liftedLearner.meanSquaredError(liftedTraining);
        double liftedInitialValidationMse = liftedLearner.meanSquaredError(liftedValidation);
        liftedLearner.train(liftedTraining, TRAINING_EPOCHS);

        WorldState mixedObservation = validationObservations.get(0);
        WorldState expected = environmentResponse(mixedObservation.name() + " expected", mixedObservation.state());
        WorldState rawPrediction = rawLearner.predict(mixedObservation);
        WorldState liftedPrediction = liftedLearner.predict(lift(mixedObservation));
        ConceptSubspace danger = ConceptSubspace.of("danger", HilbertVector.basis(OUTPUT_DIMENSION, 0));

        return new NonlinearSimulationResult(
                trainingObservations.size(),
                validationObservations.size(),
                INPUT_DIMENSION,
                LIFTED_DIMENSION,
                rawInitialTrainingMse,
                rawLearner.meanSquaredError(rawTraining),
                rawInitialValidationMse,
                rawLearner.meanSquaredError(rawValidation),
                liftedInitialTrainingMse,
                liftedLearner.meanSquaredError(liftedTraining),
                liftedInitialValidationMse,
                liftedLearner.meanSquaredError(liftedValidation),
                mixedObservation,
                expected,
                rawPrediction,
                liftedPrediction,
                squaredError(rawPrediction, expected),
                squaredError(liftedPrediction, expected),
                danger.relevance(rawPrediction.state()),
                danger.relevance(liftedPrediction.state()));
    }

    /**
     * Runs the simulation and prints its metrics.
     *
     * @param args ignored
     */
    public static void main(final String[] args) {
        NonlinearSimulationResult result = runSimulation();
        System.out.println("Training samples: " + result.trainingSamples());
        System.out.println("Validation samples: " + result.validationSamples());
        System.out.println("Raw dimension: " + result.rawDimension());
        System.out.println("Lifted dimension: " + result.liftedDimension());
        System.out.println("Raw initial training MSE: " + result.rawInitialTrainingMse());
        System.out.println("Raw final training MSE: " + result.rawFinalTrainingMse());
        System.out.println("Raw initial validation MSE: " + result.rawInitialValidationMse());
        System.out.println("Raw final validation MSE: " + result.rawFinalValidationMse());
        System.out.println("Lifted initial training MSE: " + result.liftedInitialTrainingMse());
        System.out.println("Lifted final training MSE: " + result.liftedFinalTrainingMse());
        System.out.println("Lifted initial validation MSE: " + result.liftedInitialValidationMse());
        System.out.println("Lifted final validation MSE: " + result.liftedFinalValidationMse());
        System.out.println("Held-out nonlinear observation: " + format(result.mixedObservation().state()));
        System.out.println("Raw prediction: " + format(result.rawMixedPrediction().state()));
        System.out.println("Lifted prediction: " + format(result.liftedMixedPrediction().state()));
        System.out.println("Expected: " + format(result.mixedExpected().state()));
        System.out.println("Raw held-out squared error: " + result.rawMixedErrorSquared());
        System.out.println("Lifted held-out squared error: " + result.liftedMixedErrorSquared());
        System.out.println("Raw danger relevance: " + result.rawDangerRelevance());
        System.out.println("Lifted danger relevance: " + result.liftedDangerRelevance());
    }

    private static List<WorldState> trainingObservations() {
        List<WorldState> observations = new ArrayList<>();
        observations.add(observation("smoke", 1.0, 0.0, 0.0, 0.0));
        observations.add(observation("warmth", 0.0, 1.0, 0.0, 0.0));
        observations.add(observation("question", 0.0, 0.0, 1.0, 0.0));
        observations.add(observation("silence", 0.0, 0.0, 0.0, 1.0));
        observations.add(observation("smoke + warmth", 1.0, 1.0, 0.0, 0.0));
        observations.add(observation("smoke + question", 1.0, 0.0, 1.0, 0.0));
        observations.add(observation("smoke + silence", 1.0, 0.0, 0.0, 1.0));
        observations.add(observation("warmth + question", 0.0, 1.0, 1.0, 0.0));
        observations.add(observation("warmth + silence", 0.0, 1.0, 0.0, 1.0));
        observations.add(observation("question + silence", 0.0, 0.0, 1.0, 1.0));
        return observations;
    }

    private static List<WorldState> validationObservations() {
        return List.of(
                observation("smoke + warmth + question", 1.0, 0.6, 0.8, 0.0),
                observation("warmth + question + silence", 0.0, 0.7, 0.5, 1.0),
                observation("weighted smoke + silence", 0.55, 0.0, 0.0, 0.84),
                observation("all mixed", 0.25, 0.5, 0.75, 0.35));
    }

    private static List<MemoryTrace> rawTraces(final List<WorldState> observations) {
        List<MemoryTrace> traces = new ArrayList<>();
        for (WorldState observation : observations) {
            traces.add(trace(observation.name(), observation,
                    environmentResponse(observation.name() + " response", observation.state())));
        }
        return traces;
    }

    private static List<MemoryTrace> liftedTraces(final List<WorldState> observations) {
        List<MemoryTrace> traces = new ArrayList<>();
        for (WorldState observation : observations) {
            traces.add(trace(observation.name(), lift(observation),
                    environmentResponse(observation.name() + " response", observation.state())));
        }
        return traces;
    }

    private static MemoryTrace trace(
            final String name,
            final WorldState before,
            final WorldState after) {
        return MemoryTrace.of(name, before,
                LinearOperator.zero(after.dimension(), before.dimension()), after);
    }

    private static WorldState observation(
            final String name,
            final double smoke,
            final double warmth,
            final double question,
            final double silence) {
        return WorldState.of(name, HilbertVector.of(smoke, warmth, question, silence).normalized());
    }

    private static WorldState lift(final WorldState observation) {
        HilbertVector state = observation.state();
        double[] values = new double[LIFTED_DIMENSION];
        for (int i = 0; i < INPUT_DIMENSION; i++) {
            values[i] = state.get(i);
        }
        int index = INPUT_DIMENSION;
        for (int i = 0; i < INPUT_DIMENSION; i++) {
            for (int j = i + 1; j < INPUT_DIMENSION; j++) {
                values[index++] = state.get(i) * state.get(j);
            }
        }
        return WorldState.of(observation.name() + " lifted", HilbertVector.of(values));
    }

    private static WorldState environmentResponse(final String name, final HilbertVector input) {
        double smoke = input.get(0);
        double warmth = input.get(1);
        double question = input.get(2);
        double silence = input.get(3);
        double smokeWarmth = smoke * warmth;
        double smokeQuestion = smoke * question;
        double smokeSilence = smoke * silence;
        double warmthQuestion = warmth * question;
        double warmthSilence = warmth * silence;
        double questionSilence = question * silence;

        double danger = 0.80 * smoke + 1.40 * smokeWarmth + 0.20 * smokeQuestion;
        double comfort = 0.70 * warmth + 0.90 * warmthSilence + 0.15 * smokeSilence;
        double curiosity = 0.85 * question + 0.40 * warmthQuestion + 0.25 * smokeQuestion;
        double rest = 0.75 * silence + 0.60 * warmthSilence + 0.10 * questionSilence;
        return WorldState.of(name, HilbertVector.of(danger, comfort, curiosity, rest));
    }

    private static double squaredError(final WorldState predicted, final WorldState expected) {
        return predicted.state().subtract(expected.state()).normSquared();
    }

    private static String format(final HilbertVector vector) {
        return Arrays.toString(vector.toArray());
    }
}
