package com.github.javachaos.javaneuralnetwork.examples;

/**
 * Learning-rate schedules available to evolved learners.
 */
public enum XorLearningSchedule {
    CONSTANT {
        @Override
        public double scale(final int epoch, final int maxEpochs) {
            return 1.0;
        }
    },
    INVERSE_TIME {
        @Override
        public double scale(final int epoch, final int maxEpochs) {
            return 1.0 / (1.0 + 0.01 * epoch);
        }
    },
    COSINE_DECAY {
        @Override
        public double scale(final int epoch, final int maxEpochs) {
            double progress = Math.min(1.0, epoch / (double) Math.max(1, maxEpochs));
            return 0.1 + 0.9 * (0.5 + 0.5 * Math.cos(Math.PI * progress));
        }
    },
    STEP_DECAY {
        @Override
        public double scale(final int epoch, final int maxEpochs) {
            int interval = Math.max(1, maxEpochs / 4);
            return Math.pow(0.5, epoch / interval);
        }
    };

    public abstract double scale(int epoch, int maxEpochs);
}
