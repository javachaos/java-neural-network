package com.neuralnetwork.jbrain.main;

import java.util.ArrayList;

import javax.sound.sampled.LineUnavailableException;

import com.neuralnetwork.jbrain.artificialsensors.ISensor;
import com.neuralnetwork.jbrain.sensors.AudioSensor;
import com.neuralnetwork.jbrain.sensors.LowResVisualSensor;
import com.neuralnetwork.jbrain.sensors.camera.RGBData;

public class Main {

	public static void main(String[] args) throws LineUnavailableException {
		
		ISensor<ArrayList<RGBData>> s = new LowResVisualSensor();
		ISensor<Byte[]> audio = new AudioSensor(128);
		ArrayList<RGBData> data = s.getData();
		
		for (RGBData dd : data) {
		    System.out.println(dd);
		}
		
		Byte[] audioData = audio.getData();
		for(Byte b : audioData) {
			System.out.println(b);
		}
	}
}
