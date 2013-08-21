package com.neuralnetwork.core;

import java.util.Vector;

import com.neuralnetwork.shared.nodes.SOMLattice;
import com.neuralnetwork.shared.nodes.SOMLayer;
import com.neuralnetwork.shared.training.SOMTrainer;
import com.neuralnetwork.shared.util.ErrorFunctions;

/**
 * Main class.
 * 
 * @author fredladeroute
 *
 */
public final class Main {
    
    private static int SIZE_N = 100;
    
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
        Vector<SOMLayer> data = new Vector<SOMLayer>();
        
        SOMLayer input = new SOMLayer();
        SOMLattice lattice = new SOMLattice(SIZE_N, SIZE_N, SIZE_N);
        
        SOMTrainer trainer = new SOMTrainer(0.07, 500);
        trainer.setTraining(lattice, data);
        trainer.start();
        
        lattice.getBMU(input).getNeuralnetwork().runInputs(input);
        
    }
}
