module jbrain {
	exports com.github.javachaos.javaneuralnetwork.jbrain.artificialsensors;
	exports com.github.javachaos.javaneuralnetwork.jbrain.main;
	exports com.github.javachaos.javaneuralnetwork.jbrain.sensors;
	exports com.github.javachaos.javaneuralnetwork.jbrain.exceptions;
	exports com.github.javachaos.javaneuralnetwork.jbrain.sensors.camera;
	exports com.github.javachaos.javaneuralnetwork.jbrain.vision;

	requires transitive java.desktop;
	requires transitive org.apache.logging.log4j;
	requires transitive shared;
}
