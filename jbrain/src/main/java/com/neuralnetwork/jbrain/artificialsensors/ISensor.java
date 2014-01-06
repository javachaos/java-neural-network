package com.neuralnetwork.jbrain.artificialsensors;

/**
 * Sensor interface.
 * @author fred
 *
 */
public interface ISensor {

	/**
	 * Get the data from this ISensor.
	 * 
	 * @return
	 * 		the raw data from this ISensor.
	 */
	double[] getData();
}
