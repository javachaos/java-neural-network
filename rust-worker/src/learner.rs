use crate::constants::{
    INITIAL_RECURRENT_WEIGHT_MAX, INITIAL_RECURRENT_WEIGHT_MIN, OUTPUT_MAX, OUTPUT_MIN,
    RECURRENT_CONNECTION_ATTEMPT_MULTIPLIER, RECURRENT_CONNECTION_ATTEMPT_PADDING,
    SECOND_DERIVATIVE_SIGNAL_WEIGHT,
};
use crate::genome::Genome;
use crate::math::{
    array_finite, clamp_weight, clip, cosine_prediction_energy, masked_dot, mean, normalize,
    prediction_matches, weighted_baseline_relative_error,
};
use crate::problem::{ProblemData, Sample, SmoothnessProbe};
use crate::random::Rng;
use crate::weights::FlatWeightMatrix;

pub(crate) struct Learner<'a> {
    problem: &'a ProblemData,
    genome: &'a Genome,
    hidden_weights: Vec<FlatWeightMatrix>,
    recurrent_weights: FlatWeightMatrix,
    output_weights: FlatWeightMatrix,
    memory_state: Vec<f64>,
    previous_hidden_state: Vec<f64>,
    previous_error_signal: Vec<f64>,
}

impl<'a> Learner<'a> {
    pub(crate) fn new(problem: &'a ProblemData, genome: &'a Genome, mut rng: Rng) -> Self {
        let mut hidden_weights = Vec::with_capacity(genome.hidden_layers);
        for layer in 0..genome.hidden_layers {
            let input_width = if layer == 0 {
                genome.input_size(problem)
            } else {
                genome.hidden_neurons + 1
            };
            let mut layer_weights = FlatWeightMatrix::new(genome.hidden_neurons, input_width);
            for neuron in 0..genome.hidden_neurons {
                let mut has_connection = false;
                for weight in 0..input_width {
                    let enabled = weight == 0 || rng.f64() <= genome.connection_density;
                    layer_weights.set_enabled(neuron, weight, enabled);
                    has_connection |= enabled;
                    layer_weights.set_weight(neuron, weight, rng.range(-1.0, 1.0));
                }
                if !has_connection {
                    layer_weights.set_enabled(neuron, 0, true);
                }
            }
            hidden_weights.push(layer_weights);
        }
        let mut output_weights =
            FlatWeightMatrix::new(problem.output_dimensions, genome.output_input_size());
        for output in 0..problem.output_dimensions {
            for weight in 0..genome.output_input_size() {
                output_weights.set_enabled(
                    output,
                    weight,
                    weight == 0 || rng.f64() <= genome.connection_density,
                );
                output_weights.set_weight(output, weight, rng.range(-1.0, 1.0));
            }
            if genome.output_input_size() > 1 {
                output_weights.set_enabled(
                    output,
                    1 + rng.usize(genome.output_input_size() - 1),
                    true,
                );
            }
        }
        let mut recurrent_weights =
            FlatWeightMatrix::new(genome.hidden_neurons, genome.hidden_neurons);
        let mut added = 0usize;
        let mut attempts = 0usize;
        while added < genome.recurrent_connections
            && attempts
                < genome.recurrent_connections * RECURRENT_CONNECTION_ATTEMPT_MULTIPLIER
                    + RECURRENT_CONNECTION_ATTEMPT_PADDING
        {
            let to = rng.usize(genome.hidden_neurons);
            let from = rng.usize(genome.hidden_neurons);
            if !recurrent_weights.enabled(to, from) {
                recurrent_weights.set_enabled(to, from, true);
                recurrent_weights.set_weight(
                    to,
                    from,
                    rng.range(INITIAL_RECURRENT_WEIGHT_MIN, INITIAL_RECURRENT_WEIGHT_MAX),
                );
                added += 1;
            }
            attempts += 1;
        }
        Self {
            problem,
            genome,
            hidden_weights,
            recurrent_weights,
            output_weights,
            memory_state: vec![0.0; genome.memory_cells],
            previous_hidden_state: vec![0.0; genome.hidden_neurons],
            previous_error_signal: vec![0.0; problem.output_dimensions],
        }
    }

    pub(crate) fn train(&mut self, max_epochs: usize, target_mse: f64) -> f64 {
        let mut mse = self.mean_squared_error(&self.problem.training_samples);
        for epoch in 0..max_epochs {
            if mse <= target_mse {
                break;
            }
            self.reset_state();
            let schedule_scale = self.genome.learning_schedule.scale(epoch, max_epochs);
            for sample in &self.problem.training_samples {
                self.prepare_sample_state();
                self.train_sample(&sample.input, &sample.targets, schedule_scale);
                if !self.weights_finite() {
                    return f64::INFINITY;
                }
            }
            mse = self.mean_squared_error(&self.problem.training_samples);
            if !mse.is_finite() {
                return f64::INFINITY;
            }
        }
        mse
    }

    pub(crate) fn train_sample(&mut self, input: &[f64], targets: &[f64], schedule_scale: f64) {
        let pass = self.forward(input);
        let mut signals = vec![0.0; targets.len()];
        let mut output_signals = vec![0.0; targets.len()];
        for output in 0..targets.len() {
            let mut signal = self
                .genome
                .loss_function
                .signal(targets[output], pass.outputs[output]);
            signal = clip(signal, self.genome.error_clip);
            if self.genome.second_derivative_estimate {
                signal = clip(
                    signal
                        + SECOND_DERIVATIVE_SIGNAL_WEIGHT
                            * (signal - self.previous_error_signal[output]),
                    self.genome.error_clip,
                );
            }
            signals[output] = signal;
            output_signals[output] = signal
                * self
                    .genome
                    .output_activation
                    .output_derivative(pass.output_scaled_inputs[output])
                * self.genome.activation_slope;
        }
        let aggregate_signal = mean(&signals);
        let hidden_credits = self.hidden_credits(&pass, &output_signals);
        self.update_output_weights(
            &pass.output_input,
            &signals,
            &output_signals,
            schedule_scale,
        );
        self.update_hidden_weights(&pass, &hidden_credits, aggregate_signal, schedule_scale);
        self.update_recurrent_weights(&pass, &hidden_credits, aggregate_signal, schedule_scale);
        if self.genome.normalization {
            self.normalize_weights();
        }
        self.finish_sample_state(&pass);
        self.previous_error_signal.copy_from_slice(&signals);
    }

    fn forward(&self, input: &[f64]) -> ForwardPass {
        let full_input = self.full_input(input);
        let mut hidden_inputs = Vec::with_capacity(self.hidden_weights.len());
        let mut hidden_layers =
            vec![vec![0.0; self.genome.hidden_neurons]; self.hidden_weights.len()];
        let mut hidden_scaled_inputs =
            vec![vec![0.0; self.genome.hidden_neurons]; self.hidden_weights.len()];
        let last_layer = self.hidden_weights.len() - 1;
        for layer in 0..self.hidden_weights.len() {
            let layer_input = if layer == 0 {
                full_input.clone()
            } else {
                hidden_layer_input(&hidden_layers[layer - 1])
            };
            hidden_inputs.push(layer_input.clone());
            let layer_weights = &self.hidden_weights[layer];
            for neuron in 0..layer_weights.rows() {
                let mut raw = masked_dot(
                    layer_weights.row_weights(neuron),
                    layer_weights.row_mask(neuron),
                    &layer_input,
                );
                if layer == last_layer {
                    raw += self.recurrent_dot(neuron);
                }
                let scaled = self.genome.activation_slope * raw;
                hidden_scaled_inputs[layer][neuron] = scaled;
                hidden_layers[layer][neuron] = self.genome.hidden_activation.hidden(scaled);
            }
        }
        let output_input = self.output_input(&hidden_layers[last_layer]);
        let mut output_scaled_inputs = vec![0.0; self.output_weights.rows()];
        let mut outputs = vec![0.0; self.output_weights.rows()];
        for output in 0..self.output_weights.rows() {
            let raw = masked_dot(
                self.output_weights.row_weights(output),
                self.output_weights.row_mask(output),
                &output_input,
            );
            output_scaled_inputs[output] = self.genome.activation_slope * raw;
            outputs[output] = self
                .genome
                .output_activation
                .output(output_scaled_inputs[output])
                .clamp(OUTPUT_MIN, OUTPUT_MAX);
        }
        ForwardPass {
            hidden_inputs,
            hidden_layers,
            hidden_scaled_inputs,
            output_input,
            output_scaled_inputs,
            outputs,
        }
    }

    fn full_input(&self, input: &[f64]) -> Vec<f64> {
        let mut features = self.genome.input_representation.encode(
            input,
            &self.problem.kernel_centers,
            self.genome.phase_encoding,
            self.genome.kernel_memory,
            self.genome.kernel_sharpness,
        );
        features.extend_from_slice(&self.memory_state);
        features
    }

    fn output_input(&self, hidden: &[f64]) -> Vec<f64> {
        let mut values = Vec::with_capacity(1 + hidden.len() + self.memory_state.len());
        values.push(1.0);
        values.extend_from_slice(hidden);
        values.extend_from_slice(&self.memory_state);
        values
    }

    fn hidden_credits(&self, pass: &ForwardPass, output_signals: &[f64]) -> Vec<Vec<f64>> {
        let mut credits = vec![vec![0.0; self.genome.hidden_neurons]; self.hidden_weights.len()];
        let last_layer = self.hidden_weights.len() - 1;
        for neuron in 0..self.genome.hidden_neurons {
            let mut output_credit = 0.0;
            for (output, signal) in output_signals.iter().enumerate() {
                output_credit += signal * self.connected_output_weight(output, neuron + 1);
            }
            credits[last_layer][neuron] = output_credit
                * self
                    .genome
                    .hidden_activation
                    .hidden_derivative(pass.hidden_scaled_inputs[last_layer][neuron])
                * self.genome.activation_slope;
        }
        for layer in (0..last_layer).rev() {
            for neuron in 0..self.genome.hidden_neurons {
                let mut downstream = 0.0;
                let next_input_index = neuron + 1;
                for next in 0..self.genome.hidden_neurons {
                    if self.hidden_weights[layer + 1].enabled(next, next_input_index) {
                        downstream += credits[layer + 1][next]
                            * self.hidden_weights[layer + 1].weight(next, next_input_index);
                    }
                }
                credits[layer][neuron] = downstream
                    * self
                        .genome
                        .hidden_activation
                        .hidden_derivative(pass.hidden_scaled_inputs[layer][neuron])
                    * self.genome.activation_slope;
            }
        }
        credits
    }

    fn update_output_weights(
        &mut self,
        output_input: &[f64],
        signals: &[f64],
        output_signals: &[f64],
        schedule_scale: f64,
    ) {
        for output in 0..self.output_weights.rows() {
            let (weights, previous_deltas, mask) = self.output_weights.row_parts_mut(output);
            for i in 0..weights.len() {
                if !mask[i] {
                    continue;
                }
                let mut delta = -self.genome.weight_decay * weights[i];
                if self.genome.gradient_update {
                    delta += schedule_scale
                        * self.genome.output_learning_rate
                        * output_signals[output]
                        * output_input[i];
                }
                if self.genome.hebbian_update {
                    delta += schedule_scale
                        * self.genome.hebbian_learning_rate
                        * signals[output]
                        * output_input[i];
                }
                delta += self.genome.momentum * previous_deltas[i];
                weights[i] = clamp_weight(weights[i] + delta);
                previous_deltas[i] = delta;
            }
        }
    }

    fn update_hidden_weights(
        &mut self,
        pass: &ForwardPass,
        hidden_credits: &[Vec<f64>],
        signal: f64,
        schedule_scale: f64,
    ) {
        for layer in 0..self.hidden_weights.len() {
            for neuron in 0..self.hidden_weights[layer].rows() {
                let hidden_credit = hidden_credits[layer][neuron];
                let (weights, previous_deltas, mask) =
                    self.hidden_weights[layer].row_parts_mut(neuron);
                for weight in 0..weights.len() {
                    if !mask[weight] {
                        continue;
                    }
                    let mut delta = -self.genome.weight_decay * weights[weight];
                    if self.genome.gradient_update {
                        delta += schedule_scale
                            * self.genome.input_learning_rate
                            * hidden_credit
                            * pass.hidden_inputs[layer][weight];
                    }
                    if self.genome.hebbian_update {
                        delta += schedule_scale
                            * self.genome.hebbian_learning_rate
                            * signal
                            * pass.hidden_layers[layer][neuron]
                            * pass.hidden_inputs[layer][weight];
                    }
                    delta += self.genome.momentum * previous_deltas[weight];
                    weights[weight] = clamp_weight(weights[weight] + delta);
                    previous_deltas[weight] = delta;
                }
            }
        }
    }

    fn update_recurrent_weights(
        &mut self,
        pass: &ForwardPass,
        hidden_credits: &[Vec<f64>],
        signal: f64,
        schedule_scale: f64,
    ) {
        let final_hidden = pass.final_hidden();
        let final_hidden_credits = &hidden_credits[hidden_credits.len() - 1];
        for to in 0..self.recurrent_weights.rows() {
            let hidden_credit = final_hidden_credits[to];
            let (weights, previous_deltas, mask) = self.recurrent_weights.row_parts_mut(to);
            for from in 0..weights.len() {
                if !mask[from] {
                    continue;
                }
                let mut delta = -self.genome.weight_decay * weights[from];
                if self.genome.gradient_update {
                    delta += schedule_scale
                        * self.genome.recurrent_learning_rate
                        * hidden_credit
                        * self.previous_hidden_state[from];
                }
                if self.genome.hebbian_update {
                    delta += schedule_scale
                        * self.genome.hebbian_learning_rate
                        * signal
                        * final_hidden[to]
                        * self.previous_hidden_state[from];
                }
                delta += self.genome.momentum * previous_deltas[from];
                weights[from] = clamp_weight(weights[from] + delta);
                previous_deltas[from] = delta;
            }
        }
    }

    fn normalize_weights(&mut self) {
        for layer in 0..self.hidden_weights.len() {
            for neuron in 0..self.hidden_weights[layer].rows() {
                let (weights, _, mask) = self.hidden_weights[layer].row_parts_mut(neuron);
                normalize(weights, mask, self.genome.normalization_strength);
            }
        }
        for neuron in 0..self.recurrent_weights.rows() {
            let (weights, _, mask) = self.recurrent_weights.row_parts_mut(neuron);
            normalize(weights, mask, self.genome.normalization_strength);
        }
        for output in 0..self.output_weights.rows() {
            let (weights, _, mask) = self.output_weights.row_parts_mut(output);
            normalize(weights, mask, self.genome.normalization_strength);
        }
    }

    pub(crate) fn mean_squared_error(&mut self, samples: &[Sample]) -> f64 {
        if samples.is_empty() {
            return 0.0;
        }
        self.reset_state();
        let mut total = 0.0;
        let mut count = 0usize;
        for sample in samples {
            self.prepare_sample_state();
            let pass = self.forward(&sample.input);
            if !array_finite(&pass.outputs) {
                return f64::INFINITY;
            }
            for output in 0..sample.targets.len() {
                let error = sample.targets[output] - pass.outputs[output];
                total += error * error;
                count += 1;
            }
            self.finish_sample_state(&pass);
        }
        if count == 0 {
            0.0
        } else {
            total / count as f64
        }
    }

    pub(crate) fn smoothness_penalty(&mut self) -> f64 {
        if self.problem.smoothness_probes.is_empty() {
            return 0.0;
        }
        let mut total = 0.0;
        let mut count = 0usize;
        for probe in &self.problem.smoothness_probes {
            let Some(prediction_plus) = self.predict_vector(&probe.plus.input) else {
                return f64::INFINITY;
            };
            let Some(prediction_minus) = self.predict_vector(&probe.minus.input) else {
                return f64::INFINITY;
            };
            let Some(prediction_center) = self.predict_vector(&probe.center.input) else {
                return f64::INFINITY;
            };
            total += curvature_error(
                probe,
                &prediction_plus,
                &prediction_minus,
                &prediction_center,
            );
            count += probe.center.targets.len();
        }
        if count == 0 {
            0.0
        } else {
            total / count as f64
        }
    }

    pub(crate) fn predict_vector(&mut self, input: &[f64]) -> Option<Vec<f64>> {
        self.reset_state();
        let pass = self.forward(input);
        array_finite(&pass.outputs).then_some(pass.outputs)
    }

    pub(crate) fn configured_loss(&mut self) -> f64 {
        self.reset_state();
        let mut total = 0.0;
        let mut count = 0usize;
        for sample in &self.problem.training_samples {
            self.prepare_sample_state();
            let pass = self.forward(&sample.input);
            if !array_finite(&pass.outputs) {
                return f64::INFINITY;
            }
            for output in 0..sample.targets.len() {
                total += self
                    .genome
                    .loss_function
                    .loss(sample.targets[output], pass.outputs[output]);
                count += 1;
            }
            self.finish_sample_state(&pass);
        }
        if count == 0 {
            0.0
        } else {
            total / count as f64
        }
    }

    pub(crate) fn accuracy(&mut self) -> f64 {
        if !self.problem.classification {
            return 1.0;
        }
        self.reset_state();
        let mut correct = 0usize;
        for sample in &self.problem.training_samples {
            self.prepare_sample_state();
            let pass = self.forward(&sample.input);
            if array_finite(&pass.outputs) && prediction_matches(&sample.targets, &pass.outputs) {
                correct += 1;
            }
            self.finish_sample_state(&pass);
        }
        correct as f64 / self.problem.training_samples.len() as f64
    }

    pub(crate) fn grouped_mean_squared_error(&mut self, samples: &[Sample]) -> (f64, f64) {
        if samples.is_empty() {
            return (0.0, 0.0);
        }
        let mut group_totals = vec![0.0; self.problem.output_groups.len()];
        let mut baseline_totals = vec![0.0; self.problem.output_groups.len()];
        let mut group_counts = vec![0usize; self.problem.output_groups.len()];
        let mut total = 0.0;
        let mut count = 0usize;
        self.reset_state();
        for sample in samples {
            self.prepare_sample_state();
            let pass = self.forward(&sample.input);
            if !array_finite(&pass.outputs) {
                return (f64::INFINITY, f64::INFINITY);
            }
            for (group_index, group) in self.problem.output_groups.iter().enumerate() {
                for output in group.start..group.end {
                    let error = sample.targets[output] - pass.outputs[output];
                    let baseline_error =
                        sample.targets[output] - self.problem.training_target_means[output];
                    group_totals[group_index] += error * error;
                    baseline_totals[group_index] += baseline_error * baseline_error;
                    group_counts[group_index] += 1;
                    total += error * error;
                    count += 1;
                }
            }
            self.finish_sample_state(&pass);
        }
        let mse = if count == 0 {
            0.0
        } else {
            total / count as f64
        };
        (
            mse,
            weighted_baseline_relative_error(
                &self.problem.output_groups,
                &group_totals,
                &baseline_totals,
                &group_counts,
            ),
        )
    }

    pub(crate) fn predictive_free_energy(
        &mut self,
        normalized_complexity: f64,
    ) -> (f64, f64, f64, f64) {
        if self.problem.free_energy_objective_weight <= 0.0
            || (self.problem.free_energy_sensory_weight == 0.0
                && self.problem.free_energy_latent_weight == 0.0
                && self.problem.free_energy_complexity_weight == 0.0)
            || self.problem.generalization_samples.is_empty()
        {
            return (0.0, 0.0, 0.0, 0.0);
        }

        let mut group_totals = vec![0.0; self.problem.output_groups.len()];
        let mut baseline_totals = vec![0.0; self.problem.output_groups.len()];
        let mut group_counts = vec![0usize; self.problem.output_groups.len()];
        let mut latent_total = 0.0;
        let mut latent_count = 0usize;
        self.reset_state();
        for sample in &self.problem.generalization_samples {
            self.prepare_sample_state();
            let pass = self.forward(&sample.input);
            if !array_finite(&pass.outputs) {
                return (
                    self.problem.free_energy_maximum,
                    f64::INFINITY,
                    f64::INFINITY,
                    normalized_complexity,
                );
            }
            for (group_index, group) in self.problem.output_groups.iter().enumerate() {
                for output in group.start..group.end {
                    let error = sample.targets[output] - pass.outputs[output];
                    let baseline_error =
                        sample.targets[output] - self.problem.training_target_means[output];
                    group_totals[group_index] += error * error;
                    baseline_totals[group_index] += baseline_error * baseline_error;
                    group_counts[group_index] += 1;
                }
            }
            for layer in 0..self.hidden_weights.len() {
                let layer_energy = self.latent_prediction_energy(&pass, layer);
                if !layer_energy.is_finite() {
                    return (
                        self.problem.free_energy_maximum,
                        weighted_baseline_relative_error(
                            &self.problem.output_groups,
                            &group_totals,
                            &baseline_totals,
                            &group_counts,
                        ),
                        f64::INFINITY,
                        normalized_complexity,
                    );
                }
                latent_total += layer_energy;
                latent_count += 1;
            }
            self.finish_sample_state(&pass);
        }
        let sensory_energy = weighted_baseline_relative_error(
            &self.problem.output_groups,
            &group_totals,
            &baseline_totals,
            &group_counts,
        );
        let latent_energy = if latent_count == 0 {
            0.0
        } else {
            latent_total / latent_count as f64
        };
        let raw_energy = self.problem.free_energy_sensory_weight * sensory_energy
            + self.problem.free_energy_latent_weight * latent_energy
            + self.problem.free_energy_complexity_weight * normalized_complexity;
        let bounded_energy = if raw_energy.is_finite() {
            raw_energy.max(0.0).min(self.problem.free_energy_maximum)
        } else {
            self.problem.free_energy_maximum
        };
        (
            bounded_energy,
            sensory_energy,
            latent_energy,
            normalized_complexity,
        )
    }

    fn latent_prediction_energy(&self, pass: &ForwardPass, layer: usize) -> f64 {
        let actual = &pass.hidden_inputs[layer];
        let mut prediction = vec![0.0; actual.len()];
        for input in 1..actual.len() {
            let mut predicted = 0.0;
            let mut connections = 0usize;
            for neuron in 0..self.hidden_weights[layer].rows() {
                if self.hidden_weights[layer].enabled(neuron, input) {
                    predicted += pass.hidden_layers[layer][neuron]
                        * self.hidden_weights[layer].weight(neuron, input);
                    connections += 1;
                }
            }
            if connections > 0 {
                prediction[input] = predicted / (connections as f64).sqrt();
            }
        }
        cosine_prediction_energy(actual, &prediction, 1)
    }

    fn recurrent_dot(&self, neuron: usize) -> f64 {
        let mut total = 0.0;
        for from in 0..self.previous_hidden_state.len() {
            if self.recurrent_weights.enabled(neuron, from) {
                total +=
                    self.recurrent_weights.weight(neuron, from) * self.previous_hidden_state[from];
            }
        }
        total
    }

    fn connected_output_weight(&self, output: usize, index: usize) -> f64 {
        if self.output_weights.enabled(output, index) {
            self.output_weights.weight(output, index)
        } else {
            0.0
        }
    }

    fn prepare_sample_state(&mut self) {
        if !self.problem.stateful_samples {
            self.reset_state();
        }
    }

    fn finish_sample_state(&mut self, pass: &ForwardPass) {
        if self.problem.stateful_samples {
            self.advance_state(pass.final_hidden());
        }
    }

    fn advance_state(&mut self, hidden: &[f64]) {
        for i in 0..self.memory_state.len() {
            let candidate = hidden[i % hidden.len()];
            self.memory_state[i] = clip(
                (1.0 - self.genome.memory_learning_rate) * self.memory_state[i]
                    + self.genome.memory_learning_rate * candidate,
                2.0,
            );
        }
        self.previous_hidden_state.copy_from_slice(hidden);
    }

    fn reset_state(&mut self) {
        self.memory_state.fill(0.0);
        self.previous_hidden_state.fill(0.0);
        self.previous_error_signal.fill(0.0);
    }

    fn weights_finite(&self) -> bool {
        self.hidden_weights
            .iter()
            .all(FlatWeightMatrix::weights_finite)
            && self.recurrent_weights.weights_finite()
            && self.output_weights.weights_finite()
    }
}

struct ForwardPass {
    hidden_inputs: Vec<Vec<f64>>,
    hidden_layers: Vec<Vec<f64>>,
    hidden_scaled_inputs: Vec<Vec<f64>>,
    output_input: Vec<f64>,
    output_scaled_inputs: Vec<f64>,
    outputs: Vec<f64>,
}

impl ForwardPass {
    fn final_hidden(&self) -> &[f64] {
        &self.hidden_layers[self.hidden_layers.len() - 1]
    }
}

fn hidden_layer_input(previous: &[f64]) -> Vec<f64> {
    let mut values = Vec::with_capacity(previous.len() + 1);
    values.push(1.0);
    values.extend_from_slice(previous);
    values
}

fn curvature_error(
    probe: &SmoothnessProbe,
    prediction_plus: &[f64],
    prediction_minus: &[f64],
    prediction_center: &[f64],
) -> f64 {
    let mut total = 0.0;
    for output in 0..probe.center.targets.len() {
        let prediction_curvature =
            prediction_plus[output] + prediction_minus[output] - 2.0 * prediction_center[output];
        let target_curvature = probe.plus.targets[output] + probe.minus.targets[output]
            - 2.0 * probe.center.targets[output];
        let error = target_curvature - prediction_curvature;
        total += error * error;
    }
    total
}
