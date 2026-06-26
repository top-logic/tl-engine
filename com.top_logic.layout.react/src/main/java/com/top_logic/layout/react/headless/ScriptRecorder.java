/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.headless;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.top_logic.basic.Logger;

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
 *
 * <p>
 * The recorder is observable: a {@link #addListener(Listener) registered} {@link Listener} is notified
 * after each {@link #start()}, {@link #stop()} and {@link #record(RecordedStep)}, so a view in another
 * window (a recorder side-window) can reflect the captured steps live. Listeners are notified outside
 * the recorder's monitor.
 * </p>
 */
public final class ScriptRecorder {

	/**
	 * Notified after the recorder's state changes (start, stop or a captured step).
	 */
	@FunctionalInterface
	public interface Listener {
		/**
		 * Called after a {@link ScriptRecorder} state change.
		 *
		 * @param recorder
		 *        The recorder that changed.
		 */
		void recorderChanged(ScriptRecorder recorder);
	}

	private boolean _recording;

	private final List<RecordedStep> _steps = new ArrayList<>();

	private final List<Listener> _listeners = new CopyOnWriteArrayList<>();

	/**
	 * Begins a fresh recording, discarding any previously captured steps.
	 */
	public void start() {
		synchronized (this) {
			_steps.clear();
			_recording = true;
		}
		fireChanged();
	}

	/**
	 * Stops recording. The captured {@link #steps()} remain available until the next {@link #start()}.
	 */
	public void stop() {
		synchronized (this) {
			_recording = false;
		}
		fireChanged();
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
	public void record(RecordedStep step) {
		record(step, false);
	}

	/**
	 * Captures a step if {@link #isRecording() recording}; a no-op otherwise.
	 *
	 * @param step
	 *        The step to capture.
	 * @param coalescing
	 *        When {@code true} and the previous step targets the same
	 *        {@link RecordedStep#address() address} with the same {@link RecordedStep#command()
	 *        command}, the previous step is <em>superseded</em> by this one rather than a new step
	 *        appended — so an uninterrupted run of edits (e.g. per-keystroke field input) collapses to
	 *        a single step holding the latest arguments. The decision is the caller's; the recorder
	 *        merges purely by address and command equality and never inspects the target control.
	 */
	public void record(RecordedStep step, boolean coalescing) {
		boolean captured;
		synchronized (this) {
			captured = _recording;
			if (captured) {
				if (coalescing && supersedesLast(step)) {
					_steps.set(_steps.size() - 1, step);
				} else {
					_steps.add(step);
				}
			}
		}
		if (captured) {
			fireChanged();
		}
	}

	/**
	 * Whether the last captured step targets the same addressed control with the same command as the
	 * given step (so a coalescing capture should replace it). A {@code null} address never matches:
	 * unaddressable steps stay distinct.
	 */
	private boolean supersedesLast(RecordedStep step) {
		if (_steps.isEmpty()) {
			return false;
		}
		RecordedStep last = _steps.get(_steps.size() - 1);
		return last.address() != null
			&& last.address().equals(step.address())
			&& last.command().equals(step.command());
	}

	/**
	 * A snapshot copy of the captured steps, in capture order.
	 */
	public synchronized List<RecordedStep> steps() {
		return new ArrayList<>(_steps);
	}

	/**
	 * Registers a {@link Listener} notified after each state change, until {@link
	 * #removeListener(Listener) removed}.
	 *
	 * @param listener
	 *        The listener to add.
	 */
	public void addListener(Listener listener) {
		_listeners.add(listener);
	}

	/**
	 * Unregisters a {@link #addListener(Listener) listener}.
	 *
	 * @param listener
	 *        The listener to remove.
	 */
	public void removeListener(Listener listener) {
		_listeners.remove(listener);
	}

	private void fireChanged() {
		for (Listener listener : _listeners) {
			try {
				listener.recorderChanged(this);
			} catch (RuntimeException ex) {
				Logger.error("Recorder listener failed.", ex, ScriptRecorder.class);
			}
		}
	}
}
