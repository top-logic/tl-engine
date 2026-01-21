/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo.test.agent.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple builder for required facts in a test.
 * 
 * <p>
 * Each test explicitly adds tool call results that represent the "required facts"
 * that the agent must mention in its answer.
 * </p>
 * 
 * <p>
 * Example usage:
 * <pre>
 * RequiredFacts required = new RequiredFacts();
 * required.add(browserTool.listTypes("luigis.pizzashop"));
 * required.add(browserTool.listTypeParts("luigis.pizzashop", "ProductComponent", true));
 * String json = required.toJson();
 * </pre>
 * </p>
 */
public class RequiredFacts {

	private final List<String> _toolResults;

	/**
	 * Creates a new empty required facts builder.
	 */
	public RequiredFacts() {
		_toolResults = new ArrayList<>();
	}

	/**
	 * Adds a tool call result to the required facts.
	 * 
	 * @param toolResultJson
	 *        The JSON result from a tool call
	 */
	public void add(String toolResultJson) {
		if (toolResultJson != null && !toolResultJson.isEmpty()) {
			_toolResults.add(toolResultJson);
		}
	}

	/**
	 * Converts the required facts to a simple JSON array.
	 * 
	 * @return JSON array of tool results
	 */
	public String toJson() {
		StringBuilder json = new StringBuilder();
		json.append("[\n");
		
		for (int i = 0; i < _toolResults.size(); i++) {
			if (i > 0) {
				json.append(",\n");
			}
			json.append("  ").append(_toolResults.get(i));
		}
		
		json.append("\n]");
		return json.toString();
	}

	/**
	 * Gets the number of tool results added.
	 * 
	 * @return The count
	 */
	public int size() {
		return _toolResults.size();
	}

	/**
	 * Checks if any required facts have been added.
	 * 
	 * @return true if empty
	 */
	public boolean isEmpty() {
		return _toolResults.isEmpty();
	}
}
