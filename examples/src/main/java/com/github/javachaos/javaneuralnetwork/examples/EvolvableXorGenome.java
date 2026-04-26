package com.github.javachaos.javaneuralnetwork.examples;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Random;

/**
 * A composable genome that mutates topology, state, representation, loss, and
 * local update terms for a tiny XOR learner.
 */
public record EvolvableXorGenome(
        int hiddenNeurons,
        int hiddenLayers,
        int recurrentConnections,
        int memoryCells,
        double connectionDensity,
        XorActivationFunction hiddenActivation,
        XorActivationFunction outputActivation,
        XorInputRepresentation inputRepresentation,
        XorLossFunction lossFunction,
        XorLearningSchedule learningSchedule,
        boolean hebbianUpdate,
        boolean gradientUpdate,
        boolean normalization,
        boolean kernelMemory,
        boolean phaseEncoding,
        boolean secondDerivativeEstimate,
        double inputLearningRate,
        double outputLearningRate,
        double recurrentLearningRate,
        double hebbianLearningRate,
        double memoryLearningRate,
        double momentum,
        double weightDecay,
        double normalizationStrength,
        double activationSlope,
        double errorClip,
        double kernelSharpness) {

    private static final int MIN_HIDDEN_NEURONS = 1;
    private static final int MAX_HIDDEN_NEURONS = 8;
    private static final int MIN_HIDDEN_LAYERS = 1;
    private static final int MAX_HIDDEN_LAYERS = 4;
    private static final int MIN_MEMORY_CELLS = 0;
    private static final int MAX_MEMORY_CELLS = 4;
    private static final double MIN_DENSITY = 0.15;
    private static final double MAX_DENSITY = 1.0;
    private static final double MIN_RATE = 0.0005;
    private static final double MAX_RATE = 1.5;
    private static final double MIN_RECURRENT_RATE = 0.0;
    private static final double MAX_RECURRENT_RATE = 0.5;
    private static final double MIN_HEBBIAN_RATE = -0.5;
    private static final double MAX_HEBBIAN_RATE = 0.5;
    private static final double MIN_MEMORY_RATE = 0.0;
    private static final double MAX_MEMORY_RATE = 0.95;
    private static final double MIN_MOMENTUM = 0.0;
    private static final double MAX_MOMENTUM = 0.98;
    private static final double MIN_DECAY = 0.0;
    private static final double MAX_DECAY = 0.05;
    private static final double MIN_NORMALIZATION = 0.0;
    private static final double MAX_NORMALIZATION = 1.0;
    private static final double MIN_SLOPE = 0.1;
    private static final double MAX_SLOPE = 6.0;
    private static final double MIN_CLIP = 0.05;
    private static final double MAX_CLIP = 8.0;
    private static final double MIN_KERNEL_SHARPNESS = 0.1;
    private static final double MAX_KERNEL_SHARPNESS = 8.0;

    public EvolvableXorGenome {
        hiddenActivation = Objects.requireNonNull(hiddenActivation, "Hidden activation cannot be null.");
        outputActivation = Objects.requireNonNull(outputActivation, "Output activation cannot be null.");
        inputRepresentation = Objects.requireNonNull(inputRepresentation, "Input representation cannot be null.");
        lossFunction = Objects.requireNonNull(lossFunction, "Loss function cannot be null.");
        learningSchedule = Objects.requireNonNull(learningSchedule, "Learning schedule cannot be null.");
        if (!hebbianUpdate && !gradientUpdate) {
            gradientUpdate = true;
        }
        hiddenNeurons = clampInt(hiddenNeurons, MIN_HIDDEN_NEURONS, MAX_HIDDEN_NEURONS);
        hiddenLayers = clampInt(hiddenLayers, MIN_HIDDEN_LAYERS, MAX_HIDDEN_LAYERS);
        recurrentConnections = clampInt(recurrentConnections, 0, maxRecurrentConnections(hiddenNeurons));
        memoryCells = clampInt(memoryCells, MIN_MEMORY_CELLS, MAX_MEMORY_CELLS);
        connectionDensity = clampFinite(connectionDensity, MIN_DENSITY, MAX_DENSITY, "Connection density");
        inputLearningRate = clampFinite(inputLearningRate, MIN_RATE, MAX_RATE, "Input learning rate");
        outputLearningRate = clampFinite(outputLearningRate, MIN_RATE, MAX_RATE, "Output learning rate");
        recurrentLearningRate = clampFinite(
                recurrentLearningRate,
                MIN_RECURRENT_RATE,
                MAX_RECURRENT_RATE,
                "Recurrent learning rate");
        hebbianLearningRate = clampFinite(
                hebbianLearningRate,
                MIN_HEBBIAN_RATE,
                MAX_HEBBIAN_RATE,
                "Hebbian learning rate");
        memoryLearningRate = clampFinite(memoryLearningRate, MIN_MEMORY_RATE, MAX_MEMORY_RATE, "Memory learning rate");
        momentum = clampFinite(momentum, MIN_MOMENTUM, MAX_MOMENTUM, "Momentum");
        weightDecay = clampFinite(weightDecay, MIN_DECAY, MAX_DECAY, "Weight decay");
        normalizationStrength = clampFinite(
                normalizationStrength,
                MIN_NORMALIZATION,
                MAX_NORMALIZATION,
                "Normalization strength");
        activationSlope = clampFinite(activationSlope, MIN_SLOPE, MAX_SLOPE, "Activation slope");
        errorClip = clampFinite(errorClip, MIN_CLIP, MAX_CLIP, "Error clip");
        kernelSharpness = clampFinite(kernelSharpness, MIN_KERNEL_SHARPNESS, MAX_KERNEL_SHARPNESS, "Kernel sharpness");
    }

    public EvolvableXorGenome(
            final int hiddenNeurons,
            final int recurrentConnections,
            final int memoryCells,
            final double connectionDensity,
            final XorActivationFunction hiddenActivation,
            final XorActivationFunction outputActivation,
            final XorInputRepresentation inputRepresentation,
            final XorLossFunction lossFunction,
            final XorLearningSchedule learningSchedule,
            final boolean hebbianUpdate,
            final boolean gradientUpdate,
            final boolean normalization,
            final boolean kernelMemory,
            final boolean phaseEncoding,
            final boolean secondDerivativeEstimate,
            final double inputLearningRate,
            final double outputLearningRate,
            final double recurrentLearningRate,
            final double hebbianLearningRate,
            final double memoryLearningRate,
            final double momentum,
            final double weightDecay,
            final double normalizationStrength,
            final double activationSlope,
            final double errorClip,
            final double kernelSharpness) {
        this(
                hiddenNeurons,
                MIN_HIDDEN_LAYERS,
                recurrentConnections,
                memoryCells,
                connectionDensity,
                hiddenActivation,
                outputActivation,
                inputRepresentation,
                lossFunction,
                learningSchedule,
                hebbianUpdate,
                gradientUpdate,
                normalization,
                kernelMemory,
                phaseEncoding,
                secondDerivativeEstimate,
                inputLearningRate,
                outputLearningRate,
                recurrentLearningRate,
                hebbianLearningRate,
                memoryLearningRate,
                momentum,
                weightDecay,
                normalizationStrength,
                activationSlope,
                errorClip,
                kernelSharpness);
    }

    public static EnumSet<XorMutationType> allowedMutations() {
        return EnumSet.allOf(XorMutationType.class);
    }

    public static EvolvableXorGenome random(final Random random) {
        Objects.requireNonNull(random, "Random cannot be null.");
        int hiddenNeurons = random.nextInt(2, 5);
        int hiddenLayers = random.nextInt(MIN_HIDDEN_LAYERS, 3);
        return new EvolvableXorGenome(
                hiddenNeurons,
                hiddenLayers,
                random.nextInt(0, Math.min(2, maxRecurrentConnections(hiddenNeurons)) + 1),
                random.nextInt(0, 2),
                randomRange(random, 0.55, MAX_DENSITY),
                randomEnum(random, XorActivationFunction.class),
                randomEnum(random, XorActivationFunction.class),
                randomEnum(random, XorInputRepresentation.class),
                randomEnum(random, XorLossFunction.class),
                randomEnum(random, XorLearningSchedule.class),
                random.nextDouble() < 0.35,
                true,
                random.nextDouble() < 0.25,
                random.nextDouble() < 0.2,
                random.nextDouble() < 0.2,
                random.nextDouble() < 0.15,
                randomRange(random, 0.01, 0.6),
                randomRange(random, 0.01, 0.6),
                randomRange(random, 0.0, 0.15),
                randomRange(random, -0.05, 0.15),
                randomRange(random, 0.05, 0.5),
                randomRange(random, 0.0, 0.85),
                randomRange(random, 0.0, 0.02),
                randomRange(random, 0.0, 0.4),
                randomRange(random, 0.3, 3.0),
                randomRange(random, 0.2, 3.0),
                randomRange(random, 0.4, 4.0));
    }

    public EvolvableXorGenome crossover(final EvolvableXorGenome other, final Random random) {
        Objects.requireNonNull(other, "Other genome cannot be null.");
        Objects.requireNonNull(random, "Random cannot be null.");
        return new EvolvableXorGenome(
                choose(random, hiddenNeurons, other.hiddenNeurons),
                choose(random, hiddenLayers, other.hiddenLayers),
                choose(random, recurrentConnections, other.recurrentConnections),
                choose(random, memoryCells, other.memoryCells),
                blend(random, connectionDensity, other.connectionDensity),
                choose(random, hiddenActivation, other.hiddenActivation),
                choose(random, outputActivation, other.outputActivation),
                choose(random, inputRepresentation, other.inputRepresentation),
                choose(random, lossFunction, other.lossFunction),
                choose(random, learningSchedule, other.learningSchedule),
                choose(random, hebbianUpdate, other.hebbianUpdate),
                choose(random, gradientUpdate, other.gradientUpdate),
                choose(random, normalization, other.normalization),
                choose(random, kernelMemory, other.kernelMemory),
                choose(random, phaseEncoding, other.phaseEncoding),
                choose(random, secondDerivativeEstimate, other.secondDerivativeEstimate),
                blend(random, inputLearningRate, other.inputLearningRate),
                blend(random, outputLearningRate, other.outputLearningRate),
                blend(random, recurrentLearningRate, other.recurrentLearningRate),
                blend(random, hebbianLearningRate, other.hebbianLearningRate),
                blend(random, memoryLearningRate, other.memoryLearningRate),
                blend(random, momentum, other.momentum),
                blend(random, weightDecay, other.weightDecay),
                blend(random, normalizationStrength, other.normalizationStrength),
                blend(random, activationSlope, other.activationSlope),
                blend(random, errorClip, other.errorClip),
                blend(random, kernelSharpness, other.kernelSharpness));
    }

    public EvolvableXorGenome mutate(final Random random, final double intensity) {
        Objects.requireNonNull(random, "Random cannot be null.");
        XorMutationType[] mutations = XorMutationType.values();
        return mutate(random, mutations[random.nextInt(mutations.length)], intensity);
    }

    public EvolvableXorGenome mutate(
            final Random random,
            final XorMutationType mutationType,
            final double intensity) {
        Objects.requireNonNull(random, "Random cannot be null.");
        Objects.requireNonNull(mutationType, "Mutation type cannot be null.");
        if (!Double.isFinite(intensity) || intensity < 0.0) {
            throw new IllegalArgumentException("Mutation intensity must be finite and non-negative.");
        }
        return switch (mutationType) {
            case ADD_NEURON -> withHiddenNeurons(hiddenNeurons + 1);
            case REMOVE_NEURON -> withHiddenNeurons(hiddenNeurons - 1);
            case ADD_HIDDEN_LAYER -> withHiddenLayers(hiddenLayers + 1);
            case REMOVE_HIDDEN_LAYER -> withHiddenLayers(hiddenLayers - 1);
            case ADD_CONNECTION -> withConnectionDensity(connectionDensity + randomRange(random, 0.04, 0.18));
            case REMOVE_CONNECTION -> withConnectionDensity(connectionDensity - randomRange(random, 0.04, 0.18));
            case ADD_RECURRENT_CONNECTION -> withRecurrentConnections(recurrentConnections + 1);
            case REMOVE_RECURRENT_CONNECTION -> withRecurrentConnections(recurrentConnections - 1);
            case CHANGE_ACTIVATION -> changeActivation(random);
            case ADD_MEMORY_CELL -> withMemoryCells(memoryCells + 1);
            case REMOVE_MEMORY_CELL -> withMemoryCells(memoryCells - 1);
            case ADD_HEBBIAN_UPDATE -> withHebbianUpdate(true)
                    .withHebbianLearningRate(mutateRange(
                            random,
                            hebbianLearningRate,
                            intensity,
                            MIN_HEBBIAN_RATE,
                            MAX_HEBBIAN_RATE));
            case ADD_GRADIENT_UPDATE -> withGradientUpdate(true);
            case ADD_NORMALIZATION -> withNormalization(true)
                    .withNormalizationStrength(mutateRange(
                            random,
                            Math.max(0.05, normalizationStrength),
                            intensity,
                            MIN_NORMALIZATION,
                            MAX_NORMALIZATION));
            case ADD_KERNEL_MEMORY -> withKernelMemory(true)
                    .withKernelSharpness(mutateRange(
                            random,
                            kernelSharpness,
                            intensity,
                            MIN_KERNEL_SHARPNESS,
                            MAX_KERNEL_SHARPNESS));
            case ADD_PHASE_ENCODING -> withPhaseEncoding(true);
            case ADD_SECOND_DERIVATIVE_ESTIMATE -> withSecondDerivativeEstimate(true);
            case CHANGE_LEARNING_SCHEDULE -> withLearningSchedule(randomDifferent(random, learningSchedule));
            case CHANGE_LOSS_FUNCTION -> withLossFunction(randomDifferent(random, lossFunction));
            case CHANGE_INPUT_REPRESENTATION -> withInputRepresentation(randomDifferent(random, inputRepresentation));
            case PERTURB_NUMERIC_PARAMETER -> mutateNumeric(random, intensity);
        };
    }

    public int inputSize() {
        return inputSize(new XorProblem());
    }

    public int inputSize(final NeuroEvolutionProblem problem) {
        return inputFeatureSize(problem) + memoryCells;
    }

    public int inputFeatureSize(final NeuroEvolutionProblem problem) {
        Objects.requireNonNull(problem, "Problem cannot be null.");
        return inputRepresentation
                .encode(
                        new double[problem.inputDimensions()],
                        problem.kernelCenters(),
                        phaseEncoding,
                        kernelMemory,
                        kernelSharpness)
                .length;
    }

    public int outputInputSize() {
        return hiddenNeurons + memoryCells + 1;
    }

    public int totalHiddenNeurons() {
        return hiddenNeurons * hiddenLayers;
    }

    public double complexityCost() {
        return complexityCost(new XorProblem());
    }

    public double complexityCost(final NeuroEvolutionProblem problem) {
        double cost = hiddenNeurons * inputSize(problem);
        cost += Math.max(0, hiddenLayers - 1) * hiddenNeurons * (hiddenNeurons + 1.0);
        cost += outputInputSize() * problem.outputDimensions();
        cost += Math.max(0, hiddenLayers - 1) * 3.0;
        cost += recurrentConnections * 0.5;
        cost += memoryCells * 2.0;
        cost += hebbianUpdate ? 1.0 : 0.0;
        cost += normalization ? 1.0 : 0.0;
        cost += kernelMemory ? 1.0 : 0.0;
        cost += phaseEncoding ? 1.0 : 0.0;
        cost += secondDerivativeEstimate ? 1.0 : 0.0;
        return cost;
    }

    private EvolvableXorGenome mutateNumeric(final Random random, final double intensity) {
        return new EvolvableXorGenome(
                hiddenNeurons,
                hiddenLayers,
                recurrentConnections,
                memoryCells,
                mutateRange(random, connectionDensity, intensity, MIN_DENSITY, MAX_DENSITY),
                hiddenActivation,
                outputActivation,
                inputRepresentation,
                lossFunction,
                learningSchedule,
                hebbianUpdate,
                gradientUpdate,
                normalization,
                kernelMemory,
                phaseEncoding,
                secondDerivativeEstimate,
                mutateRange(random, inputLearningRate, intensity, MIN_RATE, MAX_RATE),
                mutateRange(random, outputLearningRate, intensity, MIN_RATE, MAX_RATE),
                mutateRange(random, recurrentLearningRate, intensity, MIN_RECURRENT_RATE, MAX_RECURRENT_RATE),
                mutateRange(random, hebbianLearningRate, intensity, MIN_HEBBIAN_RATE, MAX_HEBBIAN_RATE),
                mutateRange(random, memoryLearningRate, intensity, MIN_MEMORY_RATE, MAX_MEMORY_RATE),
                mutateRange(random, momentum, intensity, MIN_MOMENTUM, MAX_MOMENTUM),
                mutateRange(random, weightDecay, intensity, MIN_DECAY, MAX_DECAY),
                mutateRange(random, normalizationStrength, intensity, MIN_NORMALIZATION, MAX_NORMALIZATION),
                mutateRange(random, activationSlope, intensity, MIN_SLOPE, MAX_SLOPE),
                mutateRange(random, errorClip, intensity, MIN_CLIP, MAX_CLIP),
                mutateRange(random, kernelSharpness, intensity, MIN_KERNEL_SHARPNESS, MAX_KERNEL_SHARPNESS));
    }

    private EvolvableXorGenome changeActivation(final Random random) {
        if (random.nextBoolean()) {
            return withHiddenActivation(randomDifferent(random, hiddenActivation));
        }
        return withOutputActivation(randomDifferent(random, outputActivation));
    }

    private EvolvableXorGenome withHiddenNeurons(final int value) {
        return new EvolvableXorGenome(
                value,
                hiddenLayers,
                recurrentConnections,
                memoryCells,
                connectionDensity,
                hiddenActivation,
                outputActivation,
                inputRepresentation,
                lossFunction,
                learningSchedule,
                hebbianUpdate,
                gradientUpdate,
                normalization,
                kernelMemory,
                phaseEncoding,
                secondDerivativeEstimate,
                inputLearningRate,
                outputLearningRate,
                recurrentLearningRate,
                hebbianLearningRate,
                memoryLearningRate,
                momentum,
                weightDecay,
                normalizationStrength,
                activationSlope,
                errorClip,
                kernelSharpness);
    }

    private EvolvableXorGenome withHiddenLayers(final int value) {
        return new EvolvableXorGenome(
                hiddenNeurons,
                value,
                recurrentConnections,
                memoryCells,
                connectionDensity,
                hiddenActivation,
                outputActivation,
                inputRepresentation,
                lossFunction,
                learningSchedule,
                hebbianUpdate,
                gradientUpdate,
                normalization,
                kernelMemory,
                phaseEncoding,
                secondDerivativeEstimate,
                inputLearningRate,
                outputLearningRate,
                recurrentLearningRate,
                hebbianLearningRate,
                memoryLearningRate,
                momentum,
                weightDecay,
                normalizationStrength,
                activationSlope,
                errorClip,
                kernelSharpness);
    }

    private EvolvableXorGenome withRecurrentConnections(final int value) {
        return new EvolvableXorGenome(
                hiddenNeurons,
                hiddenLayers,
                value,
                memoryCells,
                connectionDensity,
                hiddenActivation,
                outputActivation,
                inputRepresentation,
                lossFunction,
                learningSchedule,
                hebbianUpdate,
                gradientUpdate,
                normalization,
                kernelMemory,
                phaseEncoding,
                secondDerivativeEstimate,
                inputLearningRate,
                outputLearningRate,
                recurrentLearningRate,
                hebbianLearningRate,
                memoryLearningRate,
                momentum,
                weightDecay,
                normalizationStrength,
                activationSlope,
                errorClip,
                kernelSharpness);
    }

    private EvolvableXorGenome withMemoryCells(final int value) {
        return new EvolvableXorGenome(
                hiddenNeurons,
                hiddenLayers,
                recurrentConnections,
                value,
                connectionDensity,
                hiddenActivation,
                outputActivation,
                inputRepresentation,
                lossFunction,
                learningSchedule,
                hebbianUpdate,
                gradientUpdate,
                normalization,
                kernelMemory,
                phaseEncoding,
                secondDerivativeEstimate,
                inputLearningRate,
                outputLearningRate,
                recurrentLearningRate,
                hebbianLearningRate,
                memoryLearningRate,
                momentum,
                weightDecay,
                normalizationStrength,
                activationSlope,
                errorClip,
                kernelSharpness);
    }

    private EvolvableXorGenome withConnectionDensity(final double value) {
        return new EvolvableXorGenome(
                hiddenNeurons,
                hiddenLayers,
                recurrentConnections,
                memoryCells,
                value,
                hiddenActivation,
                outputActivation,
                inputRepresentation,
                lossFunction,
                learningSchedule,
                hebbianUpdate,
                gradientUpdate,
                normalization,
                kernelMemory,
                phaseEncoding,
                secondDerivativeEstimate,
                inputLearningRate,
                outputLearningRate,
                recurrentLearningRate,
                hebbianLearningRate,
                memoryLearningRate,
                momentum,
                weightDecay,
                normalizationStrength,
                activationSlope,
                errorClip,
                kernelSharpness);
    }

    private EvolvableXorGenome withHiddenActivation(final XorActivationFunction value) {
        return copy(value, outputActivation, inputRepresentation, lossFunction, learningSchedule);
    }

    private EvolvableXorGenome withOutputActivation(final XorActivationFunction value) {
        return copy(hiddenActivation, value, inputRepresentation, lossFunction, learningSchedule);
    }

    private EvolvableXorGenome withInputRepresentation(final XorInputRepresentation value) {
        return copy(hiddenActivation, outputActivation, value, lossFunction, learningSchedule);
    }

    private EvolvableXorGenome withLossFunction(final XorLossFunction value) {
        return copy(hiddenActivation, outputActivation, inputRepresentation, value, learningSchedule);
    }

    private EvolvableXorGenome withLearningSchedule(final XorLearningSchedule value) {
        return copy(hiddenActivation, outputActivation, inputRepresentation, lossFunction, value);
    }

    private EvolvableXorGenome withHebbianUpdate(final boolean value) {
        return copy(value, gradientUpdate, normalization, kernelMemory, phaseEncoding, secondDerivativeEstimate);
    }

    private EvolvableXorGenome withGradientUpdate(final boolean value) {
        return copy(hebbianUpdate, value, normalization, kernelMemory, phaseEncoding, secondDerivativeEstimate);
    }

    private EvolvableXorGenome withNormalization(final boolean value) {
        return copy(hebbianUpdate, gradientUpdate, value, kernelMemory, phaseEncoding, secondDerivativeEstimate);
    }

    private EvolvableXorGenome withKernelMemory(final boolean value) {
        return copy(hebbianUpdate, gradientUpdate, normalization, value, phaseEncoding, secondDerivativeEstimate);
    }

    private EvolvableXorGenome withPhaseEncoding(final boolean value) {
        return copy(hebbianUpdate, gradientUpdate, normalization, kernelMemory, value, secondDerivativeEstimate);
    }

    private EvolvableXorGenome withSecondDerivativeEstimate(final boolean value) {
        return copy(hebbianUpdate, gradientUpdate, normalization, kernelMemory, phaseEncoding, value);
    }

    private EvolvableXorGenome withHebbianLearningRate(final double value) {
        return copy(
                inputLearningRate,
                outputLearningRate,
                recurrentLearningRate,
                value,
                memoryLearningRate,
                momentum,
                weightDecay,
                normalizationStrength,
                activationSlope,
                errorClip,
                kernelSharpness);
    }

    private EvolvableXorGenome withNormalizationStrength(final double value) {
        return copy(
                inputLearningRate,
                outputLearningRate,
                recurrentLearningRate,
                hebbianLearningRate,
                memoryLearningRate,
                momentum,
                weightDecay,
                value,
                activationSlope,
                errorClip,
                kernelSharpness);
    }

    private EvolvableXorGenome withKernelSharpness(final double value) {
        return copy(
                inputLearningRate,
                outputLearningRate,
                recurrentLearningRate,
                hebbianLearningRate,
                memoryLearningRate,
                momentum,
                weightDecay,
                normalizationStrength,
                activationSlope,
                errorClip,
                value);
    }

    private EvolvableXorGenome copy(
            final XorActivationFunction hiddenActivation,
            final XorActivationFunction outputActivation,
            final XorInputRepresentation inputRepresentation,
            final XorLossFunction lossFunction,
            final XorLearningSchedule learningSchedule) {
        return new EvolvableXorGenome(
                hiddenNeurons,
                hiddenLayers,
                recurrentConnections,
                memoryCells,
                connectionDensity,
                hiddenActivation,
                outputActivation,
                inputRepresentation,
                lossFunction,
                learningSchedule,
                hebbianUpdate,
                gradientUpdate,
                normalization,
                kernelMemory,
                phaseEncoding,
                secondDerivativeEstimate,
                inputLearningRate,
                outputLearningRate,
                recurrentLearningRate,
                hebbianLearningRate,
                memoryLearningRate,
                momentum,
                weightDecay,
                normalizationStrength,
                activationSlope,
                errorClip,
                kernelSharpness);
    }

    private EvolvableXorGenome copy(
            final boolean hebbianUpdate,
            final boolean gradientUpdate,
            final boolean normalization,
            final boolean kernelMemory,
            final boolean phaseEncoding,
            final boolean secondDerivativeEstimate) {
        return new EvolvableXorGenome(
                hiddenNeurons,
                hiddenLayers,
                recurrentConnections,
                memoryCells,
                connectionDensity,
                hiddenActivation,
                outputActivation,
                inputRepresentation,
                lossFunction,
                learningSchedule,
                hebbianUpdate,
                gradientUpdate,
                normalization,
                kernelMemory,
                phaseEncoding,
                secondDerivativeEstimate,
                inputLearningRate,
                outputLearningRate,
                recurrentLearningRate,
                hebbianLearningRate,
                memoryLearningRate,
                momentum,
                weightDecay,
                normalizationStrength,
                activationSlope,
                errorClip,
                kernelSharpness);
    }

    private EvolvableXorGenome copy(
            final double inputLearningRate,
            final double outputLearningRate,
            final double recurrentLearningRate,
            final double hebbianLearningRate,
            final double memoryLearningRate,
            final double momentum,
            final double weightDecay,
            final double normalizationStrength,
            final double activationSlope,
            final double errorClip,
            final double kernelSharpness) {
        return new EvolvableXorGenome(
                hiddenNeurons,
                hiddenLayers,
                recurrentConnections,
                memoryCells,
                connectionDensity,
                hiddenActivation,
                outputActivation,
                inputRepresentation,
                lossFunction,
                learningSchedule,
                hebbianUpdate,
                gradientUpdate,
                normalization,
                kernelMemory,
                phaseEncoding,
                secondDerivativeEstimate,
                inputLearningRate,
                outputLearningRate,
                recurrentLearningRate,
                hebbianLearningRate,
                memoryLearningRate,
                momentum,
                weightDecay,
                normalizationStrength,
                activationSlope,
                errorClip,
                kernelSharpness);
    }

    private static int maxRecurrentConnections(final int hiddenNeurons) {
        return hiddenNeurons * hiddenNeurons;
    }

    private static int choose(final Random random, final int left, final int right) {
        return random.nextBoolean() ? left : right;
    }

    private static boolean choose(final Random random, final boolean left, final boolean right) {
        return random.nextBoolean() ? left : right;
    }

    private static <T> T choose(final Random random, final T left, final T right) {
        return random.nextBoolean() ? left : right;
    }

    private static double blend(final Random random, final double left, final double right) {
        if (random.nextDouble() < 0.4) {
            return random.nextBoolean() ? left : right;
        }
        double mix = random.nextDouble();
        return left * mix + right * (1.0 - mix);
    }

    private static double mutateRange(
            final Random random,
            final double value,
            final double intensity,
            final double min,
            final double max) {
        double scale = Math.max(0.0001, (max - min) * intensity);
        return clamp(value + random.nextGaussian() * scale, min, max);
    }

    private static double randomRange(final Random random, final double min, final double max) {
        return min + random.nextDouble() * (max - min);
    }

    private static <T extends Enum<T>> T randomEnum(final Random random, final Class<T> enumType) {
        T[] constants = enumType.getEnumConstants();
        return constants[random.nextInt(constants.length)];
    }

    private static <T extends Enum<T>> T randomDifferent(final Random random, final T current) {
        T[] constants = current.getDeclaringClass().getEnumConstants();
        T next = current;
        while (next == current && constants.length > 1) {
            next = constants[random.nextInt(constants.length)];
        }
        return next;
    }

    private static int clampInt(final int value, final int min, final int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clampFinite(final double value, final double min, final double max, final String label) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(label + " must be finite.");
        }
        return clamp(value, min, max);
    }

    private static double clamp(final double value, final double min, final double max) {
        return Math.max(min, Math.min(max, value));
    }
}
