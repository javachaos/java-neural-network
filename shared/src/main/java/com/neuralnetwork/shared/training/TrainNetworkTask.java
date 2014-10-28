package com.neuralnetwork.shared.training;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neuralnetwork.shared.network.INetwork;
import com.neuralnetwork.shared.values.Constants;
import com.neuralnetwork.shared.values.ErrorValue;

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
    		LoggerFactory.getLogger(TrainNetworkTask.class);

	/**
	 * Network reference.
	 */
	private INetwork network;

	/**
	 * Training stack.
	 */
	private TrainingStack trainStack;

	/**
	 * Executor service.
	 */
	private ExecutorService executorService;

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
	public final ErrorValue startTraining() {
		ErrorValue totalTrainError = new ErrorValue(0);
		while (!trainStack.getData().isEmpty()) {
			try {
				totalTrainError.updateValue(new ErrorValue(
				executorService.submit(
						new TrainNetworkSubTask(network, 
								trainStack.popSample())).get()));
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
