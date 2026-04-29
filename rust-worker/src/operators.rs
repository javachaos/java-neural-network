use crate::constants::{CROSS_ENTROPY_EPSILON, HUBER_DELTA, HUBER_OFFSET};
use crate::math::sigmoid;
use crate::random::Rng;

#[derive(Clone, Copy)]
pub(crate) enum Activation {
    Sigmoid,
    Tanh,
    Relu,
    Sine,
    Gaussian,
    Linear,
}

impl Activation {
    pub(crate) fn random(rng: &mut Rng) -> Self {
        match rng.usize(6) {
            0 => Self::Sigmoid,
            1 => Self::Tanh,
            2 => Self::Relu,
            3 => Self::Sine,
            4 => Self::Gaussian,
            _ => Self::Linear,
        }
    }

    pub(crate) fn name(self) -> &'static str {
        match self {
            Self::Sigmoid => "SIGMOID",
            Self::Tanh => "TANH",
            Self::Relu => "RELU",
            Self::Sine => "SINE",
            Self::Gaussian => "GAUSSIAN",
            Self::Linear => "LINEAR",
        }
    }

    pub(crate) fn from_name(name: &str) -> Option<Self> {
        match name {
            "SIGMOID" => Some(Self::Sigmoid),
            "TANH" => Some(Self::Tanh),
            "RELU" => Some(Self::Relu),
            "SINE" => Some(Self::Sine),
            "GAUSSIAN" => Some(Self::Gaussian),
            "LINEAR" => Some(Self::Linear),
            _ => None,
        }
    }

    pub(crate) fn hidden(self, scaled_input: f64) -> f64 {
        match self {
            Self::Sigmoid => sigmoid(scaled_input),
            Self::Tanh => scaled_input.tanh(),
            Self::Relu => {
                if scaled_input > 0.0 {
                    scaled_input
                } else {
                    0.01 * scaled_input
                }
            }
            Self::Sine => scaled_input.sin(),
            Self::Gaussian => (-scaled_input * scaled_input).exp(),
            Self::Linear => scaled_input,
        }
    }

    pub(crate) fn hidden_derivative(self, scaled_input: f64) -> f64 {
        match self {
            Self::Sigmoid => {
                let value = sigmoid(scaled_input);
                value * (1.0 - value)
            }
            Self::Tanh => {
                let value = scaled_input.tanh();
                1.0 - value * value
            }
            Self::Relu => {
                if scaled_input > 0.0 {
                    1.0
                } else {
                    0.01
                }
            }
            Self::Sine => scaled_input.cos(),
            Self::Gaussian => -2.0 * scaled_input * (-scaled_input * scaled_input).exp(),
            Self::Linear => 1.0,
        }
    }

    pub(crate) fn output(self, scaled_input: f64) -> f64 {
        match self {
            Self::Sigmoid => sigmoid(scaled_input),
            Self::Tanh => (scaled_input.tanh() + 1.0) * 0.5,
            Self::Relu => sigmoid(self.hidden(scaled_input)),
            Self::Sine => (scaled_input.sin() + 1.0) * 0.5,
            Self::Gaussian => self.hidden(scaled_input),
            Self::Linear => sigmoid(scaled_input),
        }
    }

    pub(crate) fn output_derivative(self, scaled_input: f64) -> f64 {
        match self {
            Self::Sigmoid => self.hidden_derivative(scaled_input),
            Self::Tanh => self.hidden_derivative(scaled_input) * 0.5,
            Self::Relu => {
                let activated = self.hidden(scaled_input);
                let probability = sigmoid(activated);
                probability * (1.0 - probability) * self.hidden_derivative(scaled_input)
            }
            Self::Sine => scaled_input.cos() * 0.5,
            Self::Gaussian => self.hidden_derivative(scaled_input),
            Self::Linear => {
                let probability = sigmoid(scaled_input);
                probability * (1.0 - probability)
            }
        }
    }
}

#[derive(Clone, Copy)]
pub(crate) enum InputRepresentation {
    Raw,
    Polynomial,
    PhaseComplex,
    KernelDistance,
    Mixed,
}

impl InputRepresentation {
    pub(crate) fn random(rng: &mut Rng) -> Self {
        match rng.usize(5) {
            0 => Self::Raw,
            1 => Self::Polynomial,
            2 => Self::PhaseComplex,
            3 => Self::KernelDistance,
            _ => Self::Mixed,
        }
    }

    pub(crate) fn name(self) -> &'static str {
        match self {
            Self::Raw => "RAW",
            Self::Polynomial => "POLYNOMIAL",
            Self::PhaseComplex => "PHASE_COMPLEX",
            Self::KernelDistance => "KERNEL_DISTANCE",
            Self::Mixed => "MIXED",
        }
    }

    pub(crate) fn from_name(name: &str) -> Option<Self> {
        match name {
            "RAW" => Some(Self::Raw),
            "POLYNOMIAL" => Some(Self::Polynomial),
            "PHASE_COMPLEX" => Some(Self::PhaseComplex),
            "KERNEL_DISTANCE" => Some(Self::KernelDistance),
            "MIXED" => Some(Self::Mixed),
            _ => None,
        }
    }

    pub(crate) fn encode(
        self,
        input: &[f64],
        kernel_centers: &[Vec<f64>],
        phase_encoding: bool,
        kernel_memory: bool,
        kernel_sharpness: f64,
    ) -> Vec<f64> {
        let mut values = Vec::new();
        match self {
            Self::Raw => add_raw(&mut values, input),
            Self::Polynomial => add_polynomial(&mut values, input),
            Self::PhaseComplex => {
                add_raw(&mut values, input);
                add_phase(&mut values, input);
            }
            Self::KernelDistance => {
                add_raw(&mut values, input);
                add_kernel(&mut values, input, kernel_centers, kernel_sharpness);
            }
            Self::Mixed => {
                add_polynomial(&mut values, input);
                add_phase(&mut values, input);
                add_kernel(&mut values, input, kernel_centers, kernel_sharpness);
            }
        }
        if phase_encoding && !matches!(self, Self::PhaseComplex | Self::Mixed) {
            add_phase(&mut values, input);
        }
        if kernel_memory && !matches!(self, Self::KernelDistance | Self::Mixed) {
            add_kernel(&mut values, input, kernel_centers, kernel_sharpness);
        }
        values
    }
}

#[derive(Clone, Copy)]
pub(crate) enum LossFunction {
    MeanSquared,
    Absolute,
    CrossEntropy,
    Huber,
}

impl LossFunction {
    pub(crate) fn random(rng: &mut Rng) -> Self {
        match rng.usize(4) {
            0 => Self::MeanSquared,
            1 => Self::Absolute,
            2 => Self::CrossEntropy,
            _ => Self::Huber,
        }
    }

    pub(crate) fn name(self) -> &'static str {
        match self {
            Self::MeanSquared => "MEAN_SQUARED",
            Self::Absolute => "ABSOLUTE",
            Self::CrossEntropy => "CROSS_ENTROPY",
            Self::Huber => "HUBER",
        }
    }

    pub(crate) fn from_name(name: &str) -> Option<Self> {
        match name {
            "MEAN_SQUARED" => Some(Self::MeanSquared),
            "ABSOLUTE" => Some(Self::Absolute),
            "CROSS_ENTROPY" => Some(Self::CrossEntropy),
            "HUBER" => Some(Self::Huber),
            _ => None,
        }
    }

    pub(crate) fn loss(self, target: f64, prediction: f64) -> f64 {
        match self {
            Self::MeanSquared => {
                let error = target - prediction;
                error * error
            }
            Self::Absolute => (target - prediction).abs(),
            Self::CrossEntropy => {
                let clipped = prediction.clamp(CROSS_ENTROPY_EPSILON, 1.0 - CROSS_ENTROPY_EPSILON);
                -target * clipped.ln() - (1.0 - target) * (1.0 - clipped).ln()
            }
            Self::Huber => {
                let error = (target - prediction).abs();
                if error <= HUBER_DELTA {
                    0.5 * error * error
                } else {
                    HUBER_DELTA * (error - HUBER_OFFSET)
                }
            }
        }
    }

    pub(crate) fn signal(self, target: f64, prediction: f64) -> f64 {
        match self {
            Self::MeanSquared => target - prediction,
            Self::Absolute => (target - prediction).signum(),
            Self::CrossEntropy => {
                let clipped = prediction.clamp(CROSS_ENTROPY_EPSILON, 1.0 - CROSS_ENTROPY_EPSILON);
                (target - clipped) / (clipped * (1.0 - clipped)).max(CROSS_ENTROPY_EPSILON)
            }
            Self::Huber => {
                let error = target - prediction;
                if error.abs() <= HUBER_DELTA {
                    error
                } else {
                    error.signum() * HUBER_DELTA
                }
            }
        }
    }
}

#[derive(Clone, Copy)]
pub(crate) enum LearningSchedule {
    Constant,
    InverseTime,
    CosineDecay,
    StepDecay,
}

impl LearningSchedule {
    pub(crate) fn random(rng: &mut Rng) -> Self {
        match rng.usize(4) {
            0 => Self::Constant,
            1 => Self::InverseTime,
            2 => Self::CosineDecay,
            _ => Self::StepDecay,
        }
    }

    pub(crate) fn name(self) -> &'static str {
        match self {
            Self::Constant => "CONSTANT",
            Self::InverseTime => "INVERSE_TIME",
            Self::CosineDecay => "COSINE_DECAY",
            Self::StepDecay => "STEP_DECAY",
        }
    }

    pub(crate) fn from_name(name: &str) -> Option<Self> {
        match name {
            "CONSTANT" => Some(Self::Constant),
            "INVERSE_TIME" => Some(Self::InverseTime),
            "COSINE_DECAY" => Some(Self::CosineDecay),
            "STEP_DECAY" => Some(Self::StepDecay),
            _ => None,
        }
    }

    pub(crate) fn scale(self, epoch: usize, max_epochs: usize) -> f64 {
        match self {
            Self::Constant => 1.0,
            Self::InverseTime => 1.0 / (1.0 + 0.01 * epoch as f64),
            Self::CosineDecay => {
                let progress = (epoch as f64 / max_epochs.max(1) as f64).min(1.0);
                0.1 + 0.9 * (0.5 + 0.5 * (std::f64::consts::PI * progress).cos())
            }
            Self::StepDecay => {
                let interval = (max_epochs / 4).max(1);
                0.5_f64.powi((epoch / interval) as i32)
            }
        }
    }
}

fn add_raw(values: &mut Vec<f64>, input: &[f64]) {
    values.push(1.0);
    values.extend_from_slice(input);
}

fn add_polynomial(values: &mut Vec<f64>, input: &[f64]) {
    add_raw(values, input);
    for left in 0..input.len() {
        for right in left + 1..input.len() {
            values.push(input[left] * input[right]);
        }
    }
    for value in input {
        values.push(value * value);
    }
    for left in 0..input.len() {
        for right in left + 1..input.len() {
            values.push((input[left] - input[right]).abs());
        }
    }
}

fn add_phase(values: &mut Vec<f64>, input: &[f64]) {
    for value in input {
        values.push((std::f64::consts::PI * value).sin());
        values.push((std::f64::consts::PI * value).cos());
    }
    for left in 0..input.len() {
        for right in left + 1..input.len() {
            let difference = input[left] - input[right];
            values.push((std::f64::consts::PI * difference).sin());
            values.push((std::f64::consts::PI * difference).cos());
        }
    }
}

fn add_kernel(values: &mut Vec<f64>, input: &[f64], centers: &[Vec<f64>], sharpness: f64) {
    for center in centers {
        let distance_squared = input
            .iter()
            .zip(center)
            .map(|(left, right)| {
                let delta = left - right;
                delta * delta
            })
            .sum::<f64>();
        values.push((-sharpness.max(0.001) * distance_squared).exp());
    }
}
