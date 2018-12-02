package com.neuralnetwork.core;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.SecureRandom;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neuralnetwork.shared.functions.SigmoidFunction;

/**
 * BackpropNetwork.
 * @author alfred
 *
 */
public final class BackpropagationNetwork implements Serializable {


    /**
     * Logger instance.
     */
    private static final transient Logger LOGGER =
            LoggerFactory.getLogger(BackpropagationNetwork.class);

    /**
     * Serial Version ID.
     */
    private static final long serialVersionUID = -8666581554984473793L;

    /**
     * TransferFunction Enum.
     * @author alfred
     *
     */
    public enum TransferFunction {
    	
        /**
         * No transfer function.
         */
        None,

        /**
         * Sigmoid transfer function.
         */
        Sigmoid
    }

    /**
     * Transfer functions.
     * @author alfred
     *
     */
    static class TransferFunctions {

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
                case Sigmoid:
                    return new SigmoidFunction().activate(input);
                case None:
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
                case Sigmoid:
                    return new SigmoidFunction().derivative(input);
                case None:
                default:
                    break;
            }
            return input;
        }
    }


    /**
     * Layer count.
     */
    private int numLayers;

    /**
     * Input vector length.
     */
    private int inputSize;

    /**
     * Layer size for each layer.
     */
    private int[] layerSize;

    /**
     * Transfer function for each layer.
     */
    private TransferFunction[] transferFunction;

    /**
     * LayerOutput values.
     */
    private double[][] layerOutput;

    /**
     * Layer Input values.
     */
    private double[][] layerInput;

    /**
     * Bias values.
     */
    private double[][] bias;

    /**
     * Delta values.
     */
    private double[][] delta;

    /**
     * Previous bias delta values.
     */
    private double[][] previousBiasDelta;

    /**
     * Weight matrix.
     */
    private double[][][] weight;

    /**
     * Previous weightDelta matrix.
     */
    private double[][][] previousWeightDelta;

    /**
     * Random generator.
     */
    private transient Random rand = new SecureRandom();

    /**
     * iteration counter.
     */
    private int iter = 0;

    /**
     * How many times to run the network before printing output.
     */
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

        for (int i = 0; i < numLayers; i++) {
            layerSize[i] = layerSizes[i + 1];
        }

        transferFunction = new TransferFunction[numLayers];

        for (int i = 0; i < numLayers; i++) {
            transferFunction[i] = transferFunctions[i + 1];
        }

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
        for (int i = 0; i < layerSize[numLayers - 1]; i++) {
            output[i] = layerOutput[numLayers - 1][i];
        }

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
     * @param momentum
     *         the momentum to avoid local minima.
     *         
     * @param desiredError
     *         the desired network error.
     *         
     * @return
     *         the final network error.
     */
    public double train(final double[][] trainingData,
            final double[][] desiredData,
            final double trainingRate, final double momentum,
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

        double error = 0.0, sum = 0.0, weightDelta = 0.0, biasDelta = 0.0;

        // Forward pass.
        double[] output = run(input);

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

        for (int l = 0; l < numLayers; l++) {
            for (int i = 0; i < layerSize[l]; i++) {
                biasDelta = trainingRate * delta[l][i];
                bias[l][i] -= biasDelta + momentum * previousBiasDelta[l][i];
                previousBiasDelta[l][i] = biasDelta;
            }
        }

        if (iter++ % UPDATE_EVERY  == 0) {
            prettyPrint(input, output, error, iter);
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
        StringBuffer sb = new StringBuffer();
        StringBuffer ssb = new StringBuffer();
        for (double d : input) {
            ssb.append("[" + d + "] ");
        }
        for (double d : output) {
            sb.append("[" + d + "] ");
        }
        LOGGER.info("Iteration " + i + ":\n\t\tInput "
                + ssb.toString() + "\n\t\tOutput "
                + sb.toString() + "\n\t\tError " + error);
        sb = new StringBuffer();
        ssb = new StringBuffer();
    }

    /**
     * Save the neuralnet to file.
     *
     * @param fileName
     *         the file path to store the network as.
     *         
     * @return
     *         the file path
     */
    public String saveNet(final String fileName) {
        FileOutputStream fileOut;
        try {
            fileOut = new FileOutputStream(fileName);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(this);
            out.close();
            fileOut.close();
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
        return fileName;
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
        try {
            FileInputStream fileIn = new FileInputStream(fileName);
            ObjectInputStream in = new ObjectInputStream(fileIn);
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
            in.close();
            fileIn.close();
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        } catch (ClassNotFoundException e) {
            LOGGER.error(e.getMessage());
        }
        return net;
    }
}
