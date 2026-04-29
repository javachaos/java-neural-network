#[cfg(feature = "grpc")]
mod enabled {
    use std::convert::Infallible;
    use std::io::{self, Write};
    use std::net::SocketAddr;
    use std::sync::Arc;

    use tokio::sync::mpsc;
    use tokio_stream::wrappers::ReceiverStream;
    use tonic::body::BoxBody;
    use tonic::codegen::{Body, BoxFuture, StdError, http};
    use tonic::server::{Grpc, ServerStreamingService, UnaryService};
    use tonic::transport::Server;
    use tonic::{Request, Response, Status, async_trait};

    use crate::worker_service::{WorkerService, write_ready};

    #[derive(Clone, PartialEq, ::prost::Message)]
    pub(crate) struct RunRequest {
        #[prost(string, tag = "1")]
        pub(crate) start_json: String,
    }

    #[derive(Clone, PartialEq, ::prost::Message)]
    pub(crate) struct ControlRequest {
        #[prost(string, tag = "1")]
        pub(crate) control_json: String,
    }

    #[derive(Clone, PartialEq, ::prost::Message)]
    pub(crate) struct WorkerEvent {
        #[prost(string, tag = "1")]
        pub(crate) event_json: String,
    }

    pub(crate) fn serve_blocking(address: &str) -> Result<(), String> {
        let address = address
            .parse::<SocketAddr>()
            .map_err(|error| format!("Invalid gRPC listen address '{address}': {error}"))?;
        let runtime = tokio::runtime::Runtime::new()
            .map_err(|error| format!("Could not create Tokio runtime: {error}"))?;
        runtime.block_on(async move {
            Server::builder()
                .add_service(NeuroEvolutionWorkerServiceServer::new(GrpcWorkerService {
                    service: Arc::new(WorkerService::new()),
                }))
                .serve(address)
                .await
                .map_err(|error| format!("gRPC worker failed: {error}"))
        })
    }

    struct GrpcWorkerService {
        service: Arc<WorkerService>,
    }

    #[async_trait]
    impl NeuroEvolutionWorkerService for GrpcWorkerService {
        type RunStream = ReceiverStream<Result<WorkerEvent, Status>>;

        async fn run(
            &self,
            request: Request<RunRequest>,
        ) -> Result<Response<Self::RunStream>, Status> {
            let start_json = request.into_inner().start_json;
            let (sender, receiver) = mpsc::channel(128);
            let mut writer = StreamingEventWriter::new(sender);
            let service = self.service.clone();
            std::thread::Builder::new()
                .name("neuro-evolution-grpc-run".to_string())
                .spawn(move || {
                    if let Err(error) = service.handle_start(&start_json, &mut writer) {
                        let _ = writer.emit_error(&error);
                    }
                })
                .map_err(|error| {
                    Status::internal(format!("Could not spawn evolution run: {error}"))
                })?;
            Ok(Response::new(ReceiverStream::new(receiver)))
        }

        async fn control(
            &self,
            request: Request<ControlRequest>,
        ) -> Result<Response<WorkerEvent>, Status> {
            let mut writer = CapturingEventWriter::default();
            self.service
                .handle_message(&request.into_inner().control_json, &mut writer)
                .map_err(Status::internal)?;
            Ok(Response::new(WorkerEvent {
                event_json: writer
                    .last_event()
                    .unwrap_or_else(|| "{\"type\":\"ack\",\"status\":\"idle\"}".to_string()),
            }))
        }
    }

    struct StreamingEventWriter {
        sender: mpsc::Sender<Result<WorkerEvent, Status>>,
        buffer: Vec<u8>,
    }

    impl StreamingEventWriter {
        fn new(sender: mpsc::Sender<Result<WorkerEvent, Status>>) -> Self {
            Self {
                sender,
                buffer: Vec::new(),
            }
        }

        fn emit_error(&mut self, message: &str) -> io::Result<()> {
            crate::protocol::write_event(self, "error", "error", message)
        }

        fn flush_complete_lines(&mut self) -> io::Result<()> {
            while let Some(index) = self.buffer.iter().position(|byte| *byte == b'\n') {
                let line = self.buffer.drain(..=index).collect::<Vec<_>>();
                let event_json = String::from_utf8_lossy(&line[..line.len().saturating_sub(1)])
                    .trim_end_matches('\r')
                    .to_string();
                if !event_json.is_empty() {
                    self.sender
                        .blocking_send(Ok(WorkerEvent { event_json }))
                        .map_err(|_| {
                            io::Error::new(io::ErrorKind::BrokenPipe, "gRPC stream closed")
                        })?;
                }
            }
            Ok(())
        }
    }

    impl Write for StreamingEventWriter {
        fn write(&mut self, bytes: &[u8]) -> io::Result<usize> {
            self.buffer.extend_from_slice(bytes);
            self.flush_complete_lines()?;
            Ok(bytes.len())
        }

        fn flush(&mut self) -> io::Result<()> {
            if !self.buffer.is_empty() {
                let event_json = String::from_utf8_lossy(&self.buffer)
                    .trim_end_matches('\r')
                    .to_string();
                self.buffer.clear();
                if !event_json.trim().is_empty() {
                    self.sender
                        .blocking_send(Ok(WorkerEvent { event_json }))
                        .map_err(|_| {
                            io::Error::new(io::ErrorKind::BrokenPipe, "gRPC stream closed")
                        })?;
                }
            }
            Ok(())
        }
    }

    #[derive(Default)]
    struct CapturingEventWriter {
        buffer: Vec<u8>,
        events: Vec<String>,
    }

    impl CapturingEventWriter {
        fn last_event(&self) -> Option<String> {
            self.events.last().cloned()
        }

        fn capture_complete_lines(&mut self) {
            while let Some(index) = self.buffer.iter().position(|byte| *byte == b'\n') {
                let line = self.buffer.drain(..=index).collect::<Vec<_>>();
                let event_json = String::from_utf8_lossy(&line[..line.len().saturating_sub(1)])
                    .trim_end_matches('\r')
                    .to_string();
                if !event_json.is_empty() {
                    self.events.push(event_json);
                }
            }
        }
    }

    impl Write for CapturingEventWriter {
        fn write(&mut self, bytes: &[u8]) -> io::Result<usize> {
            self.buffer.extend_from_slice(bytes);
            self.capture_complete_lines();
            Ok(bytes.len())
        }

        fn flush(&mut self) -> io::Result<()> {
            if !self.buffer.is_empty() {
                let event_json = String::from_utf8_lossy(&self.buffer)
                    .trim_end_matches('\r')
                    .to_string();
                self.buffer.clear();
                if !event_json.trim().is_empty() {
                    self.events.push(event_json);
                }
            }
            Ok(())
        }
    }

    #[async_trait]
    pub(crate) trait NeuroEvolutionWorkerService: Send + Sync + 'static {
        type RunStream: tokio_stream::Stream<Item = Result<WorkerEvent, Status>> + Send + 'static;

        async fn run(
            &self,
            request: Request<RunRequest>,
        ) -> Result<Response<Self::RunStream>, Status>;

        async fn control(
            &self,
            request: Request<ControlRequest>,
        ) -> Result<Response<WorkerEvent>, Status>;
    }

    #[derive(Debug)]
    pub(crate) struct NeuroEvolutionWorkerServiceServer<T: NeuroEvolutionWorkerService> {
        inner: Arc<T>,
    }

    impl<T: NeuroEvolutionWorkerService> NeuroEvolutionWorkerServiceServer<T> {
        pub(crate) fn new(inner: T) -> Self {
            Self {
                inner: Arc::new(inner),
            }
        }
    }

    impl<T: NeuroEvolutionWorkerService> Clone for NeuroEvolutionWorkerServiceServer<T> {
        fn clone(&self) -> Self {
            Self {
                inner: self.inner.clone(),
            }
        }
    }

    impl<T, B> tonic::codegen::Service<http::Request<B>> for NeuroEvolutionWorkerServiceServer<T>
    where
        T: NeuroEvolutionWorkerService,
        B: Body + Send + 'static,
        B::Error: Into<StdError> + Send + 'static,
    {
        type Response = http::Response<BoxBody>;
        type Error = Infallible;
        type Future = BoxFuture<Self::Response, Self::Error>;

        fn poll_ready(
            &mut self,
            _cx: &mut std::task::Context<'_>,
        ) -> std::task::Poll<Result<(), Self::Error>> {
            std::task::Poll::Ready(Ok(()))
        }

        fn call(&mut self, request: http::Request<B>) -> Self::Future {
            match request.uri().path() {
                "/neuroevolution.worker.v1.NeuroEvolutionWorkerService/Run" => {
                    let inner = self.inner.clone();
                    let fut = async move {
                        let method = RunService(inner);
                        let codec = tonic::codec::ProstCodec::default();
                        let mut grpc = Grpc::new(codec);
                        Ok(grpc.server_streaming(method, request).await)
                    };
                    Box::pin(fut)
                }
                "/neuroevolution.worker.v1.NeuroEvolutionWorkerService/Control" => {
                    let inner = self.inner.clone();
                    let fut = async move {
                        let method = ControlService(inner);
                        let codec = tonic::codec::ProstCodec::default();
                        let mut grpc = Grpc::new(codec);
                        Ok(grpc.unary(method, request).await)
                    };
                    Box::pin(fut)
                }
                _ => Box::pin(async move {
                    Ok(http::Response::builder()
                        .status(200)
                        .header("grpc-status", "12")
                        .header("content-type", "application/grpc")
                        .body(tonic::body::empty_body())
                        .expect("static response is valid"))
                }),
            }
        }
    }

    impl<T: NeuroEvolutionWorkerService> tonic::server::NamedService
        for NeuroEvolutionWorkerServiceServer<T>
    {
        const NAME: &'static str = "neuroevolution.worker.v1.NeuroEvolutionWorkerService";
    }

    struct RunService<T: NeuroEvolutionWorkerService>(Arc<T>);

    impl<T: NeuroEvolutionWorkerService> ServerStreamingService<RunRequest> for RunService<T> {
        type Response = WorkerEvent;
        type ResponseStream = T::RunStream;
        type Future = BoxFuture<Response<Self::ResponseStream>, Status>;

        fn call(&mut self, request: Request<RunRequest>) -> Self::Future {
            let inner = self.0.clone();
            Box::pin(async move { inner.run(request).await })
        }
    }

    struct ControlService<T: NeuroEvolutionWorkerService>(Arc<T>);

    impl<T: NeuroEvolutionWorkerService> UnaryService<ControlRequest> for ControlService<T> {
        type Response = WorkerEvent;
        type Future = BoxFuture<Response<Self::Response>, Status>;

        fn call(&mut self, request: Request<ControlRequest>) -> Self::Future {
            let inner = self.0.clone();
            Box::pin(async move { inner.control(request).await })
        }
    }

    #[allow(dead_code)]
    fn _assert_ready_writer() {
        let mut writer = CapturingEventWriter::default();
        let _ = write_ready(&mut writer);
    }
}

#[cfg(feature = "grpc")]
pub(crate) use enabled::serve_blocking;

#[cfg(not(feature = "grpc"))]
pub(crate) fn serve_blocking(_address: &str) -> Result<(), String> {
    Err(
        "gRPC transport was requested, but this worker was not built with --features grpc."
            .to_string(),
    )
}
