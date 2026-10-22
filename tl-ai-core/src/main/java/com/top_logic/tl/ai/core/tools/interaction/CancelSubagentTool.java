/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.interaction;

import java.util.Map;

import com.top_logic.tl.ai.core.agent.subagent.SubagentJob;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobRegistry;
import com.top_logic.tl.ai.core.agent.subagent.SubagentRegistryBinding;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.tl.ai.tool.api.ToolSchema;

/**
 * Stops one background subagent - the lead's counterpart of the Stop the user has in the chat.
 *
 * <p>
 * The subagent ends {@code CANCELLED} at its next cooperative check, a question it is parked on
 * included, and its result names the reason given here. A subagent that has already ended is
 * reported as such rather than refused: stopping it is what the lead wanted, and it is stopped.
 * </p>
 */
public class CancelSubagentTool implements Tool {

	/** The tool name as exposed to the model. */
	public static final String NAME = "cancel_subagent";

	private static final String INPUT_SCHEMA_JSON = """
		{
			"type": "object",
			"properties": {
				"subagentId": {
					"type": "string",
					"description": "The id of the subagent to stop, as spawn_subagent returned it (e.g. sa-1)."
				},
				"reason": {
					"type": "string",
					"description": "Why it is stopped, in one sentence; its result names it."
				}
			},
			"required": ["subagentId"]
		}
		""";

	@Override
	public String name() {
		return NAME;
	}

	@Override
	public String description() {
		return "Stop one background subagent you started, for example because its task is no longer"
			+ " needed or it is working on the wrong thing. It ends as cancelled and does not finish its"
			+ " task. To correct a subagent rather than stop it, use send_to_subagent.";
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
	 * Stopping a subagent one started is part of the same primitive as starting it, and changes no
	 * data: whatever the subagent changed before it is stopped stays as its own turns committed it.
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
		SubagentJobRegistry registry = SubagentRegistryBinding.getRegistry();
		if (registry == null) {
			return ToolResult.of("ERROR: No subagent session is active.");
		}
		String id = args.get("subagentId") instanceof String s ? s.strip() : "";
		String reason = args.get("reason") instanceof String s ? s.strip() : "";
		SubagentJob job = registry.get(id);
		if (job == null) {
			return ToolResult.of("ERROR: No such subagent: " + id + ". subagent_status lists the ones you started.");
		}
		if (job.status().isTerminal()) {
			return ToolResult.of("Subagent " + id + " has already ended (" + job.status() + "); nothing to stop.");
		}
		job.cancel(reason.isEmpty() ? null : reason);
		return ToolResult.of("Stopping subagent " + id + ". It ends as cancelled at its next check and"
			+ " will not finish its task.");
	}
}
