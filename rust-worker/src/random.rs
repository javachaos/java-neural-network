use crate::constants::{RNG_MIX_MULTIPLIER_A, RNG_MIX_MULTIPLIER_B, RNG_STATE_INCREMENT};

pub(crate) fn choose<T: Copy>(rng: &mut Rng, left: T, right: T) -> T {
    if rng.bool(0.5) { left } else { right }
}

pub(crate) fn blend(rng: &mut Rng, left: f64, right: f64) -> f64 {
    let weight = rng.f64();
    left * weight + right * (1.0 - weight)
}

pub(crate) fn mutate_range(rng: &mut Rng, value: f64, intensity: f64, min: f64, max: f64) -> f64 {
    let span = max - min;
    (value + rng.range(-span * intensity, span * intensity)).clamp(min, max)
}

pub(crate) fn mutate_range_pair(
    rng: &mut Rng,
    value: f64,
    intensity: f64,
    bounds: (f64, f64),
) -> f64 {
    mutate_range(rng, value, intensity, bounds.0, bounds.1)
}

pub(crate) struct Rng {
    state: u64,
}

impl Rng {
    pub(crate) fn new(seed: u64) -> Self {
        Self {
            state: seed ^ RNG_STATE_INCREMENT,
        }
    }

    pub(crate) fn next_u64(&mut self) -> u64 {
        self.state = self.state.wrapping_add(RNG_STATE_INCREMENT);
        let mut value = self.state;
        value = (value ^ (value >> 30)).wrapping_mul(RNG_MIX_MULTIPLIER_A);
        value = (value ^ (value >> 27)).wrapping_mul(RNG_MIX_MULTIPLIER_B);
        value ^ (value >> 31)
    }

    pub(crate) fn f64(&mut self) -> f64 {
        ((self.next_u64() >> 11) as f64) * (1.0 / ((1_u64 << 53) as f64))
    }

    pub(crate) fn range(&mut self, min: f64, max: f64) -> f64 {
        min + self.f64() * (max - min)
    }

    pub(crate) fn range_pair(&mut self, bounds: (f64, f64)) -> f64 {
        self.range(bounds.0, bounds.1)
    }

    pub(crate) fn bool(&mut self, probability: f64) -> bool {
        self.f64() < probability
    }

    pub(crate) fn usize(&mut self, bound: usize) -> usize {
        if bound == 0 {
            0
        } else {
            (self.next_u64() % bound as u64) as usize
        }
    }

    pub(crate) fn range_usize(&mut self, min_inclusive: usize, max_exclusive: usize) -> usize {
        min_inclusive + self.usize(max_exclusive.saturating_sub(min_inclusive).max(1))
    }
}
