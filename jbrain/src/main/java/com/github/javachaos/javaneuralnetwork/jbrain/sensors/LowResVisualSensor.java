package com.github.javachaos.javaneuralnetwork.jbrain.sensors;

import java.util.ArrayList;

import com.github.javachaos.javaneuralnetwork.jbrain.artificialsensors.ISensor;
import com.github.javachaos.javaneuralnetwork.jbrain.sensors.camera.RGBData;

/**
 * Low resolution visual sensor.
 * @author fred
 *
 */
public class LowResVisualSensor implements ISensor<ArrayList<RGBData>> {

	/**
	 * Default width resolution.
	 */
	private int resWidth = 176;
	
	/**
	 * Default height resolution.
	 */
	private int resHeight = 144;

	/**
	 * Construct a low-res visual sensor
	 * using specified resolution values.
	 * 
	 * NOTE: Max resolution of 100 pixels per axis.
	 * 
	 * @param width
	 * 		the width of the visual resolution
	 * .
	 * @param height
	 * 		the height of the visual resolution.
	 */
	public LowResVisualSensor(int width, int height) {

		/**
		 * The maximum image resolution.
		 */
		int maxResolution = 100;
		if (width > maxResolution || width < 0)
			throw new IllegalArgumentException("Width out of range.");
		if (height > maxResolution || height < 0)
			throw new IllegalArgumentException("Height out of range.");

		this.setResWidth(width);
		this.setResHeight(height);
	
	}
	
	/**
	 * Construct a low-res visual sensor using
	 * default resolution values.
	 */
	public LowResVisualSensor() {
	}
	
	@Override
	public ArrayList<RGBData> getData() {
		return new ArrayList<>();
	}

	public int getResWidth() {
		return resWidth;
	}

	public void setResWidth(int resWidth) {
		this.resWidth = resWidth;
	}

	public int getResHeight() {
		return resHeight;
	}

	public void setResHeight(int resHeight) {
		this.resHeight = resHeight;
	}

}
