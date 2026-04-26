package com.github.javachaos.javaneuralnetwork.examples;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Immutable catalog backed by neuro-evolution problem providers.
 */
public final class DefaultNeuroEvolutionProblemCatalog implements NeuroEvolutionProblemCatalog {

    private static final int DEFAULT_GRID_SIZE = 9;

    private final List<NeuroEvolutionProblemProvider> providers;
    private final String defaultProblemKey;

    public DefaultNeuroEvolutionProblemCatalog() {
        this(builtInProviders(), "xor");
    }

    public DefaultNeuroEvolutionProblemCatalog(final List<NeuroEvolutionProblemProvider> additionalProviders) {
        this(combine(builtInProviders(), additionalProviders), "xor");
    }

    public DefaultNeuroEvolutionProblemCatalog(
            final List<NeuroEvolutionProblemProvider> providers,
            final String defaultProblemKey) {
        this.providers = validatedProviders(providers);
        this.defaultProblemKey = NeuroEvolutionProblem.normalizedName(defaultProblemKey);
        find(this.defaultProblemKey, DEFAULT_GRID_SIZE);
    }

    @Override
    public List<String> problemKeys() {
        List<String> names = new ArrayList<>(providers.size());
        for (NeuroEvolutionProblemProvider provider : providers) {
            names.add(provider.create(DEFAULT_GRID_SIZE).key());
        }
        return List.copyOf(names);
    }

    @Override
    public List<NeuroEvolutionProblem> problems(final int gridSize) {
        List<NeuroEvolutionProblem> problems = new ArrayList<>(providers.size());
        for (NeuroEvolutionProblemProvider provider : providers) {
            problems.add(provider.create(gridSize));
        }
        return List.copyOf(problems);
    }

    @Override
    public NeuroEvolutionProblem find(final String name, final int gridSize) {
        for (NeuroEvolutionProblemProvider provider : providers) {
            NeuroEvolutionProblem problem = provider.create(gridSize);
            if (problem.matches(name)) {
                return problem;
            }
        }
        throw new IllegalArgumentException(
                "Unknown neuro-evolution problem '" + name + "'. Supported problems: " + problemKeys());
    }

    @Override
    public NeuroEvolutionProblem defaultProblem(final int gridSize) {
        return find(defaultProblemKey, gridSize);
    }

    private static List<NeuroEvolutionProblemProvider> builtInProviders() {
        return List.of(
                XorProblem::new,
                AndProblem::new,
                OrProblem::new,
                RadialBumpProblem::new,
                CircleProblem::new,
                RingProblem::new,
                CheckerboardProblem::new,
                SineWaveProblem::new,
                IdentityAutoencoderProblem::new,
                TransformerAttentionProblem::new,
                TransformerBlockProblem::new,
                TextTrainingMettleProblem::new,
                SpiralBandsProblem::new,
                MettleTestProblem::new);
    }

    private static List<NeuroEvolutionProblemProvider> combine(
            final List<NeuroEvolutionProblemProvider> builtIns,
            final List<NeuroEvolutionProblemProvider> additionalProviders) {
        Objects.requireNonNull(additionalProviders, "Additional problem providers cannot be null.");
        List<NeuroEvolutionProblemProvider> combined = new ArrayList<>(builtIns.size() + additionalProviders.size());
        combined.addAll(builtIns);
        combined.addAll(additionalProviders);
        return List.copyOf(combined);
    }

    private static List<NeuroEvolutionProblemProvider> validatedProviders(
            final List<NeuroEvolutionProblemProvider> providers) {
        Objects.requireNonNull(providers, "Problem providers cannot be null.");
        List<NeuroEvolutionProblemProvider> copy = new ArrayList<>(providers.size());
        for (NeuroEvolutionProblemProvider provider : providers) {
            Objects.requireNonNull(provider, "Problem providers cannot contain null entries.");
            NeuroEvolutionProblem candidate = provider.create(DEFAULT_GRID_SIZE);
            for (NeuroEvolutionProblemProvider existingProvider : copy) {
                NeuroEvolutionProblem existing = existingProvider.create(DEFAULT_GRID_SIZE);
                validateNoConflict(existing, candidate);
            }
            copy.add(provider);
        }
        if (copy.isEmpty()) {
            throw new IllegalArgumentException("Problem providers cannot be empty.");
        }
        return List.copyOf(copy);
    }

    private static void validateNoConflict(
            final NeuroEvolutionProblem existing,
            final NeuroEvolutionProblem candidate) {
        if (existing.matches(candidate.key())) {
            throw new IllegalArgumentException(
                    "Problem key '" + candidate.key() + "' is already provided by " + existing.name() + ".");
        }
        for (String alias : candidate.aliases()) {
            if (existing.matches(alias)) {
                throw new IllegalArgumentException(
                        "Problem alias '" + alias + "' conflicts with provided problem " + existing.name() + ".");
            }
        }
    }
}
