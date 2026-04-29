use crate::constants::*;
use crate::operators::{Activation, InputRepresentation, LearningSchedule, LossFunction};
use crate::problem::ProblemData;
use crate::random::{Rng, blend, choose, mutate_range_pair};

#[derive(Clone)]
pub(crate) struct Genome {
    pub(crate) hidden_neurons: usize,
    pub(crate) hidden_layers: usize,
    pub(crate) recurrent_connections: usize,
    pub(crate) memory_cells: usize,
    pub(crate) connection_density: f64,
    pub(crate) hidden_activation: Activation,
    pub(crate) output_activation: Activation,
    pub(crate) input_representation: InputRepresentation,
    pub(crate) loss_function: LossFunction,
    pub(crate) learning_schedule: LearningSchedule,
    pub(crate) hebbian_update: bool,
    pub(crate) gradient_update: bool,
    pub(crate) normalization: bool,
    pub(crate) kernel_memory: bool,
    pub(crate) phase_encoding: bool,
    pub(crate) second_derivative_estimate: bool,
    pub(crate) input_learning_rate: f64,
    pub(crate) output_learning_rate: f64,
    pub(crate) recurrent_learning_rate: f64,
    pub(crate) hebbian_learning_rate: f64,
    pub(crate) memory_learning_rate: f64,
    pub(crate) momentum: f64,
    pub(crate) weight_decay: f64,
    pub(crate) normalization_strength: f64,
    pub(crate) activation_slope: f64,
    pub(crate) error_clip: f64,
    pub(crate) kernel_sharpness: f64,
}

impl Genome {
    pub(crate) fn random(rng: &mut Rng) -> Self {
        Self {
            hidden_neurons: rng.range_usize(
                INITIAL_MIN_HIDDEN_NEURONS,
                INITIAL_MAX_HIDDEN_NEURONS_EXCLUSIVE,
            ),
            hidden_layers: rng.range_usize(MIN_HIDDEN_LAYERS, INITIAL_MAX_HIDDEN_LAYERS_EXCLUSIVE),
            recurrent_connections: rng.range_usize(0, INITIAL_MAX_RECURRENT_CONNECTIONS_EXCLUSIVE),
            memory_cells: rng.range_usize(0, INITIAL_MAX_MEMORY_CELLS_EXCLUSIVE),
            connection_density: rng.range(INITIAL_MIN_CONNECTION_DENSITY, MAX_CONNECTION_DENSITY),
            hidden_activation: Activation::random(rng),
            output_activation: Activation::random(rng),
            input_representation: InputRepresentation::random(rng),
            loss_function: LossFunction::random(rng),
            learning_schedule: LearningSchedule::random(rng),
            hebbian_update: rng.bool(INITIAL_HEBBIAN_PROBABILITY),
            gradient_update: true,
            normalization: rng.bool(INITIAL_NORMALIZATION_PROBABILITY),
            kernel_memory: rng.bool(INITIAL_KERNEL_MEMORY_PROBABILITY),
            phase_encoding: rng.bool(INITIAL_PHASE_ENCODING_PROBABILITY),
            second_derivative_estimate: rng.bool(INITIAL_SECOND_DERIVATIVE_PROBABILITY),
            input_learning_rate: rng.range_pair(INITIAL_INPUT_LEARNING_RATE),
            output_learning_rate: rng.range_pair(INITIAL_OUTPUT_LEARNING_RATE),
            recurrent_learning_rate: rng.range_pair(INITIAL_RECURRENT_LEARNING_RATE),
            hebbian_learning_rate: rng.range_pair(INITIAL_HEBBIAN_LEARNING_RATE),
            memory_learning_rate: rng.range_pair(INITIAL_MEMORY_LEARNING_RATE),
            momentum: rng.range_pair(INITIAL_MOMENTUM),
            weight_decay: rng.range_pair(INITIAL_WEIGHT_DECAY),
            normalization_strength: rng.range_pair(INITIAL_NORMALIZATION_STRENGTH),
            activation_slope: rng.range_pair(INITIAL_ACTIVATION_SLOPE),
            error_clip: rng.range_pair(INITIAL_ERROR_CLIP),
            kernel_sharpness: rng.range_pair(INITIAL_KERNEL_SHARPNESS),
        }
        .clamped()
    }

    pub(crate) fn mutate(&self, rng: &mut Rng, intensity: f64) -> Self {
        let mut next = self.clone();
        match rng.usize(19) {
            0 => next.hidden_neurons += 1,
            1 => next.hidden_neurons = next.hidden_neurons.saturating_sub(1),
            2 => next.hidden_layers += 1,
            3 => next.hidden_layers = next.hidden_layers.saturating_sub(1),
            4 => next.connection_density += rng.range_pair(CONNECTION_DENSITY_MUTATION_STEP),
            5 => next.connection_density -= rng.range_pair(CONNECTION_DENSITY_MUTATION_STEP),
            6 => next.recurrent_connections += 1,
            7 => next.recurrent_connections = next.recurrent_connections.saturating_sub(1),
            8 => next.memory_cells += 1,
            9 => next.memory_cells = next.memory_cells.saturating_sub(1),
            10 => next.hidden_activation = Activation::random(rng),
            11 => next.output_activation = Activation::random(rng),
            12 => next.input_representation = InputRepresentation::random(rng),
            13 => next.loss_function = LossFunction::random(rng),
            14 => next.learning_schedule = LearningSchedule::random(rng),
            15 => next.hebbian_update = true,
            16 => next.normalization = true,
            17 => next.kernel_memory = true,
            _ => next.mutate_numeric(rng, intensity),
        }
        next.clamped()
    }

    pub(crate) fn crossover(&self, other: &Self, rng: &mut Rng) -> Self {
        Self {
            hidden_neurons: choose(rng, self.hidden_neurons, other.hidden_neurons),
            hidden_layers: choose(rng, self.hidden_layers, other.hidden_layers),
            recurrent_connections: choose(
                rng,
                self.recurrent_connections,
                other.recurrent_connections,
            ),
            memory_cells: choose(rng, self.memory_cells, other.memory_cells),
            connection_density: blend(rng, self.connection_density, other.connection_density),
            hidden_activation: choose(rng, self.hidden_activation, other.hidden_activation),
            output_activation: choose(rng, self.output_activation, other.output_activation),
            input_representation: choose(
                rng,
                self.input_representation,
                other.input_representation,
            ),
            loss_function: choose(rng, self.loss_function, other.loss_function),
            learning_schedule: choose(rng, self.learning_schedule, other.learning_schedule),
            hebbian_update: choose(rng, self.hebbian_update, other.hebbian_update),
            gradient_update: true,
            normalization: choose(rng, self.normalization, other.normalization),
            kernel_memory: choose(rng, self.kernel_memory, other.kernel_memory),
            phase_encoding: choose(rng, self.phase_encoding, other.phase_encoding),
            second_derivative_estimate: choose(
                rng,
                self.second_derivative_estimate,
                other.second_derivative_estimate,
            ),
            input_learning_rate: blend(rng, self.input_learning_rate, other.input_learning_rate),
            output_learning_rate: blend(rng, self.output_learning_rate, other.output_learning_rate),
            recurrent_learning_rate: blend(
                rng,
                self.recurrent_learning_rate,
                other.recurrent_learning_rate,
            ),
            hebbian_learning_rate: blend(
                rng,
                self.hebbian_learning_rate,
                other.hebbian_learning_rate,
            ),
            memory_learning_rate: blend(rng, self.memory_learning_rate, other.memory_learning_rate),
            momentum: blend(rng, self.momentum, other.momentum),
            weight_decay: blend(rng, self.weight_decay, other.weight_decay),
            normalization_strength: blend(
                rng,
                self.normalization_strength,
                other.normalization_strength,
            ),
            activation_slope: blend(rng, self.activation_slope, other.activation_slope),
            error_clip: blend(rng, self.error_clip, other.error_clip),
            kernel_sharpness: blend(rng, self.kernel_sharpness, other.kernel_sharpness),
        }
        .clamped()
    }

    fn mutate_numeric(&mut self, rng: &mut Rng, intensity: f64) {
        self.connection_density = mutate_range_pair(
            rng,
            self.connection_density,
            intensity,
            (MIN_CONNECTION_DENSITY, MAX_CONNECTION_DENSITY),
        );
        self.input_learning_rate = mutate_range_pair(
            rng,
            self.input_learning_rate,
            intensity,
            INPUT_LEARNING_RATE_RANGE,
        );
        self.output_learning_rate = mutate_range_pair(
            rng,
            self.output_learning_rate,
            intensity,
            OUTPUT_LEARNING_RATE_RANGE,
        );
        self.recurrent_learning_rate = mutate_range_pair(
            rng,
            self.recurrent_learning_rate,
            intensity,
            RECURRENT_LEARNING_RATE_RANGE,
        );
        self.hebbian_learning_rate = mutate_range_pair(
            rng,
            self.hebbian_learning_rate,
            intensity,
            HEBBIAN_LEARNING_RATE_RANGE,
        );
        self.memory_learning_rate = mutate_range_pair(
            rng,
            self.memory_learning_rate,
            intensity,
            MEMORY_LEARNING_RATE_RANGE,
        );
        self.momentum = mutate_range_pair(rng, self.momentum, intensity, MOMENTUM_RANGE);
        self.weight_decay =
            mutate_range_pair(rng, self.weight_decay, intensity, WEIGHT_DECAY_RANGE);
        self.normalization_strength = mutate_range_pair(
            rng,
            self.normalization_strength,
            intensity,
            NORMALIZATION_STRENGTH_RANGE,
        );
        self.activation_slope = mutate_range_pair(
            rng,
            self.activation_slope,
            intensity,
            ACTIVATION_SLOPE_RANGE,
        );
        self.error_clip = mutate_range_pair(rng, self.error_clip, intensity, ERROR_CLIP_RANGE);
        self.kernel_sharpness = mutate_range_pair(
            rng,
            self.kernel_sharpness,
            intensity,
            KERNEL_SHARPNESS_RANGE,
        );
    }

    fn clamped(mut self) -> Self {
        self.hidden_neurons = self
            .hidden_neurons
            .clamp(MIN_HIDDEN_NEURONS, MAX_HIDDEN_NEURONS);
        self.hidden_layers = self
            .hidden_layers
            .clamp(MIN_HIDDEN_LAYERS, MAX_HIDDEN_LAYERS);
        self.recurrent_connections = self
            .recurrent_connections
            .min(self.hidden_neurons * self.hidden_neurons);
        self.memory_cells = self.memory_cells.min(MAX_MEMORY_CELLS);
        self.connection_density = self
            .connection_density
            .clamp(MIN_CONNECTION_DENSITY, MAX_CONNECTION_DENSITY);
        self.input_learning_rate = clamp_pair(self.input_learning_rate, INPUT_LEARNING_RATE_RANGE);
        self.output_learning_rate =
            clamp_pair(self.output_learning_rate, OUTPUT_LEARNING_RATE_RANGE);
        self.recurrent_learning_rate =
            clamp_pair(self.recurrent_learning_rate, RECURRENT_LEARNING_RATE_RANGE);
        self.hebbian_learning_rate =
            clamp_pair(self.hebbian_learning_rate, HEBBIAN_LEARNING_RATE_RANGE);
        self.memory_learning_rate =
            clamp_pair(self.memory_learning_rate, MEMORY_LEARNING_RATE_RANGE);
        self.momentum = clamp_pair(self.momentum, MOMENTUM_RANGE);
        self.weight_decay = clamp_pair(self.weight_decay, WEIGHT_DECAY_RANGE);
        self.normalization_strength =
            clamp_pair(self.normalization_strength, NORMALIZATION_STRENGTH_RANGE);
        self.activation_slope = clamp_pair(self.activation_slope, ACTIVATION_SLOPE_RANGE);
        self.error_clip = clamp_pair(self.error_clip, ERROR_CLIP_RANGE);
        self.kernel_sharpness = clamp_pair(self.kernel_sharpness, KERNEL_SHARPNESS_RANGE);
        if !self.hebbian_update && !self.gradient_update {
            self.gradient_update = true;
        }
        self
    }

    pub(crate) fn input_feature_size(&self, problem: &ProblemData) -> usize {
        self.input_representation
            .encode(
                &vec![0.0; problem.input_dimensions],
                &problem.kernel_centers,
                self.phase_encoding,
                self.kernel_memory,
                self.kernel_sharpness,
            )
            .len()
    }

    pub(crate) fn input_size(&self, problem: &ProblemData) -> usize {
        self.input_feature_size(problem) + self.memory_cells
    }

    pub(crate) fn output_input_size(&self) -> usize {
        self.hidden_neurons + self.memory_cells + 1
    }

    pub(crate) fn complexity(&self, problem: &ProblemData) -> f64 {
        let mut cost = self.hidden_neurons as f64 * self.input_size(problem) as f64;
        cost += self.hidden_layers.saturating_sub(1) as f64
            * self.hidden_neurons as f64
            * (self.hidden_neurons as f64 + 1.0);
        cost += self.output_input_size() as f64 * problem.output_dimensions as f64;
        cost += self.hidden_layers.saturating_sub(1) as f64 * 3.0;
        cost += self.recurrent_connections as f64 * 0.5;
        cost += self.memory_cells as f64 * 2.0;
        cost += if self.hebbian_update { 1.0 } else { 0.0 };
        cost += if self.normalization { 1.0 } else { 0.0 };
        cost += if self.kernel_memory { 1.0 } else { 0.0 };
        cost += if self.phase_encoding { 1.0 } else { 0.0 };
        cost += if self.second_derivative_estimate {
            1.0
        } else {
            0.0
        };
        cost
    }

    pub(crate) fn encode(&self) -> String {
        format!(
            "{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}",
            self.hidden_neurons,
            self.hidden_layers,
            self.recurrent_connections,
            self.memory_cells,
            self.connection_density,
            self.hidden_activation.name(),
            self.output_activation.name(),
            self.input_representation.name(),
            self.loss_function.name(),
            self.learning_schedule.name(),
            self.hebbian_update,
            self.gradient_update,
            self.normalization,
            self.kernel_memory,
            self.phase_encoding,
            self.second_derivative_estimate,
            self.input_learning_rate,
            self.output_learning_rate,
            self.recurrent_learning_rate,
            self.hebbian_learning_rate,
            self.memory_learning_rate,
            self.momentum,
            self.weight_decay,
            self.normalization_strength,
            self.activation_slope,
            self.error_clip,
            self.kernel_sharpness
        )
    }

    pub(crate) fn decode(encoded: &str) -> Result<Self, String> {
        let parts: Vec<&str> = encoded.split('|').collect();
        if parts.len() != 27 {
            return Err(format!(
                "Encoded genome had {} fields; expected 27.",
                parts.len()
            ));
        }
        Ok(Self {
            hidden_neurons: parse_usize(&parts, 0)?,
            hidden_layers: parse_usize(&parts, 1)?,
            recurrent_connections: parse_usize(&parts, 2)?,
            memory_cells: parse_usize(&parts, 3)?,
            connection_density: parse_f64(&parts, 4)?,
            hidden_activation: parse_activation(&parts, 5)?,
            output_activation: parse_activation(&parts, 6)?,
            input_representation: parse_input_representation(&parts, 7)?,
            loss_function: parse_loss_function(&parts, 8)?,
            learning_schedule: parse_learning_schedule(&parts, 9)?,
            hebbian_update: parse_bool(&parts, 10)?,
            gradient_update: parse_bool(&parts, 11)?,
            normalization: parse_bool(&parts, 12)?,
            kernel_memory: parse_bool(&parts, 13)?,
            phase_encoding: parse_bool(&parts, 14)?,
            second_derivative_estimate: parse_bool(&parts, 15)?,
            input_learning_rate: parse_f64(&parts, 16)?,
            output_learning_rate: parse_f64(&parts, 17)?,
            recurrent_learning_rate: parse_f64(&parts, 18)?,
            hebbian_learning_rate: parse_f64(&parts, 19)?,
            memory_learning_rate: parse_f64(&parts, 20)?,
            momentum: parse_f64(&parts, 21)?,
            weight_decay: parse_f64(&parts, 22)?,
            normalization_strength: parse_f64(&parts, 23)?,
            activation_slope: parse_f64(&parts, 24)?,
            error_clip: parse_f64(&parts, 25)?,
            kernel_sharpness: parse_f64(&parts, 26)?,
        }
        .clamped())
    }
}

fn clamp_pair(value: f64, bounds: (f64, f64)) -> f64 {
    value.clamp(bounds.0, bounds.1)
}

fn parse_usize(parts: &[&str], index: usize) -> Result<usize, String> {
    parts[index]
        .parse()
        .map_err(|_| format!("Genome field {index} was not an unsigned integer."))
}

fn parse_f64(parts: &[&str], index: usize) -> Result<f64, String> {
    parts[index]
        .parse()
        .map_err(|_| format!("Genome field {index} was not a finite number."))
        .and_then(|value: f64| {
            value
                .is_finite()
                .then_some(value)
                .ok_or_else(|| format!("Genome field {index} was not finite."))
        })
}

fn parse_bool(parts: &[&str], index: usize) -> Result<bool, String> {
    parts[index]
        .parse()
        .map_err(|_| format!("Genome field {index} was not a boolean."))
}

fn parse_activation(parts: &[&str], index: usize) -> Result<Activation, String> {
    Activation::from_name(parts[index]).ok_or_else(|| {
        format!(
            "Genome field {index} had unknown activation '{}'.",
            parts[index]
        )
    })
}

fn parse_input_representation(parts: &[&str], index: usize) -> Result<InputRepresentation, String> {
    InputRepresentation::from_name(parts[index]).ok_or_else(|| {
        format!(
            "Genome field {index} had unknown input representation '{}'.",
            parts[index]
        )
    })
}

fn parse_loss_function(parts: &[&str], index: usize) -> Result<LossFunction, String> {
    LossFunction::from_name(parts[index]).ok_or_else(|| {
        format!(
            "Genome field {index} had unknown loss function '{}'.",
            parts[index]
        )
    })
}

fn parse_learning_schedule(parts: &[&str], index: usize) -> Result<LearningSchedule, String> {
    LearningSchedule::from_name(parts[index]).ok_or_else(|| {
        format!(
            "Genome field {index} had unknown learning schedule '{}'.",
            parts[index]
        )
    })
}

#[cfg(test)]
mod tests {
    use super::Genome;
    use crate::random::Rng;

    #[test]
    fn genome_encoding_round_trips() {
        let genome = Genome::random(&mut Rng::new(42));

        let decoded = Genome::decode(&genome.encode()).expect("genome should decode");

        assert_eq!(genome.encode(), decoded.encode());
    }
}
