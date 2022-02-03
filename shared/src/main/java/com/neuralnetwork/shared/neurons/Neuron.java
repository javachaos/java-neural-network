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
package com.neuralnetwork.shared.neurons;

import com.neuralnetwork.shared.functions.ActivationFunction;
import com.neuralnetwork.shared.links.Link;
import com.neuralnetwork.shared.network.NeuralNetContext;

/**
 * INeuron interface for the Nodes of the network.
 * 
 * @author fredladeroute
 *
 *
 */
public interface Neuron {
    
    /**
     * Get the value that this neuron holds.
     * 
     * @return
     *      the value that this neuron holds
     */
	Double getValue();
    
    /**
     * Set the value that this neuron will hold.
     * 
     * @param v
     *      the value to set for this neuron
     */
    void setValue(Double v);
    
    /**
     * Adds a link from this node to ineuron
     * with weight weight and return the ILink.
     * 
     * @param ineuron 
     *      the node to connect to.
     *      
     * @param weight 
     *      the weight value of this connection.
     * 
     * @return a new ILink.
     */
    Link addInputLink(Neuron ineuron, Double weight);
    
    /**
     * Adds a link from this node to ineuron
     * with a random weight (uniformly distributed) value in range [0-1]
     * and return the ILink.
     * 
     * @param ineuron
     *      the node to connect to.
     * 
     * @return a new ILink.
     */
    Link addInputLink(Neuron ineuron);
    
    /**
     * Set the input link at i to be l.
     * @param i
     *      the index of the link to be set
     * @param l
     *      the new link to be set
     * @return
     *      the old link.
     */
    Link setInputLink(int i, Link l);
    
    /**
     * Set the input links for this neuron.
     * 
     * @param links
     *      the links to be set
     * @return
     *      the old input links
     */
    Link[] setInputLinks(Link[] links);
    
    /**
     * Get a ILink by the id of the other INeuron.
     * 
     * @param linkId 
     *      the id of the node to get the link of.
     *      
     * @return the ILink
     */
    Link getInputLink(int linkId);
    
    /**
     * Get ILinks by the id of the other INeuron ids.
     * 
     * @param ids 
     *      the ids of the nodes to get the links of.
     *      
     * @return the ILink.
     */
    Link[] getInputLinks(int... ids);
    
    /**
     * Get the input links to this neuron.
     * @return
     *      the input links to this neuron
     */
    Link[] getInputLinks();
    
    /**
     * Adds a link from this node to ineuron
     * with weight weight and return the ILink.
     * 
     * Also adds the input link for the ineuron neuron.
     * Thus created a full 2 way connection between
     * this neuron and ineuron.
     * 
     * @param ineuron 
     *      the node to connect to.
     *      
     * @param weight 
     *      the weight value of this connection.
     * 
     * @return a new ILink.
     */
    Link addOutputLink(Neuron ineuron, Double weight);
    
    /**
     * Adds a link from this node to ineuron
     * with a random weight (uniformly distributed) value in range [0-1]
     * and return the ILink.
     * 
     * @param ineuron
     *      the node to connect to.
     * 
     * @return a new ILink.
     * 
     */
    Link addOutputLink(Neuron ineuron);
    
    /**
     * Set the output link at i to be l.
     * 
     * @param i
     *      the index to the output link to be set
     * 
     * @param l
     *      the new link to be set
     * 
     * @return
     *      the old output link
     */
    Link setOutputLink(int i, Link l);
    
    /**
     * Set the output links of this neuron.
     * @param links
     *      the new links to be set
     *      
     * @return
     *      the old output links array
     */
    Link[] setOutputLinks(Link[] links);
    
    /**
     * Get a ILink by the id of the other INeuron.
     * 
     * @param linkId 
     *      the id of the node to get the link of.
     *      
     * @return the ILink
     */
    Link getOutputLink(int linkId);
    
    /**
     * Get ILinks by the id of the other INeuron ids.
     * 
     * @param ids 
     *      the ids of the nodes to get the links of.
     *      
     * @return the ILink.
     */
    Link[] getOutputLinks(int... ids);
    
    /**
     * Get all ILinks.
     * 
     * @return the ILinks.
     */
    Link[] getOutputLinks();
    
    /**
     * Return the type of this INeuron.
     * 
     * @return
     *      the id of this INeuron
     */
    NeuronType getType();
    
    /**
     * Sets the type of neuron. In the rare event that one needs
     * to change the type of a neuron this must be called.
     * 
     * @param t
     * 		the type of the neuron
     */
	void setType(NeuronType t);
    
    /**
     * Return the next child INeuron.
     * 
     * @return
     *      the next child INeuron
     *      and increment the child id counter.
     */
    Neuron getNextChild();

    /**
     * Return the next parent INeuron.
     * 
     * @return
     *      the next parent INeuron 
     *      and increment the parent id counter
     */
    Neuron getNextParent();

    /**
     * Reset the weights of all OUTPUT weight values to
     * a uniform randomly distributed value between [-1,1].
     */
    void reset();
    
    /**
     * Sets the activation function which this neuron
     * will use.
     * 
     * @param f
     *      the IActivationFunction to use
     */
    void setActivationFunction(ActivationFunction f);
    
    /**
     * Get the activation function for this INeuron.
     * 
     * @return
     *      the IActivationFunction of this INeuron
     */
    ActivationFunction getActivationFunction();

    /**
     * Cause this neuron to activate,
     * sum up all the inputs * weights, and then
     * run them through the IActivationFunction finally
     * update the weights of the output links.
     * 
     * @param v
     *      the value to be fed forward to the next neuron
     * @param nnctx
     * 		the neural network context to be passed along.
     * @return output value.
     */
    Double feedforward(Double v, NeuralNetContext nnctx);

    /**
     * Calculate the error.
     * 
     * @return
     * 		the 1/2 MSE.
     */
	Double getError();

	/**
	 * Propagate the error of this node to all its children.
	 * 
	 * @param error
	 * 		the error to propagate.
	 * @return
	 * 		propagated error value.
	 */
	Double propagateError(Double error);
	
	@Override
	int hashCode();
	
	@Override
	boolean equals(Object o);
}
