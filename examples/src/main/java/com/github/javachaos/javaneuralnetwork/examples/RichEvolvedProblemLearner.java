package com.github.javachaos.javaneuralnetwork.examples;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Executes an {@link EvolvableXorGenome} against any
 * {@link NeuroEvolutionProblem}.
 */
public final class RichEvolvedProblemLearner {

    private static final double MAX_WEIGHT = 12.0;

    private final NeuroEvolutionProblem problem;
    private final EvolvableXorGenome genome;
    private final double[][][] hiddenWeights;
    private final double[][][] previousHiddenDeltas;
    private final boolean[][][] hiddenMask;
    private final double[][] recurrentWeights;
    private final double[][] previousRecurrentDeltas;
    private final boolean[][] recurrentMask;
    private final double[][] outputWeights;
    private final double[][] previousOutputDeltas;
    private final boolean[][] outputMask;
    private final double[] memoryState;
    private final double[] previousHiddenState;

    private final double[] previousErrorSignal;

    public record GroupedMeanSquaredError(
            double meanSquaredError,
            double baselineRelativeMeanSquaredError) {

        public GroupedMeanSquaredError {
            requireNonNegativeScore(meanSquaredError, "Mean squared error");
            requireNonNegativeScore(baselineRelativeMeanSquaredError, "Baseline-relative mean squared error");
        }
    }

    public RichEvolvedProblemLearner(
            final NeuroEvolutionProblem problem,
            final EvolvableXorGenome genome,
            final Random random) {
        this.problem = Objects.requireNonNull(problem, "Problem cannot be null.");
        this.genome = Objects.requireNonNull(genome, "Genome cannot be null.");
        Objects.requireNonNull(random, "Random cannot be null.");
        this.hiddenWeights = new double[genome.hiddenLayers()][][];
        this.previousHiddenDeltas = new double[genome.hiddenLayers()][][];
        this.hiddenMask = new boolean[genome.hiddenLayers()][][];
        for (int layer = 0; layer < genome.hiddenLayers(); layer++) {
            int inputWidth = layer == 0 ? genome.inputSize(problem) : genome.hiddenNeurons() + 1;
            this.hiddenWeights[layer] = new double[genome.hiddenNeurons()][inputWidth];
            this.previousHiddenDeltas[layer] = new double[genome.hiddenNeurons()][inputWidth];
            this.hiddenMask[layer] = new boolean[genome.hiddenNeurons()][inputWidth];
        }
        this.recurrentWeights = new double[genome.hiddenNeurons()][genome.hiddenNeurons()];
        this.previousRecurrentDeltas = new double[genome.hiddenNeurons()][genome.hiddenNeurons()];
        this.recurrentMask = new boolean[genome.hiddenNeurons()][genome.hiddenNeurons()];
        this.outputWeights = new double[problem.outputDimensions()][genome.outputInputSize()];
        this.previousOutputDeltas = new double[problem.outputDimensions()][genome.outputInputSize()];
        this.outputMask = new boolean[problem.outputDimensions()][genome.outputInputSize()];
        this.memoryState = new double[genome.memoryCells()];
        this.previousHiddenState = new double[genome.hiddenNeurons()];
        this.previousErrorSignal = new double[problem.outputDimensions()];
        initialize(random);
    }

    public double train(final int maxEpochs, final double targetMeanSquaredError) {
        if (maxEpochs <= 0) {
            throw new IllegalArgumentException("Max epochs must be positive.");
        }
        if (!Double.isFinite(targetMeanSquaredError) || targetMeanSquaredError <= 0.0) {
            throw new IllegalArgumentException("Target mean squared error must be positive and finite.");
        }

        double meanSquaredError = evaluateMeanSquaredError();
        for (int epoch = 0; epoch < maxEpochs && meanSquaredError > targetMeanSquaredError; epoch++) {
            resetState();
            double scheduleScale = genome.learningSchedule().scale(epoch, maxEpochs);
            for (NeuroEvolutionSample sample : problem.trainingSamples()) {
                prepareSampleState();
                trainSample(sample.input(), sample.targets(), scheduleScale);
                if (!weightsAreFinite()) {
                    return Double.POSITIVE_INFINITY;
                }
            }
            meanSquaredError = evaluateMeanSquaredError();
            if (!Double.isFinite(meanSquaredError)) {
                return Double.POSITIVE_INFINITY;
            }
        }
        return meanSquaredError;
    }

    public double predict(final double x, final double y) {
        return predict(new double[] {x, y});
    }

    public double predict(final double[] input) {
        requireScalarOutput();
        return predictVector(input)[0];
    }

    public double[] predictVector(final double[] input) {
        problem.requireInput(input);
        resetState();
        return forward(input).outputs();
    }

    public double evaluateMeanSquaredError() {
        return meanSquaredError(problem.trainingSamples());
    }

    public double configuredLoss() {
        resetState();
        double total = 0.0;
        int count = 0;
        for (NeuroEvolutionSample sample : problem.trainingSamples()) {
            prepareSampleState();
            ForwardPass pass = forward(sample.input());
            if (!arrayIsFinite(pass.outputs())) {
                return Double.POSITIVE_INFINITY;
            }
            double[] targets = sample.targets();
            for (int output = 0; output < targets.length; output++) {
                total += genome.lossFunction().loss(targets[output], pass.outputs()[output]);
                count++;
            }
            finishSampleState(pass);
        }
        return count == 0 ? 0.0 : total / count;
    }

    public double accuracy() {
        if (!problem.classification()) {
            return 1.0;
        }
        resetState();
        int correct = 0;
        for (NeuroEvolutionSample sample : problem.trainingSamples()) {
            prepareSampleState();
            ForwardPass pass = forward(sample.input());
            if (arrayIsFinite(pass.outputs()) && problem.predictionMatches(sample.targets(), pass.outputs())) {
                correct++;
            }
            finishSampleState(pass);
        }
        return correct / (double) problem.trainingSamples().size();
    }

    public double meanSquaredError(final List<NeuroEvolutionSample> samples) {
        Objects.requireNonNull(samples, "Samples cannot be null.");
        if (samples.isEmpty()) {
            return 0.0;
        }
        resetState();
        double total = 0.0;
        int count = 0;
        for (NeuroEvolutionSample sample : samples) {
            prepareSampleState();
            ForwardPass pass = forward(sample.input());
            if (!arrayIsFinite(pass.outputs())) {
                return Double.POSITIVE_INFINITY;
            }
            double[] targets = sample.targets();
            for (int output = 0; output < targets.length; output++) {
                double error = targets[output] - pass.outputs()[output];
                total += error * error;
                count++;
            }
            finishSampleState(pass);
        }
        return count == 0 ? 0.0 : total / count;
    }

    public GroupedMeanSquaredError groupedMeanSquaredError(final List<NeuroEvolutionSample> samples) {
        Objects.requireNonNull(samples, "Samples cannot be null.");
        if (samples.isEmpty()) {
            return new GroupedMeanSquaredError(0.0, 0.0);
        }
        List<NeuroEvolutionOutputGroup> groups = validOutputGroups();
        double[] trainingMeans = problem.trainingTargetMeans();
        double[] groupTotals = new double[groups.size()];
        double[] baselineTotals = new double[groups.size()];
        int[] groupCounts = new int[groups.size()];
        double total = 0.0;
        int count = 0;

        resetState();
        for (NeuroEvolutionSample sample : samples) {
            prepareSampleState();
            ForwardPass pass = forward(sample.input());
            if (!arrayIsFinite(pass.outputs())) {
                return new GroupedMeanSquaredError(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
            }
            double[] targets = sample.targets();
            for (int groupIndex = 0; groupIndex < groups.size(); groupIndex++) {
                NeuroEvolutionOutputGroup group = groups.get(groupIndex);
                for (int output = group.startInclusive(); output < group.endExclusive(); output++) {
                    double error = targets[output] - pass.outputs()[output];
                    double baselineError = targets[output] - trainingMeans[output];
                    double squaredError = error * error;
                    groupTotals[groupIndex] += squaredError;
                    baselineTotals[groupIndex] += baselineError * baselineError;
                    groupCounts[groupIndex]++;
                    total += squaredError;
                    count++;
                }
            }
            finishSampleState(pass);
        }
        return new GroupedMeanSquaredError(
                count == 0 ? 0.0 : total / count,
                weightedBaselineRelativeMeanSquaredError(groups, groupTotals, baselineTotals, groupCounts));
    }

    public NeuroEvolutionFreeEnergyMetrics predictiveFreeEnergy(
            final List<NeuroEvolutionSample> samples,
            final double normalizedComplexity) {
        Objects.requireNonNull(samples, "Samples cannot be null.");
        if (!Double.isFinite(normalizedComplexity) || normalizedComplexity < 0.0) {
            throw new IllegalArgumentException("Normalized complexity must be finite and non-negative.");
        }
        NeuroEvolutionFreeEnergyProfile profile = problem.freeEnergyProfile();
        if (!profile.enabled() || samples.isEmpty()) {
            return NeuroEvolutionFreeEnergyMetrics.disabled();
        }

        List<NeuroEvolutionOutputGroup> groups = validOutputGroups();
        double[] trainingMeans = problem.trainingTargetMeans();
        double[] groupTotals = new double[groups.size()];
        double[] baselineTotals = new double[groups.size()];
        int[] groupCounts = new int[groups.size()];
        double latentTotal = 0.0;
        int latentCount = 0;

        resetState();
        for (NeuroEvolutionSample sample : samples) {
            prepareSampleState();
            ForwardPass pass = forward(sample.input());
            if (!arrayIsFinite(pass.outputs())) {
                return NeuroEvolutionFreeEnergyMetrics.from(
                        profile,
                        Double.POSITIVE_INFINITY,
                        Double.POSITIVE_INFINITY,
                        normalizedComplexity);
            }
            double[] targets = sample.targets();
            for (int groupIndex = 0; groupIndex < groups.size(); groupIndex++) {
                NeuroEvolutionOutputGroup group = groups.get(groupIndex);
                for (int output = group.startInclusive(); output < group.endExclusive(); output++) {
                    double error = targets[output] - pass.outputs()[output];
                    double baselineError = targets[output] - trainingMeans[output];
                    groupTotals[groupIndex] += error * error;
                    baselineTotals[groupIndex] += baselineError * baselineError;
                    groupCounts[groupIndex]++;
                }
            }
            for (int layer = 0; layer < hiddenWeights.length; layer++) {
                double layerEnergy = latentPredictionEnergy(pass, layer);
                if (!Double.isFinite(layerEnergy)) {
                    return NeuroEvolutionFreeEnergyMetrics.from(
                            profile,
                            weightedBaselineRelativeMeanSquaredError(
                                    groups,
                                    groupTotals,
                                    baselineTotals,
                                    groupCounts),
                            Double.POSITIVE_INFINITY,
                            normalizedComplexity);
                }
                latentTotal += layerEnergy;
                latentCount++;
            }
            finishSampleState(pass);
        }

        double sensoryEnergy = weightedBaselineRelativeMeanSquaredError(
                groups,
                groupTotals,
                baselineTotals,
                groupCounts);
        double latentEnergy = latentCount == 0 ? 0.0 : latentTotal / latentCount;
        return NeuroEvolutionFreeEnergyMetrics.from(
                profile,
                sensoryEnergy,
                latentEnergy,
                normalizedComplexity);
    }

    public List<NeuroEvolutionOutputGroupScore> outputGroupScores() {
        List<NeuroEvolutionOutputGroup> groups = validOutputGroups();
        double[] trainingMeans = targetMeans(problem.trainingSamples());
        List<NeuroEvolutionOutputGroupScore> scores = new ArrayList<>(groups.size());
        for (NeuroEvolutionOutputGroup group : groups) {
            double trainingMse = groupMeanSquaredError(problem.trainingSamples(), group);
            double generalizationMse = groupMeanSquaredError(problem.generalizationSamples(), group);
            double baselineTrainingMse = baselineGroupMeanSquaredError(problem.trainingSamples(), group, trainingMeans);
            double baselineGeneralizationMse =
                    baselineGroupMeanSquaredError(problem.generalizationSamples(), group, trainingMeans);
            scores.add(new NeuroEvolutionOutputGroupScore(
                    group,
                    trainingMse,
                    generalizationMse,
                    baselineTrainingMse,
                    baselineGeneralizationMse,
                    improvementOverBaseline(generalizationMse, baselineGeneralizationMse)));
        }
        return List.copyOf(scores);
    }

    public NetworkSnapshot snapshot() {
        List<VisualNode> nodes = new ArrayList<>();
        List<VisualLink> links = new ArrayList<>();
        int featureCount = genome.inputFeatureSize(problem);
        for (int i = 0; i < featureCount; i++) {
            nodes.add(new VisualNode(featureNodeId(i), i == 0 ? "bias" : "F" + i, VisualNodeLayer.INPUT, i));
        }
        for (int i = 0; i < genome.memoryCells(); i++) {
            nodes.add(new VisualNode(memoryNodeId(i), "M" + i, VisualNodeLayer.MEMORY, i));
        }
        for (int layer = 1; layer < hiddenWeights.length; layer++) {
            nodes.add(new VisualNode(hiddenBiasNodeId(layer), "bias", VisualNodeLayer.BIAS, layer, layer));
        }
        nodes.add(new VisualNode(outputBiasNodeId(), "bias", VisualNodeLayer.BIAS, hiddenWeights.length, hiddenWeights.length));
        for (int layer = 0; layer < hiddenWeights.length; layer++) {
            for (int i = 0; i < hiddenWeights[layer].length; i++) {
                nodes.add(new VisualNode(
                        hiddenNodeId(layer, i),
                        "H" + (layer + 1) + "." + i,
                        VisualNodeLayer.HIDDEN,
                        i,
                        layer));
            }
        }
        for (int output = 0; output < problem.outputDimensions(); output++) {
            nodes.add(new VisualNode(
                    outputNodeId(output),
                    problem.outputLabel(output),
                    VisualNodeLayer.OUTPUT,
                    output));
        }

        for (int layer = 0; layer < hiddenWeights.length; layer++) {
            for (int neuron = 0; neuron < hiddenWeights[layer].length; neuron++) {
                for (int input = 0; input < hiddenWeights[layer][neuron].length; input++) {
                    if (!hiddenMask[layer][neuron][input]) {
                        continue;
                    }
                    String sourceId;
                    VisualLinkKind linkKind = VisualLinkKind.FEED_FORWARD;
                    if (layer == 0) {
                        sourceId = input < featureCount
                                ? featureNodeId(input)
                                : memoryNodeId(input - featureCount);
                        if (input == 0) {
                            linkKind = VisualLinkKind.BIAS;
                        }
                    } else if (input == 0) {
                        sourceId = hiddenBiasNodeId(layer);
                        linkKind = VisualLinkKind.BIAS;
                    } else {
                        sourceId = hiddenNodeId(layer - 1, input - 1);
                    }
                    links.add(new VisualLink(
                            sourceId,
                            hiddenNodeId(layer, neuron),
                            hiddenWeights[layer][neuron][input],
                            linkKind));
                }
            }
        }
        int lastHiddenLayer = hiddenWeights.length - 1;
        for (int to = 0; to < recurrentWeights.length; to++) {
            for (int from = 0; from < recurrentWeights[to].length; from++) {
                if (recurrentMask[to][from]) {
                        links.add(new VisualLink(
                                hiddenNodeId(lastHiddenLayer, from),
                                hiddenNodeId(lastHiddenLayer, to),
                                recurrentWeights[to][from],
                                VisualLinkKind.RECURRENT));
                }
            }
        }
        for (int output = 0; output < outputWeights.length; output++) {
            for (int i = 0; i < outputWeights[output].length; i++) {
                if (outputMask[output][i]) {
                    String sourceId;
                    VisualLinkKind linkKind = VisualLinkKind.FEED_FORWARD;
                    if (i == 0) {
                        sourceId = outputBiasNodeId();
                        linkKind = VisualLinkKind.BIAS;
                    } else if (i <= genome.hiddenNeurons()) {
                        sourceId = hiddenNodeId(lastHiddenLayer, i - 1);
                    } else {
                        sourceId = memoryNodeId(i - genome.hiddenNeurons() - 1);
                    }
                    links.add(new VisualLink(sourceId, outputNodeId(output), outputWeights[output][i], linkKind));
                }
            }
        }
        return new NetworkSnapshot(problem, genome, nodes, links);
    }

    private void trainSample(final double[] input, final double[] targets, final double scheduleScale) {
        ForwardPass pass = forward(input);
        double[] signals = new double[targets.length];
        double[] outputSignals = new double[targets.length];
        for (int output = 0; output < targets.length; output++) {
            double signal = genome.lossFunction().learningSignal(targets[output], pass.outputs()[output]);
            signal = clip(signal, genome.errorClip());
            if (genome.secondDerivativeEstimate()) {
                signal = clip(signal + 0.25 * (signal - previousErrorSignal[output]), genome.errorClip());
            }
            signals[output] = signal;
            outputSignals[output] = signal
                    * genome.outputActivation().outputDerivative(pass.outputScaledInputs()[output])
                    * genome.activationSlope();
        }
        double aggregateSignal = mean(signals);

        double[][] hiddenCredits = hiddenCredits(pass, outputSignals);
        updateOutputWeights(pass.outputInput(), signals, outputSignals, scheduleScale);
        updateHiddenWeights(pass, hiddenCredits, aggregateSignal, scheduleScale);
        updateRecurrentWeights(pass, hiddenCredits, aggregateSignal, scheduleScale);
        if (genome.normalization()) {
            normalizeWeights();
        }
        finishSampleState(pass);
        System.arraycopy(signals, 0, previousErrorSignal, 0, previousErrorSignal.length);
    }

    private void updateOutputWeights(
            final double[] outputInput,
            final double[] signals,
            final double[] outputSignals,
            final double scheduleScale) {
        for (int output = 0; output < outputWeights.length; output++) {
            for (int i = 0; i < outputWeights[output].length; i++) {
                if (!outputMask[output][i]) {
                    continue;
                }
                double delta = -genome.weightDecay() * outputWeights[output][i];
                if (genome.gradientUpdate()) {
                    delta += scheduleScale * genome.outputLearningRate() * outputSignals[output] * outputInput[i];
                }
                if (genome.hebbianUpdate()) {
                    delta += scheduleScale * genome.hebbianLearningRate() * signals[output] * outputInput[i];
                }
                delta += genome.momentum() * previousOutputDeltas[output][i];
                outputWeights[output][i] = clampWeight(outputWeights[output][i] + delta);
                previousOutputDeltas[output][i] = delta;
            }
        }
    }

    private double[][] hiddenCredits(final ForwardPass pass, final double[] outputSignals) {
        double[][] credits = new double[hiddenWeights.length][genome.hiddenNeurons()];
        int lastLayer = hiddenWeights.length - 1;
        for (int neuron = 0; neuron < genome.hiddenNeurons(); neuron++) {
            double outputCredit = 0.0;
            for (int output = 0; output < outputSignals.length; output++) {
                outputCredit += outputSignals[output] * connectedOutputWeight(output, neuron + 1);
            }
            credits[lastLayer][neuron] = outputCredit
                    * genome.hiddenActivation().hiddenDerivative(pass.hiddenScaledInputs()[lastLayer][neuron])
                    * genome.activationSlope();
        }
        for (int layer = lastLayer - 1; layer >= 0; layer--) {
            for (int neuron = 0; neuron < genome.hiddenNeurons(); neuron++) {
                double downstream = 0.0;
                int nextInputIndex = neuron + 1;
                for (int next = 0; next < genome.hiddenNeurons(); next++) {
                    if (hiddenMask[layer + 1][next][nextInputIndex]) {
                        downstream += credits[layer + 1][next] * hiddenWeights[layer + 1][next][nextInputIndex];
                    }
                }
                credits[layer][neuron] = downstream
                        * genome.hiddenActivation().hiddenDerivative(pass.hiddenScaledInputs()[layer][neuron])
                        * genome.activationSlope();
            }
        }
        return credits;
    }

    private void updateHiddenWeights(
            final ForwardPass pass,
            final double[][] hiddenCredits,
            final double signal,
            final double scheduleScale) {
        for (int layer = 0; layer < hiddenWeights.length; layer++) {
            double[] layerInput = pass.hiddenInputs()[layer];
            for (int neuron = 0; neuron < hiddenWeights[layer].length; neuron++) {
                double hiddenCredit = hiddenCredits[layer][neuron];
                for (int weight = 0; weight < hiddenWeights[layer][neuron].length; weight++) {
                    if (!hiddenMask[layer][neuron][weight]) {
                        continue;
                    }
                    double delta = -genome.weightDecay() * hiddenWeights[layer][neuron][weight];
                    if (genome.gradientUpdate()) {
                        delta += scheduleScale * genome.inputLearningRate() * hiddenCredit * layerInput[weight];
                    }
                    if (genome.hebbianUpdate()) {
                        delta += scheduleScale
                                * genome.hebbianLearningRate()
                                * signal
                                * pass.hiddenLayers()[layer][neuron]
                                * layerInput[weight];
                    }
                    delta += genome.momentum() * previousHiddenDeltas[layer][neuron][weight];
                    hiddenWeights[layer][neuron][weight] = clampWeight(hiddenWeights[layer][neuron][weight] + delta);
                    previousHiddenDeltas[layer][neuron][weight] = delta;
                }
            }
        }
    }

    private void updateRecurrentWeights(
            final ForwardPass pass,
            final double[][] hiddenCredits,
            final double signal,
            final double scheduleScale) {
        double[] finalHidden = pass.finalHidden();
        double[] finalHiddenCredits = hiddenCredits[hiddenCredits.length - 1];
        for (int to = 0; to < recurrentWeights.length; to++) {
            double hiddenCredit = finalHiddenCredits[to];
            for (int from = 0; from < recurrentWeights[to].length; from++) {
                if (!recurrentMask[to][from]) {
                    continue;
                }
                double delta = -genome.weightDecay() * recurrentWeights[to][from];
                if (genome.gradientUpdate()) {
                    delta += scheduleScale * genome.recurrentLearningRate() * hiddenCredit * previousHiddenState[from];
                }
                if (genome.hebbianUpdate()) {
                    delta += scheduleScale * genome.hebbianLearningRate() * signal * finalHidden[to] * previousHiddenState[from];
                }
                delta += genome.momentum() * previousRecurrentDeltas[to][from];
                recurrentWeights[to][from] = clampWeight(recurrentWeights[to][from] + delta);
                previousRecurrentDeltas[to][from] = delta;
            }
        }
    }

    private ForwardPass forward(final double[] input) {
        double[] fullInput = fullInput(input);
        double[][] hiddenInputs = new double[hiddenWeights.length][];
        double[][] hiddenLayers = new double[hiddenWeights.length][genome.hiddenNeurons()];
        double[][] hiddenScaledInputs = new double[hiddenWeights.length][genome.hiddenNeurons()];
        int lastLayer = hiddenWeights.length - 1;
        for (int layer = 0; layer < hiddenWeights.length; layer++) {
            double[] layerInput = layer == 0 ? fullInput : hiddenLayerInput(hiddenLayers[layer - 1]);
            hiddenInputs[layer] = layerInput;
            for (int neuron = 0; neuron < hiddenWeights[layer].length; neuron++) {
                double raw = maskedDot(hiddenWeights[layer][neuron], hiddenMask[layer][neuron], layerInput);
                if (layer == lastLayer) {
                    raw += recurrentDot(neuron);
                }
                double scaled = genome.activationSlope() * raw;
                hiddenScaledInputs[layer][neuron] = scaled;
                hiddenLayers[layer][neuron] = genome.hiddenActivation().hidden(scaled);
            }
        }

        double[] outputInput = outputInput(hiddenLayers[lastLayer]);
        double[] outputScaledInputs = new double[outputWeights.length];
        double[] outputs = new double[outputWeights.length];
        for (int output = 0; output < outputWeights.length; output++) {
            double rawOutput = maskedDot(outputWeights[output], outputMask[output], outputInput);
            outputScaledInputs[output] = genome.activationSlope() * rawOutput;
            outputs[output] = genome.outputActivation().output(outputScaledInputs[output]);
            outputs[output] = Math.max(0.0, Math.min(1.0, outputs[output]));
        }
        return new ForwardPass(fullInput, hiddenInputs, hiddenLayers, hiddenScaledInputs, outputInput, outputScaledInputs, outputs);
    }

    private double[] fullInput(final double[] input) {
        double[] features = genome.inputRepresentation()
                .encode(input, problem.kernelCenters(), genome.phaseEncoding(), genome.kernelMemory(), genome.kernelSharpness());
        double[] fullInput = new double[features.length + memoryState.length];
        System.arraycopy(features, 0, fullInput, 0, features.length);
        System.arraycopy(memoryState, 0, fullInput, features.length, memoryState.length);
        return fullInput;
    }

    private static double[] hiddenLayerInput(final double[] previousHiddenLayer) {
        double[] layerInput = new double[previousHiddenLayer.length + 1];
        layerInput[0] = 1.0;
        System.arraycopy(previousHiddenLayer, 0, layerInput, 1, previousHiddenLayer.length);
        return layerInput;
    }

    private double[] outputInput(final double[] hidden) {
        double[] outputInput = new double[1 + hidden.length + memoryState.length];
        outputInput[0] = 1.0;
        System.arraycopy(hidden, 0, outputInput, 1, hidden.length);
        System.arraycopy(memoryState, 0, outputInput, 1 + hidden.length, memoryState.length);
        return outputInput;
    }

    private void advanceState(final double[] hidden) {
        for (int i = 0; i < memoryState.length; i++) {
            double candidate = hidden[i % hidden.length];
            memoryState[i] = clip((1.0 - genome.memoryLearningRate()) * memoryState[i]
                    + genome.memoryLearningRate() * candidate, 2.0);
        }
        System.arraycopy(hidden, 0, previousHiddenState, 0, previousHiddenState.length);
    }

    private void resetState() {
        Arrays.fill(memoryState, 0.0);
        Arrays.fill(previousHiddenState, 0.0);
        Arrays.fill(previousErrorSignal, 0.0);
    }

    private void initialize(final Random random) {
        initializeHidden(random);
        initializeOutput(random);
        initializeRecurrent(random);
    }

    private void initializeHidden(final Random random) {
        for (int layer = 0; layer < hiddenWeights.length; layer++) {
            for (int neuron = 0; neuron < hiddenWeights[layer].length; neuron++) {
                boolean hasConnection = false;
                for (int weight = 0; weight < hiddenWeights[layer][neuron].length; weight++) {
                    hiddenMask[layer][neuron][weight] = weight == 0 || random.nextDouble() <= genome.connectionDensity();
                    hasConnection = hasConnection || hiddenMask[layer][neuron][weight];
                    hiddenWeights[layer][neuron][weight] = randomWeight(random);
                }
                if (!hasConnection) {
                    hiddenMask[layer][neuron][0] = true;
                }
            }
        }
    }

    private void initializeOutput(final Random random) {
        for (int output = 0; output < outputWeights.length; output++) {
            for (int weight = 0; weight < outputWeights[output].length; weight++) {
                outputMask[output][weight] = weight == 0 || random.nextDouble() <= genome.connectionDensity();
                outputWeights[output][weight] = randomWeight(random);
            }
            if (outputWeights[output].length > 1) {
                outputMask[output][1 + random.nextInt(outputWeights[output].length - 1)] = true;
            }
        }
    }

    private void initializeRecurrent(final Random random) {
        int added = 0;
        int attempts = 0;
        while (added < genome.recurrentConnections() && attempts < genome.recurrentConnections() * 8 + 8) {
            int to = random.nextInt(recurrentMask.length);
            int from = random.nextInt(recurrentMask[to].length);
            if (!recurrentMask[to][from]) {
                recurrentMask[to][from] = true;
                recurrentWeights[to][from] = random.nextDouble(-0.35, 0.35);
                added++;
            }
            attempts++;
        }
    }

    private void normalizeWeights() {
        for (int layer = 0; layer < hiddenWeights.length; layer++) {
            for (int neuron = 0; neuron < hiddenWeights[layer].length; neuron++) {
                normalize(hiddenWeights[layer][neuron], hiddenMask[layer][neuron]);
            }
        }
        for (int neuron = 0; neuron < recurrentWeights.length; neuron++) {
            normalize(recurrentWeights[neuron], recurrentMask[neuron]);
        }
        for (int output = 0; output < outputWeights.length; output++) {
            normalize(outputWeights[output], outputMask[output]);
        }
    }

    private void normalize(final double[] weights, final boolean[] mask) {
        double norm = 0.0;
        for (int i = 0; i < weights.length; i++) {
            if (mask[i]) {
                norm += weights[i] * weights[i];
            }
        }
        norm = Math.sqrt(norm);
        if (norm <= 3.0 || norm == 0.0) {
            return;
        }
        double targetScale = 3.0 / norm;
        double scale = 1.0 - genome.normalizationStrength() + genome.normalizationStrength() * targetScale;
        for (int i = 0; i < weights.length; i++) {
            if (mask[i]) {
                weights[i] *= scale;
            }
        }
    }

    private boolean weightsAreFinite() {
        for (double[][] layerWeights : hiddenWeights) {
            for (double[] weights : layerWeights) {
                if (!arrayIsFinite(weights)) {
                    return false;
                }
            }
        }
        for (double[] weights : recurrentWeights) {
            if (!arrayIsFinite(weights)) {
                return false;
            }
        }
        for (double[] weights : outputWeights) {
            if (!arrayIsFinite(weights)) {
                return false;
            }
        }
        return true;
    }

    private static boolean arrayIsFinite(final double[] values) {
        for (double value : values) {
            if (!Double.isFinite(value)) {
                return false;
            }
        }
        return true;
    }

    private double connectedOutputWeight(final int output, final int index) {
        return outputMask[output][index] ? outputWeights[output][index] : 0.0;
    }

    private double recurrentDot(final int neuron) {
        double total = 0.0;
        for (int from = 0; from < previousHiddenState.length; from++) {
            if (recurrentMask[neuron][from]) {
                total += recurrentWeights[neuron][from] * previousHiddenState[from];
            }
        }
        return total;
    }

    private void requireScalarOutput() {
        if (problem.outputDimensions() != 1) {
            throw new IllegalStateException("Problem has " + problem.outputDimensions() + " output channels, not one.");
        }
    }

    private List<NeuroEvolutionOutputGroup> validOutputGroups() {
        List<NeuroEvolutionOutputGroup> groups = Objects.requireNonNull(
                problem.outputGroups(),
                "Problem output groups cannot be null.");
        if (groups.isEmpty()) {
            throw new IllegalArgumentException("Problem output groups cannot be empty.");
        }
        int previousEnd = 0;
        for (NeuroEvolutionOutputGroup group : groups) {
            Objects.requireNonNull(group, "Problem output groups cannot contain null entries.");
            if (group.startInclusive() != previousEnd) {
                throw new IllegalArgumentException("Problem output groups must be contiguous and ordered.");
            }
            if (group.endExclusive() > problem.outputDimensions()) {
                throw new IllegalArgumentException("Problem output group exceeds output dimensions.");
            }
            previousEnd = group.endExclusive();
        }
        if (previousEnd != problem.outputDimensions()) {
            throw new IllegalArgumentException("Problem output groups must cover every output channel.");
        }
        return groups;
    }

    private double groupMeanSquaredError(
            final List<NeuroEvolutionSample> samples,
            final NeuroEvolutionOutputGroup group) {
        if (samples.isEmpty()) {
            return 0.0;
        }
        resetState();
        double total = 0.0;
        int count = 0;
        for (NeuroEvolutionSample sample : samples) {
            prepareSampleState();
            ForwardPass pass = forward(sample.input());
            if (!arrayIsFinite(pass.outputs())) {
                return Double.POSITIVE_INFINITY;
            }
            double[] targets = sample.targets();
            for (int output = group.startInclusive(); output < group.endExclusive(); output++) {
                double error = targets[output] - pass.outputs()[output];
                total += error * error;
                count++;
            }
            finishSampleState(pass);
        }
        return count == 0 ? 0.0 : total / count;
    }

    private void prepareSampleState() {
        if (!problem.statefulSamples()) {
            resetState();
        }
    }

    private void finishSampleState(final ForwardPass pass) {
        if (problem.statefulSamples()) {
            advanceState(pass.finalHidden());
        }
    }

    private double[] targetMeans(final List<NeuroEvolutionSample> samples) {
        double[] means = new double[problem.outputDimensions()];
        if (samples.isEmpty()) {
            return means;
        }
        for (NeuroEvolutionSample sample : samples) {
            double[] targets = sample.targets();
            for (int output = 0; output < means.length; output++) {
                means[output] += targets[output];
            }
        }
        for (int output = 0; output < means.length; output++) {
            means[output] /= samples.size();
        }
        return means;
    }

    private static double baselineGroupMeanSquaredError(
            final List<NeuroEvolutionSample> samples,
            final NeuroEvolutionOutputGroup group,
            final double[] trainingMeans) {
        if (samples.isEmpty()) {
            return 0.0;
        }
        double total = 0.0;
        int count = 0;
        for (NeuroEvolutionSample sample : samples) {
            double[] targets = sample.targets();
            for (int output = group.startInclusive(); output < group.endExclusive(); output++) {
                double error = targets[output] - trainingMeans[output];
                total += error * error;
                count++;
            }
        }
        return count == 0 ? 0.0 : total / count;
    }

    private static double improvementOverBaseline(
            final double meanSquaredError,
            final double baselineMeanSquaredError) {
        if (!Double.isFinite(meanSquaredError) || !Double.isFinite(baselineMeanSquaredError)) {
            return Double.NEGATIVE_INFINITY;
        }
        if (baselineMeanSquaredError == 0.0) {
            return meanSquaredError == 0.0 ? 1.0 : Double.NEGATIVE_INFINITY;
        }
        return 1.0 - meanSquaredError / baselineMeanSquaredError;
    }

    private static double weightedBaselineRelativeMeanSquaredError(
            final List<NeuroEvolutionOutputGroup> groups,
            final double[] groupTotals,
            final double[] baselineTotals,
            final int[] groupCounts) {
        double weightedTotal = 0.0;
        double weightTotal = 0.0;
        for (int i = 0; i < groups.size(); i++) {
            if (groupCounts[i] == 0) {
                continue;
            }
            double groupMse = groupTotals[i] / groupCounts[i];
            double baselineMse = baselineTotals[i] / groupCounts[i];
            double weight = groups.get(i).objectiveWeight();
            weightedTotal += baselineRelativeError(groupMse, baselineMse) * weight;
            weightTotal += weight;
        }
        return weightTotal == 0.0 ? 0.0 : weightedTotal / weightTotal;
    }

    private double latentPredictionEnergy(final ForwardPass pass, final int layer) {
        double[] actual = pass.hiddenInputs()[layer];
        double[] prediction = new double[actual.length];
        for (int input = 1; input < actual.length; input++) {
            double predicted = 0.0;
            int connections = 0;
            for (int neuron = 0; neuron < hiddenWeights[layer].length; neuron++) {
                if (hiddenMask[layer][neuron][input]) {
                    predicted += pass.hiddenLayers()[layer][neuron] * hiddenWeights[layer][neuron][input];
                    connections++;
                }
            }
            if (connections > 0) {
                prediction[input] = predicted / Math.sqrt(connections);
            }
        }
        return cosinePredictionEnergy(actual, prediction, 1);
    }

    private static double cosinePredictionEnergy(
            final double[] actual,
            final double[] prediction,
            final int startIndex) {
        double actualNorm = 0.0;
        double predictionNorm = 0.0;
        double dot = 0.0;
        for (int i = startIndex; i < actual.length; i++) {
            actualNorm += actual[i] * actual[i];
            predictionNorm += prediction[i] * prediction[i];
            dot += actual[i] * prediction[i];
        }
        if (actualNorm <= 1.0e-12) {
            return 0.0;
        }
        if (predictionNorm <= 1.0e-12) {
            return 1.0;
        }
        double cosine = dot / Math.sqrt(actualNorm * predictionNorm);
        cosine = Math.max(-1.0, Math.min(1.0, cosine));
        return 0.5 * (1.0 - cosine);
    }

    private static double baselineRelativeError(
            final double meanSquaredError,
            final double baselineMeanSquaredError) {
        if (!Double.isFinite(meanSquaredError)) {
            return Double.POSITIVE_INFINITY;
        }
        if (!Double.isFinite(baselineMeanSquaredError) || baselineMeanSquaredError <= 1.0e-12) {
            return meanSquaredError <= 1.0e-12 ? 0.0 : Double.POSITIVE_INFINITY;
        }
        return Math.max(0.0, meanSquaredError / baselineMeanSquaredError);
    }

    private static void requireNonNegativeScore(final double value, final String label) {
        if (Double.isNaN(value) || value < 0.0) {
            throw new IllegalArgumentException(label + " must be non-negative.");
        }
    }

    private static double maskedDot(final double[] weights, final boolean[] mask, final double[] values) {
        double total = 0.0;
        for (int i = 0; i < weights.length; i++) {
            if (mask[i]) {
                total += weights[i] * values[i];
            }
        }
        return total;
    }

    private static double mean(final double[] values) {
        double total = 0.0;
        for (double value : values) {
            total += value;
        }
        return total / values.length;
    }

    private static double randomWeight(final Random random) {
        return random.nextDouble(-1.0, 1.0);
    }

    private static double clampWeight(final double value) {
        if (!Double.isFinite(value)) {
            return 0.0;
        }
        return Math.max(-MAX_WEIGHT, Math.min(MAX_WEIGHT, value));
    }

    private static double clip(final double value, final double clip) {
        return Math.max(-clip, Math.min(clip, value));
    }

    private static String featureNodeId(final int index) {
        return "feature-" + index;
    }

    private static String memoryNodeId(final int index) {
        return "memory-" + index;
    }

    private static String hiddenBiasNodeId(final int layer) {
        return "hidden-bias-" + layer;
    }

    private static String hiddenNodeId(final int layer, final int index) {
        return "hidden-" + layer + "-" + index;
    }

    private static String outputBiasNodeId() {
        return "output-bias";
    }

    private static String outputNodeId(final int index) {
        return index == 0 ? "output" : "output-" + index;
    }

    public enum VisualNodeLayer {
        INPUT,
        MEMORY,
        BIAS,
        HIDDEN,
        OUTPUT
    }

    public enum VisualLinkKind {
        FEED_FORWARD,
        BIAS,
        RECURRENT
    }

    public record VisualNode(String id, String label, VisualNodeLayer layer, int index, int depth) {

        public VisualNode {
            Objects.requireNonNull(id, "Node id cannot be null.");
            Objects.requireNonNull(label, "Node label cannot be null.");
            Objects.requireNonNull(layer, "Node layer cannot be null.");
        }

        public VisualNode(final String id, final String label, final VisualNodeLayer layer, final int index) {
            this(id, label, layer, index, 0);
        }
    }

    public record VisualLink(String fromId, String toId, double weight, VisualLinkKind kind) {

        public VisualLink {
            Objects.requireNonNull(fromId, "Source id cannot be null.");
            Objects.requireNonNull(toId, "Target id cannot be null.");
            Objects.requireNonNull(kind, "Link kind cannot be null.");
        }

        public VisualLink(
                final String fromId,
                final String toId,
                final double weight,
                final boolean recurrent) {
            this(fromId, toId, weight, recurrent ? VisualLinkKind.RECURRENT : VisualLinkKind.FEED_FORWARD);
        }

        public boolean recurrent() {
            return kind == VisualLinkKind.RECURRENT;
        }

        public boolean bias() {
            return kind == VisualLinkKind.BIAS;
        }
    }

    public record NetworkSnapshot(
            NeuroEvolutionProblem problem,
            EvolvableXorGenome genome,
            List<VisualNode> nodes,
            List<VisualLink> links) {

        public NetworkSnapshot {
            Objects.requireNonNull(problem, "Problem cannot be null.");
            Objects.requireNonNull(genome, "Genome cannot be null.");
            nodes = List.copyOf(Objects.requireNonNull(nodes, "Nodes cannot be null."));
            links = List.copyOf(Objects.requireNonNull(links, "Links cannot be null."));
        }
    }

    private record ForwardPass(
            double[] fullInput,
            double[][] hiddenInputs,
            double[][] hiddenLayers,
            double[][] hiddenScaledInputs,
            double[] outputInput,
            double[] outputScaledInputs,
            double[] outputs) {

        private double[] finalHidden() {
            return hiddenLayers[hiddenLayers.length - 1];
        }
    }
}
