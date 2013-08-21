package com.neuralnetwork.shared.network;

import java.util.Iterator;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neuralnetwork.shared.layers.IHiddenLayer;
import com.neuralnetwork.shared.layers.IInputLayer;
import com.neuralnetwork.shared.layers.ILayer;
import com.neuralnetwork.shared.layers.IOutputLayer;
import com.neuralnetwork.shared.nodes.INeuron;
import com.neuralnetwork.shared.training.TrainingSet;
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
     * Construct a 2d neural network.
     * Defined by its width and height.
     *      
     */
    public Network() {
        this.layers = new Vector<IHiddenLayer>();
    }
    
    @Override
    public ILayer<INeuron> getLayer(final int idx) {
        return layers.elementAt(idx);
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
            ILayer<INeuron> l = i.next();
            Iterator<INeuron> j = l.iterator();
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
    
}
