/*******************************************************************************
 * Copyright (c) 2014 Fred .
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *     Fred  - initial API and implementation
 ******************************************************************************/
package com.neuralnetwork.shared.neurons;

import com.neuralnetwork.shared.functions.ActivationFunction;
import com.neuralnetwork.shared.links.Link;
import com.neuralnetwork.shared.network.NeuralNetContext;

/**
 * Neuron interface for the neurons of the network.
 */
public interface Neuron {
    
    /**
     * Get the value that this neuron holds.
     * 
     * @return
     *      the value that this neuron holds
     */
	double getValue();
    
    /**
     * Set the value that this neuron will hold.
     * 
     * @param v
     *      the value to set for this neuron
     */
    void setValue(double v);
    
    /**
     * Adds a link from this neuron to neuron
     * with weight and return the link.
     * 
     * @param n
     *      the neuron to connect to.
     *      
     * @param weight 
     *      the weight value of this connection.
     * 
     * @return a new link.
     */
    Link addInputLink(Neuron n, double weight);
    
    /**
     * Adds a link from this neuron tp neuron n
     * with a random weight (uniformly distributed) value in range [0-1]
     * and return the link.
     * 
     * @param n
     *      the neuron to connect to.
     * 
     * @return a new link.
     */
    Link addInputLink(Neuron n);
    
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
     * Get a link by the id of the other neuron.
     * 
     * @param linkId 
     *      the id of the node to get the link of.
     *      
     * @return the link
     */
    Link getInputLink(int linkId);
    
    /**
     * Get link by the id of the other neuron ids.
     * 
     * @param ids 
     *      the ids of the nodes to get the links of.
     *      
     * @return the link.
     */
    Link[] getInputLinks(int... ids);
    
    /**
     * Get the input links to this neuron.
     * @return
     *      the input links to this neuron
     */
    Link[] getInputLinks();
    
    /**
     * Adds a link from this neuron to neuron n
     * with weight and return the link.
     * 
     * Also adds the input link for the n neuron.
     * Thus created a full 2 way connection between
     * this neuron and n.
     * 
     * @param n
     *      the node to connect to.
     *      
     * @param weight
     *      the weight value of this connection.
     * 
     * @return a new link.
     */
    Link addOutputLink(Neuron n, double weight);
    
    /**
     * Adds a link from this neuron to n
     * with a random weight (uniformly distributed) value in range [0-1]
     * and return the link.
     * 
     * @param n
     *      the node to connect to.
     * 
     * @return a new link.
     * 
     */
    Link addOutputLink(Neuron n);
    
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
     * Get a link by the id of the other neuron.
     * 
     * @param linkId 
     *      the id of the node to get the link of.
     *      
     * @return the link
     */
    Link getOutputLink(int linkId);
    
    /**
     * Get links by the id of the other neuron ids.
     * 
     * @param ids 
     *      the ids of the nodes to get the links of.
     *      
     * @return the link.
     */
    Link[] getOutputLinks(int... ids);
    
    /**
     * Get all links.
     * 
     * @return the links.
     */
    Link[] getOutputLinks();
    
    /**
     * Return the type of this neuron.
     * 
     * @return
     *      the id of this neuron
     */
    NeuronType getType();
    
    /**
     * Sets the type of neuron. In the rare event that one needs
     * to change the type of neuron this must be called.
     * 
     * @param t
     * 		the type of the neuron
     */
	void setType(NeuronType t);
    
    /**
     * Return the next child neuron.
     * 
     * @return
     *      the next child neuron
     *      and increment the child id counter.
     */
    Neuron getNextChild();

    /**
     * Return the next parent neuron.
     * 
     * @return
     *      the next parent neuron
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
     *      the ActivationFunction to use
     */
    void setActivationFunction(ActivationFunction f);
    
    /**
     * Get the activation function for this neuron.
     * 
     * @return
     *      the ActivationFunction of this neuron
     */
    ActivationFunction getActivationFunction();

    /**
     * Cause this neuron to activate,
     * sum up all the inputs * weights, and then
     * run them through the ActivationFunction finally
     * update the weights of the output links.
     * 
     * @param v
     *      the value to be fed forward to the next neuron
     * @param neuralNetContext
     * 		the neural network context to be passed along.
     * @return output value.
     */
    double feedforward(double v, NeuralNetContext neuralNetContext);

    /**
     * Calculate the error.
     * 
     * @return
     * 		the 1/2 MSE.
     */
	double getError();

	/**
	 * Propagate the error of this node to all its children.
	 * 
	 * @param error
	 * 		the error to propagate.
	 * @return
	 * 		propagated error value.
	 */
	double propagateError(double error);
	
	@Override
	int hashCode();
	
	@Override
	boolean equals(Object o);
}
