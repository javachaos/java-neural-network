package com.github.javachaos.javaneuralnetwork.examples.tests;

import com.github.javachaos.javaneuralnetwork.examples.XorLearningRuleEvolution;
import com.github.javachaos.javaneuralnetwork.examples.XorLearningRuleGenome;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XorLearningRuleEvolutionTest {

    @Test
    void evolvesCandidateRulesForXor() {
        XorLearningRuleEvolution.EvolutionConfig config =
                new XorLearningRuleEvolution.EvolutionConfig(
                        12,
                        4,
                        120,
                        0.02,
                        1,
                        0.05,
                        7L);

        XorLearningRuleEvolution.EvolutionResult result = XorLearningRuleEvolution.evolve(config);

        assertEquals(4, result.champions().size());
        assertTrue(Double.isFinite(result.best().score()));
        assertTrue(result.best().accuracy() >= 0.5);
    }

    @Test
    void mutatesGenomeWithinValidSearchSpace() {
        XorLearningRuleGenome genome = XorLearningRuleGenome.random(new Random(1L));
        XorLearningRuleGenome mutated = genome.mutate(new Random(2L), 0.15);

        XorLearningRuleEvolution.EvolutionConfig config =
                new XorLearningRuleEvolution.EvolutionConfig(
                        4,
                        1,
                        20,
                        0.02,
                        1,
                        0.05,
                        11L);

        assertTrue(Double.isFinite(XorLearningRuleEvolution.evaluate(mutated, config, 0).score()));
    }
}
