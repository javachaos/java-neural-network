package com.github.javachaos.javaneuralnetwork.examples;

import java.util.List;

import com.github.javachaos.javaneuralnetwork.shared.hilbert.HilbertVector;
import com.github.javachaos.javaneuralnetwork.shared.hilbert.LinearOperator;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.ConceptSubspace;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.GoalSubspace;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.HilbertStateTransitionLearner;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.MemoryTrace;
import com.github.javachaos.javaneuralnetwork.shared.knowledge.WorldState;

/**
 * Minimal example of learning a Hilbert-space transition operator.
 */
public final class HilbertStateLearningExample {

    private HilbertStateLearningExample() {
    }

    /**
     * Learns the operator that swaps the x and y basis states.
     *
     * @param args ignored
     */
    public static void main(final String[] args) {
        WorldState x = WorldState.of("x", HilbertVector.basis(2, 0));
        WorldState y = WorldState.of("y", HilbertVector.basis(2, 1));
        List<MemoryTrace> examples = List.of(
                MemoryTrace.observe("x becomes y", x, y),
                MemoryTrace.observe("y becomes x", y, x));
        HilbertStateTransitionLearner learner =
                HilbertStateTransitionLearner.zeroInitialized("swap", 2, 2, 1.0);
        double beforeError = learner.meanSquaredError(examples);
        HilbertStateTransitionLearner.TrainingReport report = learner.train(examples, 1);
        GoalSubspace seekY = GoalSubspace.approach("seek-y",
                ConceptSubspace.of("y-axis", HilbertVector.basis(2, 1)));
        WorldState predicted = learner.predict(x);

        System.out.println("Initial MSE: " + beforeError);
        System.out.println("Final MSE: " + report.finalMeanSquaredError());
        System.out.println("Learned operator: " + learner.operator());
        System.out.println("Prediction for |x>: " + predicted.state());
        System.out.println("Goal progress toward y-axis: " + seekY.progress(x, predicted));
        System.out.println("Expected swap operator: " + LinearOperator.of(new double[][]{
                {0.0, 1.0},
                {1.0, 0.0}
        }));
    }
}
