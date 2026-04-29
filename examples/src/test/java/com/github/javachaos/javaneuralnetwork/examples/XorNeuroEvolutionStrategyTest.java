package com.github.javachaos.javaneuralnetwork.examples;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XorNeuroEvolutionStrategyTest {

    @Test
    void stagnationRampsReseedingBeforeFullReheat() {
        XorNeuroEvolution.EvolutionConfig config = new XorNeuroEvolution.EvolutionConfig(
                20,
                10,
                5,
                0.05,
                1,
                0.08,
                0.001,
                1,
                1,
                101L);
        int patience = XorNeuroEvolution.stagnationPatience(config);

        assertFalse(XorNeuroEvolution.reseeding(config, 0));
        assertEquals(0.06, XorNeuroEvolution.stagnationReseedFraction(config, 0), 1.0e-12);

        int midStale = Math.max(1, patience / 2);
        assertTrue(XorNeuroEvolution.reseeding(config, midStale));
        assertTrue(XorNeuroEvolution.stagnationReseedFraction(config, midStale) > 0.06);
        assertTrue(XorNeuroEvolution.stagnationReseedFraction(config, midStale) < 0.65);

        assertTrue(XorNeuroEvolution.reheated(config, patience));
        assertEquals(0.65, XorNeuroEvolution.stagnationReseedFraction(config, patience), 1.0e-12);
        assertTrue(XorNeuroEvolution.evolutionStrategySummary(config, patience).contains("reseed"));
    }

    @Test
    void stagnationReseedingRejectsNegativeStaleCounts() {
        XorNeuroEvolution.EvolutionConfig config = XorNeuroEvolution.EvolutionConfig.defaults();

        assertThrows(
                IllegalArgumentException.class,
                () -> XorNeuroEvolution.stagnationReseedFraction(config, -1));
    }
}
