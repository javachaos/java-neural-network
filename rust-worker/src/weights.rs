use crate::precision::{MatrixCodecError, PrecisionProfile, decode_matrix, encode_matrix};

pub(crate) struct FlatWeightMatrix {
    rows: usize,
    cols: usize,
    weights: Vec<f64>,
    previous_deltas: Vec<f64>,
    mask: Vec<bool>,
}

impl FlatWeightMatrix {
    pub(crate) fn new(rows: usize, cols: usize) -> Self {
        let len = rows * cols;
        Self {
            rows,
            cols,
            weights: vec![0.0; len],
            previous_deltas: vec![0.0; len],
            mask: vec![false; len],
        }
    }

    #[inline]
    pub(crate) fn rows(&self) -> usize {
        self.rows
    }

    #[inline]
    pub(crate) fn weight(&self, row: usize, col: usize) -> f64 {
        self.weights[self.index(row, col)]
    }

    #[inline]
    pub(crate) fn set_weight(&mut self, row: usize, col: usize, value: f64) {
        let index = self.index(row, col);
        self.weights[index] = value;
    }

    #[inline]
    pub(crate) fn enabled(&self, row: usize, col: usize) -> bool {
        self.mask[self.index(row, col)]
    }

    #[inline]
    pub(crate) fn set_enabled(&mut self, row: usize, col: usize, enabled: bool) {
        let index = self.index(row, col);
        self.mask[index] = enabled;
    }

    #[inline]
    pub(crate) fn row_weights(&self, row: usize) -> &[f64] {
        let (start, end) = self.row_bounds(row);
        &self.weights[start..end]
    }

    #[inline]
    pub(crate) fn row_mask(&self, row: usize) -> &[bool] {
        let (start, end) = self.row_bounds(row);
        &self.mask[start..end]
    }

    #[inline]
    pub(crate) fn row_parts_mut(&mut self, row: usize) -> (&mut [f64], &mut [f64], &[bool]) {
        let (start, end) = self.row_bounds(row);
        (
            &mut self.weights[start..end],
            &mut self.previous_deltas[start..end],
            &self.mask[start..end],
        )
    }

    pub(crate) fn weights_finite(&self) -> bool {
        self.weights.iter().all(|weight| weight.is_finite())
    }

    #[allow(dead_code)]
    pub(crate) fn encode_binary(
        &self,
        profile: PrecisionProfile,
    ) -> Result<Vec<u8>, MatrixCodecError> {
        encode_matrix(self.rows, self.cols, &self.weights, &self.mask, profile)
    }

    #[allow(dead_code)]
    pub(crate) fn decode_binary(bytes: &[u8]) -> Result<Self, MatrixCodecError> {
        let decoded = decode_matrix(bytes)?;
        let mut matrix = Self::new(decoded.rows, decoded.cols);
        matrix.weights = decoded.values;
        matrix.mask = decoded.mask;
        Ok(matrix)
    }

    #[inline]
    fn index(&self, row: usize, col: usize) -> usize {
        debug_assert!(row < self.rows);
        debug_assert!(col < self.cols);
        row * self.cols + col
    }

    #[inline]
    fn row_bounds(&self, row: usize) -> (usize, usize) {
        debug_assert!(row < self.rows);
        let start = row * self.cols;
        (start, start + self.cols)
    }
}

#[cfg(test)]
mod tests {
    use crate::precision::PrecisionProfile;

    use super::FlatWeightMatrix;

    #[test]
    fn stores_weights_in_row_major_order() {
        let mut matrix = FlatWeightMatrix::new(2, 3);
        matrix.set_weight(1, 2, 7.0);
        matrix.set_enabled(1, 2, true);

        assert_eq!(matrix.row_weights(1), &[0.0, 0.0, 7.0]);
        assert_eq!(matrix.row_mask(1), &[false, false, true]);
        assert_eq!(matrix.weight(1, 2), 7.0);
        assert!(matrix.enabled(1, 2));
    }

    #[test]
    fn q16_binary_round_trips_with_small_error() {
        let mut matrix = FlatWeightMatrix::new(2, 3);
        matrix.set_weight(0, 0, -3.0);
        matrix.set_weight(0, 1, -0.25);
        matrix.set_weight(0, 2, 0.5);
        matrix.set_weight(1, 0, 1.25);
        matrix.set_weight(1, 1, 2.75);
        matrix.set_weight(1, 2, 3.0);
        matrix.set_enabled(0, 0, true);
        matrix.set_enabled(0, 2, true);
        matrix.set_enabled(1, 1, true);

        let bytes = matrix.encode_binary(PrecisionProfile::Q16Storage).unwrap();
        let decoded = FlatWeightMatrix::decode_binary(&bytes).unwrap();

        assert!(decoded.enabled(0, 0));
        assert!(!decoded.enabled(0, 1));
        assert!(decoded.enabled(0, 2));
        assert!(!decoded.enabled(1, 0));
        assert!(decoded.enabled(1, 1));
        assert!(!decoded.enabled(1, 2));
        for row in 0..2 {
            for col in 0..3 {
                assert!(
                    (matrix.weight(row, col) - decoded.weight(row, col)).abs() <= 6.0 / 65_535.0
                );
            }
        }
    }
}
