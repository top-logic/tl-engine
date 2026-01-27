/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

/**
 * One entry in an execution timeline: either a tool call or an agent-to-agent call.
 *
 * <p>
 * Used by the chat UI to render a unified timeline that clearly distinguishes
 * AgentCall vs ToolCall.
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
	private final String _fromAgent;
	private final String _toAgent;
	private final String _inputSummary;
	private final String _resultSummary;

	private ChatTimelineEvent(Kind kind, ChatToolCall toolCall, String fromAgent, String toAgent,
			String inputSummary, String resultSummary) {
		_kind = kind;
		_toolCall = toolCall;
		_fromAgent = fromAgent;
		_toAgent = toAgent;
		_inputSummary = inputSummary;
		_resultSummary = resultSummary;
	}

	/**
	 * Creates a timeline entry for a tool call.
	 */
	public static ChatTimelineEvent toolCall(ChatToolCall toolCall) {
		return new ChatTimelineEvent(Kind.TOOL_CALL, toolCall, null, null, null, null);
	}

	/**
	 * Creates a timeline entry for an agent-to-agent call.
	 */
	public static ChatTimelineEvent agentCall(String fromAgent, String toAgent, String inputSummary,
			String resultSummary) {
		return new ChatTimelineEvent(Kind.AGENT_CALL, null, fromAgent, toAgent, inputSummary, resultSummary);
	}

	public Kind getKind() {
		return _kind;
	}

	/**
	 * The tool call when {@link #getKind()} is {@link Kind#TOOL_CALL}; null otherwise.
	 */
	public ChatToolCall getToolCall() {
		return _toolCall;
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

	/** Result summary when kind is AGENT_CALL. */
	public String getResultSummary() {
		return _resultSummary;
	}
}
