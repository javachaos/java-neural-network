package com.neuralnetwork.core;

import java.io.*;
import java.security.SecureRandom;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neuralnetwork.shared.functions.SigmoidFunction;

/**
 * BackpropNetwork.
 *
 */
public final class BackpropagationNetwork implements Serializable {

    private static final transient Logger LOGGER =
            LoggerFactory.getLogger(BackpropagationNetwork.class);

    @Serial
    private static final long serialVersionUID = -8666581554984473793L;

    public enum TransferFunction {
        NONE,
        SIGMOID
    }

    /**
     * Transfer functions.
     *
     */
    static class TransferFunctions {

        private TransferFunctions() {}

        /**
         * Evaluate the transfer function transferFunction.
         * @param transferFunction
         *         the transfer fuction to use.
         *         
         * @param input
         *         the input data.
         *         
         * @return
         *         the result of applying the
         *         transfer function on the data.
         */
        public static double evaluate(
        		final TransferFunction transferFunction,
        		final double input) {
            switch (transferFunction) {
                case SIGMOID:
                    return new SigmoidFunction().activate(input);
                case NONE:
                default:
                    break;
            }
            return input;
        }

        /**
         * Evaluate the derivitive of the transfer function.
         * @param transferFunction
         *         the transfer fuction to use.
         *         
         * @param input
         *         the input data.
         *         
         * @return
         *         the derivitive of the transfer function.
         */
        public static double evaluateDerivitive(
                final TransferFunction transferFunction,
                final double input) {
            switch (transferFunction) {
                case SIGMOID:
                    return new SigmoidFunction().derivative(input);
                case NONE:
                default:
                    break;
            }
            return input;
        }
    }

    private int numLayers;
    private int inputSize;
    private int[] layerSize;
    private TransferFunction[] transferFunction;

    private double[][] layerOutput;
    private double[][] layerInput;
    private double[][] bias;
    private double[][] delta;
    private double[][] previousBiasDelta;
    private double[][][] weight;
    private double[][][] previousWeightDelta;
    private int iter = 0;
    private static final int UPDATE_EVERY = 1000;

    /**
     * Create and initialize new back-propagation network.
     *
     * @param layerSizes
     *         the array of layer sizes.
     *
     * @param transferFunctions
     *         the array of transfer functions for each layer.
     */
    public BackpropagationNetwork(final int[] layerSizes,
        final TransferFunction[] transferFunctions) {
        if (transferFunctions.length != layerSizes.length) {
            throw new IllegalArgumentException();
        }
        numLayers = layerSizes.length - 1;
        inputSize = layerSizes[0];
        layerSize = new int[numLayers];

        System.arraycopy(layerSizes, 1, layerSize, 0, numLayers);
        transferFunction = new TransferFunction[numLayers];
        System.arraycopy(transferFunctions, 1, transferFunction, 0, numLayers);

        bias = new double[numLayers][];
        previousBiasDelta = new double[numLayers][];
        delta = new double[numLayers][];
        layerOutput = new double[numLayers][];
        layerInput = new double[numLayers][];

        weight = new double[numLayers][][];
        previousWeightDelta = new double[numLayers][][];

        // Fill arrays.
        for (int l = 0; l < numLayers; l++) {
            bias[l] = new double[layerSize[l]];
            previousBiasDelta[l] = new double[layerSize[l]];
            delta[l] = new double[layerSize[l]];
            layerOutput[l] = new double[layerSize[l]];
            layerInput[l] = new double[layerSize[l]];

            weight[l] = new double[getLayer(l)][];
            previousWeightDelta[l] =
                    new double[getLayer(l)][];

            for (int i = 0; i < getLayer(l); i++) {
                weight[l][i] = new double[layerSize[l]];
                previousWeightDelta[l][i] = new double[layerSize[l]];
            }
        }

        // Init weights and bias.
        for (int l = 0; l < numLayers; l++) {
            Random rand = new SecureRandom();
            for (int j = 0; j < layerSize[l]; j++) {
                bias[l][j] = rand.nextGaussian();
                previousBiasDelta[l][j] = 0.0;
                layerOutput[l][j] = 0.0;
                layerInput[l][j] = 0.0;
                delta[l][j] = 0.0;
            }
            for (int i = 0; i < getLayer(l); i++) {
                for (int j = 0; j < layerSize[l]; j++) {
                    weight[l][i][j] = rand.nextGaussian();
                    previousWeightDelta[l][i][j] = 0.0;
                }
            }
        }
    }

    /**
     * Return the index to the layer.
     * 
     * @param l
     *         the layer index to transform.
     *         
     * @return
     *         the new index to the correct layer.
     */
    private int getLayer(final int l) {
        if (l == 0) {
            return inputSize;
        } else {
            return layerSize[l - 1];
        }
    }

    /**
     * Run the network.
     * 
     * @param input
     *         the input to run.
     *         
     * @return
     *         the result of running the network.
     */
    public double[] run(final double[] input) {
        double[] output;
        if (input.length != inputSize) {
            throw new IllegalArgumentException();
        }
        output = new double[layerSize[numLayers - 1]];
        // Run the network.
        for (int l = 0; l < numLayers; l++) {
            for (int j = 0; j < layerSize[l]; j++) {
                double sum = 0.0;
                for (int i = 0; i < getLayer(l); i++) {
                    sum += weight[l][i][j]
                            * getValue(l, i, input);
                }
                sum += bias[l][j];
                layerInput[l][j] = sum;
                layerOutput[l][j] =
                        TransferFunctions.evaluate(transferFunction[l], sum);
            }
        }
        if (layerSize[numLayers - 1] >= 0)
            System.arraycopy(layerOutput[numLayers - 1], 0, output, 0, layerSize[numLayers - 1]);

        return output;
    }

    /**
     * Get value from the network.
     * 
     * @param l
     *         the layer.
     *         
     * @param i
     *         the index of that layer.
     *         
     * @param input
     *         the input vector.
     *         
     * @return
     *         the value
     */
    private double getValue(final int l,
            final int i, final double[] input) {
        if (l == 0) {
            return input[i];
        } else {
            return layerOutput[l - 1][i];
        }
    }

    /**
     * Train on a vector of training samples.
     * 
     * @param trainingData
     *         the vector of training samples.
     *         
     * @param desiredData
     *         the vector of desired training samples.
     *         
     * @param trainingRate
     *         the training rate.
     *
     * @param desiredError
     *         the desired network error.
     *         
     * @return
     *         the final network error.
     */
    public double train(final double[][] trainingData,
            final double[][] desiredData,
            final double trainingRate,
            final double desiredError) {
        double error = 0.0;
        for (int i = 0; i < trainingData.length; i++) {
            do {
                error = train(trainingData[i],
                        desiredData[i], trainingRate, trainingRate);
                run(trainingData[i]);
            } while (error > desiredError);
        }
        return error;
    }

    /**
     * Train the neural network.
     * 
     * @param input
     *         the input to train.
     *         
     * @param desired
     *         the desired output.
     *         
     * @param trainingRate
     *         the trainingRate
     *         
     * @param momentum
     *         the momentum
     *         
     * @return
     *         the mse of the network after training.
     */
    public double train(final double[] input,
            final double[] desired, final double trainingRate,
            final double momentum) {
        if (input.length != inputSize
        ||  desired.length != layerSize[numLayers - 1]) {
            throw new IllegalArgumentException();
        }
        double error = 0.0;
        double[] output = run(input);
        error = backProp(output, desired, delta, error);
        updateWeights(trainingRate, momentum, input);
        updateBiases(trainingRate, momentum);

        if (iter++ % UPDATE_EVERY  == 0) {
            prettyPrint(input, output, error, iter);
        }
        return error;
    }

    private void updateBiases(double trainingRate, double momentum) {
        double biasDelta;
        for (int l = 0; l < numLayers; l++) {
            for (int i = 0; i < layerSize[l]; i++) {
                biasDelta = trainingRate * delta[l][i];
                bias[l][i] -= biasDelta + momentum * previousBiasDelta[l][i];
                previousBiasDelta[l][i] = biasDelta;
            }
        }
    }

    private void updateWeights(double trainingRate, double momentum, double[] input) {
        double weightDelta;
        // Update the weights and biases
        for (int l = 0; l < numLayers; l++) {
            for (int i = 0; i < getLayer(l); i++) {
                for (int j = 0; j < layerSize[l]; j++) {
                    weightDelta =
                            trainingRate * delta[l][j]
                                    * getValue(l, i, input);
                    weight[l][i][j] -=
                            weightDelta + momentum * previousWeightDelta[l][i][j];
                    previousWeightDelta[l][i][j] = weightDelta;
                }
            }
        }
    }

    private double backProp(double[] output, double[] desired, double[][] delta, double error) {
        double sum;
        // Back-propagation pass.
        for (int l = numLayers - 1; l >= 0; l--) {
            // Output layer
            if (l == numLayers - 1) {
                for (int k = 0; k < layerSize[l]; k++) {
                    delta[l][k] = output[k] - desired[k];
                    error += Math.pow(delta[l][k], 2);
                    delta[l][k] *= TransferFunctions.evaluateDerivitive(
                            transferFunction[l], layerInput[l][k]);
                }
            } else { // Hidden Layer
                for (int i = 0; i < layerSize[l]; i++) {
                    sum = 0.0;
                    for (int j = 0; j < layerSize[l + 1]; j++) {
                        sum += weight[l + 1][i][j] * delta[l + 1][j];
                    }
                    sum += TransferFunctions.evaluateDerivitive(
                            transferFunction[l], layerInput[l][i]);
                    delta[l][i] = sum;
                }
            }
        }
        return error;
    }

    /**
     * Clear the iteration counter.
     */
    public void clearIterationCounter() {
        iter = 0;
    }

    /**
     * Print results.
     *
     * @param input
     *         the input to be printed.
     *         
     * @param output
     *         the output to be printed.
     *         
     * @param error
     *         the error value.
     *         
     * @param i
     *         the interation number.
     *         
     */
    private void prettyPrint(final double[] input,
            final double[] output,
            final double error,
            final int i) {
        StringBuilder sb = new StringBuilder();
        StringBuilder ssb = new StringBuilder();
        for (double d : input) {
            ssb.append("[").append(d).append("] ");
        }
        for (double d : output) {
            sb.append("[").append(d).append("] ");
        }
        LOGGER.info(
                "Iteration {}:\n\t\tInput {}\n\t\tOutput {}\n\t\tError {}",
                i, ssb, sb, error);
    }

    /**
     * Save the neuralnet to file.
     *
     * @param fileName
     *         the file path to store the network as.
     *
     */
    public void saveNet(final String fileName) {
        try (FileOutputStream fileOut = new FileOutputStream(fileName);
             ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
            out.writeObject(this);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    /**
     * Load the neuralnet from file and
     * update all references in the current instance
     * of the class.
     *
     * @param fileName
     *         the filename of the file to load.
     *         
     * @return
     *         the reference to the loaded network.
     *
     */
    public BackpropagationNetwork loadNet(final String fileName) {
        BackpropagationNetwork net = null;
        try(FileInputStream fileIn = new FileInputStream(fileName);
            ObjectInputStream in = new ObjectInputStream(fileIn)) {
            net = (BackpropagationNetwork) in.readObject();
            this.bias = net.bias;
            this.delta = net.delta;
            this.inputSize = net.inputSize;
            this.iter = net.iter;
            this.layerInput = net.layerInput;
            this.layerOutput = net.layerOutput;
            this.layerSize = net.layerSize;
            this.numLayers = net.numLayers;
            this.previousBiasDelta = net.previousBiasDelta;
            this.previousWeightDelta = net.previousWeightDelta;
            this.weight = net.weight;
            this.transferFunction = net.transferFunction;
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.error(e.getMessage());
        }
        return net;
    }
}
