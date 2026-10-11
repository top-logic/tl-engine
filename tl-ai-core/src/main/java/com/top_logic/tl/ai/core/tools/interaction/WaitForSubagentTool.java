/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.interaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.top_logic.tl.ai.core.agent.ToolContext;
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
			+ "their results. The wait also ends early when a subagent asks you a question. Use this ONLY when the user explicitly asks you to wait for a subagent "
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

		// The returned results are not read: what matters here is the wait and the delivery flag
		// it sets, and the jobs themselves carry everything the answer needs.
		registry.awaitBlocking(ids, timeoutMs);
		if (context instanceof ToolContext toolContext) {
			// What a finished subagent read is now this run's to cite, like the result built on it.
			for (String id : ids) {
				SubagentJob job = registry.get(id);
				if (job.status().isTerminal()) {
					toolContext.readingList().addAll(job.readingList());
				}
			}
		}
		// The wait also ends when a subagent asks this run something; the question is handed over
		// here, so that it is not answered only after the next wait or turn. A waited-for subagent's
		// question stands at that subagent, whether this run was shown it before or not.
		List<SubagentJob> asking = registry.takeUndeliveredQuestions();
		StringBuilder answer = new StringBuilder(format(ids, registry));
		for (SubagentJob job : asking) {
			if (!ids.contains(job.subagentId())) {
				answer.append("\n\n").append(SubagentResultDelivery.question(job));
			}
		}
		return ToolResult.of(answer.toString());
	}

	/**
	 * The waited-for subagents as the parent reads them.
	 *
	 * <p>
	 * Each finished one is rendered by {@link SubagentResultDelivery}, the same formatter the
	 * turn-start injection uses, so a parent reads the identical block whether it asked for the
	 * result or was handed it unasked.
	 * </p>
	 */
	private static String format(List<String> ids, SubagentJobRegistry registry) {
		StringBuilder sb = new StringBuilder();
		for (String id : ids) {
			SubagentJob job = registry.get(id);
			if (job == null) {
				sb.append("### Subagent ").append(id).append("\n_unknown_\n\n");
			} else if (job.status().isTerminal() && job.result() != null) {
				sb.append(SubagentResultDelivery.describe(job)).append("\n\n");
			} else if (job.waitingFor() == SubagentJob.Waiting.LEAD_ANSWER) {
				sb.append(SubagentResultDelivery.question(job)).append("\n\n");
			} else {
				sb.append(SubagentResultDelivery.heading(job))
					.append("\n_still running_\n\n");
			}
		}
		return sb.toString().trim();
	}
}
