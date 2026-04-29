use std::io::{self, Write};

use crate::constants::{PROTOCOL_VERSION, WORKER_NAME};
use crate::scoring::Score;

#[derive(Clone, Copy)]
pub(crate) struct LaneTelemetry {
    pub(crate) seed_lanes: usize,
    pub(crate) best_lane: usize,
    pub(crate) lane_deaths: usize,
    pub(crate) max_lane_stale: usize,
    pub(crate) lane_died: bool,
}

pub(crate) fn write_event(
    writer: &mut dyn Write,
    kind: &str,
    status: &str,
    message: &str,
) -> io::Result<()> {
    write_event_context(writer, kind, status, message, None, None)
}

pub(crate) fn write_event_context(
    writer: &mut dyn Write,
    kind: &str,
    status: &str,
    message: &str,
    cluster_id: Option<&str>,
    storage_mode: Option<&str>,
) -> io::Result<()> {
    let mut context = String::new();
    if let Some(value) = cluster_id {
        context.push_str(&format!(",\"clusterId\":\"{}\"", escape_json(value)));
    }
    if let Some(value) = storage_mode {
        context.push_str(&format!(",\"storageMode\":\"{}\"", escape_json(value)));
    }
    writeln!(
        writer,
        "{{\"type\":\"{}\",\"protocol\":{},\"worker\":\"{}\"{},\"status\":\"{}\",\"message\":\"{}\"}}",
        escape_json(kind),
        PROTOCOL_VERSION,
        escape_json(WORKER_NAME),
        context,
        escape_json(status),
        escape_json(message)
    )?;
    writer.flush()
}

pub(crate) fn write_score_event(
    writer: &mut dyn Write,
    kind: &str,
    status: &str,
    cluster_id: &str,
    storage_mode: &str,
    problem_key: &str,
    generation: usize,
    stale_generations: usize,
    stagnation_patience: usize,
    lane_telemetry: LaneTelemetry,
    mutation_intensity: f64,
    reseed_fraction: f64,
    reheated: bool,
    reseeding: bool,
    score: &Score,
) -> io::Result<()> {
    writeln!(
        writer,
        "{{\"type\":\"{}\",\"protocol\":{},\"worker\":\"{}\",\"clusterId\":\"{}\",\"storageMode\":\"{}\",\"status\":\"{}\",\"problemKey\":\"{}\",\"generation\":{},\"staleGenerations\":{},\"stagnationPatience\":{},\"seedLanes\":{},\"bestLane\":{},\"laneDeaths\":{},\"maxLaneStale\":{},\"laneDied\":{},\"mutationIntensity\":{},\"reseedFraction\":{},\"reheated\":{},\"reseeding\":{},\"scoreGeneration\":{},\"score\":{},\"meanSquaredError\":{},\"configuredLoss\":{},\"accuracy\":{},\"generalizationMeanSquaredError\":{},\"groupRelativeGeneralizationError\":{},\"jitterMeanSquaredError\":{},\"smoothnessPenalty\":{},\"complexity\":{},\"predictiveFreeEnergy\":{},\"sensoryPredictionEnergy\":{},\"latentPredictionEnergy\":{},\"complexityPriorEnergy\":{},\"genome\":\"{}\",\"message\":\"{}\"}}",
        escape_json(kind),
        PROTOCOL_VERSION,
        escape_json(WORKER_NAME),
        escape_json(cluster_id),
        escape_json(storage_mode),
        escape_json(status),
        escape_json(problem_key),
        generation,
        stale_generations,
        stagnation_patience,
        lane_telemetry.seed_lanes,
        lane_telemetry.best_lane,
        lane_telemetry.lane_deaths,
        lane_telemetry.max_lane_stale,
        lane_telemetry.lane_died,
        json_number(mutation_intensity),
        json_number(reseed_fraction),
        reheated,
        reseeding,
        score.generation,
        json_number(score.score),
        json_number(score.mean_squared_error),
        json_number(score.configured_loss),
        json_number(score.accuracy),
        json_number(score.generalization_mse),
        json_number(score.group_relative_generalization_error),
        json_number(score.jitter_mse),
        json_number(score.smoothness_penalty),
        json_number(score.complexity),
        json_number(score.predictive_free_energy),
        json_number(score.sensory_prediction_energy),
        json_number(score.latent_prediction_energy),
        json_number(score.complexity_prior_energy),
        escape_json(&score.genome.encode()),
        escape_json(if kind == "completed" {
            "Rust evolution run completed."
        } else {
            "Rust evolution progress."
        })
    )?;
    writer.flush()
}

pub(crate) fn string_field(json: &str, field: &str) -> Option<String> {
    let key = format!("\"{}\"", field);
    let key_start = json.find(&key)?;
    let after_key = &json[key_start + key.len()..];
    let colon = after_key.find(':')?;
    let after_colon = after_key[colon + 1..].trim_start();
    let value = after_colon.strip_prefix('"')?;
    let mut result = String::new();
    let mut escaped = false;
    for character in value.chars() {
        if escaped {
            result.push(match character {
                '"' => '"',
                '\\' => '\\',
                'n' => '\n',
                'r' => '\r',
                't' => '\t',
                other => other,
            });
            escaped = false;
        } else if character == '\\' {
            escaped = true;
        } else if character == '"' {
            return Some(result);
        } else {
            result.push(character);
        }
    }
    None
}

pub(crate) fn number_field(json: &str, field: &str) -> Option<i64> {
    let key = format!("\"{}\"", field);
    let key_start = json.find(&key)?;
    let after_key = &json[key_start + key.len()..];
    let colon = after_key.find(':')?;
    let after_colon = after_key[colon + 1..].trim_start();
    let end = after_colon
        .find(|character: char| !character.is_ascii_digit() && character != '-')
        .unwrap_or(after_colon.len());
    after_colon[..end].parse().ok()
}

pub(crate) fn double_field(json: &str, field: &str) -> Option<f64> {
    let key = format!("\"{}\"", field);
    let key_start = json.find(&key)?;
    let after_key = &json[key_start + key.len()..];
    let colon = after_key.find(':')?;
    let after_colon = after_key[colon + 1..].trim_start();
    let end = after_colon
        .find(|character: char| {
            !character.is_ascii_digit()
                && character != '-'
                && character != '+'
                && character != '.'
                && character != 'e'
                && character != 'E'
        })
        .unwrap_or(after_colon.len());
    after_colon[..end].parse().ok()
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

#[cfg(test)]
mod tests {
    use super::{number_field, string_field};

    #[test]
    fn reads_string_fields() {
        let json = r#"{"type":"start","problem":{"key":"transformer-block"}}"#;

        assert_eq!(Some("start".to_string()), string_field(json, "type"));
        assert_eq!(
            Some("transformer-block".to_string()),
            string_field(json, "key")
        );
    }

    #[test]
    fn reads_number_fields() {
        let json = r#"{"config":{"generations":1000,"parallelism":8}}"#;

        assert_eq!(Some(1000), number_field(json, "generations"));
        assert_eq!(Some(8), number_field(json, "parallelism"));
    }
}
