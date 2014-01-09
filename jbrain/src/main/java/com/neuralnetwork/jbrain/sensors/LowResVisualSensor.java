package com.neuralnetwork.jbrain.sensors;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.util.ArrayList;

import com.github.sarxos.webcam.Webcam;
import com.neuralnetwork.jbrain.artificialsensors.ISensor;
import com.neuralnetwork.jbrain.sensors.camera.RGBData;

/**
 * Low resolution visual sensor.
 * @author fred
 *
 */
public class LowResVisualSensor implements ISensor<ArrayList<RGBData>> {

	/**
	 * Default width resolution.
	 */
	private int res_w = 176;
	
	/**
	 * Default height resolution.
	 */
	private int res_h = 144;
	
	/**
	 * The maximum image resolution.
	 */
	private final int MAX_RES = 100;
	
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
		
		if (width > MAX_RES || width < 0)
			throw new IllegalArgumentException("Width out of range.");
		if (width > MAX_RES || width < 0)
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
		Webcam webcam = Webcam.getDefault();
		webcam.setViewSize(new Dimension(res_w, res_h));
		webcam.open();
		BufferedImage image = webcam.getImage();
		Raster r = image.getData();
		webcam.close();
		ArrayList<RGBData> data = new ArrayList<RGBData>();
		
		for (int x = 0; x < res_w; x++) {
			for (int y = 0; y < res_h; y++) {
				double[] d = r.getPixel(x, y, new double[3]);
				data.add(new RGBData(d[0], d[1], d[2]));
			}
		}
		return data;
	}

	public int getResWidth() {
		return res_w;
	}

	public void setResWidth(int res_w) {
		this.res_w = res_w;
	}

	public int getResHeight() {
		return res_h;
	}

	public void setResHeight(int res_h) {
		this.res_h = res_h;
	}

}
