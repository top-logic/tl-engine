/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.execution;

import java.util.List;
import java.util.function.Supplier;

import com.top_logic.basic.Logger;
import com.top_logic.tl.ai.core.agent.AgentResult;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.agent.event.CompositeAgentEventListener;
import com.top_logic.tl.ai.core.execution.model.AgentExecution;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.session.ConversationLog;
import com.top_logic.tl.ai.core.session.ConversationLogListener;
import com.top_logic.tl.ai.core.session.Turn;
import com.top_logic.tl.ai.core.session.persist.ConversationLogSerializer;

/**
 * The recording of one agent run: its {@link AgentExecution} row and its transcript, fed by the
 * run's {@link AgentEvent}s.
 *
 * <p>
 * Whatever produces the events - the framework's own agent loop through {@link AgentRunner}, or a
 * process outside of it that reports its work as {@link AgentEvent}s - gets the same record: the
 * row is created on {@link AgentEvent.RunStarted} (status RUNNING), the transcript is checkpointed
 * while the run waits for a person, and the row is finished with stop reason, token usage, cost,
 * error message and the run's transcript. A conversation of such runs therefore reloads, lists and
 * shows its cost like any other.
 * </p>
 *
 * <h2>Lifecycle</h2>
 *
 * <ol>
 * <li>{@link #open(String, AgentSession, AgentExecution, ExecutionHandle, ConversationLog, AgentEventListener)}
 * before the first event. A user turn the caller started in the log beforehand (the user's message)
 * belongs to this run.</li>
 * <li>{@link #onEvent(AgentEvent)} for every event of the run, starting with
 * {@link AgentEvent.RunStarted} and ending with {@link AgentEvent.RunCompleted}. Each event is
 * recorded into the log and forwarded to the caller's listener.</li>
 * <li>Exactly one of {@link #finish(String)} (the run ended, with whatever stop reason its
 * {@link AgentEvent.RunCompleted} named) or {@link #fail(Throwable)} (the producer died before or
 * instead of completing the run).</li>
 * </ol>
 *
 * <p>
 * Without a session the run is not recorded: events still reach the log and the listener, and
 * {@link #finish(String)} / {@link #fail(Throwable)} do nothing. The same holds while the
 * {@link AgentExecutionStore} is not active.
 * </p>
 *
 * <h2>Threading and transactions</h2>
 *
 * <p>
 * The producer calls all methods from its own thread - the agent thread of the loop, or the thread
 * reading an external process's output - one at a time and in the order of the run. It needs no
 * transaction and no particular knowledge base context: every write is carried out by the
 * {@link AgentExecutionStore} on its writer thread in a transaction of its own, so a transaction
 * the caller has open is neither used nor disturbed. Creating the row on
 * {@link AgentEvent.RunStarted} waits for that write; a checkpoint and the final write of the
 * transcript take the log's state on the writer thread, while the producer may go on writing to
 * the log.
 * </p>
 *
 * <h2>Without an LLM provider</h2>
 *
 * <p>
 * The row's model is the one {@link AgentEvent.RunStarted} names, which may be <code>null</code>;
 * the provider column is resolved from the model where the {@link
 * com.top_logic.tl.ai.core.provider.LLMProviderService} knows it, and stays empty otherwise.
 * </p>
 */
public final class RunRecording implements AgentEventListener {

	private final AgentEventListener _listener;

	private final ExecutionRecorder _recorder;

	private final ExecutionHandle _handle;

	private final ConversationLog _log;

	private final int _turnsBefore;

	private RunRecording(AgentEventListener listener, ExecutionRecorder recorder, ExecutionHandle handle,
			ConversationLog log, int turnsBefore) {
		_listener = listener;
		_recorder = recorder;
		_handle = handle;
		_log = log;
		_turnsBefore = turnsBefore;
	}

	/**
	 * Opens the recording of a run that is about to start.
	 *
	 * @param agentName
	 *        The agent name recorded on the execution row, e.g. the name of a configured agent or of
	 *        the external process; must not be <code>null</code>.
	 * @param session
	 *        The session to record the execution under; <code>null</code> disables recording.
	 * @param parentExecution
	 *        The delegating execution for sub-agent runs; <code>null</code> for top-level runs.
	 * @param handle
	 *        The run's execution holder, which receives what identifies the run and the created
	 *        row; <code>null</code> for a fresh one, see {@link #handle()}.
	 * @param log
	 *        The conversation log recording this run; its turns from this run on become the
	 *        execution's transcript. A {@link ConversationLogListener} over it is added unless
	 *        {@code listener} already dispatches to one. <code>null</code> keeps no transcript.
	 * @param listener
	 *        Receives every event of the run after it is recorded; may be <code>null</code>.
	 */
	public static RunRecording open(String agentName, AgentSession session, AgentExecution parentExecution,
			ExecutionHandle handle, ConversationLog log, AgentEventListener listener) {
		ExecutionHandle runHandle = handle != null ? handle : new ExecutionHandle();
		AgentEventListener downstream = withTranscriptRecording(listener, log);

		// Entry points may start the run's turn before the run (chat records the user message
		// first); an open current turn belongs to this run, not to history.
		int turnsBefore = log != null
			? log.turns().size() - (log.currentTurn() != null ? 1 : 0)
			: 0;

		ExecutionRecorder recorder = null;
		if (session != null) {
			recorder = new ExecutionRecorder(session, agentName, parentExecution, runHandle, downstream,
				log == null ? null : () -> transcriptOf(log, turnsBefore));
		} else {
			Logger.info("Agent run of '" + agentName + "' not recorded: no session given.", RunRecording.class);
		}
		return new RunRecording(recorder != null ? recorder : downstream, recorder, runHandle, log, turnsBefore);
	}

	@Override
	public void onEvent(AgentEvent event) {
		if (_listener != null) {
			_listener.onEvent(event);
		}
	}

	@Override
	public boolean dispatchesTo(AgentEventListener target) {
		return this == target || (_listener != null && _listener.dispatchesTo(target));
	}

	/**
	 * Finishes the execution row after the run ended.
	 *
	 * <p>
	 * Status, stop reason, token usage and cost come from the run's {@link AgentEvent.RunCompleted},
	 * the error message from the last {@link AgentEvent.ProviderError}. The transcript holds the
	 * log's turns from this run on, as they stand when the row is written: a part changed meanwhile
	 * (a background subagent of the run ending) is written in its changed form, while a run the same
	 * log starts afterwards is not part of it.
	 * </p>
	 *
	 * @param response
	 *        The run's final answer text; may be <code>null</code>.
	 */
	public void finish(String response) {
		if (_recorder != null) {
			_recorder.completeWithLatestTranscript(finalTranscript(), response, null);
		}
	}

	/**
	 * Finishes the execution row of a run whose producer failed - the loop threw, or an external
	 * process ended without completing the run.
	 *
	 * <p>
	 * Recorded as an error with the failure's message, unless a {@link AgentEvent.RunCompleted} was
	 * seen, whose stop reason then decides the status.
	 * </p>
	 *
	 * @param failure
	 *        What went wrong; must not be <code>null</code>.
	 */
	public void fail(Throwable failure) {
		if (_recorder != null) {
			_recorder.complete(transcriptOf(_log, _turnsBefore), failure);
		}
	}

	/**
	 * The run's execution holder: the one given to
	 * {@link #open(String, AgentSession, AgentExecution, ExecutionHandle, ConversationLog, AgentEventListener)},
	 * or the one created there.
	 */
	public ExecutionHandle handle() {
		return _handle;
	}

	/**
	 * The recorded execution row, or <code>null</code> before {@link AgentEvent.RunStarted} and when
	 * the run is not recorded.
	 */
	public AgentExecution execution() {
		return _recorder != null ? _recorder.execution() : null;
	}

	/**
	 * The terminal {@link AgentResult} as the run's {@link AgentEvent.RunCompleted} stated it, with
	 * the last provider error as its failure detail; <code>null</code> before that event and when the
	 * run is not recorded.
	 */
	public AgentResult result() {
		return _recorder != null ? _recorder.result() : null;
	}

	/**
	 * Returns a listener that additionally records the run's events into {@code log} (the transcript
	 * source), unless {@code listener} already dispatches to a recorder for that log.
	 *
	 * <p>
	 * The recorder used is the {@link ConversationLogListener} bound to the current thread when it
	 * targets {@code log} (so tool-driven parts and loop events share one listener); otherwise a
	 * fresh {@link ConversationLogListener} over {@code log} is created. When {@code log} is
	 * <code>null</code> no transcript is kept and {@code listener} is returned unchanged.
	 * </p>
	 */
	private static AgentEventListener withTranscriptRecording(AgentEventListener listener,
			ConversationLog log) {
		if (log == null) {
			return listener;
		}
		ConversationLogListener bound = ConversationLogListener.current();
		ConversationLogListener transcriptRecorder =
			(bound != null && bound.log() == log) ? bound : new ConversationLogListener(log);
		if (listener != null && listener.dispatchesTo(transcriptRecorder)) {
			return listener;
		}
		return listener == null
			? transcriptRecorder
			: new CompositeAgentEventListener(transcriptRecorder, listener);
	}

	/**
	 * The run's final transcript, serialized when the store writes it rather than now.
	 *
	 * <p>
	 * The run's turns are fixed now, so that a run the same log starts next does not end up in this
	 * one's transcript.
	 * </p>
	 *
	 * @see ExecutionRecorder#completeWithLatestTranscript(Supplier, String, Throwable)
	 */
	private Supplier<String> finalTranscript() {
		if (_log == null) {
			return () -> null;
		}
		List<Turn> runTurns = List.copyOf(runTurns(_log, _turnsBefore));
		return () -> serialize(runTurns);
	}

	private static List<Turn> runTurns(ConversationLog log, int turnsBefore) {
		List<Turn> turns = log.turns();
		return turns.subList(Math.min(turnsBefore, turns.size()), turns.size());
	}

	/**
	 * Serializes the turns the run appended to the log, open turn included, or <code>null</code> if
	 * there is no log or serialization fails.
	 */
	private static String transcriptOf(ConversationLog log, int turnsBefore) {
		if (log == null) {
			return null;
		}
		return serialize(runTurns(log, turnsBefore));
	}

	private static String serialize(List<Turn> turns) {
		try {
			return ConversationLogSerializer.toJson(turns);
		} catch (RuntimeException ex) {
			Logger.warn("Failed to serialize run transcript: " + ex.getMessage(), ex, RunRecording.class);
			return null;
		}
	}
}
