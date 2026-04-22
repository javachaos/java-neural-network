package com.github.javachaos.javaneuralnetwork.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.javachaos.javaneuralnetwork.shared.hilbert.HilbertVector;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.ConceptSubspace;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.DevelopmentalLearner;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.WorldState;

/**
 * Tiny developmental world where a learner predicts a repeated sensory stream.
 */
public final class BabyWorldDevelopmentExample {

    private static final int RED = 0;
    private static final int BLUE = 1;
    private static final int VISIBLE = 2;
    private static final int SOUND = 3;
    private static final int HIDDEN = 4;
    private static final int RETURNED = 5;
    private static final int DIMENSION = 6;
    private static final int EPISODES = 48;

    private BabyWorldDevelopmentExample() {
    }

    /**
     * Summary of one baby-world developmental run.
     *
     * @param observations total observations consumed
     * @param traces remembered temporal traces
     * @param prototypes compressed BMU prototypes
     * @param firstPredictedSurprise surprise on the first predicted transition
     * @param finalMeanSurprise mean surprise over the last cycle
     * @param prototypeCompressionRatio prototypes divided by observations
     * @param lastObservation most recent observed state
     * @param predictedNext predicted next state after the final observation
     * @param expectedNext expected next state in the cycle
     * @param nextErrorSquared squared error of the final next-state prediction
     * @param predictedVisibleRelevance visible-concept relevance of predicted next
     * @param predictedReturnedRelevance returned-concept relevance of predicted next
     */
    public record BabyWorldResult(
            int observations,
            int traces,
            int prototypes,
            double firstPredictedSurprise,
            double finalMeanSurprise,
            double prototypeCompressionRatio,
            WorldState lastObservation,
            WorldState predictedNext,
            WorldState expectedNext,
            double nextErrorSquared,
            double predictedVisibleRelevance,
            double predictedReturnedRelevance) {
    }

    /**
     * Runs the developmental stream simulation.
     *
     * @return simulation metrics
     */
    public static BabyWorldResult runSimulation() {
        DevelopmentalLearner learner =
                DevelopmentalLearner.zeroInitialized("baby-world", DIMENSION, 0.35, 0.18, 0.20);
        List<WorldState> cycle = babyCycle();
        List<DevelopmentalLearner.ObservationStep> steps = new ArrayList<>();
        for (int episode = 0; episode < EPISODES; episode++) {
            steps.addAll(learner.observeAll(cycle));
        }

        double firstPredictedSurprise = steps.stream()
                .filter(DevelopmentalLearner.ObservationStep::hasPrediction)
                .findFirst()
                .orElseThrow()
                .normalizedSurprise();
        double finalMeanSurprise = steps.subList(steps.size() - cycle.size(), steps.size())
                .stream()
                .mapToDouble(DevelopmentalLearner.ObservationStep::normalizedSurprise)
                .average()
                .orElseThrow();

        WorldState predictedNext = learner.predictNext().orElseThrow();
        WorldState expectedNext = cycle.get(0);
        ConceptSubspace visible = ConceptSubspace.of("visible", HilbertVector.basis(DIMENSION, VISIBLE));
        ConceptSubspace returned = ConceptSubspace.of("returned", HilbertVector.basis(DIMENSION, RETURNED));

        return new BabyWorldResult(
                learner.observations(),
                learner.worldModel().traceCount(),
                learner.prototypes().size(),
                firstPredictedSurprise,
                finalMeanSurprise,
                learner.prototypeCompressionRatio(),
                learner.previousObservation().orElseThrow(),
                predictedNext,
                expectedNext,
                squaredError(predictedNext, expectedNext),
                visible.relevance(predictedNext.state()),
                returned.relevance(predictedNext.state()));
    }

    /**
     * Runs the simulation and prints its metrics.
     *
     * @param args ignored
     */
    public static void main(final String[] args) {
        BabyWorldResult result = runSimulation();
        System.out.println("Observations: " + result.observations());
        System.out.println("Memory traces: " + result.traces());
        System.out.println("BMU prototypes: " + result.prototypes());
        System.out.println("First predicted surprise: " + result.firstPredictedSurprise());
        System.out.println("Final mean surprise: " + result.finalMeanSurprise());
        System.out.println("Prototype compression ratio: " + result.prototypeCompressionRatio());
        System.out.println("Last observation: " + format(result.lastObservation().state()));
        System.out.println("Predicted next: " + format(result.predictedNext().state()));
        System.out.println("Expected next: " + format(result.expectedNext().state()));
        System.out.println("Next squared error: " + result.nextErrorSquared());
        System.out.println("Predicted visible relevance: " + result.predictedVisibleRelevance());
        System.out.println("Predicted returned relevance: " + result.predictedReturnedRelevance());
    }

    private static List<WorldState> babyCycle() {
        return List.of(
                state("red toy visible", values(1.0, 0.0, 1.0, 0.0, 0.0, 0.0)),
                state("red toy makes sound", values(1.0, 0.0, 1.0, 1.0, 0.0, 0.0)),
                state("red toy hidden", values(1.0, 0.0, 0.0, 0.0, 1.0, 0.0)),
                state("red toy returned", values(1.0, 0.0, 1.0, 0.0, 0.0, 1.0)));
    }

    private static WorldState state(final String name, final double[] values) {
        return WorldState.of(name, HilbertVector.of(values).normalized());
    }

    private static double[] values(
            final double red,
            final double blue,
            final double visible,
            final double sound,
            final double hidden,
            final double returned) {
        double[] values = new double[DIMENSION];
        values[RED] = red;
        values[BLUE] = blue;
        values[VISIBLE] = visible;
        values[SOUND] = sound;
        values[HIDDEN] = hidden;
        values[RETURNED] = returned;
        return values;
    }

    private static double squaredError(final WorldState predicted, final WorldState expected) {
        return predicted.state().subtract(expected.state()).normSquared();
    }

    private static String format(final HilbertVector vector) {
        return Arrays.toString(vector.toArray());
    }
}
