/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.tools;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.top_logic.basic.json.JSON;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.message.TokenUsage;

/**
 * Bridges tl-ai-core {@link AgentEventListener} tool execution events to {@link ToolCallTracker}
 * for the chat timeline.
 *
 * <p>
 * Automatically records all tool executions except delegation tools (whose names start with
 * {@code delegate_to_}), which are tracked separately as agent calls by the delegation executor.
 * </p>
 *
 * <p>
 * {@link com.top_logic.tl.ai.core.agent.event.AgentEvent.ToolCallStarted} events are used to cache
 * the args JSON (keyed by {@code toolCallId}) so they are available when the corresponding
 * {@link com.top_logic.tl.ai.core.agent.event.AgentEvent.ToolCallCompleted} arrives.
 * </p>
 *
 * <p>
 * Intermediate text output (text produced alongside tool calls) is buffered and flushed as an
 * {@link TraceEntry.OutputTraceEntry} when the first tool call of a turn starts, so it appears in
 * the correct timeline position before the tool calls.
 * </p>
 *
 * @see ToolCallTracker
 * @see ToolCallTracking
 */
public class TimelineAgentListener implements AgentEventListener {

	private static final String DELEGATION_PREFIX = "delegate_to_";

	private final ToolCallTracker _tracker;

	/** Pending args JSON keyed by toolCallId, populated on ToolCallStarted. */
	private final Map<String, String> _pendingArgs = new HashMap<>();

	/** Buffered text output from the current turn, flushed before the first tool call. */
	private final StringBuilder _textBuffer = new StringBuilder();

	/**
	 * Creates a new listener that records tool executions into the given tracker.
	 *
	 * @param tracker
	 *        The tracker to record tool calls into. Must not be null.
	 */
	public TimelineAgentListener(ToolCallTracker tracker) {
		_tracker = Objects.requireNonNull(tracker, "tracker");
	}

	@Override
	public void onEvent(AgentEvent event) {
		if (event instanceof AgentEvent.TextDelta delta) {
			_textBuffer.append(delta.text());
		} else if (event instanceof AgentEvent.ToolCallStarted started) {
			if (!started.toolName().startsWith(DELEGATION_PREFIX)) {
				flushTextBuffer();
				_pendingArgs.put(started.toolCallId(), started.argsJson());
			}
		} else if (event instanceof AgentEvent.ThinkingDelta delta) {
			_tracker.appendThinking(delta.text());
		} else if (event instanceof AgentEvent.TurnCompleted completed) {
			// Clear text buffer at end of turn — if no tool calls happened, the text
			// is the final answer (persisted as a ChatMessage, not a trace entry).
			_textBuffer.setLength(0);
		} else if (event instanceof AgentEvent.RunCompleted completed) {
			TokenUsage usage = completed.totalUsage();
			if (usage != null) {
				_tracker.setTotalUsage(usage.inputTokens(), usage.outputTokens(),
					usage.thinkingTokens(), usage.cacheReadTokens(), usage.cacheWriteTokens());
			}
		} else if (event instanceof AgentEvent.ToolCallCompleted completed) {
			if (completed.toolName().startsWith(DELEGATION_PREFIX)) {
				return;
			}
			String argsJson = _pendingArgs.remove(completed.toolCallId());
			Map<String, Object> args = parseArguments(argsJson);
			long durationMs = completed.duration() != null ? completed.duration().toMillis() : 0;
			_tracker.recordToolCall(completed.toolName(), args, completed.result(), durationMs);
		}
	}

	private void flushTextBuffer() {
		if (_textBuffer.length() > 0) {
			_tracker.recordOutput(_textBuffer.toString());
			_textBuffer.setLength(0);
		}
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> parseArguments(String argumentsJson) {
		if (argumentsJson == null || argumentsJson.isBlank()) {
			return Map.of();
		}
		try {
			Object parsed = JSON.fromString(argumentsJson);
			if (parsed instanceof Map) {
				return (Map<String, Object>) parsed;
			}
			return Map.of("raw", parsed);
		} catch (Exception e) {
			return Map.of("raw", argumentsJson);
		}
	}
}
