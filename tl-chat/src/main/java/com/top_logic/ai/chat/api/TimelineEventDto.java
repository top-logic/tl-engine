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

	public String getId() {
		return _id;
	}

	public void setId(String id) {
		_id = id;
	}

	public String getType() {
		return _type;
	}

	public void setType(String type) {
		_type = type;
	}

	public String getToolName() {
		return _toolName;
	}

	public void setToolName(String toolName) {
		_toolName = toolName;
	}

	public String getName() {
		return _name;
	}

	public void setName(String name) {
		_name = name;
	}

	public Map<String, Object> getArgs() {
		return _args;
	}

	public void setArgs(Map<String, Object> args) {
		_args = args;
	}

	public String getResult() {
		return _result;
	}

	public void setResult(String result) {
		_result = result;
	}

	public String getFromAgent() {
		return _fromAgent;
	}

	public void setFromAgent(String fromAgent) {
		_fromAgent = fromAgent;
	}

	public String getToAgent() {
		return _toAgent;
	}

	public void setToAgent(String toAgent) {
		_toAgent = toAgent;
	}

	public String getInputSummary() {
		return _inputSummary;
	}

	public void setInputSummary(String inputSummary) {
		_inputSummary = inputSummary;
	}

	public String getResultSummary() {
		return _resultSummary;
	}

	public void setResultSummary(String resultSummary) {
		_resultSummary = resultSummary;
	}
}
