package com.neuralnetwork.shared.network;

import java.util.Iterator;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neuralnetwork.shared.layers.ILayer;
import com.neuralnetwork.shared.nodes.INeuron;
import com.neuralnetwork.shared.training.TrainingSet;
import com.neuralnetwork.shared.values.ErrorValue;

/**
 * Represents a neural network structure.
 * @author fredladeroute
 *
 */
public class Network implements INetwork {

    /**
     * Logger instance.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Network.class);
    
    /**
     * The layers of this network.
     */
    private Vector<ILayer> layers;
    
    /**
     * Width of the network.
     */
    private int width = 0;
    
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
        this.layers = new Vector<ILayer>();
    }
    
    @Override
    public final ILayer getLayer(final int idx) {
        return layers.elementAt(idx);
    }

    @Override
    public final INeuron getNode(final int x, final int y) {
        ILayer temp = layers.get(y);
        return temp.getNeuron(x);
    }

    @Override
    public final int getWidth() {
        return width;
    }

    @Override
    public final int getHeight() {
        return height;
    }

    @Override
    public final int getSize() {
        return width * height;
    }

    @Override
    public final Vector<Double> runInputs(final Vector<Double> inputLayer) {
        //TODO Complete method.
        return null;
    }

    @Override
    public final void reset() {
        Iterator<ILayer> i = layers.iterator();
        while (i.hasNext()) {
            ILayer l = i.next();
            Iterator<INeuron> j = l.iterator();
            while (j.hasNext()) {
                j.next().reset();
            }
        }
    }

    @Override
    public final ErrorValue train(final Vector<Double> trainingVector) {
        // TODO Implement method.
        return null;
    }

    @Override
    public final ErrorValue train(final TrainingSet trainingSet,
            final ErrorValue expectedError) {
        // TODO Implement method.
        return null;
    }

    @Override
    public final void addLayer(final ILayer l, final LayerType t) {
        switch(t) {
            case HIDDEN:
                break;
            case INPUT:
                break;
            case OUTPUT:
                break;
            default:
                LOGGER.error("Illegal layer type.");
                break;
        }
    }
    
}
