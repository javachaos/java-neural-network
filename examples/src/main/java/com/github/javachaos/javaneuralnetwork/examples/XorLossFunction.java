package com.github.javachaos.javaneuralnetwork.examples;

/**
 * Loss surfaces that evolved learners may train against.
 */
public enum XorLossFunction {
    MEAN_SQUARED {
        @Override
        public double loss(final double target, final double prediction) {
            double error = target - prediction;
            return error * error;
        }

        @Override
        public double learningSignal(final double target, final double prediction) {
            return target - prediction;
        }
    },
    ABSOLUTE {
        @Override
        public double loss(final double target, final double prediction) {
            return Math.abs(target - prediction);
        }

        @Override
        public double learningSignal(final double target, final double prediction) {
            return Math.signum(target - prediction);
        }
    },
    CROSS_ENTROPY {
        @Override
        public double loss(final double target, final double prediction) {
            double clipped = clipProbability(prediction);
            return -target * Math.log(clipped) - (1.0 - target) * Math.log(1.0 - clipped);
        }

        @Override
        public double learningSignal(final double target, final double prediction) {
            double clipped = clipProbability(prediction);
            return (target - clipped) / Math.max(1.0e-6, clipped * (1.0 - clipped));
        }
    },
    HUBER {
        @Override
        public double loss(final double target, final double prediction) {
            double error = Math.abs(target - prediction);
            if (error <= 0.25) {
                return 0.5 * error * error;
            }
            return 0.25 * (error - 0.125);
        }

        @Override
        public double learningSignal(final double target, final double prediction) {
            double error = target - prediction;
            return Math.abs(error) <= 0.25 ? error : Math.copySign(0.25, error);
        }
    };

    public abstract double loss(double target, double prediction);

    public abstract double learningSignal(double target, double prediction);

    private static double clipProbability(final double value) {
        return Math.max(1.0e-6, Math.min(1.0 - 1.0e-6, value));
    }
}
