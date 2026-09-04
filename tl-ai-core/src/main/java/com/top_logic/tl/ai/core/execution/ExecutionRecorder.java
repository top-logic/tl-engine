/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.execution;

import com.top_logic.basic.Logger;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.tl.ai.core.agent.AgentResult;
import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.ExecutionStatus;
import com.top_logic.tl.ai.core.execution.model.AgentExecution;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.provider.LLMService;

/**
 * {@link AgentEventListener} decorator that records an agent run as an {@link AgentExecution}.
 *
 * <p>
 * On {@code RunStarted} it creates the execution row with status
 * {@link ExecutionStatus#RUNNING} — capturing the loop-generated run id — and publishes it via
 * the run's {@link ExecutionHandle}. On {@code RunCompleted} it captures the terminal
 * {@link AgentResult}. The caller finalizes the row by invoking
 * {@link #complete(String, Throwable)} after the loop returns (or throws), supplying the
 * serialized transcript.
 * </p>
 *
 * <p>
 * Recording is best-effort: persistence failures are logged and never disturb the run. All
 * events are forwarded to the wrapped listener unchanged.
 * </p>
 */
public final class ExecutionRecorder implements AgentEventListener {

	private final AgentEventListener _delegate;

	private final AgentSession _session;

	private final String _agentName;

	private final AgentExecution _parent;

	private final ExecutionHandle _handle;

	private volatile AgentExecution _execution;

	private volatile String _runId;

	private volatile String _lastErrorMessage;

	private volatile AgentResult _result;

	/**
	 * Creates an {@link ExecutionRecorder}.
	 *
	 * @param session
	 *        The session the recorded execution belongs to; must not be <code>null</code>.
	 * @param agentName
	 *        Name of the agent handling the run; must not be <code>null</code>.
	 * @param parent
	 *        The delegating execution for sub-agent runs; <code>null</code> for top-level runs.
	 * @param handle
	 *        The run's execution holder (from the tool context); the created execution is
	 *        published here so tools can attribute their work to it.
	 * @param delegate
	 *        The listener to forward all events to.
	 */
	public ExecutionRecorder(AgentSession session, String agentName, AgentExecution parent,
			ExecutionHandle handle, AgentEventListener delegate) {
		_session = session;
		_agentName = agentName;
		_parent = parent;
		_handle = handle;
		_delegate = delegate;

		// Publish a stable conversation id on the handle at run setup, so per-conversation resources
		// (the interactive browser session) are reused across all runs/messages of one conversation.
		// Derived here - on the setup thread, before the loop starts and before beginExecution creates
		// the revision-sensitive AgentExecution row - as a plain String, so no tool ever dereferences
		// the persistent wrapper on an execution thread reading at an older revision.
		if (handle != null) {
			handle.setConversationId(conversationIdOf(session));
		}
	}

	/**
	 * The stable conversation id of {@code session} as a plain String (its persistent object id, as
	 * used to look the session back up), or <code>null</code> when no session is given or its id
	 * cannot be read. Best-effort: any failure degrades to <code>null</code> (the browser tool then
	 * falls back to per-run keying) rather than disturbing the run. Package-visible for testing.
	 */
	static String conversationIdOf(AgentSession session) {
		if (session == null) {
			return null;
		}
		try {
			return KBUtils.getWrappedObjectName(session).toString();
		} catch (RuntimeException ex) {
			Logger.info("Could not derive a conversation id for the browser session key: " + ex.getMessage(),
				ExecutionRecorder.class);
			return null;
		}
	}

	@Override
	public void onEvent(AgentEvent event) {
		if (event instanceof AgentEvent.RunStarted e) {
			_runId = e.runId();
			// Publish the run id on the shared handle before any tool runs, so tools can derive a
			// stable per-run key without a revision-sensitive read of the persistent execution
			// wrapper. Done unconditionally (even when execution recording is inactive) so the key
			// is available whether or not the run is persisted.
			if (_handle != null) {
				_handle.setRunId(_runId);
			}
			beginRecording(e);
		} else if (event instanceof AgentEvent.ProviderError e) {
			_lastErrorMessage = e.message();
		} else if (event instanceof AgentEvent.RunCompleted e) {
			_result = new AgentResult(e.reason(), e.turnNumber(), e.totalUsage());
		}
		_delegate.onEvent(event);
	}

	private void beginRecording(AgentEvent.RunStarted e) {
		if (!AgentExecutionStore.Module.INSTANCE.isActive()) {
			Logger.info("Agent run " + e.runId() + " not recorded: AgentExecutionStore is not active.",
				ExecutionRecorder.class);
			return;
		}
		try {
			String provider = resolveProviderId(e.model());
			_execution = AgentExecutionStore.getInstance()
				.beginExecution(_session, e.runId(), _agentName, e.model(), provider, _parent);
			if (_handle != null) {
				_handle.set(_execution);
			}
		} catch (RuntimeException ex) {
			Logger.warn("Failed to record start of agent run " + e.runId() + ": " + ex.getMessage(),
				ex, ExecutionRecorder.class);
		}
	}

	/**
	 * Finalizes the execution row after the loop returned or threw, without a final answer.
	 *
	 * @see #complete(String, String, Throwable)
	 */
	public void complete(String transcript, Throwable failure) {
		complete(transcript, null, failure);
	}

	/**
	 * Finalizes the execution row after the loop returned or threw.
	 *
	 * <p>
	 * No-op when run recording never started (store inactive or begin failed).
	 * </p>
	 *
	 * @param transcript
	 *        The serialized transcript of this run's turns; may be <code>null</code>.
	 * @param response
	 *        The agent's final answer text (the value the synchronous entry points return); may be
	 *        <code>null</code> when the run produced no answer.
	 * @param failure
	 *        The exception the loop threw, or <code>null</code> if it returned normally.
	 */
	public void complete(String transcript, String response, Throwable failure) {
		AgentExecution execution = _execution;
		if (execution == null) {
			return;
		}
		try {
			AgentResult result = _result;
			StopReason stopReason = result != null ? result.stopReason()
				: (failure != null ? StopReason.ERROR : null);
			TokenUsage usage = result != null ? result.totalUsage() : TokenUsage.EMPTY;
			AgentExecutionStore.getInstance().finishExecution(execution,
				statusFor(stopReason, failure), stopReason, errorMessage(failure), usage, transcript,
				response);
		} catch (CancelledWriteException ex) {
			// Expected on cancellation: the interrupt ends the wait, the write still commits.
			Logger.info("Agent run " + _runId + " was cancelled; its final record is written"
				+ " without waiting for it.", ExecutionRecorder.class);
		} catch (RuntimeException ex) {
			Logger.warn("Failed to record end of agent run " + _runId + ": " + ex.getMessage(),
				ex, ExecutionRecorder.class);
		}
	}

	/** The recorded execution, or <code>null</code> when recording is off. */
	public AgentExecution execution() {
		return _execution;
	}

	/** The loop-generated run id, or <code>null</code> before {@code RunStarted}. */
	public String runId() {
		return _runId;
	}

	/** The terminal {@link AgentResult}, or <code>null</code> before {@code RunCompleted}. */
	public AgentResult result() {
		return _result;
	}

	private String errorMessage(Throwable failure) {
		if (failure != null) {
			return failure.getMessage() != null ? failure.getMessage() : failure.getClass().getName();
		}
		return _lastErrorMessage;
	}

	private static ExecutionStatus statusFor(StopReason stopReason, Throwable failure) {
		if (stopReason == null) {
			return failure != null ? ExecutionStatus.ERROR : ExecutionStatus.COMPLETE;
		}
		return switch (stopReason) {
			case COMPLETE, MAX_TURNS -> ExecutionStatus.COMPLETE;
			case ERROR, CONTEXT_OVERFLOW -> ExecutionStatus.ERROR;
			case REJECTED -> ExecutionStatus.REJECTED;
			case ABORTED -> ExecutionStatus.CANCELLED;
		};
	}

	private static String resolveProviderId(String model) {
		if (!LLMService.Module.INSTANCE.isActive()) {
			return null;
		}
		try {
			return LLMService.getInstance().getProviderId(model);
		} catch (RuntimeException ex) {
			return null;
		}
	}
}
