use crate::constants::*;
use crate::protocol::{double_field, number_field};

#[derive(Clone)]
pub(crate) struct EvolutionConfig {
    pub(crate) population_size: usize,
    pub(crate) generations: usize,
    pub(crate) max_epochs: usize,
    pub(crate) target_mse: f64,
    pub(crate) evaluation_repeats: usize,
    pub(crate) mutation_intensity: f64,
    pub(crate) complexity_penalty: f64,
    pub(crate) max_mutations_per_child: usize,
    pub(crate) generalization_weight: f64,
    pub(crate) jitter_weight: f64,
    pub(crate) smoothness_weight: f64,
    pub(crate) progress_interval_nanos: i64,
    pub(crate) parallelism: usize,
    pub(crate) seed_lanes: usize,
    pub(crate) seed: i64,
}

impl EvolutionConfig {
    pub(crate) fn from_json(json: &str) -> Self {
        Self {
            population_size: number_field(json, "populationSize")
                .unwrap_or(DEFAULT_POPULATION_SIZE)
                .max(4) as usize,
            generations: number_field(json, "generations")
                .unwrap_or(DEFAULT_GENERATIONS)
                .max(1) as usize,
            max_epochs: number_field(json, "maxEpochs")
                .unwrap_or(DEFAULT_MAX_EPOCHS)
                .max(1) as usize,
            target_mse: double_field(json, "targetMeanSquaredError")
                .unwrap_or(DEFAULT_TARGET_MSE)
                .max(1.0e-12),
            evaluation_repeats: number_field(json, "evaluationRepeats")
                .unwrap_or(DEFAULT_EVALUATION_REPEATS)
                .max(1) as usize,
            mutation_intensity: double_field(json, "mutationIntensity")
                .unwrap_or(DEFAULT_MUTATION_INTENSITY)
                .max(0.0),
            complexity_penalty: double_field(json, "complexityPenalty")
                .unwrap_or(DEFAULT_COMPLEXITY_PENALTY)
                .max(0.0),
            max_mutations_per_child: number_field(json, "maxMutationsPerChild")
                .unwrap_or(DEFAULT_MAX_MUTATIONS_PER_CHILD)
                .max(1) as usize,
            generalization_weight: double_field(json, "generalizationWeight")
                .unwrap_or(DEFAULT_GENERALIZATION_WEIGHT)
                .max(0.0),
            jitter_weight: double_field(json, "jitterWeight")
                .unwrap_or(DEFAULT_JITTER_WEIGHT)
                .max(0.0),
            smoothness_weight: double_field(json, "smoothnessWeight")
                .unwrap_or(DEFAULT_SMOOTHNESS_WEIGHT)
                .max(0.0),
            progress_interval_nanos: number_field(json, "progressIntervalNanos")
                .unwrap_or(DEFAULT_PROGRESS_INTERVAL_NANOS)
                .max(0),
            parallelism: number_field(json, "parallelism")
                .unwrap_or(DEFAULT_PARALLELISM)
                .max(1) as usize,
            seed_lanes: number_field(json, "seedLanes")
                .unwrap_or(DEFAULT_SEED_LANES)
                .max(1)
                .min(MAX_SEED_LANES as i64) as usize,
            seed: number_field(json, "seed").unwrap_or(DEFAULT_SEED),
        }
    }
}
