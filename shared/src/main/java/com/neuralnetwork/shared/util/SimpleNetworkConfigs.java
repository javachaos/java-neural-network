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
     * Simple network config 5 inputs, 4 hidden, 3 hidden, 4 hidden, 5 outputs.
     */
    public static final NetworkConfig CONFIG_5_4_3_4_5 =
            new NetworkConfig(5, 5, new int[]{4, 3, 4});
}
