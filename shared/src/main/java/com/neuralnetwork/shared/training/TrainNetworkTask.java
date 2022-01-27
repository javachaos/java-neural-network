package com.neuralnetwork.shared.training;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neuralnetwork.shared.network.INetwork;
import com.neuralnetwork.shared.values.Constants;

/**
 * 
 * @author alfred
 *
 */
public class TrainNetworkTask {
//TODO test
    /**
     * Logger instance.
     */
    private static final Logger LOGGER = 
    		LoggerFactory.getLogger(TrainNetworkTask.class);

	/**
	 * Network reference.
	 */
	private final INetwork network;

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
	public TrainNetworkTask(final INetwork net, final TrainingStack ts) {
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
				executorService.submit(
						new TrainNetworkSubTask(network, 
								trainStack.popSample())).get();
			} catch (InterruptedException | ExecutionException e) {
				LOGGER.error(e.getMessage());
			}
		}
		try {
			executorService.awaitTermination(
					Constants.TRAIN_TIMEOUT, TimeUnit.SECONDS);
			executorService.shutdown();
		} catch (InterruptedException e) {
			LOGGER.error(e.getMessage());
		}
		return totalTrainError;
	}

}
