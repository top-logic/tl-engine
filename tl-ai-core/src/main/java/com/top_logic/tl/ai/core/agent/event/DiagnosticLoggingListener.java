/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.event;

import java.time.Duration;
import java.time.Instant;

import com.top_logic.basic.Logger;
import com.top_logic.tl.ai.core.memory.CompactionResult;
import com.top_logic.tl.ai.core.message.TokenUsage;

/**
 * {@link AgentEventListener} decorator that writes a run's lifecycle as structured, greppable
 * diagnostic lines to the dedicated {@code com.top_logic.tl.ai} logger category, then forwards
 * every event unchanged to the wrapped listener.
 *
 * <p>
 * The lines <em>complement</em> the persisted execution model
 * ({@link com.top_logic.tl.ai.core.execution.AgentExecutionStore}) rather than duplicating it: an
 * {@link com.top_logic.tl.ai.core.execution.model.AgentExecution} row already stores run id, model,
 * status, stop reason, error message, total token usage and the transcript. This listener therefore
 * logs only the <em>operational</em> signal that is not in the database - per-turn latency and token
 * split, per-tool timing and result <em>sizes</em> (never the payloads), provider retries/backoff,
 * context compaction, loop-guard blocks, and transaction outcomes - each correlated by
 * {@code runId} so a log line can be joined back to its persisted row.
 * </p>
 *
 * <p>
 * <b>No payloads.</b> Prompts, tool arguments and tool results are persisted (and may contain
 * customer data), so they are never written here; where a reference is useful, the byte size, the
 * tool name and the {@code runId} are logged instead of the content.
 * </p>
 *
 * <h2>Line format</h2>
 * <p>
 * Every line starts with {@code run=<runId> turn=<n> <EVENT>} followed by space-separated
 * {@code key=value} diagnostics, e.g.:
 * </p>
 * <pre>
 * run=8f3c… turn=2 TOOL_OK name=search_model ms=142 resultBytes=2481
 * run=8f3c… turn=2 PROVIDER_RETRY kind=OVERLOADED attempt=1 waitMs=2000
 * run=8f3c… turn=3 RUN_END reason=COMPLETE turns=3 ms=8123
 * </pre>
 *
 * <h2>Levels</h2>
 * <ul>
 * <li><b>INFO</b> - rare, important operational events that are not persisted and are worth seeing
 * even with debug off: provider retries and reactive (context-overflow) compaction.</li>
 * <li><b>DEBUG</b> - the full per-run / per-turn / per-tool trace. Off by default, so there is zero
 * output (and, guarded by {@link Logger#isDebugEnabled(Object)}, near-zero overhead) unless the
 * {@code com.top_logic.tl.ai} category is raised to {@code debug}.</li>
 * </ul>
 *
 * @see AgentEventListener
 * @see com.top_logic.tl.ai.core.execution.ExecutionRecorder
 */
public final class DiagnosticLoggingListener implements AgentEventListener {

	private final AgentEventListener _delegate;

	/** Run start, captured on {@link AgentEvent.RunStarted} for the {@code RUN_END} duration. */
	private Instant _runStart;

	/** Start of the current turn, captured on {@link AgentEvent.TurnStarted} for turn duration. */
	private Instant _turnStart;

	/**
	 * Creates a {@link DiagnosticLoggingListener}.
	 *
	 * @param delegate
	 *        The listener to forward all events to; must not be <code>null</code>.
	 */
	public DiagnosticLoggingListener(AgentEventListener delegate) {
		_delegate = delegate;
	}

	@Override
	public void onEvent(AgentEvent event) {
		try {
			trace(event);
		} catch (RuntimeException ex) {
			// Diagnostics must never disturb a run or drop an event for the real listener.
			Logger.warn("Failed to log agent diagnostics: " + ex.getMessage(), ex,
				DiagnosticLoggingListener.class);
		} finally {
			_delegate.onEvent(event);
		}
	}

	private void trace(AgentEvent event) {
		// INFO: rare, non-persisted operational events worth seeing even with debug off.
		if (event instanceof AgentEvent.RetryScheduled e) {
			Logger.info(line(e, "PROVIDER_RETRY")
				+ " kind=" + e.kind() + " attempt=" + e.attempt() + " waitMs=" + e.waitMs(),
				DiagnosticLoggingListener.class);
			return;
		}
		if (event instanceof AgentEvent.CompactionStarted e && "reactive".equals(e.trigger())) {
			Logger.info(line(e, "COMPACT_START")
				+ " trigger=reactive tokensBefore=" + e.tokensBefore(),
				DiagnosticLoggingListener.class);
			return;
		}

		// Everything else is DEBUG detail. Guard once: no work when the category is not at debug.
		if (!Logger.isDebugEnabled(DiagnosticLoggingListener.class)) {
			// Still capture timing anchors so a later RUN_END/TURN_END has a start to measure from,
			// should debug be toggled on mid-run.
			captureTiming(event);
			return;
		}

		captureTiming(event);
		String msg = detail(event);
		if (msg != null) {
			Logger.debug(msg, DiagnosticLoggingListener.class);
		}
	}

	private void captureTiming(AgentEvent event) {
		if (event instanceof AgentEvent.RunStarted) {
			_runStart = event.timestamp();
		} else if (event instanceof AgentEvent.TurnStarted) {
			_turnStart = event.timestamp();
		}
	}

	/**
	 * Renders the DEBUG line for the given event, or <code>null</code> if the event is not traced.
	 *
	 * <p>
	 * Package-visible so the formatting of every event type can be unit-tested directly, without
	 * depending on the effective log level.
	 * </p>
	 */
	String detail(AgentEvent event) {
		if (event instanceof AgentEvent.RunStarted e) {
			return line(e, "RUN_START") + " model=" + e.model();
		}
		if (event instanceof AgentEvent.RunCompleted e) {
			return line(e, "RUN_END") + " reason=" + e.reason() + " turns=" + e.turnNumber()
				+ ms("ms", _runStart, e.timestamp());
		}
		if (event instanceof AgentEvent.TurnStarted e) {
			return line(e, "TURN_START");
		}
		if (event instanceof AgentEvent.TurnCompleted e) {
			return line(e, "TURN_END") + usage(e.turnUsage())
				+ " parts=" + e.contentParts().size() + ms("ms", _turnStart, e.timestamp());
		}
		if (event instanceof AgentEvent.ToolCallStarted e) {
			return line(e, "TOOL_START") + " name=" + e.toolName() + " argsBytes=" + len(e.argsJson());
		}
		if (event instanceof AgentEvent.ToolCallCompleted e) {
			return line(e, "TOOL_OK") + " name=" + e.toolName() + duration(e.duration())
				+ " resultBytes=" + len(e.result());
		}
		if (event instanceof AgentEvent.ToolCallFailed e) {
			return line(e, isLoopGuard(e.errorMessage()) ? "LOOP_BLOCK" : "TOOL_FAIL")
				+ " name=" + e.toolName() + duration(e.duration())
				+ " err=" + summary(e.errorMessage());
		}
		if (event instanceof AgentEvent.ToolCallDenied e) {
			return line(e, "TOOL_DENIED") + " name=" + e.toolName() + " reason=" + summary(e.reason());
		}
		if (event instanceof AgentEvent.ToolCallResultTruncated e) {
			return line(e, "TOOL_TRUNCATED") + " name=" + e.toolName()
				+ " from=" + e.originalLength() + " to=" + e.truncatedLength();
		}
		if (event instanceof AgentEvent.ApprovalRequired e) {
			return line(e, "APPROVAL_REQUIRED") + " name=" + e.toolName();
		}
		if (event instanceof AgentEvent.ApprovalResolved e) {
			return line(e, "APPROVAL_RESOLVED") + " outcome=" + e.outcome();
		}
		if (event instanceof AgentEvent.TransactionBegan e) {
			return line(e, "TX_BEGIN");
		}
		if (event instanceof AgentEvent.TransactionCommitted e) {
			return line(e, "TX_COMMIT") + " rev=" + e.revisionNumber();
		}
		if (event instanceof AgentEvent.TransactionRolledBack e) {
			return line(e, "TX_ROLLBACK") + " reason=" + summary(e.reason());
		}
		if (event instanceof AgentEvent.CompactionStarted e) {
			return line(e, "COMPACT_START") + " trigger=" + e.trigger()
				+ " tokensBefore=" + e.tokensBefore();
		}
		if (event instanceof AgentEvent.CompactionCompleted e) {
			CompactionResult r = e.result();
			return line(e, "COMPACT_DONE") + " trigger=" + r.trigger()
				+ " messages=" + r.messagesSummarized()
				+ " tokensBefore=" + r.tokensBefore() + " tokensAfter=" + r.tokensAfter();
		}
		if (event instanceof AgentEvent.TaskUpdated e) {
			return line(e, "TASKS_UPDATED") + " bytes=" + len(e.tasksJson());
		}
		if (event instanceof AgentEvent.ProviderError e) {
			return line(e, "PROVIDER_ERROR") + " kind=" + e.kind() + " msg=" + summary(e.message());
		}
		// Content deltas carry LLM output; delegated child events are logged by the child's own
		// loop under its own runId; ToolProgress/RetryScheduled are handled elsewhere or elided.
		// None of these produce a DEBUG detail line here.
		return null;
	}

	private static String line(AgentEvent e, String tag) {
		return "run=" + e.runId() + " turn=" + e.turnNumber() + " " + tag;
	}

	private static String usage(TokenUsage u) {
		if (u == null) {
			return "";
		}
		return " in=" + u.inputTokens() + " out=" + u.outputTokens()
			+ " think=" + u.thinkingTokens()
			+ " cacheR=" + u.cacheReadTokens() + " cacheW=" + u.cacheWriteTokens();
	}

	private static String duration(Duration d) {
		return d != null ? " ms=" + d.toMillis() : "";
	}

	private static String ms(String key, Instant start, Instant end) {
		if (start == null || end == null) {
			return "";
		}
		return " " + key + "=" + Duration.between(start, end).toMillis();
	}

	private static int len(String s) {
		return s != null ? s.length() : 0;
	}

	/** A short, single-line, length-bounded rendering of a diagnostic string (never a payload). */
	private static String summary(String s) {
		if (s == null) {
			return "";
		}
		String flat = s.replace('\n', ' ').replace('\r', ' ');
		return flat.length() <= 200 ? flat : flat.substring(0, 200) + "…";
	}

	private static boolean isLoopGuard(String errorMessage) {
		return errorMessage != null && errorMessage.startsWith("DUPLICATE CALL BLOCKED");
	}
}
