use crate::config::EvolutionConfig;
use crate::constants::{
    CANDIDATE_INDEX_SEED_STRIDE, CLASSIFICATION_ERROR_WEIGHT, CONFIGURED_LOSS_WEIGHT,
    EVALUATION_REPEAT_SEED_STRIDE, GENERATION_SEED_STRIDE, GROUP_RELATIVE_GENERALIZATION_WEIGHT,
    MAX_GROUP_RELATIVE_GENERALIZATION_ERROR,
};
use crate::genome::Genome;
use crate::learner::Learner;
use crate::problem::ProblemData;
use crate::random::Rng;

#[derive(Clone)]
pub(crate) struct Score {
    pub(crate) genome: Genome,
    pub(crate) score: f64,
    pub(crate) mean_squared_error: f64,
    pub(crate) configured_loss: f64,
    pub(crate) accuracy: f64,
    pub(crate) generalization_mse: f64,
    pub(crate) group_relative_generalization_error: f64,
    pub(crate) jitter_mse: f64,
    pub(crate) smoothness_penalty: f64,
    pub(crate) complexity: f64,
    pub(crate) predictive_free_energy: f64,
    pub(crate) sensory_prediction_energy: f64,
    pub(crate) latent_prediction_energy: f64,
    pub(crate) complexity_prior_energy: f64,
    pub(crate) generation: usize,
}

pub(crate) trait CandidateEvaluator: Sync {
    fn evaluate_with_complexity(
        &self,
        problem: &ProblemData,
        genome: &Genome,
        config: &EvolutionConfig,
        generation: usize,
        candidate_index: usize,
        precomputed_complexity: Option<f64>,
    ) -> Score;
}

pub(crate) struct LearnerCandidateEvaluator;

impl CandidateEvaluator for LearnerCandidateEvaluator {
    fn evaluate_with_complexity(
        &self,
        problem: &ProblemData,
        genome: &Genome,
        config: &EvolutionConfig,
        generation: usize,
        candidate_index: usize,
        precomputed_complexity: Option<f64>,
    ) -> Score {
        evaluate_with_learner(
            problem,
            genome,
            config,
            generation,
            candidate_index,
            precomputed_complexity,
        )
    }
}

fn evaluate_with_learner(
    problem: &ProblemData,
    genome: &Genome,
    config: &EvolutionConfig,
    generation: usize,
    candidate_index: usize,
    precomputed_complexity: Option<f64>,
) -> Score {
    let mut mse_total = 0.0;
    let mut loss_total = 0.0;
    let mut accuracy_total = 0.0;
    let mut generalization_total = 0.0;
    let mut group_relative_total = 0.0;
    let mut jitter_total = 0.0;
    let mut smoothness_total = 0.0;
    let mut predictive_free_energy_total = 0.0;
    let mut sensory_prediction_energy_total = 0.0;
    let mut latent_prediction_energy_total = 0.0;
    let mut complexity_prior_energy_total = 0.0;
    let complexity = precomputed_complexity.unwrap_or_else(|| genome.complexity(problem));
    let normalized_complexity = complexity / problem.complexity_scale;
    let candidate_seed = config.seed as u64 + CANDIDATE_INDEX_SEED_STRIDE * candidate_index as u64;
    for repeat in 0..config.evaluation_repeats {
        let seed = candidate_seed
            + GENERATION_SEED_STRIDE * generation as u64
            + EVALUATION_REPEAT_SEED_STRIDE * repeat as u64;
        let mut learner = Learner::new(problem, genome, Rng::new(seed));
        let mse = learner.train(config.max_epochs, config.target_mse);
        let configured_loss = learner.configured_loss();
        let accuracy = learner.accuracy();
        let grouped = learner.grouped_mean_squared_error(&problem.generalization_samples);
        let jitter_mse = learner.mean_squared_error(&problem.jitter_samples);
        let smoothness_penalty = learner.smoothness_penalty();
        let free_energy = learner.predictive_free_energy(normalized_complexity);
        mse_total += mse;
        loss_total += configured_loss;
        accuracy_total += accuracy;
        generalization_total += grouped.0;
        group_relative_total += grouped.1;
        jitter_total += jitter_mse;
        smoothness_total += smoothness_penalty;
        predictive_free_energy_total += free_energy.0;
        sensory_prediction_energy_total += free_energy.1;
        latent_prediction_energy_total += free_energy.2;
        complexity_prior_energy_total += free_energy.3;
    }
    let repeats = config.evaluation_repeats as f64;
    let mean_squared_error = mse_total / repeats;
    let configured_loss = loss_total / repeats;
    let accuracy = accuracy_total / repeats;
    let generalization_mse = generalization_total / repeats;
    let group_relative_generalization_error = group_relative_total / repeats;
    let jitter_mse = jitter_total / repeats;
    let smoothness_penalty = smoothness_total / repeats;
    let predictive_free_energy = predictive_free_energy_total / repeats;
    let sensory_prediction_energy = sensory_prediction_energy_total / repeats;
    let latent_prediction_energy = latent_prediction_energy_total / repeats;
    let complexity_prior_energy = complexity_prior_energy_total / repeats;
    let score = ObjectiveComponents {
        mean_squared_error,
        configured_loss,
        accuracy,
        generalization_mse,
        group_relative_generalization_error,
        jitter_mse,
        smoothness_penalty,
        complexity,
        predictive_free_energy,
    }
    .score(problem, config);
    Score {
        genome: genome.clone(),
        score,
        mean_squared_error,
        configured_loss,
        accuracy,
        generalization_mse,
        group_relative_generalization_error,
        jitter_mse,
        smoothness_penalty,
        complexity,
        predictive_free_energy,
        sensory_prediction_energy,
        latent_prediction_energy,
        complexity_prior_energy,
        generation,
    }
}

struct ObjectiveComponents {
    mean_squared_error: f64,
    configured_loss: f64,
    accuracy: f64,
    generalization_mse: f64,
    group_relative_generalization_error: f64,
    jitter_mse: f64,
    smoothness_penalty: f64,
    complexity: f64,
    predictive_free_energy: f64,
}

impl ObjectiveComponents {
    fn score(&self, problem: &ProblemData, config: &EvolutionConfig) -> f64 {
        if !self.is_finite() {
            return f64::INFINITY;
        }
        self.mean_squared_error
            + self.configured_loss * CONFIGURED_LOSS_WEIGHT
            + (1.0 - self.accuracy) * CLASSIFICATION_ERROR_WEIGHT
            + self.generalization_mse * config.generalization_weight
            + self.jitter_mse * config.jitter_weight
            + self.smoothness_penalty * config.smoothness_weight
            + (self.complexity / problem.complexity_scale) * config.complexity_penalty
            + self
                .group_relative_generalization_error
                .clamp(0.0, MAX_GROUP_RELATIVE_GENERALIZATION_ERROR)
                * GROUP_RELATIVE_GENERALIZATION_WEIGHT
            + self.predictive_free_energy * problem.free_energy_objective_weight
    }

    fn is_finite(&self) -> bool {
        [
            self.mean_squared_error,
            self.configured_loss,
            self.generalization_mse,
            self.group_relative_generalization_error,
            self.jitter_mse,
            self.smoothness_penalty,
            self.predictive_free_energy,
        ]
        .iter()
        .all(|value| value.is_finite())
    }
}
