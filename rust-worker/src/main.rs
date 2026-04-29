mod backend;
mod block_ablation;
mod config;
mod constants;
mod cuda_runtime;
mod evolution;
mod genome;
mod grpc_transport;
mod jsonl_transport;
mod learner;
mod math;
mod operators;
mod precision;
mod problem;
mod protocol;
mod random;
mod scoring;
mod weights;
mod worker_service;

use std::io;

fn main() -> io::Result<()> {
    let mut args = std::env::args().skip(1);
    match args.next().as_deref() {
        Some("--grpc") => {
            let address = args.next().unwrap_or_else(|| "0.0.0.0:50051".to_string());
            grpc_transport::serve_blocking(&address).map_err(io::Error::other)
        }
        Some(other) => Err(io::Error::new(
            io::ErrorKind::InvalidInput,
            format!(
                "Unsupported worker argument '{other}'. Use --grpc <host:port> or no arguments."
            ),
        )),
        None => jsonl_transport::run(),
    }
}
