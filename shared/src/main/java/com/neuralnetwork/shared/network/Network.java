package com.neuralnetwork.shared.network;

import java.util.Iterator;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neuralnetwork.shared.layers.IHiddenLayer;
import com.neuralnetwork.shared.layers.IInputLayer;
import com.neuralnetwork.shared.layers.ILayer;
import com.neuralnetwork.shared.layers.IOutputLayer;
import com.neuralnetwork.shared.layers.InputLayer;
import com.neuralnetwork.shared.layers.OutputLayer;
import com.neuralnetwork.shared.nodes.IHiddenNeuron;
import com.neuralnetwork.shared.nodes.IInputNeuron;
import com.neuralnetwork.shared.nodes.INeuron;
import com.neuralnetwork.shared.nodes.IOutputNeuron;
import com.neuralnetwork.shared.training.TrainingSet;
import com.neuralnetwork.shared.util.Connections;
import com.neuralnetwork.shared.values.ErrorValue;

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
    private Vector<IHiddenLayer> layers;
    
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
     * Construct a 2d neural network.
     * Defined by its width and height.
     * 
     * @param numIn
     *      the number of inputs to the network
     *      
     * @param numOut
     *      the number of outputs from the network
     *      
     * @param numHide
     *      the number of hidden layers in the network
     */
    public Network(final int numIn, final int numOut, final int numHide) {
        this.numInputs = numIn;
        this.numOutputs = numOut;
        this.numHidden = numHide;
        this.inputLayer = new InputLayer(numInputs);
        this.outputLayer = new OutputLayer(numOutputs);
        this.layers = new Vector<IHiddenLayer>(numHidden);
    }
    
    /**
     * Construct a 2d neural network.
     * Defined by its width and height.
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
        this.layers = new Vector<IHiddenLayer>();
    }
    
    @Override
    public INeuron getNode(final int x, final int y) {
        
        if (y == 0) {
            return getInputNeuron(x);
        } else if (y == getHeight()) {
            return getOutputNeuron(x);
        } else {
            return layers.get(y).getNeuron(x);
        }
    }

    @Override
    public INeuron getOutputNeuron(final int x) {
        return getOutputLayer().getNeuron(x);
    }

    @Override
    public INeuron getInputNeuron(final int x) {
        return getInputLayer().getNeuron(x);
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public Vector<Double> runInputs(final Vector<Double> l) {
        //TODO Complete method.
        return null;
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
    public ErrorValue train(final Vector<Double> trainingVector) {
        // TODO Implement method.
        return null;
    }

    @Override
    public ErrorValue train(final TrainingSet trainingSet,
            final ErrorValue expectedError) {
        // TODO Implement method.
        return null;
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
        layers.add(l);
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
    @Deprecated
    public ILayer<IHiddenNeuron> getLayer(final int idx) {
        return layers.get(idx);
    }

    @Override
    public void build() {
        inputLayer.build();
        Iterator<IHiddenLayer> i = layers.iterator();
        while (i.hasNext()) {
            i.next().build();
        }
        outputLayer.build();
        Connections.create(inputLayer, layers.get(0));
        
        if (layers.size() >= 2) {
            i = layers.iterator();
            while (i.hasNext()) {
                IHiddenLayer layer1 = i.next();
                if (i.hasNext()) {
                    Connections.create(layer1, i.next());
                }
            }
        }
        
        Connections.create(outputLayer, layers.get(layers.size() - 1));
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
