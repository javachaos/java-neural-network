package com.github.javachaos.javaneuralnetwork.shared.training;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.github.javachaos.javaneuralnetwork.shared.network.BackPropNetworkTrainer;
import com.github.javachaos.javaneuralnetwork.shared.network.Network;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 
 * @author alfred
 *
 */
public class TrainNetworkTask {
    /**
     * Logger instance.
     */
    private static final Logger LOGGER =
    		LogManager.getLogger(TrainNetworkTask.class);

	/**
	 * Network reference.
	 */
	private final Network network;

	/**
	 * Training stack.
	 */
	private final TrainingStack trainStack;

	/**
	 * Executor service.
	 */
	private final ExecutorService executorService;

	/**
	 * Create a Train network task.
	 * @param net
	 * 		the network to train.
	 * @param ts
	 * 		the training data.
	 */
	public TrainNetworkTask(final Network net, final TrainingStack ts) {
		this.network = net;
		this.trainStack = ts;
		executorService = Executors.newSingleThreadExecutor();
	}

	/**
	 * Start training on multiple threads.
	 * 
	 * @return
	 * 		total error value
	 */
	public final Double startTraining() {
		double totalTrainError = 0.0;
		while (!trainStack.getData().isEmpty()) {
			try {
				totalTrainError +=
				executorService.submit(() -> new BackPropNetworkTrainer(network)
						.train(new TrainSample(
								trainStack.popSample(), trainStack.popSample()), 0.0001)).get();
				if (executorService.awaitTermination(
						10, TimeUnit.SECONDS)) {
					LOGGER.debug("TRAINING COMPLETE!");
				} else {
					LOGGER.debug("TRAINING TIMED OUT.");
				}
				executorService.shutdown();
			} catch (ExecutionException e) {
				LOGGER.error(e.getMessage());
			} catch (InterruptedException e1) {
				LOGGER.error(e1.getMessage());
				Thread.currentThread().interrupt();
			}
		}
		return totalTrainError;
	}

}
