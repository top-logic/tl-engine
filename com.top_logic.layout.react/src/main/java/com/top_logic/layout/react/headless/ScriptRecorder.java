/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.headless;

import java.util.ArrayList;
import java.util.List;

/**
 * Captures a sequence of user interactions on one window as replayable {@link RecordedStep}s.
 *
 * <p>
 * While {@link #isRecording() recording}, the React command servlet feeds each dispatched command
 * (translated to a semantic {@link RecordedStep#address() address}) into {@link
 * #record(RecordedStep)}. The captured list is the successor to a legacy {@code ScriptingRecorder}
 * trace: a script that {@link AgentSession#act(String, String, java.util.Map) replays} through the
 * same command path the browser uses.
 * </p>
 *
 * <p>
 * One recorder lives per window (on its {@code SSEUpdateQueue}). Commands run under the window's
 * request lock, so capture is single-threaded per window; the methods are nonetheless synchronized so
 * that the agent endpoint may start/stop/read concurrently with command dispatch.
 * </p>
 */
public final class ScriptRecorder {

	private boolean _recording;

	private final List<RecordedStep> _steps = new ArrayList<>();

	/**
	 * Begins a fresh recording, discarding any previously captured steps.
	 */
	public synchronized void start() {
		_steps.clear();
		_recording = true;
	}

	/**
	 * Stops recording. The captured {@link #steps()} remain available until the next {@link #start()}.
	 */
	public synchronized void stop() {
		_recording = false;
	}

	/**
	 * Whether interactions are currently being captured.
	 */
	public synchronized boolean isRecording() {
		return _recording;
	}

	/**
	 * Appends a step if {@link #isRecording() recording}; a no-op otherwise.
	 *
	 * @param step
	 *        The step to capture.
	 */
	public synchronized void record(RecordedStep step) {
		if (_recording) {
			_steps.add(step);
		}
	}

	/**
	 * A snapshot copy of the captured steps, in capture order.
	 */
	public synchronized List<RecordedStep> steps() {
		return new ArrayList<>(_steps);
	}
}
