use std::io::{self, Write};
use std::sync::{Arc, Condvar, Mutex};

use crate::backend::backend_from_json;
use crate::block_ablation::run_block_ablation;
use crate::evolution::run_evolution;
use crate::protocol::{number_field, string_field, write_event, write_event_context};

pub(crate) fn write_ready(writer: &mut dyn Write) -> io::Result<()> {
    write_event(
        writer,
        "ready",
        "idle",
        "Rust neuro-evolution worker is ready.",
    )
}

#[derive(Clone)]
pub(crate) struct WorkerService {
    run_control: RunControl,
}

impl WorkerService {
    pub(crate) fn new() -> Self {
        Self {
            run_control: RunControl::new(),
        }
    }

    pub(crate) fn handle_message(
        &self,
        message: &str,
        writer: &mut dyn Write,
    ) -> Result<WorkerControl, String> {
        let trimmed = message.trim();
        if trimmed.is_empty() {
            Ok(WorkerControl::Continue)
        } else {
            self.handle_trimmed_message(trimmed, writer)
        }
    }

    pub(crate) fn handle_start(&self, json: &str, writer: &mut dyn Write) -> Result<(), String> {
        self.run_control.reset_for_start();
        handle_start(json, writer, &self.run_control)
    }

    pub(crate) fn handle_block_ablation(
        &self,
        json: &str,
        writer: &mut dyn Write,
    ) -> Result<(), String> {
        self.run_control.reset_for_start();
        run_block_ablation(json, writer, &self.run_control)
    }

    pub(crate) fn pause(&self, writer: &mut dyn Write) -> Result<(), String> {
        self.run_control.pause();
        write_event(writer, "paused", "paused", "Paused run request.")
            .map_err(|error| error.to_string())
    }

    pub(crate) fn resume(&self, writer: &mut dyn Write) -> Result<(), String> {
        self.run_control.resume();
        write_event(writer, "resumed", "running", "Resumed run request.")
            .map_err(|error| error.to_string())
    }

    pub(crate) fn stop(&self, writer: &mut dyn Write) -> Result<(), String> {
        self.run_control.stop();
        write_event(writer, "stopped", "stopped", "Stopped run request.")
            .map_err(|error| error.to_string())
    }

    pub(crate) fn message_type(message: &str) -> Option<String> {
        string_field(message.trim(), "type")
    }

    fn handle_trimmed_message(
        &self,
        trimmed: &str,
        writer: &mut dyn Write,
    ) -> Result<WorkerControl, String> {
        match Self::message_type(trimmed).as_deref() {
            Some("start") => {
                self.handle_start(trimmed, writer)?;
                Ok(WorkerControl::Continue)
            }
            Some("blockAblation") => {
                self.handle_block_ablation(trimmed, writer)?;
                Ok(WorkerControl::Continue)
            }
            Some("pause") => {
                self.pause(writer)?;
                Ok(WorkerControl::Continue)
            }
            Some("resume") => {
                self.resume(writer)?;
                Ok(WorkerControl::Continue)
            }
            Some("ping") => {
                write_event(writer, "pong", "idle", "pong").map_err(|error| error.to_string())?;
                Ok(WorkerControl::Continue)
            }
            Some("status") => {
                write_event(writer, "status", "idle", "Worker process is alive.")
                    .map_err(|error| error.to_string())?;
                Ok(WorkerControl::Continue)
            }
            Some("stop") => {
                self.stop(writer)?;
                Ok(WorkerControl::Stop)
            }
            Some(other) => {
                write_event(
                    writer,
                    "error",
                    "error",
                    &format!("Unsupported message type '{other}'."),
                )
                .map_err(|error| error.to_string())?;
                Ok(WorkerControl::Continue)
            }
            None => {
                write_event(
                    writer,
                    "error",
                    "error",
                    "Message did not include a JSON string field named 'type'.",
                )
                .map_err(|error| error.to_string())?;
                Ok(WorkerControl::Continue)
            }
        }
    }
}

#[derive(Clone)]
pub(crate) struct RunControl {
    state: Arc<(Mutex<RunControlState>, Condvar)>,
}

impl RunControl {
    fn new() -> Self {
        Self {
            state: Arc::new((Mutex::new(RunControlState::default()), Condvar::new())),
        }
    }

    fn reset_for_start(&self) {
        let (lock, condition) = &*self.state;
        let mut state = lock.lock().expect("run control mutex poisoned");
        *state = RunControlState::default();
        condition.notify_all();
    }

    fn pause(&self) {
        let (lock, _) = &*self.state;
        let mut state = lock.lock().expect("run control mutex poisoned");
        state.paused = true;
    }

    fn resume(&self) {
        let (lock, condition) = &*self.state;
        let mut state = lock.lock().expect("run control mutex poisoned");
        state.paused = false;
        condition.notify_all();
    }

    fn stop(&self) {
        let (lock, condition) = &*self.state;
        let mut state = lock.lock().expect("run control mutex poisoned");
        state.stopped = true;
        state.paused = false;
        condition.notify_all();
    }

    pub(crate) fn wait_if_paused(&self) -> RunControlDecision {
        let (lock, condition) = &*self.state;
        let mut state = lock.lock().expect("run control mutex poisoned");
        while state.paused && !state.stopped {
            state = condition
                .wait(state)
                .expect("run control mutex poisoned while paused");
        }
        if state.stopped {
            RunControlDecision::Stop
        } else {
            RunControlDecision::Continue
        }
    }
}

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub(crate) enum RunControlDecision {
    Continue,
    Stop,
}

#[derive(Default)]
struct RunControlState {
    paused: bool,
    stopped: bool,
}

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub(crate) enum WorkerControl {
    Continue,
    Stop,
}

fn handle_start(json: &str, writer: &mut dyn Write, control: &RunControl) -> Result<(), String> {
    let problem_key = string_field(json, "key").unwrap_or_else(|| "unknown-problem".to_string());
    let generations = number_field(json, "generations").unwrap_or(0);
    let parallelism = number_field(json, "parallelism").unwrap_or(1).max(1);
    let cluster_id = string_field(json, "clusterId").unwrap_or_else(|| "local".to_string());
    let storage_mode = string_field(json, "storageMode").unwrap_or_else(|| "LOCAL".to_string());
    let backend = backend_from_json(json)?;
    write_event_context(
        writer,
        "accepted",
        "running",
        &format!(
            "Accepted problem '{problem_key}' for {generations} generations in cluster '{cluster_id}' using {storage_mode} storage. Rust compute kernel is active on the {} backend with {parallelism} Rayon worker threads.",
            backend.name()
        ),
        Some(&cluster_id),
        Some(&storage_mode),
    )
    .map_err(|error| error.to_string())?;
    if let Err(error) = run_evolution(
        json,
        writer,
        &cluster_id,
        &storage_mode,
        backend.as_ref(),
        control,
    ) {
        write_event_context(
            writer,
            "error",
            "error",
            &error,
            Some(&cluster_id),
            Some(&storage_mode),
        )
        .map_err(|write_error| write_error.to_string())?;
    }
    Ok(())
}

#[cfg(test)]
mod tests {
    use std::sync::mpsc;
    use std::time::Duration;

    use super::{RunControl, RunControlDecision};

    #[test]
    fn pause_blocks_until_resume() {
        let control = RunControl::new();
        control.pause();
        let paused_control = control.clone();
        let (sender, receiver) = mpsc::channel();

        std::thread::spawn(move || {
            sender
                .send(paused_control.wait_if_paused())
                .expect("test receiver should be open");
        });

        assert!(receiver.recv_timeout(Duration::from_millis(30)).is_err());
        control.resume();
        assert_eq!(
            RunControlDecision::Continue,
            receiver
                .recv_timeout(Duration::from_secs(1))
                .expect("resume should release paused run")
        );
    }

    #[test]
    fn stop_releases_paused_run() {
        let control = RunControl::new();
        control.pause();
        let paused_control = control.clone();
        let (sender, receiver) = mpsc::channel();

        std::thread::spawn(move || {
            sender
                .send(paused_control.wait_if_paused())
                .expect("test receiver should be open");
        });

        assert!(receiver.recv_timeout(Duration::from_millis(30)).is_err());
        control.stop();
        assert_eq!(
            RunControlDecision::Stop,
            receiver
                .recv_timeout(Duration::from_secs(1))
                .expect("stop should release paused run")
        );
    }
}
