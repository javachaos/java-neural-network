package com.github.javachaos.javaneuralnetwork.examples;

/**
 * Activation functions available to evolved XOR learners.
 */
public enum XorActivationFunction {
    SIGMOID {
        @Override
        public double hidden(final double scaledInput) {
            return sigmoid(scaledInput);
        }

        @Override
        public double hiddenDerivative(final double scaledInput) {
            double value = sigmoid(scaledInput);
            return value * (1.0 - value);
        }

        @Override
        public double output(final double scaledInput) {
            return sigmoid(scaledInput);
        }

        @Override
        public double outputDerivative(final double scaledInput) {
            return hiddenDerivative(scaledInput);
        }
    },
    TANH {
        @Override
        public double hidden(final double scaledInput) {
            return Math.tanh(scaledInput);
        }

        @Override
        public double hiddenDerivative(final double scaledInput) {
            double value = Math.tanh(scaledInput);
            return 1.0 - value * value;
        }

        @Override
        public double output(final double scaledInput) {
            return (Math.tanh(scaledInput) + 1.0) * 0.5;
        }

        @Override
        public double outputDerivative(final double scaledInput) {
            return hiddenDerivative(scaledInput) * 0.5;
        }
    },
    RELU {
        @Override
        public double hidden(final double scaledInput) {
            return scaledInput > 0.0 ? scaledInput : 0.01 * scaledInput;
        }

        @Override
        public double hiddenDerivative(final double scaledInput) {
            return scaledInput > 0.0 ? 1.0 : 0.01;
        }

        @Override
        public double output(final double scaledInput) {
            return sigmoid(hidden(scaledInput));
        }

        @Override
        public double outputDerivative(final double scaledInput) {
            double activated = hidden(scaledInput);
            double probability = sigmoid(activated);
            return probability * (1.0 - probability) * hiddenDerivative(scaledInput);
        }
    },
    SINE {
        @Override
        public double hidden(final double scaledInput) {
            return Math.sin(scaledInput);
        }

        @Override
        public double hiddenDerivative(final double scaledInput) {
            return Math.cos(scaledInput);
        }

        @Override
        public double output(final double scaledInput) {
            return (Math.sin(scaledInput) + 1.0) * 0.5;
        }

        @Override
        public double outputDerivative(final double scaledInput) {
            return Math.cos(scaledInput) * 0.5;
        }
    },
    GAUSSIAN {
        @Override
        public double hidden(final double scaledInput) {
            return Math.exp(-scaledInput * scaledInput);
        }

        @Override
        public double hiddenDerivative(final double scaledInput) {
            return -2.0 * scaledInput * hidden(scaledInput);
        }

        @Override
        public double output(final double scaledInput) {
            return hidden(scaledInput);
        }

        @Override
        public double outputDerivative(final double scaledInput) {
            return hiddenDerivative(scaledInput);
        }
    },
    LINEAR {
        @Override
        public double hidden(final double scaledInput) {
            return scaledInput;
        }

        @Override
        public double hiddenDerivative(final double scaledInput) {
            return 1.0;
        }

        @Override
        public double output(final double scaledInput) {
            return sigmoid(scaledInput);
        }

        @Override
        public double outputDerivative(final double scaledInput) {
            double probability = sigmoid(scaledInput);
            return probability * (1.0 - probability);
        }
    };

    public abstract double hidden(double scaledInput);

    public abstract double hiddenDerivative(double scaledInput);

    public abstract double output(double scaledInput);

    public abstract double outputDerivative(double scaledInput);

    private static double sigmoid(final double value) {
        if (value > 40.0) {
            return 1.0;
        }
        if (value < -40.0) {
            return 0.0;
        }
        return 1.0 / (1.0 + Math.exp(-value));
    }
}
