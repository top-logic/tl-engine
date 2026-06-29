/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.interaction;

import java.util.Collection;
import java.util.Map;

import com.top_logic.tl.ai.core.agent.AgentDefinition;
import com.top_logic.tl.ai.core.agent.AgentDefinitionQuery;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.tl.ai.tool.api.ToolSchema;

/**
 * Lists all predefined agents available to spawn as subagents.
 */
public class ListAgentsTool implements Tool {

	private static final String INPUT_SCHEMA_JSON = """
		{
			"type": "object",
			"properties": {}
		}
		""";

	/** */
	public ListAgentsTool() {
	}

	@Override
	public String name() {
		return "list_agents";
	}

	@Override
	public String description() {
		return "List the predefined agents available to spawn as subagents, with their descriptions.";
	}

	@Override
	public ToolSchema schema() {
		return new ToolSchema(name(), description(), INPUT_SCHEMA_JSON);
	}

	@Override
	public ToolMode mode() {
		return ToolMode.READ;
	}

	@Override
	public boolean isConcurrencySafe() {
		return true;
	}

	@Override
	public ToolResult execute(Map<String, Object> args, ToolExecutionContext context) {
		Collection<AgentDefinition> agents = AgentDefinitionQuery.getInstance().getAllAgents();

		StringBuilder json = new StringBuilder("[");
		boolean first = true;
		for (AgentDefinition agent : agents) {
			if (!first) {
				json.append(",");
			}
			first = false;
			json.append("{\"name\":\"").append(jsonEscape(agent.name())).append("\",");
			json.append("\"description\":\"").append(jsonEscape(agent.description())).append("\"}");
		}
		json.append("]");

		return ToolResult.of(json.toString());
	}

	/** Escapes a string for JSON embedding. */
	private static String jsonEscape(String value) {
		if (value == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder(value.length() + 4);
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			switch (c) {
				case '"' -> sb.append("\\\"");
				case '\\' -> sb.append("\\\\");
				case '\n' -> sb.append("\\n");
				case '\r' -> sb.append("\\r");
				case '\t' -> sb.append("\\t");
				default -> {
					if (c < 0x20) {
						sb.append(String.format("\\u%04x", (int) c));
					} else {
						sb.append(c);
					}
				}
			}
		}
		return sb.toString();
	}
}
