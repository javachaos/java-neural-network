package com.github.javachaos.javaneuralnetwork.examples;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Extensible problem definition consumed by the neuro-evolution engine.
 * Subclasses own their metadata, target function, and sampling policy.
 */
public abstract class NeuroEvolutionProblem {

    protected static final int DEFAULT_GRID_SIZE = 9;
    protected static final int DEFAULT_TRAINING_GRID_SIZE = 7;
    protected static final int DEFAULT_KERNEL_GRID_SIZE = 5;
    protected static final int HARD_TRAINING_GRID_SIZE = 13;
    protected static final int HARD_GENERALIZATION_GRID_SIZE = 21;
    protected static final int HARD_KERNEL_GRID_SIZE = 9;
    protected static final double TWO_PI = Math.PI * 2.0;

    private final String key;
    private final String name;
    private final String description;
    private final List<String> aliases;
    private final int inputDimensions;
    private final int outputDimensions;
    private final List<NeuroEvolutionSample> trainingSamples;
    private final List<NeuroEvolutionSample> generalizationSamples;
    private final List<NeuroEvolutionSample> jitterAnchors;
    private final List<double[]> kernelCenters;
    private final boolean classification;
    private final NeuroEvolutionVectorTargetFunction targetFunction;
    private final double[] trainingTargetMeans;
    private final double baselineTrainingMeanSquaredError;
    private final double baselineGeneralizationMeanSquaredError;

    protected NeuroEvolutionProblem(
            final String key,
            final String name,
            final String description,
            final List<String> aliases,
            final int inputDimensions,
            final List<NeuroEvolutionSample> trainingSamples,
            final List<NeuroEvolutionSample> generalizationSamples,
            final List<NeuroEvolutionSample> jitterAnchors,
            final List<double[]> kernelCenters,
            final boolean classification,
            final NeuroEvolutionTargetFunction targetFunction) {
        this(
                key,
                name,
                description,
                aliases,
                inputDimensions,
                1,
                trainingSamples,
                generalizationSamples,
                jitterAnchors,
                kernelCenters,
                classification,
                scalarTargetFunction(targetFunction));
    }

    protected NeuroEvolutionProblem(
            final String key,
            final String name,
            final String description,
            final List<String> aliases,
            final int inputDimensions,
            final int outputDimensions,
            final List<NeuroEvolutionSample> trainingSamples,
            final List<NeuroEvolutionSample> generalizationSamples,
            final List<NeuroEvolutionSample> jitterAnchors,
            final List<double[]> kernelCenters,
            final boolean classification,
            final NeuroEvolutionVectorTargetFunction targetFunction) {
        this.key = normalizedName(requireText(key, "Problem key"));
        this.name = requireText(name, "Problem name");
        this.description = requireText(description, "Problem description");
        this.aliases = copyAliases(aliases);
        if (inputDimensions < 1) {
            throw new IllegalArgumentException("Input dimensions must be positive.");
        }
        if (outputDimensions < 1) {
            throw new IllegalArgumentException("Output dimensions must be positive.");
        }
        this.inputDimensions = inputDimensions;
        this.outputDimensions = outputDimensions;
        this.targetFunction = Objects.requireNonNull(targetFunction, "Target function cannot be null.");
        this.trainingSamples = copySamples(trainingSamples, inputDimensions, outputDimensions, "Training samples");
        if (this.trainingSamples.isEmpty()) {
            throw new IllegalArgumentException("Training samples cannot be empty.");
        }
        this.generalizationSamples =
                copySamples(generalizationSamples, inputDimensions, outputDimensions, "Generalization samples");
        List<NeuroEvolutionSample> anchors =
                copySamples(jitterAnchors, inputDimensions, outputDimensions, "Jitter anchors");
        this.jitterAnchors = anchors.isEmpty() ? this.trainingSamples : anchors;
        List<double[]> centers = copyCenters(kernelCenters, inputDimensions);
        this.kernelCenters = centers.isEmpty() ? sampleInputs(this.trainingSamples) : centers;
        this.classification = classification;
        this.trainingTargetMeans = targetMeans(this.trainingSamples, outputDimensions);
        this.baselineTrainingMeanSquaredError =
                baselineMeanSquaredError(this.trainingSamples, this.trainingTargetMeans);
        this.baselineGeneralizationMeanSquaredError =
                baselineMeanSquaredError(this.generalizationSamples, this.trainingTargetMeans);
    }

    public final String key() {
        return key;
    }

    public final String name() {
        return name;
    }

    public final String description() {
        return description;
    }

    public final List<String> aliases() {
        return aliases;
    }

    public final int inputDimensions() {
        return inputDimensions;
    }

    public final int outputDimensions() {
        return outputDimensions;
    }

    public final List<NeuroEvolutionSample> trainingSamples() {
        return trainingSamples;
    }

    public final List<NeuroEvolutionSample> generalizationSamples() {
        return generalizationSamples;
    }

    public final List<NeuroEvolutionSample> jitterAnchors() {
        return jitterAnchors;
    }

    public final List<double[]> kernelCenters() {
        return copyCenters(kernelCenters, inputDimensions);
    }

    public final boolean classification() {
        return classification;
    }

    public final double baselineTrainingMeanSquaredError() {
        return baselineTrainingMeanSquaredError;
    }

    public final double baselineGeneralizationMeanSquaredError() {
        return baselineGeneralizationMeanSquaredError;
    }

    public final double[] trainingTargetMeans() {
        return trainingTargetMeans.clone();
    }

    public double complexityScale() {
        return Math.max(1.0, outputDimensions);
    }

    public String outputLabel(final int outputIndex) {
        if (outputIndex < 0 || outputIndex >= outputDimensions) {
            throw new IllegalArgumentException("Output index must be in the problem output range.");
        }
        return outputDimensions == 1 ? "Y" : "Y" + outputIndex;
    }

    public List<NeuroEvolutionOutputGroup> outputGroups() {
        return List.of(new NeuroEvolutionOutputGroup("All outputs", 0, outputDimensions));
    }

    public NeuroEvolutionFreeEnergyProfile freeEnergyProfile() {
        return NeuroEvolutionFreeEnergyProfile.disabled();
    }

    public boolean statefulSamples() {
        return false;
    }

    public boolean predictionMatches(final double[] targets, final double[] predictions) {
        double[] expected = validatedTargets(targets, outputDimensions, "Accuracy targets");
        double[] actual = validatedTargets(predictions, outputDimensions, "Accuracy predictions");
        if (outputDimensions == 1) {
            return (actual[0] >= 0.5) == (expected[0] >= 0.5);
        }

        double targetCutoff = targetCutoff(expected);
        double weakestPositivePrediction = Double.POSITIVE_INFINITY;
        double strongestNegativePrediction = Double.NEGATIVE_INFINITY;
        int positiveTargets = 0;
        for (int output = 0; output < expected.length; output++) {
            if (expected[output] >= targetCutoff) {
                positiveTargets++;
                weakestPositivePrediction = Math.min(weakestPositivePrediction, actual[output]);
            } else {
                strongestNegativePrediction = Math.max(strongestNegativePrediction, actual[output]);
            }
        }
        return positiveTargets > 0 && weakestPositivePrediction > strongestNegativePrediction;
    }

    public final boolean matches(final String candidateName) {
        String normalized = normalizedName(candidateName);
        if (key.equals(normalized) || normalizedName(name).equals(normalized)) {
            return true;
        }
        for (String alias : aliases) {
            if (normalizedName(alias).equals(normalized)) {
                return true;
            }
        }
        return false;
    }

    public final double target(final double[] input) {
        if (outputDimensions != 1) {
            throw new IllegalStateException("Problem has " + outputDimensions + " output channels, not one.");
        }
        return targetVector(input)[0];
    }

    public final double[] targetVector(final double[] input) {
        requireInput(input);
        return validatedTargets(targetFunction.target(input.clone()), outputDimensions, "Problem target function");
    }

    public final void requireInput(final double[] input) {
        Objects.requireNonNull(input, "Input cannot be null.");
        if (input.length != inputDimensions) {
            throw new IllegalArgumentException("Expected " + inputDimensions + " input dimensions.");
        }
        for (double value : input) {
            if (!Double.isFinite(value)) {
                throw new IllegalArgumentException("Input values must be finite.");
            }
        }
    }

    static String normalizedName(final String name) {
        return Objects.requireNonNull(name, "Problem name cannot be null.")
                .trim()
                .toLowerCase(Locale.ROOT)
                .replace('_', '-')
                .replace(' ', '-');
    }

    protected static List<NeuroEvolutionSample> twoInputBooleanSamples(final NeuroEvolutionTargetFunction target) {
        return List.of(
                sample(0.0, 0.0, target),
                sample(0.0, 1.0, target),
                sample(1.0, 0.0, target),
                sample(1.0, 1.0, target));
    }

    protected static List<NeuroEvolutionSample> gridSamples(
            final int gridSize,
            final NeuroEvolutionTargetFunction target) {
        if (gridSize < 2) {
            throw new IllegalArgumentException("Grid size must be at least two.");
        }
        List<NeuroEvolutionSample> samples = new ArrayList<>(gridSize * gridSize);
        double denominator = gridSize - 1.0;
        for (int row = 0; row < gridSize; row++) {
            double y = row / denominator;
            for (int column = 0; column < gridSize; column++) {
                double x = column / denominator;
                samples.add(sample(x, y, target));
            }
        }
        return List.copyOf(samples);
    }

    protected static List<NeuroEvolutionSample> lineSamples(
            final int sampleCount,
            final NeuroEvolutionTargetFunction target) {
        if (sampleCount < 2) {
            throw new IllegalArgumentException("Sample count must be at least two.");
        }
        List<NeuroEvolutionSample> samples = new ArrayList<>(sampleCount);
        double denominator = sampleCount - 1.0;
        for (int i = 0; i < sampleCount; i++) {
            samples.add(sample(new double[] {i / denominator}, target));
        }
        return List.copyOf(samples);
    }

    protected static List<NeuroEvolutionSample> vectorGridSamples(
            final int gridSize,
            final NeuroEvolutionVectorTargetFunction target) {
        if (gridSize < 2) {
            throw new IllegalArgumentException("Grid size must be at least two.");
        }
        List<NeuroEvolutionSample> samples = new ArrayList<>(gridSize * gridSize);
        double denominator = gridSize - 1.0;
        for (int row = 0; row < gridSize; row++) {
            double y = row / denominator;
            for (int column = 0; column < gridSize; column++) {
                double x = column / denominator;
                samples.add(vectorSample(new double[] {x, y}, target));
            }
        }
        return List.copyOf(samples);
    }

    protected static List<NeuroEvolutionSample> vectorLineSamples(
            final int sampleCount,
            final NeuroEvolutionVectorTargetFunction target) {
        if (sampleCount < 2) {
            throw new IllegalArgumentException("Sample count must be at least two.");
        }
        List<NeuroEvolutionSample> samples = new ArrayList<>(sampleCount);
        double denominator = sampleCount - 1.0;
        for (int i = 0; i < sampleCount; i++) {
            samples.add(vectorSample(new double[] {i / denominator}, target));
        }
        return List.copyOf(samples);
    }

    protected static List<double[]> gridInputs(final int gridSize) {
        List<double[]> inputs = new ArrayList<>(gridSize * gridSize);
        double denominator = gridSize - 1.0;
        for (int row = 0; row < gridSize; row++) {
            double y = row / denominator;
            for (int column = 0; column < gridSize; column++) {
                double x = column / denominator;
                inputs.add(new double[] {x, y});
            }
        }
        return List.copyOf(inputs);
    }

    protected static List<double[]> lineInputs(final int sampleCount) {
        if (sampleCount < 2) {
            throw new IllegalArgumentException("Sample count must be at least two.");
        }
        List<double[]> inputs = new ArrayList<>(sampleCount);
        double denominator = sampleCount - 1.0;
        for (int i = 0; i < sampleCount; i++) {
            inputs.add(new double[] {i / denominator});
        }
        return List.copyOf(inputs);
    }

    protected static List<double[]> cornerInputs() {
        return List.of(
                new double[] {0.0, 0.0},
                new double[] {0.0, 1.0},
                new double[] {1.0, 0.0},
                new double[] {1.0, 1.0});
    }

    protected static NeuroEvolutionSample sample(
            final double x,
            final double y,
            final NeuroEvolutionTargetFunction target) {
        double[] input = {x, y};
        return new NeuroEvolutionSample(input, target.target(input));
    }

    protected static NeuroEvolutionSample sample(
            final double[] input,
            final NeuroEvolutionTargetFunction target) {
        return new NeuroEvolutionSample(input, target.target(input));
    }

    protected static NeuroEvolutionSample vectorSample(
            final double[] input,
            final NeuroEvolutionVectorTargetFunction target) {
        return new NeuroEvolutionSample(input, target.target(input));
    }

    protected static double clamp01(final double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    protected static double distanceFromCenter(final double[] input) {
        double dx = input[0] - 0.5;
        double dy = input[1] - 0.5;
        return Math.sqrt(dx * dx + dy * dy);
    }

    protected static int cellIndex(final double value, final int cells) {
        return Math.min(cells - 1, Math.max(0, (int) (clamp01(value) * cells)));
    }

    protected static double gaussian(
            final double x,
            final double y,
            final double centerX,
            final double centerY,
            final double width) {
        double dx = x - centerX;
        double dy = y - centerY;
        return Math.exp(-(dx * dx + dy * dy) / Math.max(1.0e-6, width));
    }

    private static String requireText(final String value, final String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " cannot be blank.");
        }
        return value;
    }

    private static List<String> copyAliases(final List<String> aliases) {
        Objects.requireNonNull(aliases, "Problem aliases cannot be null.");
        List<String> copy = new ArrayList<>(aliases.size());
        for (String alias : aliases) {
            copy.add(requireText(alias, "Problem alias"));
        }
        return List.copyOf(copy);
    }

    private static List<NeuroEvolutionSample> copySamples(
            final List<NeuroEvolutionSample> samples,
            final int inputDimensions,
            final int outputDimensions,
            final String label) {
        Objects.requireNonNull(samples, label + " cannot be null.");
        List<NeuroEvolutionSample> copy = new ArrayList<>(samples.size());
        for (NeuroEvolutionSample sample : samples) {
            Objects.requireNonNull(sample, label + " cannot contain null samples.");
            if (sample.input().length != inputDimensions) {
                throw new IllegalArgumentException(label + " must match the problem input dimensions.");
            }
            if (sample.outputDimensions() != outputDimensions) {
                throw new IllegalArgumentException(label + " must match the problem output dimensions.");
            }
            copy.add(new NeuroEvolutionSample(sample.input(), sample.targets()));
        }
        return List.copyOf(copy);
    }

    private static List<double[]> copyCenters(final List<double[]> centers, final int inputDimensions) {
        Objects.requireNonNull(centers, "Kernel centers cannot be null.");
        List<double[]> copy = new ArrayList<>(centers.size());
        for (double[] center : centers) {
            Objects.requireNonNull(center, "Kernel centers cannot contain null entries.");
            if (center.length != inputDimensions) {
                throw new IllegalArgumentException("Kernel centers must match the problem input dimensions.");
            }
            for (double value : center) {
                if (!Double.isFinite(value)) {
                    throw new IllegalArgumentException("Kernel centers must be finite.");
                }
            }
            copy.add(center.clone());
        }
        return List.copyOf(copy);
    }

    private static List<double[]> sampleInputs(final List<NeuroEvolutionSample> samples) {
        List<double[]> inputs = new ArrayList<>(samples.size());
        for (NeuroEvolutionSample sample : samples) {
            inputs.add(sample.input());
        }
        return List.copyOf(inputs);
    }

    private static double[] targetMeans(
            final List<NeuroEvolutionSample> samples,
            final int outputDimensions) {
        double[] means = new double[outputDimensions];
        if (samples.isEmpty()) {
            return means;
        }
        for (NeuroEvolutionSample sample : samples) {
            double[] targets = sample.targets();
            for (int output = 0; output < outputDimensions; output++) {
                means[output] += targets[output];
            }
        }
        for (int output = 0; output < outputDimensions; output++) {
            means[output] /= samples.size();
        }
        return means;
    }

    private static double baselineMeanSquaredError(
            final List<NeuroEvolutionSample> samples,
            final double[] means) {
        if (samples.isEmpty() || means.length == 0) {
            return 0.0;
        }
        double total = 0.0;
        int count = 0;
        for (NeuroEvolutionSample sample : samples) {
            double[] targets = sample.targets();
            for (int output = 0; output < means.length; output++) {
                double error = targets[output] - means[output];
                total += error * error;
                count++;
            }
        }
        return count == 0 ? 0.0 : total / count;
    }

    private static NeuroEvolutionVectorTargetFunction scalarTargetFunction(
            final NeuroEvolutionTargetFunction targetFunction) {
        Objects.requireNonNull(targetFunction, "Target function cannot be null.");
        return input -> new double[] {targetFunction.target(input)};
    }

    private static double[] validatedTargets(
            final double[] targets,
            final int outputDimensions,
            final String label) {
        Objects.requireNonNull(targets, label + " cannot return null.");
        if (targets.length != outputDimensions) {
            throw new IllegalArgumentException(label + " must return " + outputDimensions + " target channels.");
        }
        double[] copy = targets.clone();
        for (double target : copy) {
            if (!Double.isFinite(target) || target < 0.0 || target > 1.0) {
                throw new IllegalArgumentException(label + " must return finite values in [0, 1].");
            }
        }
        return copy;
    }

    private static double targetCutoff(final double[] targets) {
        double max = Double.NEGATIVE_INFINITY;
        for (double target : targets) {
            max = Math.max(max, target);
        }
        return Math.max(1.0e-9, max * 0.5);
    }
}
