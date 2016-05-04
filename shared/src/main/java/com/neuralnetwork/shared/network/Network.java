/*******************************************************************************
 * Copyright (c) 2014 Fred Laderoute.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *     Fred Laderoute - initial API and implementation
 ******************************************************************************/
package com.neuralnetwork.shared.network;

import java.util.Iterator;
import java.util.Stack;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neuralnetwork.shared.layers.HiddenLayer;
import com.neuralnetwork.shared.layers.IHiddenLayer;
import com.neuralnetwork.shared.layers.IInputLayer;
import com.neuralnetwork.shared.layers.ILayer;
import com.neuralnetwork.shared.layers.IOutputLayer;
import com.neuralnetwork.shared.layers.InputLayer;
import com.neuralnetwork.shared.layers.OutputLayer;
import com.neuralnetwork.shared.neurons.IHiddenNeuron;
import com.neuralnetwork.shared.neurons.IInputNeuron;
import com.neuralnetwork.shared.neurons.INeuron;
import com.neuralnetwork.shared.neurons.IOutputNeuron;
import com.neuralnetwork.shared.training.BackpropAlgorithm;
import com.neuralnetwork.shared.training.TrainType;
import com.neuralnetwork.shared.util.Connections;

/**
 * Represents a neural network structure.
 * @author fredladeroute
 *
 */
public final class Network implements INetwork {

    /**
     * Logger instance.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Network.class);
    
    /**
     * The input layer to this network.
     */
    private IInputLayer inputLayer;
    
    /**
     * The hidden layer(s) of this network.
     */
    private Stack<IHiddenLayer> layers;
    
    /**
     * The ouput layer of this network.
     */
    private IOutputLayer outputLayer;
    
    /**
     * Height of the network.
     */
    private int height = 0;

    /**
     * Number of inputs in the network.
     */
    private int numInputs;

    /**
     * Number of outputs in the network.
     */
    private int numOutputs;

    /**
     * Number of hidden layers in the network.
     */
    private int numHidden;

    /**
     * The size of each respective hidden layer in this network.
     */
	private int[] layerSizes;
	
	/**
	 * The context for this network.
	 */
	private INeuralNetContext nnctx;
	
	/**
	 * Default training algorithm.
	 */
	private TrainType trainAlgorithm = TrainType.BACKPROP;
	
	/**
	 * Marker to indicate that training is occurring.
	 */
	private boolean isTraining = false;
	
	/**
	 * Learning Rate.
	 */
	private static final double LEARN_RATE = 0.61803398875;
    
    /**
     * Construct a 2d neural network.
     * With {@link TrainType#RPROP}
     * as the default training algorithm.
     * 
     * @param numIn
     *      the number of inputs to the network
     *      
     * @param numOut
     *      the number of outputs from the network
     *      
     * @param numHide
     *      the number of hidden layers in the network
     *      
     * @param sizes
     * 		the sizes of each hidden layer respectively.
     */
    public Network(final int numIn, final int numOut, 
                final int numHide, final int[] sizes) {
        
    	if (numHide < 0) {
        	throw new IllegalArgumentException(
        			"Error cannot have negative amount of hidden layers.");
        } else if (numIn < 0) {
        	throw new IllegalArgumentException(
        			"Error cannot have negative amount of input layers.");
        } else if (numOut < 0) {
        	throw new IllegalArgumentException(
        			"Error cannot have negative amount of output layers.");
        }
        this.numInputs = numIn;
        this.numOutputs = numOut;
        this.numHidden = numHide;
        this.height = numHide + 2;
        this.layerSizes = sizes;
        this.inputLayer = new InputLayer(numInputs);
        this.outputLayer = new OutputLayer(numOutputs);
        this.layers = new Stack<IHiddenLayer>();
        this.nnctx = new NeuralNetContext(this);
    }
    
    /**
     * Construct a 2d neural network.
     * 
     * @param numIn
     *      the number of inputs to the network
     *      
     * @param numOut
     *      the number of outputs from the network
     *      
     * @param numHide
     *      the number of hidden layers in the network
     *      
     * @param sizes
     * 		the sizes of each hidden layer respectively.
     * 
     * @param t
     * 		the training algorithm for the network
     */
    public Network(final int numIn, final int numOut, 
                final int numHide, final int[] sizes,
                final TrainType t) {
    	this(numIn, numOut, numHide, sizes);
    	this.setTrainingAlgorithm(t);
    }
    
    /**
     * Construct a 2d neural network.
     * With {@link TrainType#RPROP}
     * as the default training algorithm.
     * Note: No hidden layers are created.
     * 
     * @param numIn
     *      the number of inputs to the network
     *      
     * @param numOut
     *      the number of outputs from the network
     */
    public Network(final int numIn, final int numOut) {
        this.numInputs = numIn;
        this.numOutputs = numOut;
        this.inputLayer = new InputLayer(numInputs);
        this.outputLayer = new OutputLayer(numOutputs);
        this.layers = new Stack<IHiddenLayer>();
        this.nnctx = new NeuralNetContext(this);
    }
    
    /**
     * Construct a 2d neural network.
     * Note: No hidden layers are created.
     * 
     * @param numIn
     *      the number of inputs to the network
     *      
     * @param numOut
     *      the number of outputs from the network
     *      
     * @param t
     * 		the training algorithm for the network
     */
    public Network(final int numIn, final int numOut,
    		final TrainType t) {
    	this(numIn, numOut);
    	this.setTrainingAlgorithm(t);
    }
    
    @Override
    public synchronized INeuron getNeuron(final int x, final int y) {

        if (x < 0 || y < 0) {
            return null;
        }

        if (x + 1 >= 0 && y == 0) {
            return getInputNeuron(x);
        } else if (x + 1 >= 0 && y < getHeight() - 1) {
            return layers.get(y - 1).getNeuron(x + 1);
        } else if (x + 1 >= 0 && y == getHeight() - 1) {
            return getOutputNeuron(x);
        }
        
        return null;
    }

    @Override
    public IOutputNeuron getOutputNeuron(final int x) {
        if (x < 0) {
            return null;
        }
        return (IOutputNeuron) getOutputLayer().getNeuron(x + 1);
    }

    @Override
    public IInputNeuron getInputNeuron(final int x) {
        if (x < 0) {
            return null;
        }
        return (IInputNeuron) getInputLayer().getNeuron(x + 1);
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public synchronized Vector<Double> runInputs(final Vector<Double> l) {
    	if (l.size() != inputLayer.getSize()) {
    		LOGGER.error("Input vector does not have correct "
    				+ "dimension to be run through this network.");
    		return null;
    	}
    	getInputLayer().addValues(l);
        IOutputLayer ol = getInputLayer().propagate(nnctx);
        return ol.getOutputValues();
    }

    @Override
    public void reset() {
        LOGGER.debug("Resetting the network.");
        Iterator<IHiddenLayer> i = layers.iterator();
        while (i.hasNext()) {
            ILayer<IHiddenNeuron> l = i.next();
            Iterator<IHiddenNeuron> j = l.iterator();
            while (j.hasNext()) {
                j.next().reset();
            }
        }
    }

    @Override
    public synchronized Double train(final boolean online, 
    		final Vector<Double> trainingVector,
    		final Double expectedError) {
    	Double errorValue = 0.0;
    	toggleTraining();
//    	if (!online) {
//    	    //reset();
//    	}
    	switch (getTrainAlgorithm()) {
			case BACKPROP:
					BackpropAlgorithm algo = 
					new BackpropAlgorithm(trainingVector,
							trainingVector, this, expectedError);
					errorValue = algo.compute();
				break;
			case QPROP:
				break;
			case RPROP:
				break;
			default:
				break;
    	}
    	toggleTraining();
    	return errorValue;
    }

    /**
     * Private helper function.
     */
    private void toggleTraining() {
    	isTraining = !isTraining;
    }

    /**
     * Get the training algorithm.
	 * @return the trainAlgorithm
	 */
	public TrainType getTrainAlgorithm() {
		return trainAlgorithm;
	}

	/**
	 * Set the training algorithm.
	 * 
	 * @param t 
	 * 		the trainAlgorithm to set
	 */
	public void setTrainingAlgorithm(
			final TrainType t) {
		this.trainAlgorithm = t;
	}

	@Override
    public IOutputLayer getOutputLayer() {
        return outputLayer;
    }
    
    @Override
    public void setOutputLayer(final IOutputLayer l) {
        this.outputLayer = l;
        rebuild();
    }

    @Override
    public void addHiddenLayer(final IHiddenLayer l) {
    	l.build();
        layers.push(l);
        rebuild();
    }

    @Override
    public IHiddenLayer getHiddenLayer(final int i) {
        return layers.get(i);
    }

    @Override
    public IInputLayer getInputLayer() {
        return inputLayer;
    }

    @Override
    public void setInputLayer(final IInputLayer l) {
        this.inputLayer = l;
        rebuild();
    }
    

    @Override
	public double getLearnRate() {
		return LEARN_RATE;
	}

	/**
     * Rebuild the network fixing only those links
     * which are not connected and keeping all other
     * links intact.
     */
    private void rebuild() {
        
    }

    @Override
    public synchronized void build() {

    	Connections c = Connections.getInstance();
		Stack<IHiddenLayer> temp = new Stack<IHiddenLayer>();
		while (!layers.empty()) {
			temp.push(layers.pop());
		}
    	
    	for (int i = 0; i < numHidden; i++) {
    		IHiddenLayer h = new HiddenLayer(layerSizes[i]);
    		h.build();
    		layers.push(h);
    	}
    	
		while (!temp.empty()) {
			layers.push(temp.pop());
		}
    	
        inputLayer.build();
        Iterator<IHiddenLayer> i = layers.iterator();
        while (i.hasNext()) {
            i.next().build();
        }
        outputLayer.build();
        
        if (!layers.isEmpty()) {
	        //Connect input layer to first hidden layer.
	        c.create(inputLayer, layers.get(0));
	        
	        //Connect each hidden layer to its child hidden layer.
	        if (layers.size() >= 2) {
	            i = layers.iterator();
	            while (i.hasNext()) {
	                IHiddenLayer layer = i.next();
	                if (i.hasNext()) {
	                    c.create(layer, i.next());
	                }
	            }
	        }
	        
	        //Connect the output layer to the last hidden layer.
	        c.create(outputLayer, layers.get(layers.size() - 1));
        } else {
        	c.create(inputLayer, outputLayer);
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        Iterator<IInputNeuron> inputIter = inputLayer.iterator();
        
        while (inputIter.hasNext()) {
            sb.append(inputIter.next().toString());
        }
        
        sb.append("\n");
        
        Iterator<IHiddenLayer> hiddenIter = layers.iterator();
        while (hiddenIter.hasNext()) {
            Iterator<IHiddenNeuron> hnIter = hiddenIter.next().iterator();
            while (hnIter.hasNext()) {
                sb.append(hnIter.next());
            }
            sb.append("\n");
        }
        
        Iterator<IOutputNeuron> outputIter = outputLayer.iterator();
        
        while (outputIter.hasNext()) {
            sb.append(outputIter.next().toString());
        }

        sb.append("\n");
        return sb.toString();
    }    
}
