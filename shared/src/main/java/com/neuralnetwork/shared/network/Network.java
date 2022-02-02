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

import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.IntStream;
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
import com.neuralnetwork.shared.training.TrainType;
import com.neuralnetwork.shared.util.Connections;

/**
 * Represents a neural network structure.
 *
 */
public final class Network implements INetwork {

    private static final Logger LOGGER = LoggerFactory.getLogger(Network.class);
    private IInputLayer inputLayer;
    private final ConcurrentMap<Integer, IHiddenLayer> layers;
    private IOutputLayer outputLayer;
    private int height = 0;
    private final int numInputs;
    private final int numOutputs;
    private int numHidden;
	private int[] layerSizes;
	private final INeuralNetContext nnctx;
	private TrainType trainAlgorithm = TrainType.BACKPROP;
    
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
        this.layers = new ConcurrentHashMap<>();
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
        this.layers = new ConcurrentHashMap<>();
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
    public INeuron getNeuron(final int x, final int y) {

        if (x < 0 || y < 0 || x == Integer.MAX_VALUE || y == Integer.MAX_VALUE) {
            return null;
        }
        int n = x + 1;
        if (y == 0) {
            return getInputNeuron(x);
        } else if (y < getHeight() - 1) {
            return layers.get(y - 1).getNeuron(n);
        } else if (y == getHeight() - 1) {
            return getOutputNeuron(x);
        }
        
        return null;
    }

    @Override
    public IOutputNeuron getOutputNeuron(final int x) {
        if (x < 0) {
            return null;
        }
        return getOutputLayer().getNeuron(x + 1);
    }

    @Override
    public IInputNeuron getInputNeuron(final int x) {
        if (x < 0) {
            return null;
        }
        return getInputLayer().getNeuron(x + 1);
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public synchronized List<Double> runInputs(final List<Double> l) {
        nnctx.setRunning(true);
    	if (l.size() != inputLayer.getSize()) {
    		LOGGER.error("Input vector does not have correct "
    				+ "dimension to be run through this network.");
    		return new ArrayList<>();
    	}
    	getInputLayer().addValues(l);
        IOutputLayer ol = getInputLayer().propagate(nnctx);
        nnctx.setRunning(false);
        return ol.getOutputValues();
    }

    @Override
    public void reset() {
        LOGGER.debug("Resetting the network.");

        for (ILayer<IHiddenNeuron> l : layers.values()) {
            for (IHiddenNeuron iHiddenNeuron : l) {
                iHiddenNeuron.reset();
            }
        }
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
    }

    @Override
    public void addHiddenLayer(final IHiddenLayer l) {
    	l.build();
        layers.put(l.getIndex(), l);
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
    }

    @Override
    public void build() {
    	Connections c = Connections.getInstance();
		Deque<IHiddenLayer> temp = new LinkedBlockingDeque<>();
        for (IHiddenLayer hiddenNeurons : layers.values()) {
            temp.push(hiddenNeurons);
        }
        IntStream.range(0, numHidden).parallel().forEach(i -> {
            IHiddenLayer h = new HiddenLayer(layerSizes[i], i);
            h.build();
            layers.put(h.getIndex(), h);
        });
		while (!temp.isEmpty()) {
            IHiddenLayer l = temp.pop();
            layers.put(l.getIndex(), l);
		}
        inputLayer.build();
        for (IHiddenLayer iHiddenNeurons : layers.values()) {
            iHiddenNeurons.build();
        }
        outputLayer.build();
        
        if (!layers.isEmpty()) {
	        //Connect input layer to first hidden layer.
	        c.create(inputLayer, layers.get(0));
	        
	        //Connect each hidden layer to its child hidden layer.
	        if (layers.size() >= 2) {
                Iterator<IHiddenLayer> i = layers.values().iterator();
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
        for (IInputNeuron iInputNeuron : inputLayer) {
            sb.append(iInputNeuron.toString());
        }
        sb.append("\n");
        for (IHiddenLayer iHiddenNeurons : layers.values()) {
            for (IHiddenNeuron iHiddenNeuron : iHiddenNeurons) {
                sb.append(iHiddenNeuron);
            }
            sb.append("\n");
        }
        for (IOutputNeuron iOutputNeuron : outputLayer) {
            sb.append(iOutputNeuron.toString());
        }
        sb.append("\n");
        return sb.toString();
    }    
}
