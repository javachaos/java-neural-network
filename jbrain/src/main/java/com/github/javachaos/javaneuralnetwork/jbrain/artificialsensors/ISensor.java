package com.github.javachaos.javaneuralnetwork.jbrain.artificialsensors;


/**
 * Sensor interface.
 * @author fred
 * @param <E>
 * 		return type for the data of this sensor.
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
