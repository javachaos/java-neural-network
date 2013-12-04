package com.neuralnetwork.core;

import java.util.Random;
import java.util.Vector;

import com.neuralnetwork.shared.layers.HiddenLayer;
import com.neuralnetwork.shared.nodes.ISOMNeuron;
import com.neuralnetwork.shared.nodes.SOMLattice;
import com.neuralnetwork.shared.nodes.SOMLayer;
import com.neuralnetwork.shared.training.SOMTrainer;
import com.neuralnetwork.shared.util.NeuralNetBuilder;
import com.neuralnetwork.shared.util.SimpleNetworkConfigs;

/**
 * Main class.
 * 
 * @author fredladeroute
 *
 */
public final class Main {
    
    /**
     * Size of the Self organizing map.
     */
    private static final int SIZE_N = 4;
    
    /**
     * Number of iterations for training.
     */
    private static final int NUM_ITER = 50000;
    
    /**
     * Training thread checkin time.
     */
    private static final int CHECKIN_DELAY = 1000;
    
    /**
     * Learning rate for SOM algorithm.
     */
    private static final double LEARNING_RATE = 0.001;
    
    /**
     * Seed value for the random number generator.
     */
    private static final long SEED = 1234L;
    
    /**
     * Unused ctor.
     */
    private Main() {
    }

    /**
     * Main method.
     * @param args
     *      command line args
     */
    public static void main(final String[] args) {
    	Random r = new Random(SEED);
    	
        Vector<SOMLayer> inData = new Vector<SOMLayer>(SIZE_N);
        SOMLayer input = new SOMLayer();
        
        for (int i = 0; i < SIZE_N; i++) {
        	input = new SOMLayer();
        	for (int j = 0; j < SIZE_N; j++) {
        		input.add(r.nextDouble());
        	}
        	inData.add(input);
        }
        
    	input = new SOMLayer();
    	for (int j = 0; j < SIZE_N; j++) {
    		input.add(r.nextDouble());
    	}
    	
        SOMLattice lattice = new SOMLattice(SIZE_N, SIZE_N, SIZE_N);
        
        SOMTrainer trainer = new SOMTrainer(LEARNING_RATE, NUM_ITER);
        trainer.setTraining(lattice, inData);
        trainer.start();
        
        //Wait for training to complete, check every second.
        while (trainer.isRunning()) {
        	try {
				Thread.sleep(CHECKIN_DELAY);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
        lattice = trainer.getLattice();
        ISOMNeuron n = lattice.getBMU(input);
        System.out.println("X: " + n.getX() + " Y: " + n.getY());
        
        NeuralNetBuilder builder = new NeuralNetBuilder(SIZE_N, SIZE_N);
        builder.addHiddenLayer(new HiddenLayer(2));
        builder.addHiddenLayer(new HiddenLayer(1));
        builder.build();
        
        NeuralNetBuilder b = 
                new NeuralNetBuilder(SimpleNetworkConfigs.CONFIG_5_4_3_4_5);
        b.build();
    }
}
