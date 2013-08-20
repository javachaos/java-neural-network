package com.neuralnetwork.shared.layers;

import java.util.Vector;

import com.neuralnetwork.shared.nodes.INeuron;

/**
 * Represents a Layer of INeurons.
 * @author fredladeroute
 *
 */
public class Layer extends Vector<INeuron> implements ILayer {

    /**
     * Generated Serial Version UID.
     */
    private static final long serialVersionUID = -94028484942015525L;
    
    /**
     * Construct a new Layer of width w.
     * 
     * @param w
     *      the width of the Layer to be created
     */
    public Layer(final int w) {
        super(w);
    }

    @Override
    public final INeuron getNeuron(final int idx) {
        return get(idx);
    }

}
