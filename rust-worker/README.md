# Neuro Evolution Worker

## Default worker

The default build includes both `grpc` and `cuda`.

```bash
cargo run --release --manifest-path rust-worker/Cargo.toml
```

That starts the local JSONL transport. To start the network gRPC transport with
the default CUDA-capable build:

```bash
cargo run --release --manifest-path rust-worker/Cargo.toml -- --grpc 0.0.0.0:50051
```

The gRPC transport wraps the existing JSON worker protocol in a small protobuf
envelope so the Java GUI and Rust worker keep a single evolution message shape.

## Explicit feature builds

```bash
cargo run --release --manifest-path rust-worker/Cargo.toml --no-default-features
cargo run --release --manifest-path rust-worker/Cargo.toml --no-default-features --features grpc -- --grpc 0.0.0.0:50051
cargo run --release --manifest-path rust-worker/Cargo.toml --features grpc,cuda -- --grpc 0.0.0.0:50051
```

The Java GUI prefers `target/release/neuro-evolution-worker` by default. Use
`-DneuroEvolution.rustWorkerProfile=debug` only when you intentionally want a
debug worker for troubleshooting.

The `cuda` feature loads the NVIDIA driver directly, creates a CUDA context,
loads an embedded PTX module, and launches a real GPU kernel each generation to
compute population genome complexity. The learner training and prediction score
remain on the exact CPU scorer for now, with the CUDA runtime layer isolated so
larger kernels can move behind the same backend interface.

## Binary matrix layout direction

The worker stores trainable matrices as flat row-major buffers:

```text
index = row * columns + column
```

The matrix codec uses this little-endian per-matrix payload:

```text
magic[4] = NNM1
version u16
scalar_encoding u16   # 1=f64, 2=f32, 3=q16-linear
rows u32
columns u32
flags u32
minimum f64           # used by q16-linear
scale f64             # used by q16-linear
mask_length u32
payload_length u32
packed mask bits
row-major scalar payload
```

The default compact storage target is `q16-linear`, which stores each matrix
with its own min/scale pair and a `u16` payload. Masks are packed bitwise.
Keep encryption as an outer envelope around this payload unless we specifically
need homomorphic or secure-computation behavior. Fully homomorphic encryption
preserves selected mathematical operations, but it is far too expensive for the
current inner loop.
