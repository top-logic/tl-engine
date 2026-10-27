/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.backend;

import com.top_logic.basic.Logger;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.execution.RunRecording;
import com.top_logic.tl.ai.core.session.ConversationLog;
import com.top_logic.tl.ai.core.session.ConversationLogListener;
import com.top_logic.tl.ai.core.session.ConversationLogListenerBinding;
import com.top_logic.tl.ai.tool.api.CancellationToken;

/**
 * One run of a chat answered by a {@link ConversationBackend}: the backend's events recorded as an
 * execution of the conversation and delivered to the chat's listener.
 *
 * <p>
 * The chat calls {@link #run(ConversationBackend, BackendTurn, ConversationLog,
 * ConversationLogListener, AgentEventListener, CancellationToken)} on its worker thread, inside the
 * frame it sets up for every run (send lock, processing state, the windows' stream). What this adds
 * is the recording through {@link RunRecording} and the enforcement of the backend's contract: its
 * events arrive on the calling thread while the call lasts, and the run ends with
 * {@link AgentEvent.RunCompleted}. A backend that throws, breaks the contract or returns without
 * completing the run ends the run in an error, recorded and thrown to the chat, which reports it.
 * </p>
 */
public final class BackendRun {

	private BackendRun() {
		// Static utility.
	}

	/**
	 * Lets the backend answer one message and records the run.
	 *
	 * @param backend
	 *        What answers.
	 * @param turn
	 *        The message and its conversation.
	 * @param log
	 *        The chat's conversation log, whose open turn holds the user's message.
	 * @param logListener
	 *        The run's listener recording into {@code log}; bound to the thread for the run.
	 * @param listener
	 *        Receives every event of the run after it is recorded: {@code logListener} and the
	 *        delivery to the windows.
	 * @param cancel
	 *        The run's cancellation token.
	 * @return The backend's answer.
	 * @throws Exception
	 *         When the run failed; it is recorded as failed.
	 */
	public static String run(ConversationBackend backend, BackendTurn turn, ConversationLog log,
			ConversationLogListener logListener, AgentEventListener listener, CancellationToken cancel)
			throws Exception {
		// Bound, so that the recording takes this listener as the transcript's recorder instead of
		// adding a second one over the same log.
		try (ConversationLogListenerBinding.Binding binding = ConversationLogListenerBinding.bind(logListener)) {
			String agentName = backend.agentName();
			Logger.info("Chat " + turn.sessionId() + " is answered by the backend '" + agentName + "'.",
				BackendRun.class);
			RunRecording recording = RunRecording.open(agentName, turn.session(), null, null, log, listener);
			Sink sink = new Sink(recording, Thread.currentThread());
			String answer;
			try {
				answer = backend.send(turn, sink, cancel);
			} catch (Exception | Error ex) {
				sink.close();
				recording.fail(ex);
				throw ex;
			}
			sink.close();
			IllegalStateException broken = sink.contractViolation(agentName);
			if (broken != null) {
				recording.fail(broken);
				throw broken;
			}
			recording.finish(answer);
			return answer;
		}
	}

	/**
	 * The listener a backend is handed: delivers on the run's thread while the run lasts, and
	 * notes whether the run was completed. After the first refused event it refuses every further
	 * one.
	 */
	private static final class Sink implements AgentEventListener {

		private final AgentEventListener _delegate;

		private final Thread _owner;

		private volatile boolean _closed;

		private boolean _completed;

		private volatile String _violation;

		Sink(AgentEventListener delegate, Thread owner) {
			_delegate = delegate;
			_owner = owner;
		}

		@Override
		public void onEvent(AgentEvent event) {
			if (Thread.currentThread() != _owner) {
				throw refuse("delivered on thread '" + Thread.currentThread().getName()
					+ "' instead of the thread that called send()");
			}
			if (_closed) {
				throw refuse("delivered after send() returned");
			}
			String violation = _violation;
			if (violation != null) {
				// Nothing after a refused event counts, its completion included: the run ends in an
				// error, in the chat and in its recording alike.
				throw new IllegalStateException("Backend event refused, the run broke its contract: " + violation);
			}
			if (event instanceof AgentEvent.RunCompleted) {
				_completed = true;
			}
			try {
				_delegate.onEvent(event);
			} catch (RuntimeException ex) {
				// As for the framework's own loop: a listener that fails must not end the run.
				Logger.warn("Chat listener failed for event " + event.getClass().getSimpleName() + ": "
					+ ex.getMessage(), ex, BackendRun.class);
			}
		}

		private IllegalStateException refuse(String what) {
			String message = "Backend event " + what + ".";
			if (_violation == null) {
				_violation = message;
			}
			return new IllegalStateException(message);
		}

		void close() {
			_closed = true;
		}

		/**
		 * Why the run cannot count as completed, or <code>null</code> when it can.
		 */
		IllegalStateException contractViolation(String agentName) {
			String violation = _violation;
			if (violation != null) {
				return new IllegalStateException("Backend '" + agentName + "' broke its contract: " + violation);
			}
			if (!_completed) {
				return new IllegalStateException(
					"Backend '" + agentName + "' returned without completing the run.");
			}
			return null;
		}
	}
}
