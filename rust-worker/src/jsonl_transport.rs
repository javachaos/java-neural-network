use std::io::{self, BufRead, Write};
use std::sync::{Arc, Mutex};

use crate::worker_service::{WorkerControl, WorkerService, write_ready};

pub(crate) fn run() -> io::Result<()> {
    let stdin = io::stdin();
    let stdout = Arc::new(Mutex::new(io::stdout()));
    let service = WorkerService::new();

    write_ready(&mut SharedWriter::new(stdout.clone()))?;

    for line in stdin.lock().lines() {
        let line = line?;
        if matches!(
            WorkerService::message_type(&line).as_deref(),
            Some("start" | "blockAblation")
        ) {
            let run_service = service.clone();
            let run_stdout = stdout.clone();
            let message_type = WorkerService::message_type(&line).unwrap_or_default();
            std::thread::Builder::new()
                .name(format!("neuro-evolution-jsonl-{message_type}"))
                .spawn(move || {
                    let mut writer = SharedWriter::new(run_stdout);
                    let result = if message_type == "blockAblation" {
                        run_service.handle_block_ablation(&line, &mut writer)
                    } else {
                        run_service.handle_start(&line, &mut writer)
                    };
                    if let Err(error) = result {
                        let _ = crate::protocol::write_event(&mut writer, "error", "error", &error);
                    }
                })
                .map_err(io::Error::other)?;
            continue;
        }
        match service.handle_message(&line, &mut SharedWriter::new(stdout.clone())) {
            Ok(WorkerControl::Continue) => {}
            Ok(WorkerControl::Stop) => break,
            Err(error) => {
                crate::protocol::write_event(
                    &mut SharedWriter::new(stdout.clone()),
                    "error",
                    "error",
                    &error,
                )?;
            }
        }
    }

    Ok(())
}

struct SharedWriter<W> {
    writer: Arc<Mutex<W>>,
}

impl<W> SharedWriter<W> {
    fn new(writer: Arc<Mutex<W>>) -> Self {
        Self { writer }
    }
}

impl<W: Write> Write for SharedWriter<W> {
    fn write(&mut self, bytes: &[u8]) -> io::Result<usize> {
        self.writer
            .lock()
            .map_err(|_| io::Error::other("worker output lock poisoned"))?
            .write(bytes)
    }

    fn flush(&mut self) -> io::Result<()> {
        self.writer
            .lock()
            .map_err(|_| io::Error::other("worker output lock poisoned"))?
            .flush()
    }
}
