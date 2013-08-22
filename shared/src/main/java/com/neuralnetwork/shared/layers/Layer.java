package com.neuralnetwork.shared.layers;

import java.util.Vector;

import com.neuralnetwork.shared.network.LayerType;

/**
 * Represents a Layer of INeurons.
 * @author fredladeroute
 *
 *@param <T>
 *      the type of Layer this is
 */
public abstract class Layer<T> extends Vector<T> implements ILayer<T> {

    /**
     * Generated Serial Version UID.
     */
    private static final long serialVersionUID = -94028484942015525L;
    
    /**
     * Represents the width of the layer.
     */
    private int width;
    
    /**
     * The layer type for this layer.
     */
    private LayerType layerType;
    
    /**
     * Construct a new Layer of width w.
     * 
     * @param w
     *      the width of the Layer to be created
     */
    public Layer(final int w) {
        super(w);
        this.width = w;
    }

    @Override
    public final T getNeuron(final int idx) {
        return get(idx);
    }

    @Override
    public final LayerType getLayerType() {
        return layerType;
    }

    /**
     * Set the layer type for this layer.
     * 
     * @param t
     *      the layerType to set
     */
    protected final void setLayerType(final LayerType t) {
        this.layerType = t;
    }

    /**
     * @return the width
     */
    public final int getWidth() {
        return width;
    }

}