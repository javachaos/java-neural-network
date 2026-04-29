use rayon::prelude::*;

use crate::config::EvolutionConfig;
use crate::cuda_runtime;
use crate::genome::Genome;
use crate::problem::ProblemData;
use crate::protocol::string_field;
use crate::scoring::{CandidateEvaluator, LearnerCandidateEvaluator, Score};

pub(crate) trait EvolutionBackend: Sync {
    fn name(&self) -> &'static str;

    fn score_population(
        &self,
        pool: &rayon::ThreadPool,
        population: &[Genome],
        problem: &ProblemData,
        config: &EvolutionConfig,
        generation: usize,
    ) -> Result<Vec<Score>, String>;
}

pub(crate) fn backend_from_json(json: &str) -> Result<Box<dyn EvolutionBackend>, String> {
    match string_field(json, "backend")
        .unwrap_or_else(|| "CPU".to_string())
        .trim()
        .to_ascii_uppercase()
        .as_str()
    {
        "CPU" => Ok(Box::new(CpuEvolutionBackend::new())),
        "CUDA" => CudaEvolutionBackend::new()
            .map(|backend| Box::new(backend) as Box<dyn EvolutionBackend>),
        other => Err(format!("Unsupported compute backend '{other}'.")),
    }
}

pub(crate) struct CpuEvolutionBackend {
    evaluator: Box<dyn CandidateEvaluator>,
}

impl CpuEvolutionBackend {
    pub(crate) fn new() -> Self {
        Self {
            evaluator: Box::new(LearnerCandidateEvaluator),
        }
    }

    fn score_population_with_complexities(
        &self,
        pool: &rayon::ThreadPool,
        population: &[Genome],
        problem: &ProblemData,
        config: &EvolutionConfig,
        generation: usize,
        complexities: Option<&[f64]>,
    ) -> Vec<Score> {
        pool.install(|| {
            population
                .par_iter()
                .enumerate()
                .map(|(index, genome)| {
                    self.evaluator.evaluate_with_complexity(
                        problem,
                        genome,
                        config,
                        generation,
                        index,
                        complexities.map(|values| values[index]),
                    )
                })
                .collect()
        })
    }
}

impl EvolutionBackend for CpuEvolutionBackend {
    fn name(&self) -> &'static str {
        "CPU"
    }

    fn score_population(
        &self,
        pool: &rayon::ThreadPool,
        population: &[Genome],
        problem: &ProblemData,
        config: &EvolutionConfig,
        generation: usize,
    ) -> Result<Vec<Score>, String> {
        Ok(self.score_population_with_complexities(
            pool, population, problem, config, generation, None,
        ))
    }
}

pub(crate) struct CudaEvolutionBackend {
    cpu_fallback: CpuEvolutionBackend,
    device_count: usize,
}

#[cfg(feature = "cuda")]
impl CudaEvolutionBackend {
    fn new() -> Result<Self, String> {
        let device_count = cuda_runtime::device_count()?;
        if device_count == 0 {
            return Err("CUDA backend was requested, but no CUDA devices were found.".to_string());
        }
        Ok(Self {
            cpu_fallback: CpuEvolutionBackend::new(),
            device_count,
        })
    }
}

#[cfg(not(feature = "cuda"))]
impl CudaEvolutionBackend {
    fn new() -> Result<Self, String> {
        Err(
            "CUDA backend was requested, but this worker was not built with --features cuda."
                .to_string(),
        )
    }
}

impl EvolutionBackend for CudaEvolutionBackend {
    fn name(&self) -> &'static str {
        "CUDA_HYBRID_SCORER"
    }

    fn score_population(
        &self,
        pool: &rayon::ThreadPool,
        population: &[Genome],
        problem: &ProblemData,
        config: &EvolutionConfig,
        generation: usize,
    ) -> Result<Vec<Score>, String> {
        let _device_count = self.device_count;
        let complexities = cuda_runtime::genome_complexities(population, problem)?;
        Ok(self.cpu_fallback.score_population_with_complexities(
            pool,
            population,
            problem,
            config,
            generation,
            Some(&complexities),
        ))
    }
}
