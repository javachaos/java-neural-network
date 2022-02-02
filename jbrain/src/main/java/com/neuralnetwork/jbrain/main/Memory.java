package com.neuralnetwork.jbrain.main;

import java.util.*;

import com.neuralnetwork.jbrain.sensors.camera.RGBData;
import com.neuralnetwork.shared.network.INetwork;
import com.neuralnetwork.shared.util.NeuralNetBuilder;
import com.neuralnetwork.shared.util.SimpleNetworkConfigs;

/**
 * This is a memory for the JBrain
 * a memory consists of a related
 * collection of 2 or more data points from
 * various sensors.
 *
 */
public class Memory {

	/**
	 * The network for this memory.
	 */
	private final INetwork network;

	/**
	 * Visual data for this memory.
	 */
	private final Deque<ArrayList<RGBData>> visualData;

	/**
	 * Audio data for this memory.
	 */
	private final Deque<ArrayList<Double>> audioData;

	/**
	 * Memory
	 */
	public Memory() {
		visualData = new ArrayDeque<>();
		audioData = new ArrayDeque<>();
		network = (new NeuralNetBuilder(SimpleNetworkConfigs.CONFIG_5_4_3_4_5)).build();
	}

	/**
	 * Pop an audio sample from this memory.
	 * @return
	 * 		the latest audio sample from this memory.
	 */
	public List<Double> popAudioData() {
		return audioData.pop();
	}

	/**
	 * Push a new audio sample into this memory.
	 * @param audioData
	 * 		the new audio sample to be added to this memory.
	 */
	public void pushAudioData(final List<Double> audioData) {
		this.audioData.push((ArrayList<Double>) audioData);
	}

	/**
	 * Get the neural network for this memory.
	 * @return
	 * 		the neural network for this memory.
	 */
	public INetwork getNetwork() {
		return network;
	}

	/**
	 * Pop the top of the visual
	 * data stack and return it.
	 * @return
	 * 		the top of the visual data stack
	 */
	public List<RGBData> popVisualData() {
		return visualData.pop();
	}

	/**
	 * Push a visual data element onto the top of the
	 * visual data stack.
	 * 
	 * @param visualData
	 * 		the new visual data element to be added to the top
	 * 		of the stack.
	 */
	public void pushVisualData(List<RGBData> visualData) {
		this.visualData.push((ArrayList<RGBData>) visualData);
	}
}
