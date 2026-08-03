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
import com.top_logic.tl.ai.core.agent.subagent.SubagentResult;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.tl.ai.tool.api.ToolSchema;

/**
 * Blocks the parent run until the named asynchronous subagents finish, then returns their
 * result envelopes. With no {@code subagentIds}, waits for all outstanding (non-delivered)
 * subagents. The join half of the async-delegation hybrid.
 */
public class WaitForSubagentTool implements Tool {

	/** The tool name as exposed to the model. */
	public static final String NAME = "wait_for_subagent";

	/** Default wait timeout in milliseconds. */
	public static final long DEFAULT_TIMEOUT_MS = 300_000L;

	private static final String INPUT_SCHEMA_JSON = """
		{
			"type": "object",
			"properties": {
				"subagentIds": {
					"type": "array",
					"items": { "type": "string" },
					"description": "Subagent ids to wait for. Omit or leave empty to wait for all outstanding subagents."
				},
				"timeoutMs": {
					"type": "integer",
					"description": "Maximum time to wait in milliseconds (default 300000)."
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
		return "BLOCK the current turn until one or more background subagents finish, then return "
			+ "their results. Use this ONLY when the user explicitly asks you to wait for a subagent "
			+ "(e.g. \"wait for it and then summarize\"). Do NOT call it right after spawning: a spawn "
			+ "is fire-and-forget and results arrive on their own in a later turn. For a status "
			+ "question use subagent_status, which does not block.";
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
	 * Awaiting a subagent one already started is part of the same primitive.
	 * </p>
	 */
	@Override
	public String group() {
		return ToolGroups.INTERACTION;
	}

	@Override
	public boolean isConcurrencySafe() {
		return false;
	}

	@Override
	@SuppressWarnings("unchecked")
	public ToolResult execute(Map<String, Object> args, ToolExecutionContext context) {
		SubagentJobRegistry registry = SubagentRegistryBinding.getRegistry();
		if (registry == null) {
			return ToolResult.of("ERROR: No subagent session is active.");
		}

		long timeoutMs = DEFAULT_TIMEOUT_MS;
		Object t = args.get("timeoutMs");
		if (t instanceof Number n) {
			timeoutMs = n.longValue();
		}

		List<String> ids = new ArrayList<>();
		Object raw = args.get("subagentIds");
		if (raw instanceof List<?> list && !list.isEmpty()) {
			for (Object o : list) {
				ids.add(String.valueOf(o));
			}
		} else {
			// All outstanding (running or terminal-but-undelivered).
			for (SubagentJob job : registry.byStatus(
					SubagentJobStatus.QUEUED, SubagentJobStatus.RUNNING)) {
				ids.add(job.subagentId());
			}
			for (SubagentJob job : registry.byStatus(
					SubagentJobStatus.DONE, SubagentJobStatus.FAILED, SubagentJobStatus.CANCELLED)) {
				if (!job.isDelivered()) {
					ids.add(job.subagentId());
				}
			}
		}

		if (ids.isEmpty()) {
			return ToolResult.of("No outstanding subagents to wait for.");
		}

		List<String> unknown = new ArrayList<>();
		for (String id : ids) {
			if (registry.get(id) == null) {
				unknown.add(id);
			}
		}
		if (!unknown.isEmpty()) {
			return ToolResult.of("ERROR: No such subagent(s): " + String.join(", ", unknown));
		}

		List<SubagentResult> results = registry.awaitBlocking(ids, timeoutMs);
		return ToolResult.of(format(ids, results, registry));
	}

	private static String format(List<String> ids, List<SubagentResult> results,
			SubagentJobRegistry registry) {
		StringBuilder sb = new StringBuilder();
		for (String id : ids) {
			SubagentJob job = registry.get(id);
			sb.append("### Subagent ").append(id);
			if (job != null) {
				sb.append(" (").append(job.targetAgentName()).append(", ")
					.append(job.status()).append(")");
			}
			sb.append("\n");
			if (job != null && job.status().isTerminal() && job.result() != null) {
				SubagentResult r = job.result();
				sb.append(r.finalText()).append("\n");
				sb.append("_tokens: ").append(r.tokenUsage().totalTokens())
					.append(", duration: ").append(r.durationMs()).append("ms, tools: ")
					.append(r.toolCallCount()).append("_\n\n");
			} else {
				sb.append("_still running (wait timed out)_\n\n");
			}
		}
		return sb.toString().trim();
	}
}
