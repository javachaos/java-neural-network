package com.github.javachaos.javaneuralnetwork.shared.network;

import com.github.javachaos.javaneuralnetwork.shared.training.BackpropagationAlgorithm;
import com.github.javachaos.javaneuralnetwork.shared.training.TrainSample;

public class BackPropNetworkTrainer {

    private final Network networkUnderTrain;
    private boolean isTraining;

    public BackPropNetworkTrainer(final Network n) {
        this.networkUnderTrain = n;
    }

    /**
     * Trains this network on one training sample.
     *
     * @param trainingSample
     * 		a pair of 2 vectors used to train the network.
     * 		the first vector is the train vector
     * 		the second is the desired vector.
     *
     * @param expectedError
     *      the expected error rate to stop training at
     *
     * @return
     *      the Mean squared error value
     */
    public synchronized Double train(final TrainSample trainingSample,
                                     final Double expectedError) {
        Double errorValue;
        toggleTraining();
        BackpropagationAlgorithm algo =
                new BackpropagationAlgorithm(
                        trainingSample,
                        networkUnderTrain,
                        expectedError);
        errorValue = algo.compute();
        toggleTraining();
        return errorValue;
    }

    private void toggleTraining() {
        isTraining = !isTraining;
    }
}
