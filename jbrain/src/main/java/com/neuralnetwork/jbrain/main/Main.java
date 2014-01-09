package com.neuralnetwork.jbrain.main;

import java.util.ArrayList;

import com.neuralnetwork.jbrain.artificialsensors.ISensor;
import com.neuralnetwork.jbrain.sensors.LowResVisualSensor;
import com.neuralnetwork.jbrain.sensors.camera.RGBData;

public class Main {

	public static void main(String[] args) {
		ISensor<ArrayList<RGBData>> s = new LowResVisualSensor();
		
		ArrayList<RGBData> data = s.getData();
		for (RGBData dd : data) {
		    System.out.println(dd);
		}
		
	}
}
