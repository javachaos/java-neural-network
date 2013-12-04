package com.neuralnetwork.shared.util;

/**
 * Class containing simple network configurations.
 * 
 * Configuration naming follows the following format.
 * 
 * CONFIG_#INPUTS_#OUTPUTS_#HIDDEN
 * 
 * This file is intended to be used in testing.
 * @author fred
 *
 */
public final class SimpleNetworkConfigs {
    
    /**
     * Unused Constructor.
     */
    private SimpleNetworkConfigs() {
    }
    
    /**
     * Simple network config 5 inputs, 5 outputs, 3 hidden.
     */
    public static final NetworkConfig CONFIG_5_5_3 = 
            new NetworkConfig(5, 5, new int[]{4, 3, 4});
}
