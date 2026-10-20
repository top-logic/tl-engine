/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.interaction;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.Logger;
import com.top_logic.tl.ai.core.agent.AgentCapabilities;
import com.top_logic.tl.ai.core.agent.AgentDefinition;
import com.top_logic.tl.ai.core.agent.AgentDefinitionQuery;
import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.tl.ai.tool.api.ToolSchema;

/**
 * Lists the predefined agents available to spawn as subagents.
 *
 * <p>
 * Filtered the way the prompt's delegate catalog is filtered: an agent whose every reachable tool is
 * denied to the acting user is left out, and one that is only partly reachable is listed with the
 * capabilities it cannot use. Listing what a spawn would spend a provider round trip failing at is
 * what this filter exists to stop; see {@link AgentCapabilities#unusableReason(AgentDefinition)} and
 * {@code doc/security/agent-security-architecture.md} section 3.2.7.
 * </p>
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

	/**
	 * {@link ToolGroups#INTERACTION}.
	 *
	 * <p>
	 * Listing the delegation targets is part of the delegation primitive itself.
	 * </p>
	 */
	@Override
	public String group() {
		return ToolGroups.INTERACTION;
	}

	@Override
	public boolean isConcurrencySafe() {
		return true;
	}

	@Override
	public ToolResult execute(Map<String, Object> args, ToolExecutionContext context) {
		Collection<AgentDefinition> agents = AgentDefinitionQuery.getInstance().getAllAgents();
		AgentCapabilities capabilities = AgentCapabilities.forRun(security(context));

		StringBuilder json = new StringBuilder("[");
		boolean first = true;
		for (AgentDefinition agent : agents) {
			AgentCapabilities.Unusable unusable = capabilities.unusableReason(agent);
			if (unusable != null) {
				// Capability must not vanish silently: an operator asked why an agent is no longer
				// offered for spawning must find the answer here.
				Logger.info("Agent '" + agent.name() + "' is not offered for spawning to the current"
					+ " user - " + unusable.message(), ListAgentsTool.class);
				continue;
			}
			if (!first) {
				json.append(",");
			}
			first = false;
			json.append("{\"name\":\"").append(jsonEscape(agent.name())).append("\",");
			json.append("\"description\":\"").append(jsonEscape(agent.description())).append("\"");
			Set<String> unreachable = capabilities.unreachableCapabilities(agent);
			if (!unreachable.isEmpty()) {
				// The same thing the prompt's delegate catalog says: it works, but not for these.
				json.append(",\"cannotUse\":[");
				boolean firstCapability = true;
				for (String capability : unreachable) {
					if (!firstCapability) {
						json.append(",");
					}
					firstCapability = false;
					json.append("\"").append(jsonEscape(capability)).append("\"");
				}
				json.append("]");
			}
			json.append("}");
		}
		json.append("]");

		return ToolResult.of(json.toString());
	}

	/**
	 * The security context of the run this call belongs to.
	 *
	 * <p>
	 * There is no permissive fallback: the listing is filtered to what the run's principal could
	 * actually delegate to, and a listing that cannot name the principal would advertise agents the
	 * caller may not use.
	 * </p>
	 */
	private static SecurityContext security(ToolExecutionContext context) {
		if (context instanceof ToolContext toolContext) {
			return toolContext.security();
		}
		throw new IllegalStateException("The agent listing is filtered for the run's principal, and this"
			+ " call has no run to ask. Execute the tool with the ToolContext of an agent run.");
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
