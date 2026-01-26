/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.json.JSON;

/**
 * Tracks tool calls made during agent execution.
 * 
 * <p>
 * Records tool name, arguments, and results with result references (T1, T2, etc.)
 * for various purposes such as debugging, monitoring, analysis, and judge scoring.
 * </p>
 * 
 * @author jhu
 */
public class ToolCallTracker {

	/**
	 * Represents a single tool call with its arguments and result reference.
	 */
	public static class ToolCall {
		private final String _tool;
		private final Map<String, Object> _args;
		private final String _resultRef;

		public ToolCall(String tool, Map<String, Object> args, String resultRef) {
			_tool = tool;
			_args = args;
			_resultRef = resultRef;
		}

		/**
		 * The name of the tool that was called.
		 */
		public String getTool() {
			return _tool;
		}

		/**
		 * The arguments passed to the tool.
		 */
		public Map<String, Object> getArgs() {
			return _args;
		}

		/**
		 * The result reference (e.g., "T1", "T2") for this tool call.
		 */
		public String getResultRef() {
			return _resultRef;
		}
	}

	private final List<ToolCall> _toolCalls = new ArrayList<>();
	private final Map<String, Object> _toolResults = new HashMap<>();
	private final Map<String, String> _rawResultJsons = new HashMap<>();
	private int _nextResultRef = 1;

	/**
	 * Records a tool call and its result.
	 *
	 * @param toolName
	 *        The name of the tool (e.g., "listTypes", "listTypeParts")
	 * @param args
	 *        The arguments passed to the tool
	 * @param resultJson
	 *        The JSON result from the tool
	 * @return The result reference (e.g., "T1", "T2")
	 */
	public String recordToolCall(String toolName, Map<String, Object> args, String resultJson) {
		String resultRef = "T" + _nextResultRef++;
		_toolCalls.add(new ToolCall(toolName, args, resultRef));

		// Store the raw JSON
		_rawResultJsons.put(resultRef, resultJson);

		// Parse and store the result JSON
		try {
			Object parsedResult = JSON.fromString(resultJson);
			_toolResults.put(resultRef, parsedResult);
		} catch (Exception ex) {
			// If parsing fails, store as error
			Map<String, Object> errorResult = new HashMap<>();
			errorResult.put("error", "Failed to parse result: " + ex.getMessage());
			_toolResults.put(resultRef, errorResult);
		}

		return resultRef;
	}

	/**
	 * Gets the list of tool calls in order.
	 */
	public List<ToolCall> getToolCalls() {
		return Collections.unmodifiableList(_toolCalls);
	}

	/**
	 * Gets the map of result references to tool results.
	 */
	public Map<String, Object> getToolResults() {
		return Collections.unmodifiableMap(_toolResults);
	}

	/**
	 * Gets the map of result references to raw JSON result strings.
	 */
	public Map<String, String> getRawResultJsons() {
		return Collections.unmodifiableMap(_rawResultJsons);
	}

	/**
	 * Clears all recorded tool calls and results.
	 */
	public void clear() {
		_toolCalls.clear();
		_toolResults.clear();
		_rawResultJsons.clear();
		_nextResultRef = 1;
	}
}
