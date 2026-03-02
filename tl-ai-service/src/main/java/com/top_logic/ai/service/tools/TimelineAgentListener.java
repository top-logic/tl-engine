/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.tools;

import java.util.Map;
import java.util.Objects;

import com.top_logic.basic.json.JSON;

import dev.langchain4j.agentic.observability.AgentListener;
import dev.langchain4j.service.tool.ToolExecution;

/**
 * Bridges LangChain4j's {@link AgentListener} tool execution events to
 * {@link ToolCallTracker} for the chat timeline.
 *
 * <p>
 * Automatically records all tool executions except delegation tools
 * (whose names start with {@code delegate_to_}), which are tracked
 * separately as agent calls by the delegation executor.
 * </p>
 *
 * @see ToolCallTracker
 * @see ToolCallTracking
 */
public class TimelineAgentListener implements AgentListener {

	private static final String DELEGATION_PREFIX = "delegate_to_";

	private final ToolCallTracker _tracker;

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
	public void afterToolExecution(ToolExecution execution) {
		String toolName = execution.request().name();
		if (toolName.startsWith(DELEGATION_PREFIX)) {
			return;
		}

		Map<String, Object> args = parseArguments(execution.request().arguments());
		String resultJson = execution.result();
		_tracker.recordToolCall(toolName, args, resultJson);
	}

	@Override
	public boolean inheritedBySubagents() {
		return true;
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
