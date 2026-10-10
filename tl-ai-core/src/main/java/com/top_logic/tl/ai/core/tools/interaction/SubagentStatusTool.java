/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.interaction;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.top_logic.tl.ai.core.agent.subagent.SubagentJob;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobRegistry;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobStatus;
import com.top_logic.tl.ai.core.agent.subagent.SubagentProgress;
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
 * <p>Reports each subagent's current lifecycle state - for a running one what it does, from its
 * {@link SubagentProgress}, and whether it waits for somebody; its result once terminal - and
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
				},
				"detail": {
					"type": "string",
					"enum": ["summary", "steps"],
					"description": "'summary' (default): state, turns, tokens, elapsed time, the current step and the subagent's last text. 'steps': additionally its recent tool calls."
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
			+ "Returns each subagent's state (queued/running/waiting/done/failed/cancelled), what a "
			+ "running one is doing - its current step and the last text it wrote, with detail=steps "
			+ "also its recent tool calls - and, once finished, its result. A waiting subagent is "
			+ "parked on the user's answer or an approval. Returns immediately - use this to answer a "
			+ "status question (\"how is it going?\"). To block until a subagent finishes, use "
			+ "wait_for_subagent instead.";
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

		boolean steps = "steps".equals(args.get("detail"));
		return ToolResult.of(format(ids, registry, steps));
	}

	private static String format(List<String> ids, SubagentJobRegistry registry, boolean steps) {
		StringBuilder sb = new StringBuilder();
		for (String id : ids) {
			SubagentJob job = registry.get(id);
			if (job == null) {
				sb.append("### Subagent ").append(id).append("\n_no such subagent_\n\n");
				continue;
			}
			if (job.status().isTerminal() && job.result() != null) {
				// The same block the delivery paths render, report marked as data included.
				sb.append(SubagentResultDelivery.describe(job)).append('\n');
				if (steps) {
					appendProgress(sb, job, true, false);
				}
			} else if (job.status() == SubagentJobStatus.QUEUED) {
				sb.append(SubagentResultDelivery.heading(job)).append("\n_not started yet_\n");
			} else {
				appendRunning(sb, job, steps);
			}
			sb.append('\n');
		}
		return sb.toString().trim();
	}

	/**
	 * A running subagent: its state - {@code WAITING} with the reason while it is parked - and what
	 * the framework counted, then what it did and wrote, which the child produced and which is
	 * therefore marked as data.
	 */
	private static void appendRunning(StringBuilder sb, SubagentJob job, boolean steps) {
		SubagentJob.Waiting waiting = job.waitingFor();
		sb.append("### Subagent ").append(job.subagentId()).append(" (").append(job.targetAgentName())
			.append(", ");
		if (waiting == null) {
			sb.append(job.status());
		} else {
			sb.append("WAITING - ").append(waitingText(waiting));
		}
		sb.append(")\n");
		SubagentProgress progress = job.progress();
		long elapsedSeconds = Duration.between(job.startedAt(), Instant.now()).toSeconds();
		sb.append("_turns: ").append(progress.turns())
			.append(", tokens: ").append(progress.tokens())
			.append(", elapsed: ").append(elapsedSeconds).append("s_\n");
		appendProgress(sb, job, steps, true);
	}

	private static String waitingText(SubagentJob.Waiting waiting) {
		return switch (waiting) {
			case USER_ANSWER -> "for the user's answer to its question";
			case APPROVAL -> "for the user's approval of a tool call";
		};
	}

	/**
	 * The child's own part of the status - its current step, its recent calls and its last text - in
	 * one {@link com.top_logic.tl.ai.tool.api.UntrustedEnvelope#SUBAGENT_REPORT} block: call
	 * arguments and text are what the child wrote, and may repeat what it read.
	 */
	private static void appendProgress(StringBuilder sb, SubagentJob job, boolean steps, boolean current) {
		SubagentProgress progress = job.progress();
		StringBuilder body = new StringBuilder();
		SubagentProgress.Step step = progress.currentStep();
		if (current) {
			body.append("Current step: ").append(step == null ? "none yet" : describe(step)).append('\n');
		}
		if (steps) {
			List<SubagentProgress.Step> recent = progress.steps();
			body.append("Recent tool calls (last ").append(SubagentProgress.MAX_STEPS).append(", oldest first):");
			if (recent.isEmpty()) {
				body.append(" none");
			}
			body.append('\n');
			for (SubagentProgress.Step each : recent) {
				body.append("- turn ").append(each.turn()).append(": ").append(describe(each)).append('\n');
			}
		}
		if (current) {
			String text = progress.lastText();
			body.append("Last text: ").append(text.isEmpty() ? "none yet" : text);
		}
		if (body.isEmpty()) {
			return;
		}
		sb.append(SubagentResultDelivery.report(
			"subagent " + job.subagentId() + " (" + job.targetAgentName() + ")", body.toString().strip()))
			.append('\n');
	}

	private static String describe(SubagentProgress.Step step) {
		return step.toolName() + " " + step.arguments() + " - " + step.outcome().name().toLowerCase();
	}
}
