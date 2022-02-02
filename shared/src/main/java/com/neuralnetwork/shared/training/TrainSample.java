package com.neuralnetwork.shared.training;

import java.util.List;

/**
 * Represents a single training sample of inputs and outputs
 */
public class TrainSample {

    private final List<Double> outputs;
    private final List<Double> inputs;

    public TrainSample(final List<Double> inputVector, final List<Double> outputVector) {
        if (inputVector == null || outputVector == null)
            throw new IllegalArgumentException("Inputs should not be null.");
        if (inputVector.isEmpty() || outputVector.isEmpty())
            throw new IllegalArgumentException("Input vectors should not be empty.");
        if (inputVector.size() != outputVector.size())
            throw new IllegalArgumentException("Input vectors should be the same length.");
        this.outputs = outputVector;
        this.inputs = inputVector;
    }

    public List<Double> getOutputs() {
        return outputs;
    }
    public List<Double> getInputs() { return inputs; }
}
