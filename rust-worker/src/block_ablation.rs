use std::io::Write;

use crate::config::EvolutionConfig;
use crate::constants::{
    BLOCK_ABLATION_SEED_OFFSET, DEFAULT_BLOCK_ABLATION_PROBE_HIDDEN_NEURONS,
    DEFAULT_BLOCK_ABLATION_PROBE_MAX_EPOCHS, DEFAULT_BLOCK_ABLATION_TRIALS, PROTOCOL_VERSION,
    WORKER_NAME,
};
use crate::genome::Genome;
use crate::learner::Learner;
use crate::operators::{Activation, InputRepresentation, LearningSchedule, LossFunction};
use crate::problem::{OutputGroup, ProblemData, Sample, SmoothnessProbe};
use crate::protocol::{number_field, string_field, write_event};
use crate::random::Rng;
use crate::worker_service::{RunControl, RunControlDecision};

pub(crate) fn run_block_ablation(
    json: &str,
    writer: &mut dyn Write,
    control: &RunControl,
) -> Result<(), String> {
    let problem_key = string_field(json, "key").unwrap_or_else(|| "unknown-problem".to_string());
    let payload = string_field(json, "problemPayloadHex")
        .ok_or_else(|| "Block ablation request missing problemPayloadHex.".to_string())?;
    let block_genome = Genome::decode(
        &string_field(json, "blockGenome")
            .ok_or_else(|| "Block ablation request missing blockGenome.".to_string())?,
    )?;
    let control_mode = ControlMode::from_json(json);
    let problem = ProblemData::from_hex(&payload)?;
    let feature_slice = FeatureSlice::from_json(json);
    let feature_indices = feature_slice.indices(&problem);
    if feature_indices.is_empty() {
        return Err(format!(
            "Feature slice '{}' did not select any block outputs.",
            feature_slice.name()
        ));
    }
    let config = EvolutionConfig::from_json(json);
    let trials = number_field(json, "probeTrials")
        .unwrap_or(DEFAULT_BLOCK_ABLATION_TRIALS)
        .max(1) as usize;
    let probe_hidden_neurons = number_field(json, "probeHiddenNeurons")
        .unwrap_or(DEFAULT_BLOCK_ABLATION_PROBE_HIDDEN_NEURONS)
        .max(1) as usize;
    let probe_max_epochs = number_field(json, "probeMaxEpochs")
        .unwrap_or(DEFAULT_BLOCK_ABLATION_PROBE_MAX_EPOCHS)
        .max(1) as usize;

    write_event(
        writer,
        "blockAblationAccepted",
        "running",
        &format!(
            "Accepted {} frozen-block ablation for '{problem_key}' with {trials} probe trials using '{}' features.",
            control_mode.name(),
            feature_slice.name()
        ),
    )
    .map_err(|error| error.to_string())?;

    let block_seed = config.seed as u64 + BLOCK_ABLATION_SEED_OFFSET;
    let active_block_genome = match control_mode {
        ControlMode::Champion | ControlMode::Shuffled => block_genome,
        ControlMode::Random => Genome::random(&mut Rng::new(block_seed + 17)),
    };
    let mut block_learner = Learner::new(&problem, &active_block_genome, Rng::new(block_seed));
    let block_training_mse = if control_mode.trains_block() {
        block_learner.train(config.max_epochs, config.target_mse)
    } else {
        block_learner.mean_squared_error(&problem.training_samples)
    };
    let block_generalization_mse =
        block_learner.mean_squared_error(&problem.generalization_samples);
    let mixed_problem = if matches!(control_mode, ControlMode::Shuffled) {
        augment_problem_shuffled(&problem, &mut block_learner, &feature_indices)?
    } else {
        augment_problem(&problem, &mut block_learner, &feature_indices)?
    };
    let probe_genome = probe_genome(probe_hidden_neurons);
    let mut totals = ProbeTotals::default();

    for trial in 0..trials {
        if matches!(control.wait_if_paused(), RunControlDecision::Stop) {
            write_event(
                writer,
                "blockAblationStopped",
                "stopped",
                "Stopped frozen-block ablation.",
            )
            .map_err(|error| error.to_string())?;
            return Ok(());
        }
        let seed = config.seed as u64 + BLOCK_ABLATION_SEED_OFFSET + 1_000_003 * (trial as u64 + 1);
        let baseline = probe_once(
            &problem,
            &probe_genome,
            probe_max_epochs,
            config.target_mse,
            seed,
        );
        let mixed = probe_once(
            &mixed_problem,
            &probe_genome,
            probe_max_epochs,
            config.target_mse,
            seed,
        );
        totals.add(baseline, mixed);
    }

    write_result(
        writer,
        &problem_key,
        trials,
        probe_hidden_neurons,
        probe_max_epochs,
        problem.input_dimensions,
        mixed_problem.input_dimensions,
        problem.output_dimensions,
        control_mode,
        feature_slice,
        feature_indices.len(),
        block_training_mse,
        block_generalization_mse,
        totals,
    )
    .map_err(|error| error.to_string())
}

#[derive(Clone, Copy)]
enum ControlMode {
    Champion,
    Random,
    Shuffled,
}

impl ControlMode {
    fn from_json(json: &str) -> Self {
        match string_field(json, "controlMode")
            .unwrap_or_else(|| "champion".to_string())
            .trim()
            .to_ascii_lowercase()
            .as_str()
        {
            "random" => Self::Random,
            "shuffled" | "shuffle" => Self::Shuffled,
            _ => Self::Champion,
        }
    }

    fn name(self) -> &'static str {
        match self {
            Self::Champion => "champion",
            Self::Random => "random",
            Self::Shuffled => "shuffled",
        }
    }

    fn trains_block(self) -> bool {
        matches!(self, Self::Champion | Self::Shuffled)
    }
}

#[derive(Clone, Copy)]
enum FeatureSlice {
    All,
    Final,
    Query,
    Key,
    Value,
    Qkv,
    Attention,
    AttentionHead0,
    AttentionHead1,
    Group(usize),
}

impl FeatureSlice {
    fn from_json(json: &str) -> Self {
        let requested = string_field(json, "featureSlice")
            .unwrap_or_else(|| "all".to_string())
            .trim()
            .to_ascii_lowercase();
        match requested.as_str() {
            "final" | "residual" | "y" => Self::Final,
            "query" | "queries" | "q" => Self::Query,
            "key" | "keys" | "k" => Self::Key,
            "value" | "values" | "v" => Self::Value,
            "qkv" | "projections" => Self::Qkv,
            "attention" | "attn" | "heads" => Self::Attention,
            "attention0" | "attn0" | "head0" => Self::AttentionHead0,
            "attention1" | "attn1" | "head1" => Self::AttentionHead1,
            value if value.starts_with("group:") => {
                value[6..].parse().map(Self::Group).unwrap_or(Self::All)
            }
            value if value.starts_with("group") => {
                value[5..].parse().map(Self::Group).unwrap_or(Self::All)
            }
            _ => Self::All,
        }
    }

    fn name(self) -> String {
        match self {
            Self::All => "all".to_string(),
            Self::Final => "final".to_string(),
            Self::Query => "q".to_string(),
            Self::Key => "k".to_string(),
            Self::Value => "v".to_string(),
            Self::Qkv => "qkv".to_string(),
            Self::Attention => "attention".to_string(),
            Self::AttentionHead0 => "attention0".to_string(),
            Self::AttentionHead1 => "attention1".to_string(),
            Self::Group(index) => format!("group:{index}"),
        }
    }

    fn indices(self, problem: &ProblemData) -> Vec<usize> {
        match self {
            Self::All => range_indices(0, problem.output_dimensions, problem.output_dimensions),
            Self::Final => range_indices(0, 9, problem.output_dimensions),
            Self::Query => range_indices(9, 21, problem.output_dimensions),
            Self::Key => range_indices(21, 33, problem.output_dimensions),
            Self::Value => range_indices(33, 45, problem.output_dimensions),
            Self::Qkv => range_indices(9, 45, problem.output_dimensions),
            Self::Attention => range_indices(45, 63, problem.output_dimensions),
            Self::AttentionHead0 => range_indices(45, 54, problem.output_dimensions),
            Self::AttentionHead1 => range_indices(54, 63, problem.output_dimensions),
            Self::Group(index) => problem
                .output_groups
                .get(index)
                .map(|group| range_indices(group.start, group.end, problem.output_dimensions))
                .unwrap_or_default(),
        }
    }
}

fn range_indices(start: usize, end: usize, length: usize) -> Vec<usize> {
    let clipped_start = start.min(length);
    let clipped_end = end.min(length);
    (clipped_start..clipped_end).collect()
}

fn augment_problem(
    problem: &ProblemData,
    block: &mut Learner<'_>,
    feature_indices: &[usize],
) -> Result<ProblemData, String> {
    let training_samples = augment_samples(&problem.training_samples, block, feature_indices)?;
    let generalization_samples =
        augment_samples(&problem.generalization_samples, block, feature_indices)?;
    let jitter_samples = augment_samples(&problem.jitter_samples, block, feature_indices)?;
    let smoothness_probes =
        augment_smoothness_probes(&problem.smoothness_probes, block, feature_indices)?;
    let kernel_centers = problem
        .kernel_centers
        .iter()
        .map(|input| augment_input(input, block, feature_indices))
        .collect::<Result<Vec<_>, _>>()?;
    let input_dimensions = problem.input_dimensions + feature_indices.len();
    let training_target_means = target_means(&training_samples, problem.output_dimensions);
    Ok(ProblemData {
        input_dimensions,
        output_dimensions: problem.output_dimensions,
        classification: problem.classification,
        stateful_samples: problem.stateful_samples,
        complexity_scale: problem.complexity_scale,
        free_energy_objective_weight: problem.free_energy_objective_weight,
        free_energy_sensory_weight: problem.free_energy_sensory_weight,
        free_energy_latent_weight: problem.free_energy_latent_weight,
        free_energy_complexity_weight: problem.free_energy_complexity_weight,
        free_energy_maximum: problem.free_energy_maximum,
        training_samples,
        generalization_samples,
        jitter_samples,
        smoothness_probes,
        kernel_centers,
        output_groups: problem
            .output_groups
            .iter()
            .map(|group| OutputGroup {
                start: group.start,
                end: group.end,
                weight: group.weight,
            })
            .collect(),
        training_target_means,
    })
}

fn augment_problem_shuffled(
    problem: &ProblemData,
    block: &mut Learner<'_>,
    feature_indices: &[usize],
) -> Result<ProblemData, String> {
    let training_samples =
        augment_samples_shuffled(&problem.training_samples, block, feature_indices)?;
    let generalization_samples =
        augment_samples_shuffled(&problem.generalization_samples, block, feature_indices)?;
    let jitter_samples = augment_samples_shuffled(&problem.jitter_samples, block, feature_indices)?;
    let smoothness_probes =
        augment_smoothness_probes(&problem.smoothness_probes, block, feature_indices)?;
    let kernel_centers = problem
        .kernel_centers
        .iter()
        .map(|input| augment_input(input, block, feature_indices))
        .collect::<Result<Vec<_>, _>>()?;
    let input_dimensions = problem.input_dimensions + feature_indices.len();
    let training_target_means = target_means(&training_samples, problem.output_dimensions);
    Ok(ProblemData {
        input_dimensions,
        output_dimensions: problem.output_dimensions,
        classification: problem.classification,
        stateful_samples: problem.stateful_samples,
        complexity_scale: problem.complexity_scale,
        free_energy_objective_weight: problem.free_energy_objective_weight,
        free_energy_sensory_weight: problem.free_energy_sensory_weight,
        free_energy_latent_weight: problem.free_energy_latent_weight,
        free_energy_complexity_weight: problem.free_energy_complexity_weight,
        free_energy_maximum: problem.free_energy_maximum,
        training_samples,
        generalization_samples,
        jitter_samples,
        smoothness_probes,
        kernel_centers,
        output_groups: problem
            .output_groups
            .iter()
            .map(|group| OutputGroup {
                start: group.start,
                end: group.end,
                weight: group.weight,
            })
            .collect(),
        training_target_means,
    })
}

fn augment_samples(
    samples: &[Sample],
    block: &mut Learner<'_>,
    feature_indices: &[usize],
) -> Result<Vec<Sample>, String> {
    samples
        .iter()
        .map(|sample| {
            Ok(Sample {
                input: augment_input(&sample.input, block, feature_indices)?,
                targets: sample.targets.clone(),
            })
        })
        .collect()
}

fn augment_samples_shuffled(
    samples: &[Sample],
    block: &mut Learner<'_>,
    feature_indices: &[usize],
) -> Result<Vec<Sample>, String> {
    if samples.is_empty() {
        return Ok(Vec::new());
    }
    let predictions = samples
        .iter()
        .map(|sample| {
            let prediction = block
                .predict_vector(&sample.input)
                .ok_or_else(|| "Frozen block produced a non-finite prediction.".to_string())?;
            Ok::<Vec<f64>, String>(select_prediction(&prediction, feature_indices))
        })
        .collect::<Result<Vec<_>, _>>()?;
    let shift = shuffled_shift(samples.len());
    samples
        .iter()
        .enumerate()
        .map(|(index, sample)| {
            let prediction = &predictions[(index + shift) % predictions.len()];
            Ok(Sample {
                input: append_prediction(&sample.input, prediction),
                targets: sample.targets.clone(),
            })
        })
        .collect()
}

fn augment_smoothness_probes(
    probes: &[SmoothnessProbe],
    block: &mut Learner<'_>,
    feature_indices: &[usize],
) -> Result<Vec<SmoothnessProbe>, String> {
    probes
        .iter()
        .map(|probe| {
            Ok(SmoothnessProbe {
                center: augment_sample(&probe.center, block, feature_indices)?,
                plus: augment_sample(&probe.plus, block, feature_indices)?,
                minus: augment_sample(&probe.minus, block, feature_indices)?,
            })
        })
        .collect()
}

fn augment_sample(
    sample: &Sample,
    block: &mut Learner<'_>,
    feature_indices: &[usize],
) -> Result<Sample, String> {
    Ok(Sample {
        input: augment_input(&sample.input, block, feature_indices)?,
        targets: sample.targets.clone(),
    })
}

fn augment_input(
    input: &[f64],
    block: &mut Learner<'_>,
    feature_indices: &[usize],
) -> Result<Vec<f64>, String> {
    let prediction = block
        .predict_vector(input)
        .ok_or_else(|| "Frozen block produced a non-finite prediction.".to_string())?;
    Ok(append_prediction(
        input,
        &select_prediction(&prediction, feature_indices),
    ))
}

fn select_prediction(prediction: &[f64], feature_indices: &[usize]) -> Vec<f64> {
    feature_indices
        .iter()
        .filter_map(|index| prediction.get(*index).copied())
        .collect()
}

fn append_prediction(input: &[f64], prediction: &[f64]) -> Vec<f64> {
    let mut augmented = Vec::with_capacity(input.len() + prediction.len());
    augmented.extend_from_slice(input);
    augmented.extend_from_slice(prediction);
    augmented
}

fn shuffled_shift(length: usize) -> usize {
    if length < 2 { 0 } else { (length / 2).max(1) }
}

fn target_means(samples: &[Sample], outputs: usize) -> Vec<f64> {
    let mut means = vec![0.0; outputs];
    if samples.is_empty() {
        return means;
    }
    for sample in samples {
        for (index, value) in sample.targets.iter().enumerate() {
            means[index] += value;
        }
    }
    for value in &mut means {
        *value /= samples.len() as f64;
    }
    means
}

fn probe_once(
    problem: &ProblemData,
    genome: &Genome,
    max_epochs: usize,
    target_mse: f64,
    seed: u64,
) -> ProbeMetrics {
    let mut learner = Learner::new(problem, genome, Rng::new(seed));
    let training_mse = learner.train(max_epochs, target_mse);
    let generalization_mse = learner.mean_squared_error(&problem.generalization_samples);
    ProbeMetrics {
        training_mse,
        generalization_mse,
    }
}

fn probe_genome(hidden_neurons: usize) -> Genome {
    Genome {
        hidden_neurons,
        hidden_layers: 1,
        recurrent_connections: 0,
        memory_cells: 0,
        connection_density: 1.0,
        hidden_activation: Activation::Tanh,
        output_activation: Activation::Sigmoid,
        input_representation: InputRepresentation::Raw,
        loss_function: LossFunction::MeanSquared,
        learning_schedule: LearningSchedule::StepDecay,
        hebbian_update: false,
        gradient_update: true,
        normalization: true,
        kernel_memory: false,
        phase_encoding: false,
        second_derivative_estimate: false,
        input_learning_rate: 0.25,
        output_learning_rate: 0.25,
        recurrent_learning_rate: 0.0,
        hebbian_learning_rate: 0.0,
        memory_learning_rate: 0.0,
        momentum: 0.15,
        weight_decay: 1.0e-4,
        normalization_strength: 0.15,
        activation_slope: 1.0,
        error_clip: 2.0,
        kernel_sharpness: 1.0,
    }
}

#[derive(Clone, Copy)]
struct ProbeMetrics {
    training_mse: f64,
    generalization_mse: f64,
}

#[derive(Default, Clone, Copy)]
struct ProbeTotals {
    baseline_training_mse: f64,
    baseline_generalization_mse: f64,
    mixed_training_mse: f64,
    mixed_generalization_mse: f64,
    mixed_wins: usize,
}

impl ProbeTotals {
    fn add(&mut self, baseline: ProbeMetrics, mixed: ProbeMetrics) {
        self.baseline_training_mse += baseline.training_mse;
        self.baseline_generalization_mse += baseline.generalization_mse;
        self.mixed_training_mse += mixed.training_mse;
        self.mixed_generalization_mse += mixed.generalization_mse;
        if mixed.generalization_mse < baseline.generalization_mse {
            self.mixed_wins += 1;
        }
    }
}

fn write_result(
    writer: &mut dyn Write,
    problem_key: &str,
    trials: usize,
    probe_hidden_neurons: usize,
    probe_max_epochs: usize,
    original_input_dimensions: usize,
    mixed_input_dimensions: usize,
    block_output_dimensions: usize,
    control_mode: ControlMode,
    feature_slice: FeatureSlice,
    mixed_feature_dimensions: usize,
    block_training_mse: f64,
    block_generalization_mse: f64,
    totals: ProbeTotals,
) -> std::io::Result<()> {
    let trials_f64 = trials as f64;
    let baseline_training_mse = totals.baseline_training_mse / trials_f64;
    let baseline_generalization_mse = totals.baseline_generalization_mse / trials_f64;
    let mixed_training_mse = totals.mixed_training_mse / trials_f64;
    let mixed_generalization_mse = totals.mixed_generalization_mse / trials_f64;
    let absolute_delta = baseline_generalization_mse - mixed_generalization_mse;
    let relative_improvement = if baseline_generalization_mse > 0.0 {
        absolute_delta / baseline_generalization_mse
    } else {
        0.0
    };
    writeln!(
        writer,
        "{{\"type\":\"blockAblation\",\"protocol\":{},\"worker\":\"{}\",\"status\":\"completed\",\"problemKey\":\"{}\",\"controlMode\":\"{}\",\"featureSlice\":\"{}\",\"trials\":{},\"probeHiddenNeurons\":{},\"probeMaxEpochs\":{},\"originalInputDimensions\":{},\"mixedInputDimensions\":{},\"mixedFeatureDimensions\":{},\"blockOutputDimensions\":{},\"blockTrainingMse\":{},\"blockGeneralizationMse\":{},\"baselineTrainingMse\":{},\"baselineGeneralizationMse\":{},\"mixedTrainingMse\":{},\"mixedGeneralizationMse\":{},\"absoluteGeneralizationDelta\":{},\"relativeGeneralizationImprovement\":{},\"mixedWins\":{},\"message\":\"{}\"}}",
        PROTOCOL_VERSION,
        escape_json(WORKER_NAME),
        escape_json(problem_key),
        control_mode.name(),
        escape_json(&feature_slice.name()),
        trials,
        probe_hidden_neurons,
        probe_max_epochs,
        original_input_dimensions,
        mixed_input_dimensions,
        mixed_feature_dimensions,
        block_output_dimensions,
        json_number(block_training_mse),
        json_number(block_generalization_mse),
        json_number(baseline_training_mse),
        json_number(baseline_generalization_mse),
        json_number(mixed_training_mse),
        json_number(mixed_generalization_mse),
        json_number(absolute_delta),
        json_number(relative_improvement),
        totals.mixed_wins,
        escape_json("Frozen-block ablation completed.")
    )?;
    writer.flush()
}

fn json_number(value: f64) -> String {
    if value.is_finite() {
        value.to_string()
    } else {
        "1.0e309".to_string()
    }
}

fn escape_json(value: &str) -> String {
    let mut escaped = String::with_capacity(value.len());
    for character in value.chars() {
        match character {
            '"' => escaped.push_str("\\\""),
            '\\' => escaped.push_str("\\\\"),
            '\n' => escaped.push_str("\\n"),
            '\r' => escaped.push_str("\\r"),
            '\t' => escaped.push_str("\\t"),
            other => escaped.push(other),
        }
    }
    escaped
}
