package com.github.javachaos.javaneuralnetwork.examples;

import com.github.javachaos.javaneuralnetwork.examples.RichEvolvedProblemLearner.NetworkSnapshot;

import java.util.Objects;

/**
 * Renderable best-champion view used by GUI and worker progress events.
 */
public record NeuroEvolutionChampionView(
        XorNeuroEvolution.CandidateScore score,
        NetworkSnapshot snapshot) {

    public NeuroEvolutionChampionView {
        Objects.requireNonNull(score, "Score cannot be null.");
        Objects.requireNonNull(snapshot, "Snapshot cannot be null.");
    }
}
