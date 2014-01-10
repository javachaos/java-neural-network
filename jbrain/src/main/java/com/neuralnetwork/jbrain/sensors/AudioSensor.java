package com.neuralnetwork.jbrain.sensors;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import org.apache.commons.lang.ArrayUtils;

import com.neuralnetwork.jbrain.artificialsensors.ISensor;
import com.neuralnetwork.jbrain.exceptions.JbrainException;

/**
 * Low resolution audio sensor.
 * @author fred
 *
 */
public class AudioSensor implements ISensor<Byte[]> {
	
	/**
	 * The size of the audio sample.
	 */
	private int dataSize = 256;

	/**
	 * Construct a new AudioSensor.
	 * 
	 * @param dataResolution
	 * 		the resolution of the data.
	 */
	public AudioSensor(int dataResolution) {
		this.setDataSize(dataResolution);
	}

	@Override
	public Byte[] getData() {
		AudioFormat format = new AudioFormat(1000.0f, 16, 2, true, true);
		
		TargetDataLine line;
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, 
				format);
		
		if (!AudioSystem.isLineSupported(info)) {
			throw new JbrainException("Line not supported.");
		}
		
		byte[] data = new byte[dataSize];
		
		try {
		    line = (TargetDataLine) AudioSystem.getLine(info);
		    line.open(format);
			line.read(data, 0, data.length);
			line.flush();
			line.close();
		} catch (LineUnavailableException ex) {
			throw new JbrainException(ex.getMessage());
		}
		
		return ArrayUtils.toObject(data);
	}

	/**
	 * Get the size of the data.
	 * 
	 * @return
	 * 		the size of the data in bytes
	 */
	public int getDataSize() {
		return dataSize;
	}

	/**
	 * Set the size of the data in bytes.
	 * 
	 * @param dataSize
	 * 		the size of the data to set.
	 */
	private void setDataSize(int dataSize) {
		this.dataSize = dataSize;
	}

}
