/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.tools;

import java.util.Map;
import java.util.StringJoiner;

/**
 * Utility class for managing thread-local tool call tracking.
 * 
 * <p>
 * Tools can use this class to record their calls if tracking is enabled in the current thread.
 * This allows automatic tracking without requiring wrapper classes. Tracking can be used for
 * debugging, monitoring, analysis, judge scoring, and other purposes.
 * </p>
 */
public class ToolCallTracking {

	/**
	 * Thread-local storage for the current tracker.
	 */
	private static final ThreadLocal<ToolCallTracker> TRACKER = new ThreadLocal<>();

	/**
	 * Sets the tracker for the current thread.
	 * 
	 * @param tracker
	 *        The tracker to use, or null to disable tracking
	 */
	public static void setTracker(ToolCallTracker tracker) {
		if (tracker == null) {
			TRACKER.remove();
		} else {
			TRACKER.set(tracker);
		}
	}

	/**
	 * Gets the tracker for the current thread.
	 * 
	 * @return The tracker, or null if not set
	 */
	public static ToolCallTracker getTracker() {
		return TRACKER.get();
	}

	/**
	 * Records a tool call if tracking is enabled in the current thread.
	 *
	 * <p>
	 * This method is safe to call even when tracking is not enabled - it will simply do nothing.
	 * </p>
	 *
	 * @param toolName
	 *        The name of the tool method (e.g., {@code listTypes}, {@code listTypeParts})
	 * @param args
	 *        The arguments passed to the tool
	 * @param resultJson
	 *        The JSON result from the tool
	 */
	public static void recordIfTracking(String toolName, Map<String, Object> args, String resultJson) {
		ToolCallTracker tracker = getTracker();
		if (tracker != null) {
			tracker.recordToolCall(toolName, args, resultJson);
		}
	}

	/**
	 * Records the start of an agent-to-agent call when the request is sent (before invoking the sub-agent).
	 * The agent call will appear in the timeline before that agent's tool calls.
	 * Call {@link #recordAgentCallCompleteIfTracking(String)} when the sub-agent returns.
	 *
	 * @param fromAgent
	 *        Name of the calling agent (e.g. "Orchestrator").
	 * @param toAgent
	 *        Name of the called agent (e.g. "ModelExplorer").
	 * @param inputSummary
	 *        Short summary of the work order or input sent.
	 */
	public static void recordAgentCallStartIfTracking(String fromAgent, String toAgent, String inputSummary) {
		ToolCallTracker tracker = getTracker();
		if (tracker != null) {
			tracker.recordAgentCallStart(fromAgent, toAgent, inputSummary);
		}
	}

	/**
	 * Completes the most recently started agent call with the result summary.
	 * Call when the sub-agent returns (success or error).
	 *
	 * @param resultSummary
	 *        Short summary of the result returned.
	 */
	public static void recordAgentCallCompleteIfTracking(String resultSummary) {
		ToolCallTracker tracker = getTracker();
		if (tracker != null) {
			tracker.recordAgentCallComplete(resultSummary);
		}
	}

	/**
	 * Records an agent-to-agent call if tracking is enabled (single call).
	 * Prefer {@link #recordAgentCallStartIfTracking(String, String, String)} and
	 * {@link #recordAgentCallCompleteIfTracking(String)} so the agent call appears before the sub-agent's tool calls.
	 *
	 * @param fromAgent
	 *        Name of the calling agent (e.g. "Orchestrator").
	 * @param toAgent
	 *        Name of the called agent (e.g. "ModelExplorer").
	 * @param inputSummary
	 *        Short summary of the work order or input sent.
	 * @param resultSummary
	 *        Short summary of the result returned.
	 */
	public static void recordAgentCallIfTracking(String fromAgent, String toAgent, String inputSummary,
			String resultSummary) {
		ToolCallTracker tracker = getTracker();
		if (tracker != null) {
			tracker.recordAgentCall(fromAgent, toAgent, inputSummary, resultSummary);
		}
	}

	/**
	 * Formats a tool call for logging purposes.
	 *
	 * <p>
	 * Returns a human-readable string representation of the tool call including the tool name,
	 * arguments, and optionally a result summary.
	 * </p>
	 *
	 * @param toolName
	 *        The name of the tool method (e.g., {@code listTypes}, {@code listTypeParts})
	 * @param args
	 *        The arguments passed to the tool
	 * @param resultJson
	 *        Optional JSON result from the tool. If provided, a summary (length) will be included.
	 * @return A formatted string representation of the tool call
	 */
	public static String formatToolCall(String toolName, Map<String, Object> args, String resultJson) {
		StringJoiner joiner = new StringJoiner(", ", toolName + "(", ")");
		
		if (args != null && !args.isEmpty()) {
			for (Map.Entry<String, Object> entry : args.entrySet()) {
				String value = formatArgValue(entry.getValue());
				joiner.add(entry.getKey() + "=" + value);
			}
		}
		
		String result = joiner.toString();
		
		if (resultJson != null) {
			int resultLength = resultJson.length();
			result += " -> " + resultLength + " chars";
		}
		
		return result;
	}

	/**
	 * Formats an argument value for display in logs.
	 * 
	 * @param value
	 *        The argument value
	 * @return A string representation suitable for logging
	 */
	private static String formatArgValue(Object value) {
		if (value == null) {
			return "null";
		}
		if (value instanceof String) {
			String str = (String) value;
			// Truncate long strings
			if (str.length() > 50) {
				return "\"" + str.substring(0, 47) + "...\"";
			}
			return "\"" + str + "\"";
		}
		if (value instanceof java.util.Collection) {
			java.util.Collection<?> coll = (java.util.Collection<?>) value;
			return "[" + coll.size() + " items]";
		}
		return String.valueOf(value);
	}
}
