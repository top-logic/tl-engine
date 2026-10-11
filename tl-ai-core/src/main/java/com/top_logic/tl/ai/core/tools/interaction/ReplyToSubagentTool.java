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
 * The lead answers the question a background subagent asked it with {@code ask_lead}; the subagent
 * goes on with the answer in the same turn.
 *
 * <p>
 * Refused, with a sentence, when the subagent is not waiting for an answer - it never asked, its
 * question timed out, or it has ended.
 * </p>
 */
public class ReplyToSubagentTool implements Tool {

	/** The tool name as exposed to the model. */
	public static final String NAME = "reply_to_subagent";

	private static final String INPUT_SCHEMA_JSON = """
		{
			"type": "object",
			"properties": {
				"subagentId": {
					"type": "string",
					"description": "The id of the subagent that asked (e.g. sa-1)."
				},
				"answer": {
					"type": "string",
					"description": "Your answer, self-contained. The subagent goes on with it."
				}
			},
			"required": ["subagentId", "answer"]
		}
		""";

	@Override
	public String name() {
		return NAME;
	}

	@Override
	public String description() {
		return "Answer the question a background subagent asked you. It is waiting and goes on with your"
			+ " answer. If you do not know the answer, ask the user with askUser first and then reply.";
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
	 * Answering a subagent one started is part of the same primitive as starting it.
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
		String answer = args.get("answer") instanceof String s ? s.strip() : "";
		if (answer.isEmpty()) {
			return ToolResult.of("ERROR: 'answer' is required and must not be empty.");
		}
		SubagentJob job = registry.get(id);
		if (job == null) {
			return ToolResult.of("ERROR: No such subagent: " + id + ". subagent_status lists the ones you started.");
		}
		if (!job.answerLead(answer)) {
			return ToolResult.of("Not delivered: subagent " + id + " is not waiting for an answer (it asked"
				+ " nothing, its question timed out, or it has ended). To tell it something, use"
				+ " send_to_subagent.");
		}
		return ToolResult.of("Answered subagent " + id + "; it goes on with your answer.");
	}
}
