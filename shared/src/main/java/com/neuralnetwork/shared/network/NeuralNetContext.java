package com.neuralnetwork.shared.network;

/**
 * Neural network context, stores a reference to the network.
 * Intended to be pass around during training.
 * @author fred
 *
 */
public class NeuralNetContext implements INeuralNetContext {
    
    /**
     * The network reference.
     */
    private INetwork network;
    
    /**
     * Create a new neuralnet context with network n.
     * @param n
     *      the network for this context.
     */
    public NeuralNetContext(final INetwork n) {
        this.network = n;
    }

    @Override
    public final INetwork getNetwork() {
        return network;
    }
}
