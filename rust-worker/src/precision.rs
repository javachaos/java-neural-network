#![allow(dead_code)]

use std::mem::size_of;

const MATRIX_MAGIC: [u8; 4] = *b"NNM1";
const MATRIX_VERSION: u16 = 1;
const HEADER_LEN: usize = 44;
const Q16_LEVELS: f64 = u16::MAX as f64;

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub(crate) enum PrecisionProfile {
    ExactF64,
    FastF32,
    Q16Storage,
}

impl PrecisionProfile {
    pub(crate) fn encoding(self) -> ScalarEncoding {
        match self {
            Self::ExactF64 => ScalarEncoding::F64,
            Self::FastF32 => ScalarEncoding::F32,
            Self::Q16Storage => ScalarEncoding::Q16Linear,
        }
    }
}

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub(crate) enum ScalarEncoding {
    F64 = 1,
    F32 = 2,
    Q16Linear = 3,
}

impl ScalarEncoding {
    fn from_code(code: u16) -> Result<Self, MatrixCodecError> {
        match code {
            1 => Ok(Self::F64),
            2 => Ok(Self::F32),
            3 => Ok(Self::Q16Linear),
            _ => Err(MatrixCodecError::UnknownEncoding(code)),
        }
    }
}

#[derive(Debug, Eq, PartialEq)]
pub(crate) enum MatrixCodecError {
    DimensionOverflow,
    InvalidMagic,
    LengthMismatch,
    NonFiniteValue,
    Truncated,
    UnknownEncoding(u16),
    UnsupportedVersion(u16),
}

pub(crate) struct DecodedMatrix {
    pub(crate) rows: usize,
    pub(crate) cols: usize,
    pub(crate) values: Vec<f64>,
    pub(crate) mask: Vec<bool>,
    pub(crate) encoding: ScalarEncoding,
}

pub(crate) fn encode_matrix(
    rows: usize,
    cols: usize,
    values: &[f64],
    mask: &[bool],
    profile: PrecisionProfile,
) -> Result<Vec<u8>, MatrixCodecError> {
    let len = matrix_len(rows, cols)?;
    if values.len() != len || mask.len() != len {
        return Err(MatrixCodecError::LengthMismatch);
    }
    if values.iter().any(|value| !value.is_finite()) {
        return Err(MatrixCodecError::NonFiniteValue);
    }

    let encoding = profile.encoding();
    let mask_bits = pack_mask(mask);
    let (minimum, scale, payload) = encode_payload(values, encoding);
    let rows_u32 = u32::try_from(rows).map_err(|_| MatrixCodecError::DimensionOverflow)?;
    let cols_u32 = u32::try_from(cols).map_err(|_| MatrixCodecError::DimensionOverflow)?;
    let mask_len_u32 =
        u32::try_from(mask_bits.len()).map_err(|_| MatrixCodecError::DimensionOverflow)?;
    let payload_len_u32 =
        u32::try_from(payload.len()).map_err(|_| MatrixCodecError::DimensionOverflow)?;

    let mut bytes = Vec::with_capacity(HEADER_LEN + mask_bits.len() + payload.len());
    bytes.extend_from_slice(&MATRIX_MAGIC);
    write_u16(&mut bytes, MATRIX_VERSION);
    write_u16(&mut bytes, encoding as u16);
    write_u32(&mut bytes, rows_u32);
    write_u32(&mut bytes, cols_u32);
    write_u32(&mut bytes, 0);
    write_f64(&mut bytes, minimum);
    write_f64(&mut bytes, scale);
    write_u32(&mut bytes, mask_len_u32);
    write_u32(&mut bytes, payload_len_u32);
    bytes.extend_from_slice(&mask_bits);
    bytes.extend_from_slice(&payload);
    Ok(bytes)
}

pub(crate) fn decode_matrix(bytes: &[u8]) -> Result<DecodedMatrix, MatrixCodecError> {
    if bytes.len() < HEADER_LEN {
        return Err(MatrixCodecError::Truncated);
    }
    if bytes[0..4] != MATRIX_MAGIC {
        return Err(MatrixCodecError::InvalidMagic);
    }

    let version = read_u16(bytes, 4)?;
    if version != MATRIX_VERSION {
        return Err(MatrixCodecError::UnsupportedVersion(version));
    }
    let encoding = ScalarEncoding::from_code(read_u16(bytes, 6)?)?;
    let rows = read_u32(bytes, 8)? as usize;
    let cols = read_u32(bytes, 12)? as usize;
    let minimum = read_f64(bytes, 20)?;
    let scale = read_f64(bytes, 28)?;
    let mask_len = read_u32(bytes, 36)? as usize;
    let payload_len = read_u32(bytes, 40)? as usize;
    let len = matrix_len(rows, cols)?;
    let expected_mask_len = packed_mask_len(len);
    if mask_len != expected_mask_len {
        return Err(MatrixCodecError::LengthMismatch);
    }

    let payload_start = HEADER_LEN
        .checked_add(mask_len)
        .ok_or(MatrixCodecError::DimensionOverflow)?;
    let payload_end = payload_start
        .checked_add(payload_len)
        .ok_or(MatrixCodecError::DimensionOverflow)?;
    if bytes.len() < payload_end {
        return Err(MatrixCodecError::Truncated);
    }

    let mask = unpack_mask(&bytes[HEADER_LEN..payload_start], len);
    let values = decode_payload(
        &bytes[payload_start..payload_end],
        encoding,
        minimum,
        scale,
        len,
    )?;
    Ok(DecodedMatrix {
        rows,
        cols,
        values,
        mask,
        encoding,
    })
}

fn encode_payload(values: &[f64], encoding: ScalarEncoding) -> (f64, f64, Vec<u8>) {
    match encoding {
        ScalarEncoding::F64 => {
            let mut payload = Vec::with_capacity(values.len() * size_of::<f64>());
            for value in values {
                write_f64(&mut payload, *value);
            }
            (0.0, 0.0, payload)
        }
        ScalarEncoding::F32 => {
            let mut payload = Vec::with_capacity(values.len() * size_of::<f32>());
            for value in values {
                payload.extend_from_slice(&(*value as f32).to_le_bytes());
            }
            (0.0, 0.0, payload)
        }
        ScalarEncoding::Q16Linear => encode_q16(values),
    }
}

fn encode_q16(values: &[f64]) -> (f64, f64, Vec<u8>) {
    let minimum = values.iter().copied().fold(f64::INFINITY, f64::min);
    let maximum = values.iter().copied().fold(f64::NEG_INFINITY, f64::max);
    let scale = if values.is_empty() || maximum <= minimum {
        0.0
    } else {
        (maximum - minimum) / Q16_LEVELS
    };

    let mut payload = Vec::with_capacity(values.len() * size_of::<u16>());
    for value in values {
        let quantized = if scale == 0.0 {
            0
        } else {
            ((*value - minimum) / scale).round().clamp(0.0, Q16_LEVELS) as u16
        };
        write_u16(&mut payload, quantized);
    }
    (minimum, scale, payload)
}

fn decode_payload(
    payload: &[u8],
    encoding: ScalarEncoding,
    minimum: f64,
    scale: f64,
    len: usize,
) -> Result<Vec<f64>, MatrixCodecError> {
    match encoding {
        ScalarEncoding::F64 => {
            if payload.len() != len * size_of::<f64>() {
                return Err(MatrixCodecError::LengthMismatch);
            }
            let mut values = Vec::with_capacity(len);
            for chunk in payload.chunks_exact(size_of::<f64>()) {
                values.push(f64::from_le_bytes(
                    chunk.try_into().expect("chunk size is fixed"),
                ));
            }
            Ok(values)
        }
        ScalarEncoding::F32 => {
            if payload.len() != len * size_of::<f32>() {
                return Err(MatrixCodecError::LengthMismatch);
            }
            let mut values = Vec::with_capacity(len);
            for chunk in payload.chunks_exact(size_of::<f32>()) {
                values.push(
                    f32::from_le_bytes(chunk.try_into().expect("chunk size is fixed")) as f64,
                );
            }
            Ok(values)
        }
        ScalarEncoding::Q16Linear => {
            if payload.len() != len * size_of::<u16>() {
                return Err(MatrixCodecError::LengthMismatch);
            }
            let mut values = Vec::with_capacity(len);
            for chunk in payload.chunks_exact(size_of::<u16>()) {
                let quantized = u16::from_le_bytes(chunk.try_into().expect("chunk size is fixed"));
                values.push(minimum + f64::from(quantized) * scale);
            }
            Ok(values)
        }
    }
}

fn pack_mask(mask: &[bool]) -> Vec<u8> {
    let mut bytes = vec![0u8; packed_mask_len(mask.len())];
    for (index, enabled) in mask.iter().enumerate() {
        if *enabled {
            bytes[index / 8] |= 1 << (index % 8);
        }
    }
    bytes
}

fn unpack_mask(bytes: &[u8], len: usize) -> Vec<bool> {
    (0..len)
        .map(|index| bytes[index / 8] & (1 << (index % 8)) != 0)
        .collect()
}

fn matrix_len(rows: usize, cols: usize) -> Result<usize, MatrixCodecError> {
    rows.checked_mul(cols)
        .ok_or(MatrixCodecError::DimensionOverflow)
}

fn packed_mask_len(len: usize) -> usize {
    len.div_ceil(8)
}

fn read_u16(bytes: &[u8], offset: usize) -> Result<u16, MatrixCodecError> {
    let end = offset + size_of::<u16>();
    let slice = bytes.get(offset..end).ok_or(MatrixCodecError::Truncated)?;
    Ok(u16::from_le_bytes(
        slice.try_into().expect("slice size is fixed"),
    ))
}

fn read_u32(bytes: &[u8], offset: usize) -> Result<u32, MatrixCodecError> {
    let end = offset + size_of::<u32>();
    let slice = bytes.get(offset..end).ok_or(MatrixCodecError::Truncated)?;
    Ok(u32::from_le_bytes(
        slice.try_into().expect("slice size is fixed"),
    ))
}

fn read_f64(bytes: &[u8], offset: usize) -> Result<f64, MatrixCodecError> {
    let end = offset + size_of::<f64>();
    let slice = bytes.get(offset..end).ok_or(MatrixCodecError::Truncated)?;
    Ok(f64::from_le_bytes(
        slice.try_into().expect("slice size is fixed"),
    ))
}

fn write_u16(bytes: &mut Vec<u8>, value: u16) {
    bytes.extend_from_slice(&value.to_le_bytes());
}

fn write_u32(bytes: &mut Vec<u8>, value: u32) {
    bytes.extend_from_slice(&value.to_le_bytes());
}

fn write_f64(bytes: &mut Vec<u8>, value: f64) {
    bytes.extend_from_slice(&value.to_le_bytes());
}

#[cfg(test)]
mod tests {
    use super::{MatrixCodecError, PrecisionProfile, ScalarEncoding, decode_matrix, encode_matrix};

    #[test]
    fn q16_linear_round_trip_stays_within_quantization_step() {
        let values = [-12.0, -0.5, 0.0, 1.25, 12.0, 7.5];
        let mask = [true, false, true, true, false, true];

        let bytes = encode_matrix(2, 3, &values, &mask, PrecisionProfile::Q16Storage).unwrap();
        let decoded = decode_matrix(&bytes).unwrap();

        assert_eq!(2, decoded.rows);
        assert_eq!(3, decoded.cols);
        assert_eq!(ScalarEncoding::Q16Linear, decoded.encoding);
        assert_eq!(mask, decoded.mask.as_slice());
        let max_error = values
            .iter()
            .zip(decoded.values.iter())
            .map(|(expected, actual)| (expected - actual).abs())
            .fold(0.0, f64::max);
        assert!(max_error <= 24.0 / 65_535.0);
    }

    #[test]
    fn exact_f64_round_trip_is_lossless() {
        let values = [-1.25, 0.0, 4.5, 9.75];
        let mask = [true, true, false, true];

        let bytes = encode_matrix(2, 2, &values, &mask, PrecisionProfile::ExactF64).unwrap();
        let decoded = decode_matrix(&bytes).unwrap();

        assert_eq!(ScalarEncoding::F64, decoded.encoding);
        assert_eq!(values, decoded.values.as_slice());
        assert_eq!(mask, decoded.mask.as_slice());
    }

    #[test]
    fn rejects_wrong_dimensions() {
        let values = [1.0, 2.0];
        let mask = [true, false];

        assert_eq!(
            Err(MatrixCodecError::LengthMismatch),
            encode_matrix(2, 2, &values, &mask, PrecisionProfile::Q16Storage)
        );
    }
}
