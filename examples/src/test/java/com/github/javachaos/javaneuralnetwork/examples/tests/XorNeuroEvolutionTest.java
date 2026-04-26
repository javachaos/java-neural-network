package com.github.javachaos.javaneuralnetwork.examples.tests;

import com.github.javachaos.javaneuralnetwork.examples.CheckerboardProblem;
import com.github.javachaos.javaneuralnetwork.examples.CircleProblem;
import com.github.javachaos.javaneuralnetwork.examples.DefaultNeuroEvolutionProblemCatalog;
import com.github.javachaos.javaneuralnetwork.examples.EvolvableXorGenome;
import com.github.javachaos.javaneuralnetwork.examples.GeometricClassificationProblem;
import com.github.javachaos.javaneuralnetwork.examples.IdentityAutoencoderProblem;
import com.github.javachaos.javaneuralnetwork.examples.MettleTestProblem;
import com.github.javachaos.javaneuralnetwork.examples.NeuroEvolution;
import com.github.javachaos.javaneuralnetwork.examples.NeuroEvolutionChampionCheckpoint;
import com.github.javachaos.javaneuralnetwork.examples.NeuroEvolutionGeneralizationEvaluator;
import com.github.javachaos.javaneuralnetwork.examples.NeuroEvolutionProblem;
import com.github.javachaos.javaneuralnetwork.examples.NeuroEvolutionProblemCatalog;
import com.github.javachaos.javaneuralnetwork.examples.RingProblem;
import com.github.javachaos.javaneuralnetwork.examples.RichEvolvedXorLearner;
import com.github.javachaos.javaneuralnetwork.examples.RichEvolvedProblemLearner;
import com.github.javachaos.javaneuralnetwork.examples.SineWaveProblem;
import com.github.javachaos.javaneuralnetwork.examples.TextTrainingMettleProblem;
import com.github.javachaos.javaneuralnetwork.examples.TransformerAttentionProblem;
import com.github.javachaos.javaneuralnetwork.examples.TransformerBlockProblem;
import com.github.javachaos.javaneuralnetwork.examples.XorActivationFunction;
import com.github.javachaos.javaneuralnetwork.examples.XorGeneralizationEvaluator;
import com.github.javachaos.javaneuralnetwork.examples.XorInputRepresentation;
import com.github.javachaos.javaneuralnetwork.examples.XorLearningSchedule;
import com.github.javachaos.javaneuralnetwork.examples.XorLossFunction;
import com.github.javachaos.javaneuralnetwork.examples.XorMutationType;
import com.github.javachaos.javaneuralnetwork.examples.XorNeuroEvolution;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XorNeuroEvolutionTest {

    private final NeuroEvolutionProblemCatalog catalog = new DefaultNeuroEvolutionProblemCatalog();

    @Test
    void supportsEveryDeclaredMutation() {
        EvolvableXorGenome genome = EvolvableXorGenome.random(new Random(1L));

        for (XorMutationType mutationType : EvolvableXorGenome.allowedMutations()) {
            genome = genome.mutate(new Random(100L + mutationType.ordinal()), mutationType, 0.12);

            assertTrue(genome.inputSize() > 0);
            assertTrue(genome.outputInputSize() > 0);
            assertTrue(Double.isFinite(genome.complexityCost()));
        }
    }

    @Test
    void richLearnerRunsWithMemoryRecurrenceAndLocalUpdates() {
        EvolvableXorGenome genome = new EvolvableXorGenome(
                3,
                3,
                2,
                0.85,
                XorActivationFunction.TANH,
                XorActivationFunction.SIGMOID,
                XorInputRepresentation.MIXED,
                XorLossFunction.HUBER,
                XorLearningSchedule.INVERSE_TIME,
                true,
                true,
                true,
                true,
                true,
                true,
                0.08,
                0.08,
                0.03,
                0.02,
                0.25,
                0.2,
                0.002,
                0.25,
                1.0,
                2.0,
                2.0);

        RichEvolvedXorLearner learner = new RichEvolvedXorLearner(genome, new Random(2L));

        assertTrue(Double.isFinite(learner.train(50, 0.02)));
        assertTrue(Double.isFinite(learner.evaluateMeanSquaredError()));
        assertTrue(learner.accuracy() >= 0.0);
        assertTrue(Double.isFinite(learner.predict(1.0, 0.0)));
        XorGeneralizationEvaluator.GeneralizationMetrics metrics =
                XorGeneralizationEvaluator.evaluate(learner, 5, 2, 0.08, 3L);
        assertTrue(Double.isFinite(metrics.surfaceMeanSquaredError()));
        assertTrue(Double.isFinite(metrics.jitterMeanSquaredError()));
        assertTrue(Double.isFinite(metrics.smoothnessPenalty()));
        assertTrue(learner.snapshot().nodes().size() > 0);
        assertTrue(learner.snapshot().links().size() > 0);
    }

    @Test
    void richLearnerSupportsFourHiddenLayers() {
        EvolvableXorGenome genome = EvolvableXorGenome.random(new Random(11L));
        for (int i = 0; i < 8; i++) {
            genome = genome.mutate(new Random(200L + i), XorMutationType.ADD_HIDDEN_LAYER, 0.1);
        }

        RichEvolvedXorLearner learner = new RichEvolvedXorLearner(genome, new Random(12L));

        assertEquals(4, genome.hiddenLayers());
        assertEquals(genome.hiddenNeurons() * 4, genome.totalHiddenNeurons());
        assertTrue(Double.isFinite(learner.train(25, 0.04)));
        assertTrue(Double.isFinite(learner.predict(1.0, 0.0)));
        long renderedHiddenLayers = learner.snapshot().nodes().stream()
                .filter(node -> node.layer() == RichEvolvedProblemLearner.VisualNodeLayer.HIDDEN)
                .mapToInt(RichEvolvedProblemLearner.VisualNode::depth)
                .distinct()
                .count();
        assertEquals(4, renderedHiddenLayers);
    }

    @Test
    void genericLearnerRunsAgainstNonXorProblem() {
        NeuroEvolutionProblem problem = catalog.find("and", 5);
        EvolvableXorGenome genome = EvolvableXorGenome.random(new Random(7L));
        RichEvolvedProblemLearner learner = new RichEvolvedProblemLearner(problem, genome, new Random(8L));

        assertEquals("OR", catalog.find("or", 5).name());
        assertEquals("Radial bump", catalog.find("radial-bump", 5).name());
        assertEquals("Circle", catalog.find("circle", 5).name());
        assertEquals("Ring", catalog.find("ring", 5).name());
        assertEquals("Checkerboard", catalog.find("checkerboard", 5).name());
        assertEquals("Sine wave", catalog.find("sine-wave", 5).name());
        assertEquals("Identity autoencoder", catalog.find("autoencoder", 5).name());
        assertEquals("Transformer attention", catalog.find("transformer", 5).name());
        assertEquals("Text training mettle", catalog.find("text", 5).name());
        assertEquals("Spiral bands", catalog.find("spiral-bands", 5).name());
        assertEquals("Mettle test", catalog.find("mettle-test", 5).name());
        assertTrue(Double.isFinite(learner.train(30, 0.03)));
        assertTrue(Double.isFinite(learner.evaluateMeanSquaredError()));
        assertTrue(Double.isFinite(learner.predict(new double[] {1.0, 1.0})));
        assertTrue(genome.inputSize(problem) > 0);
        assertTrue(Double.isFinite(genome.complexityCost(problem)));

        NeuroEvolutionGeneralizationEvaluator.GeneralizationMetrics metrics =
                NeuroEvolutionGeneralizationEvaluator.evaluate(learner, problem, 2, 0.08, 9L);
        assertTrue(Double.isFinite(metrics.generalizationMeanSquaredError()));
        assertTrue(Double.isFinite(metrics.jitterMeanSquaredError()));
        assertTrue(Double.isFinite(metrics.smoothnessPenalty()));
    }

    @Test
    void expandedProblemCatalogProducesValidTargets() {
        for (String problemName : catalog.problemKeys()) {
            NeuroEvolutionProblem problem = catalog.find(problemName, 7);

            assertTrue(problem.inputDimensions() > 0);
            assertTrue(problem.trainingSamples().size() >= 4);
            assertTrue(!problem.generalizationSamples().isEmpty());
            assertTrue(problem.kernelCenters().size() > 0);
            assertTrue(problem.description().length() > 80);
            for (var sample : problem.trainingSamples()) {
                assertEquals(problem.outputDimensions(), sample.outputDimensions());
                double[] expected = problem.targetVector(sample.input());
                double[] actual = sample.targets();
                for (int output = 0; output < expected.length; output++) {
                    assertTrue(actual[output] >= 0.0 && actual[output] <= 1.0);
                    assertEquals(expected[output], actual[output]);
                }
            }
        }

        assertEquals(1.0, new CircleProblem(7).target(new double[] {0.5, 0.5}));
        assertEquals(0.0, new CircleProblem(7).target(new double[] {0.0, 0.0}));
        assertEquals(1.0, new RingProblem(7).target(new double[] {0.75, 0.5}));
        assertEquals(0.0, new RingProblem(7).target(new double[] {0.5, 0.5}));
        assertEquals(1.0, new SineWaveProblem(7).target(new double[] {0.25}), 1.0e-12);
        assertEquals(0.0, new SineWaveProblem(7).target(new double[] {0.75}), 1.0e-12);

        NeuroEvolutionProblem hardProblem = new MettleTestProblem(7);
        long positives = hardProblem.trainingSamples().stream().filter(sample -> sample.target() >= 0.5).count();
        assertTrue(positives > 0);
        assertTrue(positives < hardProblem.trainingSamples().size());
        assertTrue(hardProblem.trainingSamples().size() > new CheckerboardProblem(7).trainingSamples().size());
    }

    @Test
    void textTrainingMettleProblemProducesNextTokenTargets() {
        TextTrainingMettleProblem problem = new TextTrainingMettleProblem(7);

        double[] startTarget = problem.targetVector(problem.encodeTokenContext(0, 0, 0, 0, 0));
        double[] redCubeTarget = problem.targetVector(problem.encodeTokenContext(0, 0, 1, 3, 5));
        double[] blueCubeTarget = problem.targetVector(problem.encodeTokenContext(0, 0, 1, 4, 5));
        double[] exceptionTarget = problem.targetVector(problem.encodeTokenContext(0, 0, 2, 4, 5));

        assertEquals(5, problem.inputDimensions());
        assertEquals(9, problem.outputDimensions());
        assertTrue(problem.classification());
        assertEquals("GLOWS", problem.outputLabel(8));
        assertEquals(0.5, startTarget[1], 1.0e-12);
        assertEquals(0.5, startTarget[2], 1.0e-12);
        assertEquals(1.0, redCubeTarget[8], 1.0e-12);
        assertEquals(1.0, blueCubeTarget[7], 1.0e-12);
        assertEquals(1.0, exceptionTarget[8], 1.0e-12);
        assertTrue(problem.predictionMatches(startTarget, new double[] {0.0, 0.8, 0.7, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1}));
    }

    @Test
    void genericLearnerSupportsTextTrainingMettleProblem() {
        TextTrainingMettleProblem problem = new TextTrainingMettleProblem(5);
        EvolvableXorGenome genome = EvolvableXorGenome.random(new Random(51L));
        RichEvolvedProblemLearner learner = new RichEvolvedProblemLearner(problem, genome, new Random(52L));

        double mse = learner.train(8, 0.04);
        double[] prediction = learner.predictVector(problem.encodeTokenContext(0, 0, 1, 3, 5));
        long outputNodes = learner.snapshot().nodes().stream()
                .filter(node -> node.layer() == RichEvolvedProblemLearner.VisualNodeLayer.OUTPUT)
                .count();

        assertEquals(9, prediction.length);
        assertEquals(9, outputNodes);
        assertTrue(Double.isFinite(mse));
        assertTrue(Double.isFinite(learner.meanSquaredError(problem.generalizationSamples())));
    }

    @Test
    void transformerAttentionProblemProducesAttentionTargets() {
        NeuroEvolutionProblem problem = new TransformerAttentionProblem(7);

        double[] firstKeyTarget = problem.targetVector(new double[] {0.0, 0.0, 1.0, 0.2, 0.8});
        double[] secondKeyTarget = problem.targetVector(new double[] {1.0, 0.0, 1.0, 0.2, 0.8});
        double[] balancedTarget = problem.targetVector(new double[] {0.5, 0.25, 0.75, 0.2, 0.8});

        assertEquals(5, problem.inputDimensions());
        assertEquals(3, problem.outputDimensions());
        assertTrue(firstKeyTarget[0] > 0.99);
        assertTrue(firstKeyTarget[2] < 0.21);
        assertTrue(secondKeyTarget[1] > 0.99);
        assertTrue(secondKeyTarget[2] > 0.79);
        assertEquals(0.5, balancedTarget[0], 1.0e-12);
        assertEquals(0.5, balancedTarget[1], 1.0e-12);
        assertEquals(0.5, balancedTarget[2], 1.0e-12);
    }

    @Test
    void genericLearnerSupportsTransformerAttentionProblem() {
        NeuroEvolutionProblem problem = new TransformerAttentionProblem(5);
        EvolvableXorGenome genome = EvolvableXorGenome.random(new Random(41L));
        RichEvolvedProblemLearner learner = new RichEvolvedProblemLearner(problem, genome, new Random(42L));

        double mse = learner.train(10, 0.04);
        double[] prediction = learner.predictVector(new double[] {0.25, 0.2, 0.8, 0.1, 0.9});
        long outputNodes = learner.snapshot().nodes().stream()
                .filter(node -> node.layer() == RichEvolvedProblemLearner.VisualNodeLayer.OUTPUT)
                .count();

        assertEquals(3, prediction.length);
        assertEquals(3, outputNodes);
        assertTrue(Double.isFinite(mse));
        assertTrue(Double.isFinite(learner.configuredLoss()));
    }

    @Test
    void transformerBlockProblemProducesTeacherBlockTargets() {
        TransformerBlockProblem problem = new TransformerBlockProblem(7);

        double[] redCubeTarget = problem.targetVector(problem.encodeTokenSequence(0, 1, 3));
        double[] blueSphereTarget = problem.targetVector(problem.encodeTokenSequence(0, 2, 4));

        assertEquals(3, problem.inputDimensions());
        assertEquals(63, problem.outputDimensions());
        assertEquals("Y0.0", problem.outputLabel(0));
        assertEquals("Y2.2", problem.outputLabel(8));
        assertEquals("Q0.P0.D0", problem.outputLabel(9));
        assertEquals("A1.Q2.K2", problem.outputLabel(62));
        assertEquals("RED", problem.tokenLabel(1));
        assertEquals(6, problem.outputGroups().size());
        assertEquals("Final residual outputs", problem.outputGroups().get(0).name());
        assertEquals("Attention head 1", problem.outputGroups().get(5).name());
        assertTrue(problem.outputGroups().get(4).objectiveWeight()
                > problem.outputGroups().get(1).objectiveWeight());
        assertTrue(problem.generalizationSamples().size() > 20);
        assertTrue(different(redCubeTarget, blueSphereTarget));
        for (double target : redCubeTarget) {
            assertTrue(target >= 0.0 && target <= 1.0);
        }
    }

    @Test
    void genericLearnerSupportsTransformerBlockProblem() {
        TransformerBlockProblem problem = new TransformerBlockProblem(5);
        EvolvableXorGenome genome = EvolvableXorGenome.random(new Random(61L));
        RichEvolvedProblemLearner learner = new RichEvolvedProblemLearner(problem, genome, new Random(62L));

        double mse = learner.train(6, 0.04);
        double[] prediction = learner.predictVector(problem.encodeTokenSequence(0, 1, 3));
        long outputNodes = learner.snapshot().nodes().stream()
                .filter(node -> node.layer() == RichEvolvedProblemLearner.VisualNodeLayer.OUTPUT)
                .count();

        assertEquals(63, prediction.length);
        assertEquals(63, outputNodes);
        assertEquals(6, learner.outputGroupScores().size());
        assertTrue(learner.outputGroupScores().stream()
                .allMatch(score -> Double.isFinite(score.generalizationMeanSquaredError())
                        && Double.isFinite(score.baselineGeneralizationMeanSquaredError())));
        assertTrue(Double.isFinite(learner.groupedMeanSquaredError(problem.generalizationSamples())
                .baselineRelativeMeanSquaredError()));
        assertTrue(Double.isFinite(mse));
        assertTrue(Double.isFinite(learner.meanSquaredError(problem.generalizationSamples())));
    }

    @Test
    void visualSnapshotMarksBiasLinksSeparatelyFromSignedFeedForwardLinks() {
        NeuroEvolutionProblem problem = new TransformerAttentionProblem(5);
        EvolvableXorGenome genome = new EvolvableXorGenome(
                3,
                2,
                0,
                0,
                1.0,
                XorActivationFunction.TANH,
                XorActivationFunction.LINEAR,
                XorInputRepresentation.RAW,
                XorLossFunction.MEAN_SQUARED,
                XorLearningSchedule.CONSTANT,
                true,
                true,
                false,
                false,
                false,
                false,
                0.04,
                0.04,
                0.0,
                0.02,
                0.5,
                0.1,
                0.0,
                0.0,
                1.0,
                1.0,
                1.0);
        RichEvolvedProblemLearner.NetworkSnapshot snapshot =
                new RichEvolvedProblemLearner(problem, genome, new Random(63L)).snapshot();

        assertTrue(snapshot.links().stream()
                .anyMatch(link -> link.bias() && link.fromId().equals("feature-0")));
        assertTrue(snapshot.links().stream()
                .anyMatch(link -> link.bias() && link.fromId().equals("hidden-bias-1")));
        assertTrue(snapshot.links().stream()
                .anyMatch(link -> link.bias() && link.fromId().equals("output-bias")));
        assertTrue(snapshot.links().stream()
                .anyMatch(link -> !link.bias() && !link.recurrent()));
    }

    @Test
    void statelessProblemsEvaluateSamplesIndependently() {
        NeuroEvolutionProblem problem = new TransformerBlockProblem(5);
        EvolvableXorGenome genome = new EvolvableXorGenome(
                3,
                1,
                3,
                2,
                0.9,
                XorActivationFunction.TANH,
                XorActivationFunction.LINEAR,
                XorInputRepresentation.RAW,
                XorLossFunction.MEAN_SQUARED,
                XorLearningSchedule.CONSTANT,
                true,
                true,
                false,
                false,
                false,
                false,
                0.04,
                0.04,
                0.2,
                0.02,
                0.5,
                0.1,
                0.0,
                0.0,
                1.0,
                1.0,
                1.0);
        RichEvolvedProblemLearner learner = new RichEvolvedProblemLearner(problem, genome, new Random(71L));

        double manualMeanSquaredError = 0.0;
        int count = 0;
        for (var sample : problem.trainingSamples()) {
            double[] prediction = learner.predictVector(sample.input());
            for (int output = 0; output < prediction.length; output++) {
                double error = sample.targets()[output] - prediction[output];
                manualMeanSquaredError += error * error;
                count++;
            }
        }

        assertTrue(!problem.statefulSamples());
        assertEquals(
                manualMeanSquaredError / count,
                learner.meanSquaredError(problem.trainingSamples()),
                1.0e-12);
    }

    @Test
    void championCheckpointsRoundTripGenomeScoreAndConfig() throws Exception {
        NeuroEvolutionProblem problem = new TransformerAttentionProblem(5);
        XorNeuroEvolution.EvolutionConfig config =
                new XorNeuroEvolution.EvolutionConfig(
                        8,
                        2,
                        20,
                        0.03,
                        1,
                        0.08,
                        0.001,
                        2,
                        37L);
        EvolvableXorGenome genome = EvolvableXorGenome.random(new Random(38L));
        XorNeuroEvolution.CandidateScore score = XorNeuroEvolution.evaluate(problem, genome, config, 4);
        Path checkpoint = Files.createTempFile("neuro-evolution-champion", ".properties");

        NeuroEvolutionChampionCheckpoint.save(checkpoint, problem, config, score);
        NeuroEvolutionChampionCheckpoint.LoadedChampion loaded =
                NeuroEvolutionChampionCheckpoint.load(checkpoint, catalog);
        Files.deleteIfExists(checkpoint);

        assertEquals(problem.key(), loaded.problem().key());
        assertEquals(config, loaded.config());
        assertEquals(score.genome(), loaded.score().genome());
        assertEquals(score.score(), loaded.score().score(), 1.0e-12);
        assertEquals(score.generation(), loaded.score().generation());
    }

    @Test
    void genericLearnerSupportsVectorOutputReconstructionProblems() {
        NeuroEvolutionProblem problem = new IdentityAutoencoderProblem(5);
        EvolvableXorGenome genome = EvolvableXorGenome.random(new Random(31L));
        RichEvolvedProblemLearner learner = new RichEvolvedProblemLearner(problem, genome, new Random(32L));

        double mse = learner.train(20, 0.03);
        double[] prediction = learner.predictVector(new double[] {0.25, 0.75});
        long outputNodes = learner.snapshot().nodes().stream()
                .filter(node -> node.layer() == RichEvolvedProblemLearner.VisualNodeLayer.OUTPUT)
                .count();

        assertEquals(2, problem.outputDimensions());
        assertEquals(2, prediction.length);
        assertEquals(2, outputNodes);
        assertTrue(Double.isFinite(mse));
        assertTrue(Double.isFinite(learner.meanSquaredError(problem.generalizationSamples())));
    }

    @Test
    void customProblemsCanBeAddedWithoutStaticFactoriesOrRegistries() {
        NeuroEvolutionProblemCatalog customCatalog =
                new DefaultNeuroEvolutionProblemCatalog(List.of(DiagonalProblem::new));

        NeuroEvolutionProblem problem = customCatalog.find("diagonal-test", 5);

        assertEquals("Diagonal test", problem.name());
        assertTrue(customCatalog.problemKeys().contains("diagonal-test"));
        assertEquals(1.0, problem.target(new double[] {0.8, 0.2}));
        assertEquals(0.0, problem.target(new double[] {0.2, 0.8}));
    }

    @Test
    void fuzzyXorTargetMatchesBooleanCorners() {
        assertEquals(0.0, XorGeneralizationEvaluator.fuzzyXorTarget(0.0, 0.0));
        assertEquals(1.0, XorGeneralizationEvaluator.fuzzyXorTarget(0.0, 1.0));
        assertEquals(1.0, XorGeneralizationEvaluator.fuzzyXorTarget(1.0, 0.0));
        assertEquals(0.0, XorGeneralizationEvaluator.fuzzyXorTarget(1.0, 1.0));
        assertEquals(0.5, XorGeneralizationEvaluator.fuzzyXorTarget(0.5, 0.5));
    }

    @Test
    void evolvesRicherLearnersForXor() {
        XorNeuroEvolution.EvolutionConfig config =
                new XorNeuroEvolution.EvolutionConfig(
                        8,
                        3,
                        60,
                        0.02,
                        1,
                        0.08,
                        0.001,
                        2,
                        23L);

        XorNeuroEvolution.EvolutionResult result = XorNeuroEvolution.evolve(config);

        assertEquals(3, result.champions().size());
        assertNotNull(result.best().genome());
        assertTrue(Double.isFinite(result.best().score()));
        assertTrue(result.best().accuracy() >= 0.0);
        assertTrue(Double.isFinite(result.best().generalizationMeanSquaredError()));
        assertTrue(Double.isFinite(result.best().jitterMeanSquaredError()));
        assertTrue(Double.isFinite(result.best().smoothnessPenalty()));
    }

    @Test
    void evolvesAgainstGenericProblemDefinition() {
        XorNeuroEvolution.EvolutionConfig config =
                new XorNeuroEvolution.EvolutionConfig(
                        8,
                        2,
                        40,
                        0.03,
                        1,
                        0.08,
                        0.001,
                        2,
                        29L);

        XorNeuroEvolution.EvolutionResult result = NeuroEvolution.evolve(catalog.find("and", 5), config);

        assertEquals("AND", result.problem().name());
        assertEquals(2, result.champions().size());
        assertNotNull(result.best().genome());
        assertTrue(Double.isFinite(result.best().score()));
        assertTrue(Double.isFinite(result.best().generalizationMeanSquaredError()));
    }

    private static final class DiagonalProblem extends GeometricClassificationProblem {

        DiagonalProblem(final int gridSize) {
            super(
                    "diagonal-test",
                    "Diagonal test",
                    "Diagonal test is a small custom classification problem used to verify that callers can add "
                            + "new problems through the NeuroEvolutionProblemCatalog interface.",
                    List.of("diagonal"),
                    gridSize,
                    DiagonalProblem::targetValue);
        }

        private static double targetValue(final double[] input) {
            return input[0] > input[1] ? 1.0 : 0.0;
        }
    }

    private static boolean different(final double[] first, final double[] second) {
        for (int i = 0; i < first.length; i++) {
            if (Math.abs(first[i] - second[i]) > 1.0e-9) {
                return true;
            }
        }
        return false;
    }
}
