/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.api;

import java.util.Map;

/**
 * One timeline event (tool call or agent call) for JSON API.
 */
public class TimelineEventDto {

	private String _id;
	private String _type; // "tool_call" or "agent_call"
	private String _toolName;
	private String _name;
	private Map<String, Object> _args;
	private String _result;
	private String _fromAgent;
	private String _toAgent;
	private String _inputSummary;
	private String _resultSummary;

	/** Event identifier. */
	public String getId() {
		return _id;
	}

	/** @see #getId() */
	public void setId(String id) {
		_id = id;
	}

	/** Event type: "tool_call" or "agent_call". */
	public String getType() {
		return _type;
	}

	/** @see #getType() */
	public void setType(String type) {
		_type = type;
	}

	/** Tool name (for tool_call events). */
	public String getToolName() {
		return _toolName;
	}

	/** @see #getToolName() */
	public void setToolName(String toolName) {
		_toolName = toolName;
	}

	/** Event name. */
	public String getName() {
		return _name;
	}

	/** @see #getName() */
	public void setName(String name) {
		_name = name;
	}

	/** Tool arguments (for tool_call events). */
	public Map<String, Object> getArgs() {
		return _args;
	}

	/** @see #getArgs() */
	public void setArgs(Map<String, Object> args) {
		_args = args;
	}

	/** Tool result (for tool_call events). */
	public String getResult() {
		return _result;
	}

	/** @see #getResult() */
	public void setResult(String result) {
		_result = result;
	}

	/** Source agent name (for agent_call events). */
	public String getFromAgent() {
		return _fromAgent;
	}

	/** @see #getFromAgent() */
	public void setFromAgent(String fromAgent) {
		_fromAgent = fromAgent;
	}

	/** Target agent name (for agent_call events). */
	public String getToAgent() {
		return _toAgent;
	}

	/** @see #getToAgent() */
	public void setToAgent(String toAgent) {
		_toAgent = toAgent;
	}

	/** Input summary (for agent_call events). */
	public String getInputSummary() {
		return _inputSummary;
	}

	/** @see #getInputSummary() */
	public void setInputSummary(String inputSummary) {
		_inputSummary = inputSummary;
	}

	/** Result summary (for agent_call events). */
	public String getResultSummary() {
		return _resultSummary;
	}

	/** @see #getResultSummary() */
	public void setResultSummary(String resultSummary) {
		_resultSummary = resultSummary;
	}
}
