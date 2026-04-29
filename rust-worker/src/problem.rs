use crate::constants::{
    MAX_PROBLEM_PAYLOAD_VERSION, MIN_PROBLEM_PAYLOAD_VERSION, PROBLEM_PAYLOAD_MAGIC,
};

#[derive(Clone)]
pub(crate) struct ProblemData {
    pub(crate) input_dimensions: usize,
    pub(crate) output_dimensions: usize,
    pub(crate) classification: bool,
    pub(crate) stateful_samples: bool,
    pub(crate) complexity_scale: f64,
    pub(crate) free_energy_objective_weight: f64,
    pub(crate) free_energy_sensory_weight: f64,
    pub(crate) free_energy_latent_weight: f64,
    pub(crate) free_energy_complexity_weight: f64,
    pub(crate) free_energy_maximum: f64,
    pub(crate) training_samples: Vec<Sample>,
    pub(crate) generalization_samples: Vec<Sample>,
    pub(crate) jitter_samples: Vec<Sample>,
    pub(crate) smoothness_probes: Vec<SmoothnessProbe>,
    pub(crate) kernel_centers: Vec<Vec<f64>>,
    pub(crate) output_groups: Vec<OutputGroup>,
    pub(crate) training_target_means: Vec<f64>,
}

impl ProblemData {
    pub(crate) fn from_hex(hex: &str) -> Result<Self, String> {
        let bytes = decode_hex(hex)?;
        let mut cursor = Cursor::new(bytes);
        let magic = cursor.read_i32()?;
        if magic != PROBLEM_PAYLOAD_MAGIC {
            return Err("Problem payload magic did not match.".to_string());
        }
        let version = cursor.read_i32()?;
        if !(MIN_PROBLEM_PAYLOAD_VERSION..=MAX_PROBLEM_PAYLOAD_VERSION).contains(&version) {
            return Err(format!("Unsupported problem payload version {}.", version));
        }
        let input_dimensions = cursor.read_i32()?.max(1) as usize;
        let output_dimensions = cursor.read_i32()?.max(1) as usize;
        let classification = cursor.read_bool()?;
        let stateful_samples = cursor.read_bool()?;
        let complexity_scale = cursor.read_f64()?.max(1.0);
        let free_energy_objective_weight = cursor.read_f64()?.max(0.0);
        let (
            free_energy_sensory_weight,
            free_energy_latent_weight,
            free_energy_complexity_weight,
            free_energy_maximum,
        ) = if version >= 2 {
            (
                cursor.read_f64()?.max(0.0),
                cursor.read_f64()?.max(0.0),
                cursor.read_f64()?.max(0.0),
                cursor.read_f64()?.max(1.0e-12),
            )
        } else {
            (0.0, 0.0, 0.0, 1.0)
        };
        let training_samples = cursor.read_samples(input_dimensions, output_dimensions)?;
        let generalization_samples = cursor.read_samples(input_dimensions, output_dimensions)?;
        let (jitter_samples, smoothness_probes) = if version >= 3 {
            (
                cursor.read_samples(input_dimensions, output_dimensions)?,
                cursor.read_smoothness_probes(input_dimensions, output_dimensions)?,
            )
        } else {
            let _jitter_anchors = cursor.read_vectors(input_dimensions)?;
            (Vec::new(), Vec::new())
        };
        let kernel_centers = cursor.read_vectors(input_dimensions)?;
        let group_count = cursor.read_i32()?.max(0) as usize;
        let mut output_groups = Vec::with_capacity(group_count);
        for _ in 0..group_count {
            output_groups.push(OutputGroup {
                start: cursor.read_i32()?.max(0) as usize,
                end: cursor.read_i32()?.max(0) as usize,
                weight: cursor.read_f64()?.max(0.0),
            });
        }
        if output_groups.is_empty() {
            output_groups.push(OutputGroup {
                start: 0,
                end: output_dimensions,
                weight: 1.0,
            });
        }
        let training_target_means = target_means(&training_samples, output_dimensions);
        Ok(Self {
            input_dimensions,
            output_dimensions,
            classification,
            stateful_samples,
            complexity_scale,
            free_energy_objective_weight,
            free_energy_sensory_weight,
            free_energy_latent_weight,
            free_energy_complexity_weight,
            free_energy_maximum,
            training_samples,
            generalization_samples,
            jitter_samples,
            smoothness_probes,
            kernel_centers,
            output_groups,
            training_target_means,
        })
    }
}

#[derive(Clone)]
pub(crate) struct Sample {
    pub(crate) input: Vec<f64>,
    pub(crate) targets: Vec<f64>,
}

#[derive(Clone)]
pub(crate) struct OutputGroup {
    pub(crate) start: usize,
    pub(crate) end: usize,
    pub(crate) weight: f64,
}

#[derive(Clone)]
pub(crate) struct SmoothnessProbe {
    pub(crate) center: Sample,
    pub(crate) plus: Sample,
    pub(crate) minus: Sample,
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

struct Cursor {
    bytes: Vec<u8>,
    offset: usize,
}

impl Cursor {
    fn new(bytes: Vec<u8>) -> Self {
        Self { bytes, offset: 0 }
    }

    fn read_i32(&mut self) -> Result<i32, String> {
        let bytes = self.take(4)?;
        Ok(i32::from_be_bytes(bytes.try_into().unwrap()))
    }

    fn read_bool(&mut self) -> Result<bool, String> {
        let byte = self.take(1)?[0];
        Ok(byte != 0)
    }

    fn read_f64(&mut self) -> Result<f64, String> {
        let bytes = self.take(8)?;
        Ok(f64::from_bits(u64::from_be_bytes(
            bytes.try_into().unwrap(),
        )))
    }

    fn read_samples(
        &mut self,
        input_dims: usize,
        output_dims: usize,
    ) -> Result<Vec<Sample>, String> {
        let count = self.read_i32()?.max(0) as usize;
        let mut samples = Vec::with_capacity(count);
        for _ in 0..count {
            samples.push(Sample {
                input: self.read_vector(input_dims)?,
                targets: self.read_vector(output_dims)?,
            });
        }
        Ok(samples)
    }

    fn read_vectors(&mut self, dims: usize) -> Result<Vec<Vec<f64>>, String> {
        let count = self.read_i32()?.max(0) as usize;
        let mut vectors = Vec::with_capacity(count);
        for _ in 0..count {
            vectors.push(self.read_vector(dims)?);
        }
        Ok(vectors)
    }

    fn read_smoothness_probes(
        &mut self,
        input_dims: usize,
        output_dims: usize,
    ) -> Result<Vec<SmoothnessProbe>, String> {
        let count = self.read_i32()?.max(0) as usize;
        let mut probes = Vec::with_capacity(count);
        for _ in 0..count {
            probes.push(SmoothnessProbe {
                center: self.read_sample(input_dims, output_dims)?,
                plus: self.read_sample(input_dims, output_dims)?,
                minus: self.read_sample(input_dims, output_dims)?,
            });
        }
        Ok(probes)
    }

    fn read_sample(&mut self, input_dims: usize, output_dims: usize) -> Result<Sample, String> {
        Ok(Sample {
            input: self.read_vector(input_dims)?,
            targets: self.read_vector(output_dims)?,
        })
    }

    fn read_vector(&mut self, dims: usize) -> Result<Vec<f64>, String> {
        (0..dims).map(|_| self.read_f64()).collect()
    }

    fn take(&mut self, length: usize) -> Result<&[u8], String> {
        if self.offset + length > self.bytes.len() {
            return Err("Problem payload ended early.".to_string());
        }
        let start = self.offset;
        self.offset += length;
        Ok(&self.bytes[start..self.offset])
    }
}

fn decode_hex(hex: &str) -> Result<Vec<u8>, String> {
    if hex.len() % 2 != 0 {
        return Err("Hex payload length must be even.".to_string());
    }
    let mut bytes = Vec::with_capacity(hex.len() / 2);
    let chars = hex.as_bytes();
    for index in (0..hex.len()).step_by(2) {
        let high = hex_digit(chars[index])?;
        let low = hex_digit(chars[index + 1])?;
        bytes.push((high << 4) | low);
    }
    Ok(bytes)
}

fn hex_digit(byte: u8) -> Result<u8, String> {
    match byte {
        b'0'..=b'9' => Ok(byte - b'0'),
        b'a'..=b'f' => Ok(byte - b'a' + 10),
        b'A'..=b'F' => Ok(byte - b'A' + 10),
        _ => Err("Invalid hex digit in payload.".to_string()),
    }
}
