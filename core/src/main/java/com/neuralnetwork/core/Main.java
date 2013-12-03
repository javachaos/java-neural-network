package com.neuralnetwork.core;

import java.util.Random;
import java.util.Vector;

import com.neuralnetwork.shared.nodes.ISOMNeuron;
import com.neuralnetwork.shared.nodes.SOMLattice;
import com.neuralnetwork.shared.nodes.SOMLayer;
import com.neuralnetwork.shared.training.SOMTrainer;

/**
 * Main class.
 * 
 * @author fredladeroute
 *
 */
public final class Main {
    
    private static int SIZE_N = 4;
    private static int NUM_ITER = 50000;
    
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
    	long seed = 1234l;
    	Random r = new Random(seed);
    	
        Vector<SOMLayer> inData = new Vector<SOMLayer>(SIZE_N);
        SOMLayer input = new SOMLayer();
        
        for (int i = 0; i < SIZE_N; i++) {
        	input = new SOMLayer();
        	for(int j = 0; j < SIZE_N; j++) {
        		input.add(r.nextDouble());
        	}
        	inData.add(input);
        }
        
    	input = new SOMLayer();
    	for(int j = 0; j < SIZE_N; j++) {
    		input.add(r.nextDouble());
    	}
    	
        SOMLattice lattice = new SOMLattice(SIZE_N, SIZE_N, SIZE_N);
        
        SOMTrainer trainer = new SOMTrainer(0.001, NUM_ITER);
        trainer.setTraining(lattice, inData);
        trainer.start();
        
        //Wait for training to complete, check every second.
        while(trainer.isRunning()) {
        	try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
        lattice = trainer.getLattice();
        ISOMNeuron n = lattice.getBMU(input);//.getNeuralnetwork().runInputs(input);
        System.out.println("X: " + n.getX() + " Y: "+ n.getY());
    }
}
