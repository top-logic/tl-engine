/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.interaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.top_logic.tl.ai.core.agent.subagent.SubagentJob;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobRegistry;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobStatus;
import com.top_logic.tl.ai.core.agent.subagent.SubagentRegistryBinding;
import com.top_logic.tl.ai.core.agent.subagent.SubagentResultDelivery;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.tl.ai.tool.api.ToolSchema;

/**
 * Non-blocking status probe for asynchronous subagents.
 *
 * <p>Reports each subagent's current lifecycle state - and its result once terminal - and
 * returns immediately without waiting. Unlike {@link WaitForSubagentTool}, this never blocks
 * the parent turn, so it is the right tool to answer a "how is it going?" question. It also
 * does <em>not</em> mark any result delivered: a finished subagent's result still arrives on
 * its own in a later parent turn (or via {@code wait_for_subagent}).</p>
 */
public class SubagentStatusTool implements Tool {

	/** The tool name as exposed to the model. */
	public static final String NAME = "subagent_status";

	private static final String INPUT_SCHEMA_JSON = """
		{
			"type": "object",
			"properties": {
				"subagentIds": {
					"type": "array",
					"items": { "type": "string" },
					"description": "Subagent ids to report on. Omit or leave empty to report on all subagents."
				}
			}
		}
		""";

	@Override
	public String name() {
		return NAME;
	}

	@Override
	public String description() {
		return "Check the current status of background subagents WITHOUT blocking. "
			+ "Returns each subagent's state (queued/running/done/failed/cancelled) and, once "
			+ "finished, its result. Returns immediately - use this to answer a status question "
			+ "(\"how is it going?\"). To block until a subagent finishes, use wait_for_subagent instead.";
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
	 * Observing a subagent one already started is part of the same primitive.
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
	@SuppressWarnings("unchecked")
	public ToolResult execute(Map<String, Object> args, ToolExecutionContext context) {
		SubagentJobRegistry registry = SubagentRegistryBinding.getRegistry();
		if (registry == null) {
			return ToolResult.of("ERROR: No subagent session is active.");
		}

		List<String> ids = new ArrayList<>();
		Object raw = args.get("subagentIds");
		if (raw instanceof List<?> list && !list.isEmpty()) {
			for (Object o : list) {
				ids.add(String.valueOf(o));
			}
		} else {
			for (SubagentJob job : registry.byStatus(
					SubagentJobStatus.QUEUED, SubagentJobStatus.RUNNING,
					SubagentJobStatus.DONE, SubagentJobStatus.FAILED, SubagentJobStatus.CANCELLED)) {
				ids.add(job.subagentId());
			}
		}

		if (ids.isEmpty()) {
			return ToolResult.of("No subagents have been spawned yet.");
		}

		return ToolResult.of(format(ids, registry));
	}

	private static String format(List<String> ids, SubagentJobRegistry registry) {
		StringBuilder sb = new StringBuilder();
		for (String id : ids) {
			SubagentJob job = registry.get(id);
			if (job == null) {
				sb.append("### Subagent ").append(id).append("\n_no such subagent_\n\n");
				continue;
			}
			if (job.status().isTerminal() && job.result() != null) {
				// The same block the delivery paths render, report marked as data included.
				sb.append(SubagentResultDelivery.describe(job)).append("\n\n");
			} else {
				sb.append(SubagentResultDelivery.heading(job)).append("\n_still running_\n\n");
			}
		}
		return sb.toString().trim();
	}
}
