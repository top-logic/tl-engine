/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.util.function.Supplier;

/**
 * One entry in an execution timeline: either a tool call or an agent-to-agent call.
 *
 * <p>
 * Used by the chat UI to render a unified timeline that clearly distinguishes
 * {@code AgentCall} vs {@code ToolCall}.
 * </p>
 */
public class ChatTimelineEvent {

	/**
	 * Kind of timeline entry.
	 */
	public enum Kind {
		/** A tool was invoked. */
		TOOL_CALL,
		/** One agent delegated to another. */
		AGENT_CALL
	}

	private final Kind _kind;
	private final ChatToolCall _toolCall;
	private final long _durationMs;
	private final String _fromAgent;
	private final String _toAgent;
	private final String _inputSummary;
	private final String _resultSummary;
	private final Supplier<String> _resultSummarySupplier;

	private ChatTimelineEvent(Kind kind, ChatToolCall toolCall, long durationMs, String fromAgent, String toAgent,
			String inputSummary, String resultSummary, Supplier<String> resultSummarySupplier) {
		_kind = kind;
		_toolCall = toolCall;
		_durationMs = durationMs;
		_fromAgent = fromAgent;
		_toAgent = toAgent;
		_inputSummary = inputSummary;
		_resultSummary = resultSummary;
		_resultSummarySupplier = resultSummarySupplier;
	}

	/**
	 * Creates a timeline entry for a tool call.
	 */
	public static ChatTimelineEvent toolCall(ChatToolCall toolCall) {
		return new ChatTimelineEvent(Kind.TOOL_CALL, toolCall, 0, null, null, null, null, null);
	}

	/**
	 * Creates a timeline entry for a tool call with explicit duration.
	 */
	public static ChatTimelineEvent toolCall(ChatToolCall toolCall, long durationMs) {
		return new ChatTimelineEvent(Kind.TOOL_CALL, toolCall, durationMs, null, null, null, null, null);
	}

	/**
	 * Creates a timeline entry for an agent-to-agent call.
	 */
	public static ChatTimelineEvent agentCall(String fromAgent, String toAgent, String inputSummary,
			String resultSummary) {
		return new ChatTimelineEvent(Kind.AGENT_CALL, null, 0, fromAgent, toAgent, inputSummary, resultSummary, null);
	}

	/**
	 * Creates a timeline entry for an agent-to-agent call whose result summary is read at render time
	 * (e.g. when the call was recorded at start and completed later).
	 */
	public static ChatTimelineEvent agentCallWithDynamicResult(String fromAgent, String toAgent, String inputSummary,
			Supplier<String> resultSummarySupplier) {
		return new ChatTimelineEvent(Kind.AGENT_CALL, null, 0, fromAgent, toAgent, inputSummary, null,
			resultSummarySupplier);
	}

	/**
	 * The kind of timeline event.
	 */
	public Kind getKind() {
		return _kind;
	}

	/**
	 * The tool call when {@link #getKind()} is {@link Kind#TOOL_CALL}; null otherwise.
	 */
	public ChatToolCall getToolCall() {
		return _toolCall;
	}

	/** Execution duration in milliseconds, or 0 if unknown. */
	public long getDurationMs() {
		return _durationMs;
	}

	/** Calling agent when kind is AGENT_CALL. */
	public String getFromAgent() {
		return _fromAgent;
	}

	/** Called agent when kind is AGENT_CALL. */
	public String getToAgent() {
		return _toAgent;
	}

	/** Input/work-order summary when kind is AGENT_CALL. */
	public String getInputSummary() {
		return _inputSummary;
	}

	/** Result summary when kind is AGENT_CALL (read at call time; may be updated if using dynamic result). */
	public String getResultSummary() {
		if (_resultSummarySupplier != null) {
			String s = _resultSummarySupplier.get();
			return s != null ? s : "";
		}
		return _resultSummary != null ? _resultSummary : "";
	}
}
