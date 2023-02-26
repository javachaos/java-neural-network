package com.github.javachaos.javaneuralnetwork.jbrain.sensors.camera;


/**
 * 
 * @author fred
 *
 */
public class RGBData {

	/**
	 * Red color value.
	 */
	private double red;
	
	/**
	 * Green color value.
	 */
	private double green;
	
	/**
	 * Blue color value.
	 */
	private double blue;
	
	/**
	 * Construct a new RGB data element.
	 * 
	 * @param d
	 * 		red value
	 * @param d2
	 * 		green value
	 * @param d3
	 * 		blue value
	 */
	public RGBData(double d, double d2, double d3) {
		
		if ((d < 0 || d > 255)
		 || (d2 < 0 || d2 > 255)
		 || (d3 < 0 || d3 > 255)) {
			throw new IllegalArgumentException("Invalid color value.");
		}
		
		this.setRed(d);
		this.setGreen(d2);
		this.setBlue(d3);
	}

	/**
	 * Get the red color value.
	 * @return
	 * 		the red color value.
	 */
	public double getRed() {
		return red;
	}

	/**
	 * Set the red color value.
	 * @param red
	 * 		the red color value to be set.
	 */
	public void setRed(double red) {
		this.red = red;
	}

	/**
	 * Get the green color value.
	 * @return
	 * 		the green color value.
	 */
	public double getGreen() {
		return green;
	}

	/**
	 * Set the green color value.
	 * @param green
	 * 		the green color value to be set.
	 */
	public void setGreen(double green) {
		this.green = green;
	}

	/**
	 * Get the blue color value.
	 * @return
	 * 		the blue color value.
	 */
	public double getBlue() {
		return blue;
	}

	/**
	 * Set the blue color value.
	 * @param blue
	 * 		the blue color value to be set.
	 */
	public void setBlue(double blue) {
		this.blue = blue;
	}
	
	@Override
	public String toString() {
		return "[ "+red+", "+green+", "+blue+" ]";
	}

}
