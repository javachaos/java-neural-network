use std::io::Write;
use std::time::Instant;

use rayon::ThreadPoolBuilder;

use crate::backend::EvolutionBackend;
use crate::config::EvolutionConfig;
use crate::constants::{
    ELITE_POPULATION_DIVISOR, LANE_RESTART_SEED_STRIDE, LANE_SEED_STRIDE,
    MAX_STAGNATION_RESEED_RATIO, MIN_ELITES, NORMAL_IMMIGRANT_RATIO, REHEAT_MAX_MUTATION_INTENSITY,
    REHEAT_MUTATION_MULTIPLIER, REHEAT_STALE_GENERATION_FACTOR, STAGNATION_RESEED_START_PRESSURE,
    TOURNAMENT_SIZE,
};
use crate::genome::Genome;
use crate::problem::ProblemData;
use crate::protocol::{LaneTelemetry, string_field, write_score_event};
use crate::random::Rng;
use crate::scoring::Score;
use crate::worker_service::{RunControl, RunControlDecision};

struct SeedLane {
    id: usize,
    seed: i64,
    rng: Rng,
    population: Vec<Genome>,
    best: Option<Score>,
    stale_generations: usize,
    deaths: usize,
}

impl SeedLane {
    fn new(id: usize, config: &EvolutionConfig) -> Self {
        let seed = lane_seed(config.seed, id, 0);
        let mut rng = Rng::new(seed as u64);
        let population = random_population(&mut rng, config.population_size);
        Self {
            id,
            seed,
            rng,
            population,
            best: None,
            stale_generations: 0,
            deaths: 0,
        }
    }

    fn scoring_config(&self, config: &EvolutionConfig) -> EvolutionConfig {
        let mut lane_config = config.clone();
        lane_config.seed = self.seed;
        lane_config
    }

    fn die_and_respawn(&mut self, config: &EvolutionConfig) {
        self.deaths += 1;
        self.seed = lane_seed(config.seed, self.id, self.deaths);
        self.rng = Rng::new(self.seed as u64);
        self.population = random_population(&mut self.rng, config.population_size);
        self.best = None;
        self.stale_generations = 0;
    }
}

pub(crate) fn run_evolution(
    json: &str,
    writer: &mut dyn Write,
    cluster_id: &str,
    storage_mode: &str,
    backend: &dyn EvolutionBackend,
    control: &RunControl,
) -> Result<(), String> {
    let payload_hex = string_field(json, "problemPayloadHex")
        .ok_or_else(|| "Start message did not include problemPayloadHex.".to_string())?;
    let problem = ProblemData::from_hex(&payload_hex)?;
    let config = EvolutionConfig::from_json(json);
    let problem_key = string_field(json, "key").unwrap_or_else(|| "unknown-problem".to_string());
    let lane_count = config.seed_lanes.min(config.population_size).max(1);
    let mut lanes = (0..lane_count)
        .map(|id| SeedLane::new(id, &config))
        .collect::<Vec<_>>();
    let mut best: Option<Score> = None;
    let mut stale_generations = 0usize;
    let mut best_lane = 0usize;
    let mut last_progress: Option<Instant> = None;
    let backend_thread_name = backend.name().to_ascii_lowercase();
    let scoring_pool = ThreadPoolBuilder::new()
        .num_threads(config.parallelism)
        .thread_name(move |index| format!("neuro-evolution-{backend_thread_name}-scorer-{index}"))
        .build()
        .map_err(|error| format!("Failed to create Rayon scoring pool: {error}"))?;

    'evolution: for generation in 0..config.generations {
        let mut improved_this_generation = false;
        let mut died_this_generation = false;
        for lane in &mut lanes {
            if control.wait_if_paused() == RunControlDecision::Stop {
                break 'evolution;
            }
            let lane_config = lane.scoring_config(&config);
            let mut scored = backend.score_population(
                &scoring_pool,
                &lane.population,
                &problem,
                &lane_config,
                generation,
            )?;
            if control.wait_if_paused() == RunControlDecision::Stop {
                break 'evolution;
            }
            scored.sort_by(|left, right| left.score.total_cmp(&right.score));
            let champion = scored[0].clone();
            if lane
                .best
                .as_ref()
                .map(|current| champion.score < current.score)
                .unwrap_or(true)
            {
                lane.best = Some(champion.clone());
                lane.stale_generations = 0;
            } else {
                lane.stale_generations += 1;
            }
            if best
                .as_ref()
                .map(|current| champion.score < current.score)
                .unwrap_or(true)
            {
                best = Some(champion);
                best_lane = lane.id;
                improved_this_generation = true;
            }
            if lane.stale_generations >= stagnation_patience(&config) {
                lane.die_and_respawn(&config);
                died_this_generation = true;
            } else {
                let lane_best = lane.best.clone();
                lane.population = next_generation(
                    &scored,
                    &lane_config,
                    &mut lane.rng,
                    lane_best.as_ref(),
                    lane.stale_generations,
                );
            }
        }
        if improved_this_generation {
            stale_generations = 0;
        } else {
            stale_generations += 1;
        }
        let now = Instant::now();
        let final_generation = generation + 1 == config.generations;
        let should_emit_progress = generation == 0
            || final_generation
            || config.progress_interval_nanos == 0
            || last_progress
                .map(|previous| {
                    now.duration_since(previous).as_nanos()
                        >= config.progress_interval_nanos as u128
                })
                .unwrap_or(true);
        if should_emit_progress {
            let best_ref = best.as_ref().unwrap();
            let lane_telemetry = lane_telemetry(&lanes, best_lane, died_this_generation);
            let active_stale_generations = lane_telemetry.max_lane_stale;
            write_score_event(
                writer,
                "progress",
                "running",
                cluster_id,
                storage_mode,
                &problem_key,
                generation,
                stale_generations,
                stagnation_patience(&config),
                lane_telemetry,
                adaptive_mutation_intensity(&config, active_stale_generations),
                stagnation_reseed_ratio(&config, active_stale_generations),
                reheated(&config, active_stale_generations) || died_this_generation,
                reseeding(&config, active_stale_generations) || died_this_generation,
                best_ref,
            )
            .map_err(|error| error.to_string())?;
            last_progress = Some(Instant::now());
        }
    }

    if let Some(best_score) = best {
        let lane_telemetry = lane_telemetry(&lanes, best_lane, false);
        let active_stale_generations = lane_telemetry.max_lane_stale;
        write_score_event(
            writer,
            "completed",
            "stopped",
            cluster_id,
            storage_mode,
            &problem_key,
            best_score.generation,
            stale_generations,
            stagnation_patience(&config),
            lane_telemetry,
            adaptive_mutation_intensity(&config, active_stale_generations),
            stagnation_reseed_ratio(&config, active_stale_generations),
            reheated(&config, active_stale_generations),
            reseeding(&config, active_stale_generations),
            &best_score,
        )
        .map_err(|error| error.to_string())?;
    }
    Ok(())
}

fn random_population(rng: &mut Rng, population_size: usize) -> Vec<Genome> {
    (0..population_size).map(|_| Genome::random(rng)).collect()
}

fn lane_seed(base_seed: i64, lane_id: usize, deaths: usize) -> i64 {
    base_seed
        .wrapping_add(LANE_SEED_STRIDE.wrapping_mul(lane_id as i64))
        .wrapping_add(LANE_RESTART_SEED_STRIDE.wrapping_mul(deaths as i64))
}

fn lane_telemetry(lanes: &[SeedLane], best_lane: usize, lane_died: bool) -> LaneTelemetry {
    LaneTelemetry {
        seed_lanes: lanes.len(),
        best_lane,
        lane_deaths: lanes.iter().map(|lane| lane.deaths).sum(),
        max_lane_stale: lanes
            .iter()
            .map(|lane| lane.stale_generations)
            .max()
            .unwrap_or(0),
        lane_died,
    }
}

fn next_generation(
    scored: &[Score],
    config: &EvolutionConfig,
    rng: &mut Rng,
    best: Option<&Score>,
    stale_generations: usize,
) -> Vec<Genome> {
    let reheated = reheated(config, stale_generations);
    let mutation_intensity = adaptive_mutation_intensity(config, stale_generations);
    let elite_count = (config.population_size / ELITE_POPULATION_DIVISOR).max(MIN_ELITES);
    let immigrant_count = ((config.population_size as f64)
        * stagnation_reseed_ratio(config, stale_generations))
    .round()
    .max(1.0) as usize;
    let breeding_limit = config
        .population_size
        .saturating_sub(immigrant_count)
        .max(1);
    let mut next = Vec::with_capacity(config.population_size);
    if let Some(best_score) = best {
        add_if_absent(&mut next, &best_score.genome, config.population_size);
    }
    for candidate in scored.iter().take(elite_count) {
        add_if_absent(&mut next, &candidate.genome, config.population_size);
    }
    while next.len() < breeding_limit {
        let parent_a = tournament(scored, rng);
        let parent_b = tournament(scored, rng);
        let mut child = parent_a.genome.crossover(&parent_b.genome, rng);
        let mutation_count =
            1 + rng.usize(config.max_mutations_per_child + if reheated { 2 } else { 0 });
        for _ in 0..mutation_count {
            child = child.mutate(rng, mutation_intensity);
        }
        next.push(child);
    }
    while next.len() < config.population_size {
        let mut immigrant = Genome::random(rng);
        if reheated {
            for _ in 0..(2 + rng.usize(5)) {
                immigrant = immigrant.mutate(rng, mutation_intensity);
            }
        }
        next.push(immigrant);
    }
    next
}

fn stagnation_patience(config: &EvolutionConfig) -> usize {
    config.population_size * REHEAT_STALE_GENERATION_FACTOR
}

fn stagnation_pressure(config: &EvolutionConfig, stale_generations: usize) -> f64 {
    ((stale_generations as f64) / (stagnation_patience(config) as f64)).min(1.0)
}

fn reheated(config: &EvolutionConfig, stale_generations: usize) -> bool {
    stale_generations >= stagnation_patience(config)
}

fn reseeding(config: &EvolutionConfig, stale_generations: usize) -> bool {
    stagnation_reseed_ratio(config, stale_generations) > NORMAL_IMMIGRANT_RATIO
}

fn adaptive_mutation_intensity(config: &EvolutionConfig, stale_generations: usize) -> f64 {
    let pressure = stagnation_pressure(config, stale_generations);
    let multiplier = 1.0 + pressure * (REHEAT_MUTATION_MULTIPLIER - 1.0);
    (config.mutation_intensity * multiplier).min(REHEAT_MAX_MUTATION_INTENSITY)
}

fn stagnation_reseed_ratio(config: &EvolutionConfig, stale_generations: usize) -> f64 {
    let pressure = stagnation_pressure(config, stale_generations);
    if pressure <= STAGNATION_RESEED_START_PRESSURE {
        return NORMAL_IMMIGRANT_RATIO;
    }
    let ramp =
        (pressure - STAGNATION_RESEED_START_PRESSURE) / (1.0 - STAGNATION_RESEED_START_PRESSURE);
    NORMAL_IMMIGRANT_RATIO + (MAX_STAGNATION_RESEED_RATIO - NORMAL_IMMIGRANT_RATIO) * ramp.min(1.0)
}

fn tournament<'a>(scored: &'a [Score], rng: &mut Rng) -> &'a Score {
    let mut best = &scored[rng.usize(scored.len())];
    for _ in 1..scored.len().min(TOURNAMENT_SIZE) {
        let candidate = &scored[rng.usize(scored.len())];
        if candidate.score < best.score {
            best = candidate;
        }
    }
    best
}

fn add_if_absent(genomes: &mut Vec<Genome>, genome: &Genome, limit: usize) {
    if genomes.len() < limit
        && !genomes
            .iter()
            .any(|candidate| same_architecture(candidate, genome))
    {
        genomes.push(genome.clone());
    }
}

fn same_architecture(left: &Genome, right: &Genome) -> bool {
    left.hidden_neurons == right.hidden_neurons
        && left.hidden_layers == right.hidden_layers
        && left.memory_cells == right.memory_cells
        && left.input_representation.name() == right.input_representation.name()
        && left.hidden_activation.name() == right.hidden_activation.name()
}

#[cfg(test)]
mod tests {
    use super::*;

    fn config() -> EvolutionConfig {
        EvolutionConfig {
            population_size: 20,
            generations: 10,
            max_epochs: 5,
            target_mse: 0.05,
            evaluation_repeats: 1,
            mutation_intensity: 0.08,
            complexity_penalty: 0.001,
            max_mutations_per_child: 1,
            generalization_weight: 0.6,
            jitter_weight: 0.4,
            smoothness_weight: 0.1,
            progress_interval_nanos: 0,
            parallelism: 1,
            seed_lanes: 1,
            seed: 101,
        }
    }

    #[test]
    fn stale_runs_ramp_reseed_ratio_before_full_reheat() {
        let config = config();
        let patience = stagnation_patience(&config);
        let mid_stale = patience / 2;

        assert_eq!(NORMAL_IMMIGRANT_RATIO, stagnation_reseed_ratio(&config, 0));
        assert!(stagnation_reseed_ratio(&config, mid_stale) > NORMAL_IMMIGRANT_RATIO);
        assert!(stagnation_reseed_ratio(&config, mid_stale) < MAX_STAGNATION_RESEED_RATIO);
        assert!(
            (stagnation_reseed_ratio(&config, patience) - MAX_STAGNATION_RESEED_RATIO).abs()
                < f64::EPSILON
        );
    }

    #[test]
    fn stale_lane_death_assigns_new_seed_and_population() {
        let config = EvolutionConfig {
            seed_lanes: 3,
            ..config()
        };
        let mut lane = SeedLane::new(2, &config);
        let original_seed = lane.seed;
        let original_population = lane.population.len();

        lane.die_and_respawn(&config);

        assert_ne!(original_seed, lane.seed);
        assert_eq!(1, lane.deaths);
        assert_eq!(0, lane.stale_generations);
        assert_eq!(original_population, lane.population.len());
        assert_eq!(lane_seed(config.seed, 2, 1), lane.seed);
    }
}
