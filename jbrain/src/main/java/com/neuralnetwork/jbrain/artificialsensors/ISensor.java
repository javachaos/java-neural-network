package com.neuralnetwork.jbrain.artificialsensors;


/**
 * Sensor interface.
 * @author fred
 *
 */
public interface ISensor<E> {

	/**
	 * Get the data from this ISensor.
	 * 
	 * @return
	 * 		the raw data from this ISensor.
	 */
	E getData();
}
