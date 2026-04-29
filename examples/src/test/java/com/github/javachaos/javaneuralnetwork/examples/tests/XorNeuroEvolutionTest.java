package com.github.javachaos.javaneuralnetwork.examples.tests;

import com.github.javachaos.javaneuralnetwork.examples.CheckerboardProblem;
import com.github.javachaos.javaneuralnetwork.examples.CircleProblem;
import com.github.javachaos.javaneuralnetwork.examples.DefaultNeuroEvolutionProblemCatalog;
import com.github.javachaos.javaneuralnetwork.examples.EvolvableXorGenome;
import com.github.javachaos.javaneuralnetwork.examples.GeometricClassificationProblem;
import com.github.javachaos.javaneuralnetwork.examples.IdentityAutoencoderProblem;
import com.github.javachaos.javaneuralnetwork.examples.LocalNeuroEvolutionWorker;
import com.github.javachaos.javaneuralnetwork.examples.MettleTestProblem;
import com.github.javachaos.javaneuralnetwork.examples.NeuroEvolution;
import com.github.javachaos.javaneuralnetwork.examples.NeuroEvolutionArtifactRef;
import com.github.javachaos.javaneuralnetwork.examples.NeuroEvolutionChampionCheckpoint;
import com.github.javachaos.javaneuralnetwork.examples.NeuroEvolutionCheckpointEvent;
import com.github.javachaos.javaneuralnetwork.examples.NeuroEvolutionDistributedConfig;
import com.github.javachaos.javaneuralnetwork.examples.NeuroEvolutionFreeEnergyMetrics;
import com.github.javachaos.javaneuralnetwork.examples.NeuroEvolutionGeneralizationEvaluator;
import com.github.javachaos.javaneuralnetwork.examples.NeuroEvolutionProblem;
import com.github.javachaos.javaneuralnetwork.examples.NeuroEvolutionProblemCatalog;
import com.github.javachaos.javaneuralnetwork.examples.NeuroEvolutionRunCompletion;
import com.github.javachaos.javaneuralnetwork.examples.NeuroEvolutionRunHandle;
import com.github.javachaos.javaneuralnetwork.examples.NeuroEvolutionRunListener;
import com.github.javachaos.javaneuralnetwork.examples.NeuroEvolutionRunProgress;
import com.github.javachaos.javaneuralnetwork.examples.NeuroEvolutionRunRequest;
import com.github.javachaos.javaneuralnetwork.examples.NeuroEvolutionWorker;
import com.github.javachaos.javaneuralnetwork.examples.RingProblem;
import com.github.javachaos.javaneuralnetwork.examples.RichEvolvedXorLearner;
import com.github.javachaos.javaneuralnetwork.examples.RichEvolvedProblemLearner;
import com.github.javachaos.javaneuralnetwork.examples.RemoteRustNeuroEvolutionWorker;
import com.github.javachaos.javaneuralnetwork.examples.RealTransformerBlockProblem;
import com.github.javachaos.javaneuralnetwork.examples.RustNeuroEvolutionSidecar;
import com.github.javachaos.javaneuralnetwork.examples.RustNeuroEvolutionWorker;
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
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

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
    void genomesCanExploreWiderHiddenLayers() {
        EvolvableXorGenome genome = EvolvableXorGenome.random(new Random(13L));
        for (int i = 0; i < 20; i++) {
            genome = genome.mutate(new Random(300L + i), XorMutationType.ADD_NEURON, 0.1);
        }

        assertEquals(12, genome.hiddenNeurons());
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
        assertEquals("Real transformer block", catalog.find("real-transformer", 5).name());
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
        assertTrue(problem.freeEnergyProfile().enabled());
        assertTrue(problem.freeEnergyProfile().latentPredictionWeight() > 0.0);
        assertTrue(problem.outputGroups().get(4).objectiveWeight()
                > problem.outputGroups().get(1).objectiveWeight());
        assertTrue(problem.generalizationSamples().size() > 20);
        assertTrue(different(redCubeTarget, blueSphereTarget));
        for (double target : redCubeTarget) {
            assertTrue(target >= 0.0 && target <= 1.0);
        }
    }

    @Test
    void realTransformerBlockProblemProducesFullBlockTargets() {
        RealTransformerBlockProblem problem = new RealTransformerBlockProblem(7);

        double[] redCubeTarget = problem.targetVector(problem.encodeTokenSequence(0, 1, 3, 5));
        double[] blueSphereTarget = problem.targetVector(problem.encodeTokenSequence(0, 2, 4, 6));

        assertEquals(4, problem.inputDimensions());
        assertEquals(160, problem.outputDimensions());
        assertEquals("Y.P0.D0", problem.outputLabel(0));
        assertEquals("LN1.P0.D0", problem.outputLabel(16));
        assertEquals("LN2.P0.D0", problem.outputLabel(32));
        assertEquals("Q0.P0.D0", problem.outputLabel(48));
        assertEquals("A1.Q3.K3", problem.outputLabel(127));
        assertEquals("MLP.P3.H7", problem.outputLabel(159));
        assertEquals("RED", problem.tokenLabel(3));
        assertEquals(9, problem.outputGroups().size());
        assertEquals("Final residual stream", problem.outputGroups().get(0).name());
        assertEquals("MLP GELU hidden", problem.outputGroups().get(8).name());
        assertTrue(problem.freeEnergyProfile().enabled());
        assertTrue(problem.outputGroups().get(6).objectiveWeight()
                > problem.outputGroups().get(1).objectiveWeight());
        assertTrue(problem.generalizationSamples().size() > 60);
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
        NeuroEvolutionFreeEnergyMetrics freeEnergy = learner.predictiveFreeEnergy(
                problem.generalizationSamples(),
                genome.complexityCost(problem) / problem.complexityScale());
        assertTrue(Double.isFinite(freeEnergy.value()));
        assertTrue(freeEnergy.value() > 0.0);
        assertTrue(freeEnergy.latentPredictionEnergy() >= 0.0);
        XorNeuroEvolution.EvolutionConfig config = new XorNeuroEvolution.EvolutionConfig(
                8,
                1,
                4,
                0.04,
                1,
                0.08,
                0.001,
                2,
                73L);
        XorNeuroEvolution.CandidateScore score = XorNeuroEvolution.evaluate(problem, genome, config, 0);
        assertTrue(score.predictiveFreeEnergy() > 0.0);
        assertTrue(score.sensoryPredictionEnergy() > 0.0);
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
        assertEquals(score.predictiveFreeEnergy(), loaded.score().predictiveFreeEnergy(), 1.0e-12);
        assertEquals(score.generation(), loaded.score().generation());
    }

    @Test
    void localWorkerEmitsProgressAndCheckpointsThroughWorkerBoundary() throws Exception {
        NeuroEvolutionProblem problem = catalog.find("and", 5);
        XorNeuroEvolution.EvolutionConfig config =
                new XorNeuroEvolution.EvolutionConfig(
                        4,
                        2,
                        6,
                        0.05,
                        1,
                        0.06,
                        0.001,
                        1,
                        1,
                        41L);
        Path checkpoint = Files.createTempFile("local-neuro-evolution-worker", ".properties");
        CountDownLatch progressLatch = new CountDownLatch(1);
        CountDownLatch checkpointLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(1);
        AtomicReference<NeuroEvolutionRunProgress> progress = new AtomicReference<>();
        AtomicReference<NeuroEvolutionCheckpointEvent> checkpointEvent = new AtomicReference<>();
        AtomicReference<NeuroEvolutionRunCompletion> completion = new AtomicReference<>();

        try (NeuroEvolutionWorker worker = new LocalNeuroEvolutionWorker()) {
            NeuroEvolutionRunHandle handle = worker.start(
                    new NeuroEvolutionRunRequest(problem, config, true, checkpoint, 0L),
                    new NeuroEvolutionRunListener() {
                        @Override
                        public void onProgress(final NeuroEvolutionRunProgress update) {
                            progress.compareAndSet(null, update);
                            progressLatch.countDown();
                        }

                        @Override
                        public void onCheckpoint(final NeuroEvolutionCheckpointEvent event) {
                            checkpointEvent.set(event);
                            checkpointLatch.countDown();
                        }

                        @Override
                        public void onComplete(final NeuroEvolutionRunCompletion result) {
                            completion.set(result);
                            completionLatch.countDown();
                        }
                    });

            try {
                assertTrue(progressLatch.await(20, TimeUnit.SECONDS));
                assertTrue(checkpointLatch.await(20, TimeUnit.SECONDS));
                assertTrue(completionLatch.await(20, TimeUnit.SECONDS));
                assertTrue(handle.isStopped());
                assertEquals(problem.key(), progress.get().problem().key());
                assertEquals(problem.key(), completion.get().problem().key());
                assertTrue(Double.isFinite(progress.get().best().score()));
                assertTrue(checkpointEvent.get().saved());
                assertTrue(Files.exists(checkpoint));
                assertEquals(problem.key(), NeuroEvolutionChampionCheckpoint.load(checkpoint, catalog).problem().key());
            } finally {
                handle.stop();
                Files.deleteIfExists(checkpoint);
            }
        }
    }

    @Test
    void rustSidecarSpeaksJsonlProtocolWhenBuilt() throws Exception {
        Path repositoryRoot = repositoryRoot();
        Path workerBinary = RustNeuroEvolutionSidecar.defaultBinaryPath(repositoryRoot);
        Assumptions.assumeTrue(
                Files.isExecutable(workerBinary),
                () -> "Build rust-worker first with cargo build --release --manifest-path "
                        + repositoryRoot.resolve("rust-worker").resolve("Cargo.toml"));

        CountDownLatch readyLatch = new CountDownLatch(1);
        CountDownLatch acceptedLatch = new CountDownLatch(1);
        CountDownLatch pausedLatch = new CountDownLatch(1);
        CountDownLatch resumedLatch = new CountDownLatch(1);
        CountDownLatch stoppedLatch = new CountDownLatch(1);
        List<RustNeuroEvolutionSidecar.RustWorkerEvent> events = new CopyOnWriteArrayList<>();

        try (RustNeuroEvolutionSidecar sidecar = new RustNeuroEvolutionSidecar(
                RustNeuroEvolutionSidecar.defaultCommand(repositoryRoot),
                event -> {
                    events.add(event);
                    if ("ready".equals(event.type())) {
                        readyLatch.countDown();
                    } else if ("accepted".equals(event.type())) {
                        acceptedLatch.countDown();
                    } else if ("paused".equals(event.type())) {
                        pausedLatch.countDown();
                    } else if ("resumed".equals(event.type())) {
                        resumedLatch.countDown();
                    } else if ("stopped".equals(event.type())) {
                        stoppedLatch.countDown();
                    }
                })) {
            NeuroEvolutionProblem problem = catalog.find("transformer-block", 5);
            XorNeuroEvolution.EvolutionConfig config = new XorNeuroEvolution.EvolutionConfig(
                    4,
                    3,
                    5,
                    0.05,
                    1,
                    0.05,
                    0.001,
                    1,
                    1,
                    83L);

            sidecar.start();
            assertTrue(readyLatch.await(5, TimeUnit.SECONDS));
            sidecar.submit(new NeuroEvolutionRunRequest(problem, config));
            assertTrue(acceptedLatch.await(5, TimeUnit.SECONDS));
            sidecar.pause();
            assertTrue(pausedLatch.await(5, TimeUnit.SECONDS));
            sidecar.resume();
            assertTrue(resumedLatch.await(5, TimeUnit.SECONDS));
            sidecar.stop();
            assertTrue(stoppedLatch.await(5, TimeUnit.SECONDS));
        }

        assertTrue(events.stream().anyMatch(event -> event.protocol() == RustNeuroEvolutionSidecar.PROTOCOL_VERSION));
        assertTrue(events.stream().anyMatch(event -> event.message() != null
                && event.message().contains("Rust compute kernel is active")));
    }

    @Test
    void rustSidecarCarriesDistributedStorageEnvelopeWhenBuilt() throws Exception {
        Path repositoryRoot = repositoryRoot();
        Path workerBinary = RustNeuroEvolutionSidecar.defaultBinaryPath(repositoryRoot);
        Assumptions.assumeTrue(
                Files.isExecutable(workerBinary),
                () -> "Build rust-worker first with cargo build --release --manifest-path "
                        + repositoryRoot.resolve("rust-worker").resolve("Cargo.toml"));

        NeuroEvolutionDistributedConfig distributedConfig = NeuroEvolutionDistributedConfig.dht(
                "test-cluster",
                "java-test",
                "worker-a",
                3,
                "champions");
        CountDownLatch acceptedLatch = new CountDownLatch(1);
        AtomicReference<RustNeuroEvolutionSidecar.RustWorkerEvent> accepted = new AtomicReference<>();

        try (RustNeuroEvolutionSidecar sidecar = new RustNeuroEvolutionSidecar(
                RustNeuroEvolutionSidecar.defaultCommand(repositoryRoot),
                distributedConfig,
                event -> {
                    if ("accepted".equals(event.type())) {
                        accepted.set(event);
                        acceptedLatch.countDown();
                    }
                })) {
            NeuroEvolutionProblem problem = catalog.find("and", 5);
            XorNeuroEvolution.EvolutionConfig config = new XorNeuroEvolution.EvolutionConfig(
                    4,
                    1,
                    5,
                    0.05,
                    1,
                    0.05,
                    0.001,
                    1,
                    1,
                    89L);

            sidecar.start();
            sidecar.submit(new NeuroEvolutionRunRequest(problem, config));
            assertTrue(acceptedLatch.await(5, TimeUnit.SECONDS));
            sidecar.stop();
        }

        assertEquals("test-cluster", accepted.get().clusterId());
        assertEquals("CONTENT_ADDRESSED_DHT", accepted.get().storageMode());
        assertTrue(accepted.get().message().contains("CONTENT_ADDRESSED_DHT"));
    }

    @Test
    void distributedArtifactRefsAreContentAddressable() {
        NeuroEvolutionArtifactRef ref = NeuroEvolutionArtifactRef.sha256(
                "champions",
                "abc".getBytes(StandardCharsets.UTF_8),
                "application/octet-stream");

        assertEquals(
                "neuro-dht://champions/sha-256/"
                        + "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
                ref.uri());
        assertEquals(3L, ref.sizeBytes());
        assertEquals("application/octet-stream", ref.mediaType());
    }

    @Test
    void rustWorkerStartsSidecarAndCompletesLocalRunWhenBuilt() throws Exception {
        Path repositoryRoot = repositoryRoot();
        Path workerBinary = RustNeuroEvolutionSidecar.defaultBinaryPath(repositoryRoot);
        Assumptions.assumeTrue(
                Files.isExecutable(workerBinary),
                () -> "Build rust-worker first with cargo build --release --manifest-path "
                        + repositoryRoot.resolve("rust-worker").resolve("Cargo.toml"));

        NeuroEvolutionProblem problem = catalog.find("and", 5);
        XorNeuroEvolution.EvolutionConfig config = new XorNeuroEvolution.EvolutionConfig(
                4,
                2,
                6,
                0.05,
                1,
                0.06,
                0.001,
                1,
                2,
                47L);
        Path checkpoint = Files.createTempFile("rust-neuro-evolution-worker", ".properties");
        CountDownLatch acceptedLatch = new CountDownLatch(1);
        CountDownLatch stoppedLatch = new CountDownLatch(1);
        CountDownLatch progressLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(1);
        AtomicReference<NeuroEvolutionRunProgress> progress = new AtomicReference<>();
        AtomicReference<NeuroEvolutionRunCompletion> completion = new AtomicReference<>();
        List<RustNeuroEvolutionSidecar.RustWorkerEvent> events = new CopyOnWriteArrayList<>();

        NeuroEvolutionWorker worker = new RustNeuroEvolutionWorker(
                RustNeuroEvolutionSidecar.defaultCommand(repositoryRoot),
                event -> {
                    events.add(event);
                    if ("accepted".equals(event.type())) {
                        acceptedLatch.countDown();
                    } else if ("stopped".equals(event.type())) {
                        stoppedLatch.countDown();
                    }
                });
        NeuroEvolutionRunHandle handle = worker.start(
                new NeuroEvolutionRunRequest(problem, config, true, checkpoint, 0L),
                new NeuroEvolutionRunListener() {
                    @Override
                    public void onProgress(final NeuroEvolutionRunProgress update) {
                        progress.compareAndSet(null, update);
                        progressLatch.countDown();
                    }

                    @Override
                    public void onComplete(final NeuroEvolutionRunCompletion result) {
                        completion.set(result);
                        completionLatch.countDown();
                    }
                });

        try {
            assertTrue(acceptedLatch.await(5, TimeUnit.SECONDS));
            assertTrue(progressLatch.await(20, TimeUnit.SECONDS));
            assertTrue(completionLatch.await(20, TimeUnit.SECONDS));
            assertTrue(stoppedLatch.await(5, TimeUnit.SECONDS));
            assertTrue(handle.isStopped());
            assertEquals(problem.key(), progress.get().problem().key());
            assertEquals(problem.key(), completion.get().problem().key());
            assertTrue(progress.get().generation() >= progress.get().best().generation());
            assertTrue(completion.get().best().generation() < config.generations());
            assertTrue(completion.get().best().jitterMeanSquaredError() > 0.0);
            assertTrue(completion.get().best().smoothnessPenalty() > 0.0);
            assertTrue(events.stream()
                    .filter(event -> "progress".equals(event.type()))
                    .anyMatch(event -> event.intValue("scoreGeneration", -1) >= 0));
            assertTrue(events.stream()
                    .filter(event -> "progress".equals(event.type()))
                    .anyMatch(event -> event.intValue("staleGenerations", -1) >= 0
                            && event.doubleValue("reseedFraction", -1.0) >= 0.0
                            && event.intValue("seedLanes", -1) >= 2
                            && event.intValue("laneDeaths", -1) >= 0
                            && event.intValue("maxLaneStale", -1) >= 0));
            assertTrue(progress.get().strategySummary().contains("reseed"));
        } finally {
            handle.stop();
            worker.close();
            Files.deleteIfExists(checkpoint);
        }
    }

    @Test
    void rustGrpcWorkerCompletesRemoteRunWhenBuilt() throws Exception {
        Path repositoryRoot = repositoryRoot();
        Path workerBinary = RustNeuroEvolutionSidecar.defaultBinaryPath(repositoryRoot);
        Assumptions.assumeTrue(
                Files.isExecutable(workerBinary),
                () -> "Build rust-worker first with cargo build --release --manifest-path "
                        + repositoryRoot.resolve("rust-worker").resolve("Cargo.toml")
                        + " --features grpc");

        int port;
        try {
            port = openLoopbackPort();
        } catch (IOException exception) {
            Assumptions.assumeTrue(
                    false,
                    () -> "Loopback binding is unavailable in this sandbox: " + exception.getMessage());
            return;
        }
        Process process = new ProcessBuilder(
                workerBinary.toString(),
                "--grpc",
                "127.0.0.1:" + port)
                .redirectErrorStream(true)
                .start();
        try {
            Assumptions.assumeTrue(
                    waitForPort(port, process, 5_000L),
                    "Build rust-worker with --features grpc to run the gRPC transport test.");

            NeuroEvolutionProblem problem = catalog.find("and", 5);
            XorNeuroEvolution.EvolutionConfig config = new XorNeuroEvolution.EvolutionConfig(
                    4,
                    2,
                    6,
                    0.05,
                    1,
                    0.06,
                    0.001,
                    1,
                    1,
                    97L);
            CountDownLatch acceptedLatch = new CountDownLatch(1);
            CountDownLatch completionLatch = new CountDownLatch(1);
            AtomicReference<NeuroEvolutionRunCompletion> completion = new AtomicReference<>();

            try (NeuroEvolutionWorker worker = new RemoteRustNeuroEvolutionWorker(
                    "127.0.0.1",
                    port,
                    NeuroEvolutionDistributedConfig.local(),
                    event -> {
                        if ("accepted".equals(event.type())) {
                            acceptedLatch.countDown();
                        }
                    })) {
                NeuroEvolutionRunHandle handle = worker.start(
                        new NeuroEvolutionRunRequest(problem, config),
                        new NeuroEvolutionRunListener() {
                            @Override
                            public void onComplete(final NeuroEvolutionRunCompletion result) {
                                completion.set(result);
                                completionLatch.countDown();
                            }
                        });
                try {
                    assertTrue(acceptedLatch.await(5, TimeUnit.SECONDS));
                    assertTrue(completionLatch.await(20, TimeUnit.SECONDS));
                    assertEquals(problem.key(), completion.get().problem().key());
                } finally {
                    handle.stop();
                }
            }
        } finally {
            process.destroy();
            if (!process.waitFor(2, TimeUnit.SECONDS)) {
                process.destroyForcibly();
            }
        }
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

    private static int openLoopbackPort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private static boolean waitForPort(
            final int port,
            final Process process,
            final long timeoutMillis) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMillis);
        while (System.nanoTime() < deadline) {
            if (!process.isAlive()) {
                return false;
            }
            try (Socket ignored = new Socket("127.0.0.1", port)) {
                return true;
            } catch (IOException ignored) {
                Thread.sleep(50L);
            }
        }
        return false;
    }

    private static Path repositoryRoot() {
        Path current = Path.of("").toAbsolutePath().normalize();
        if (Files.exists(current.resolve("rust-worker").resolve("Cargo.toml"))) {
            return current;
        }
        Path parent = current.getParent();
        if (parent != null && Files.exists(parent.resolve("rust-worker").resolve("Cargo.toml"))) {
            return parent;
        }
        return current;
    }
}
