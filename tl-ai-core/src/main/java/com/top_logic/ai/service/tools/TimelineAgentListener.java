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
 * @see ToolCallTracker
 * @see ToolCallTracking
 */
public class TimelineAgentListener implements AgentEventListener {

	private static final String DELEGATION_PREFIX = "delegate_to_";

	private final ToolCallTracker _tracker;

	/** Pending args JSON keyed by toolCallId, populated on ToolCallStarted. */
	private final Map<String, String> _pendingArgs = new HashMap<>();

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
		switch (event) {
			case AgentEvent.ToolCallStarted started -> {
				if (!started.toolName().startsWith(DELEGATION_PREFIX)) {
					_pendingArgs.put(started.toolCallId(), started.argsJson());
				}
			}
			case AgentEvent.ToolCallCompleted completed -> {
				if (completed.toolName().startsWith(DELEGATION_PREFIX)) {
					return;
				}
				String argsJson = _pendingArgs.remove(completed.toolCallId());
				Map<String, Object> args = parseArguments(argsJson);
				_tracker.recordToolCall(completed.toolName(), args, completed.result());
			}
			default -> { /* ignore */ }
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
