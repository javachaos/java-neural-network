package com.github.javachaos.javaneuralnetwork.examples;

import java.util.ArrayList;
import java.util.List;

/**
 * Fixed-window next-token benchmark for text-shaped neuro-evolution pressure.
 */
public final class TextTrainingMettleProblem extends NeuroEvolutionProblem {

    private static final String KEY = "text-training-mettle";
    private static final int CONTEXT_LENGTH = 5;
    private static final int VOCAB_SIZE = 9;
    private static final int PAD = 0;
    private static final int THE = 1;
    private static final int A = 2;
    private static final int RED = 3;
    private static final int BLUE = 4;
    private static final int CUBE = 5;
    private static final int SPHERE = 6;
    private static final int ROLLS = 7;
    private static final int GLOWS = 8;
    private static final String DESCRIPTION = "Text training mettle is a tiny next-token language-model benchmark. "
            + "The five raw inputs are a scrambled normalized token context, and the nine outputs are a next-token "
            + "probability distribution over PAD, THE, A, RED, BLUE, CUBE, SPHERE, ROLLS, and GLOWS. The scrambled token "
            + "codes avoid giving the learner a simple ordinal shortcut. The grammar includes ambiguous next-token "
            + "choices, long-range color/object agreement, and an exception that flips the verb when an earlier "
            + "determiner changes, so it pressures sequence memory, categorical prediction, and transformer-like "
            + "context use.";
    private static final String[] TOKEN_LABELS = {
            "PAD", "THE", "A", "RED", "BLUE", "CUBE", "SPHERE", "ROLLS", "GLOWS"
    };
    private static final double[] TOKEN_CODES = {
            0.00, 0.82, 0.17, 0.61, 0.34, 0.95, 0.08, 0.48, 0.73
    };

    public TextTrainingMettleProblem(final int gridSize) {
        super(
                KEY,
                "Text training mettle",
                DESCRIPTION,
                List.of("text", "text-mettle", "language-mettle", "next-token"),
                CONTEXT_LENGTH,
                VOCAB_SIZE,
                buildTrainingSamples(),
                buildGeneralizationSamples(Math.max(gridSize, DEFAULT_GRID_SIZE)),
                buildJitterSamples(),
                buildKernelCenters(),
                true,
                TextTrainingMettleProblem::targetValue);
    }

    public double[] encodeTokenContext(final int... tokens) {
        return context(tokens);
    }

    @Override
    public String outputLabel(final int outputIndex) {
        if (outputIndex < 0 || outputIndex >= TOKEN_LABELS.length) {
            throw new IllegalArgumentException("Output index must be in the vocabulary range.");
        }
        return TOKEN_LABELS[outputIndex];
    }

    private static List<NeuroEvolutionSample> buildTrainingSamples() {
        List<NeuroEvolutionSample> samples = new ArrayList<>();
        for (int determiner : determiners()) {
            for (int color : colors()) {
                for (int object : objects()) {
                    addSentenceSamples(samples, determiner, color, object);
                }
            }
        }
        return List.copyOf(samples);
    }

    private static List<NeuroEvolutionSample> buildGeneralizationSamples(final int gridSize) {
        List<NeuroEvolutionSample> samples = new ArrayList<>(buildTrainingSamples());
        int extraSamples = Math.max(24, gridSize * 2);
        for (int i = 0; i < extraSamples; i++) {
            int determiner = tokenChoice(i, 0, determiners());
            int color = tokenChoice(i, 1, colors());
            int object = tokenChoice(i, 2, objects());
            int verb = verbFor(determiner, color, object);
            samples.add(sample(context(PAD, determiner, color, object, verb)));
            samples.add(sample(context(determiner, color, object, verb, PAD)));
        }
        return List.copyOf(samples);
    }

    private static List<NeuroEvolutionSample> buildJitterSamples() {
        return List.of(
                sample(context(PAD, PAD, PAD, PAD, PAD)),
                sample(context(PAD, PAD, PAD, THE, RED)),
                sample(context(PAD, THE, RED, CUBE, GLOWS)),
                sample(context(PAD, A, BLUE, CUBE, GLOWS)),
                sample(context(THE, BLUE, SPHERE, GLOWS, PAD)));
    }

    private static List<double[]> buildKernelCenters() {
        return List.of(
                context(PAD, PAD, PAD, PAD, PAD),
                context(PAD, PAD, PAD, THE, RED),
                context(PAD, PAD, A, BLUE, CUBE),
                context(PAD, THE, RED, SPHERE, ROLLS),
                context(PAD, A, BLUE, CUBE, GLOWS),
                context(THE, BLUE, SPHERE, GLOWS, PAD));
    }

    private static void addSentenceSamples(
            final List<NeuroEvolutionSample> samples,
            final int determiner,
            final int color,
            final int object) {
        int[] sentence = {determiner, color, object, verbFor(determiner, color, object), PAD};
        int[] previous = {PAD, PAD, PAD, PAD, PAD};
        for (int next : sentence) {
            samples.add(sample(previous));
            shiftAppend(previous, next);
        }
    }

    private static NeuroEvolutionSample sample(final int[] tokens) {
        return sample(context(tokens));
    }

    private static NeuroEvolutionSample sample(final double[] input) {
        return vectorSample(input, TextTrainingMettleProblem::targetValue);
    }

    private static double[] targetValue(final double[] input) {
        int[] tokens = quantizedTokens(input);
        int start = firstNonPad(tokens);
        if (start == tokens.length) {
            return distribution(THE, A);
        }
        int length = tokens.length - start;
        if (length == 1 && isDeterminer(tokens[start])) {
            return distribution(RED, BLUE);
        }
        if (length == 2 && isDeterminer(tokens[start]) && isColor(tokens[start + 1])) {
            return distribution(CUBE, SPHERE);
        }
        if (length == 3
                && isDeterminer(tokens[start])
                && isColor(tokens[start + 1])
                && isObject(tokens[start + 2])) {
            return oneHot(verbFor(tokens[start], tokens[start + 1], tokens[start + 2]));
        }
        return oneHot(PAD);
    }

    private static int verbFor(final int determiner, final int color, final int object) {
        boolean glows = (color == RED && object == CUBE) || (color == BLUE && object == SPHERE);
        if (determiner == A && color == BLUE) {
            glows = !glows;
        }
        return glows ? GLOWS : ROLLS;
    }

    private static double[] distribution(final int firstToken, final int secondToken) {
        double[] targets = new double[VOCAB_SIZE];
        targets[firstToken] = 0.5;
        targets[secondToken] = 0.5;
        return targets;
    }

    private static double[] oneHot(final int token) {
        double[] targets = new double[VOCAB_SIZE];
        targets[token] = 1.0;
        return targets;
    }

    private static double[] context(final int... tokens) {
        if (tokens.length != CONTEXT_LENGTH) {
            throw new IllegalArgumentException("Text context must contain " + CONTEXT_LENGTH + " tokens.");
        }
        double[] input = new double[CONTEXT_LENGTH];
        for (int i = 0; i < tokens.length; i++) {
            input[i] = tokenCode(tokens[i]);
        }
        return input;
    }

    private static int[] quantizedTokens(final double[] input) {
        int[] tokens = new int[input.length];
        for (int i = 0; i < input.length; i++) {
            tokens[i] = nearestToken(clamp01(input[i]));
        }
        return tokens;
    }

    private static int firstNonPad(final int[] tokens) {
        int index = 0;
        while (index < tokens.length && tokens[index] == PAD) {
            index++;
        }
        return index;
    }

    private static void shiftAppend(final int[] tokens, final int token) {
        System.arraycopy(tokens, 1, tokens, 0, tokens.length - 1);
        tokens[tokens.length - 1] = token;
    }

    private static int tokenChoice(final int index, final int salt, final int[] choices) {
        return choices[Math.floorMod(index * 31 + salt * 17, choices.length)];
    }

    private static int[] determiners() {
        return new int[] {THE, A};
    }

    private static int[] colors() {
        return new int[] {RED, BLUE};
    }

    private static int[] objects() {
        return new int[] {CUBE, SPHERE};
    }

    private static boolean isDeterminer(final int token) {
        return token == THE || token == A;
    }

    private static boolean isColor(final int token) {
        return token == RED || token == BLUE;
    }

    private static boolean isObject(final int token) {
        return token == CUBE || token == SPHERE;
    }

    private static double tokenCode(final int token) {
        if (token < PAD || token >= VOCAB_SIZE) {
            throw new IllegalArgumentException("Token must be in the text vocabulary range.");
        }
        return TOKEN_CODES[token];
    }

    private static int nearestToken(final double value) {
        int nearest = PAD;
        double bestDistance = Double.POSITIVE_INFINITY;
        for (int token = PAD; token < TOKEN_CODES.length; token++) {
            double distance = Math.abs(value - TOKEN_CODES[token]);
            if (distance < bestDistance) {
                bestDistance = distance;
                nearest = token;
            }
        }
        return nearest;
    }
}
