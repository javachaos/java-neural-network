use crate::constants::{EPSILON, MAX_WEIGHT, NORMALIZATION_TARGET_NORM, SIGMOID_SATURATION};
use crate::problem::OutputGroup;

pub(crate) fn masked_dot(weights: &[f64], mask: &[bool], values: &[f64]) -> f64 {
    weights
        .iter()
        .zip(mask)
        .zip(values)
        .filter_map(|((weight, enabled), value)| enabled.then_some(weight * value))
        .sum()
}

pub(crate) fn normalize(weights: &mut [f64], mask: &[bool], normalization_strength: f64) {
    let norm = weights
        .iter()
        .zip(mask)
        .filter_map(|(weight, enabled)| enabled.then_some(weight * weight))
        .sum::<f64>()
        .sqrt();
    if norm <= NORMALIZATION_TARGET_NORM || norm == 0.0 {
        return;
    }
    let target_scale = NORMALIZATION_TARGET_NORM / norm;
    let scale = 1.0 - normalization_strength + normalization_strength * target_scale;
    for (weight, enabled) in weights.iter_mut().zip(mask) {
        if *enabled {
            *weight *= scale;
        }
    }
}

pub(crate) fn weighted_baseline_relative_error(
    groups: &[OutputGroup],
    group_totals: &[f64],
    baseline_totals: &[f64],
    group_counts: &[usize],
) -> f64 {
    let mut weighted_total = 0.0;
    let mut weight_total = 0.0;
    for (index, group) in groups.iter().enumerate() {
        if group_counts[index] == 0 {
            continue;
        }
        let group_mse = group_totals[index] / group_counts[index] as f64;
        let baseline_mse = baseline_totals[index] / group_counts[index] as f64;
        weighted_total += baseline_relative_error(group_mse, baseline_mse) * group.weight;
        weight_total += group.weight;
    }
    if weight_total == 0.0 {
        0.0
    } else {
        weighted_total / weight_total
    }
}

pub(crate) fn baseline_relative_error(
    mean_squared_error: f64,
    baseline_mean_squared_error: f64,
) -> f64 {
    if !mean_squared_error.is_finite() {
        return f64::INFINITY;
    }
    if !baseline_mean_squared_error.is_finite() || baseline_mean_squared_error <= EPSILON {
        return if mean_squared_error <= EPSILON {
            0.0
        } else {
            f64::INFINITY
        };
    }
    (mean_squared_error / baseline_mean_squared_error).max(0.0)
}

pub(crate) fn cosine_prediction_energy(
    actual: &[f64],
    prediction: &[f64],
    start_index: usize,
) -> f64 {
    let mut actual_norm = 0.0;
    let mut prediction_norm = 0.0;
    let mut dot = 0.0;
    for index in start_index..actual.len() {
        actual_norm += actual[index] * actual[index];
        prediction_norm += prediction[index] * prediction[index];
        dot += actual[index] * prediction[index];
    }
    if actual_norm <= EPSILON {
        return 0.0;
    }
    if prediction_norm <= EPSILON {
        return 1.0;
    }
    let cosine = (dot / (actual_norm * prediction_norm).sqrt()).clamp(-1.0, 1.0);
    0.5 * (1.0 - cosine)
}

pub(crate) fn prediction_matches(targets: &[f64], predictions: &[f64]) -> bool {
    if targets.len() == 1 {
        return (predictions[0] >= 0.5) == (targets[0] >= 0.5);
    }
    let max_target = targets.iter().copied().fold(f64::NEG_INFINITY, f64::max);
    let cutoff = 1.0e-9_f64.max(max_target * 0.5);
    let mut positives = 0usize;
    let mut weakest_positive = f64::INFINITY;
    let mut strongest_negative = f64::NEG_INFINITY;
    for (target, prediction) in targets.iter().zip(predictions) {
        if *target >= cutoff {
            positives += 1;
            weakest_positive = weakest_positive.min(*prediction);
        } else {
            strongest_negative = strongest_negative.max(*prediction);
        }
    }
    positives > 0 && weakest_positive > strongest_negative
}

pub(crate) fn mean(values: &[f64]) -> f64 {
    values.iter().sum::<f64>() / values.len() as f64
}

pub(crate) fn array_finite(values: &[f64]) -> bool {
    values.iter().all(|value| value.is_finite())
}

pub(crate) fn clamp_weight(value: f64) -> f64 {
    if value.is_finite() {
        value.clamp(-MAX_WEIGHT, MAX_WEIGHT)
    } else {
        0.0
    }
}

pub(crate) fn clip(value: f64, limit: f64) -> f64 {
    value.clamp(-limit, limit)
}

pub(crate) fn sigmoid(value: f64) -> f64 {
    if value > SIGMOID_SATURATION {
        1.0
    } else if value < -SIGMOID_SATURATION {
        0.0
    } else {
        1.0 / (1.0 + (-value).exp())
    }
}
