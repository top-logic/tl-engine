/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.interaction;

import java.util.Map;

import com.top_logic.tl.ai.core.agent.subagent.SubagentInbox;
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
 * Sends a running background subagent a message: a correction, a new fact, a narrower task.
 *
 * <p>
 * The message lands in the subagent's {@link com.top_logic.tl.ai.core.agent.subagent.SubagentInbox}
 * and is read at the start of its next turn - after the tool call it may be inside. The answer says so,
 * so the lead does not take the message for read the moment it is sent. A subagent that has ended reads
 * nothing any more, and is refused with a sentence.
 * </p>
 *
 * <p>
 * Text only: steering cannot change a subagent's tools, rights or model.
 * </p>
 */
public class SendToSubagentTool implements Tool {

	/** The tool name as exposed to the model. */
	public static final String NAME = "send_to_subagent";

	private static final String INPUT_SCHEMA_JSON = """
		{
			"type": "object",
			"properties": {
				"subagentId": {
					"type": "string",
					"description": "The id of the running subagent, as spawn_subagent returned it (e.g. sa-1)."
				},
				"message": {
					"type": "string",
					"description": "What the subagent should take into account from now on. Write it to the subagent, self-contained."
				}
			},
			"required": ["subagentId", "message"]
		}
		""";

	@Override
	public String name() {
		return NAME;
	}

	@Override
	public String description() {
		return "Send a running background subagent a message - a correction, a new fact, a narrower"
			+ " task. It reads the message at the start of its next turn, which is after the tool call"
			+ " it may be busy with; it does not reply to the message. A subagent that has finished"
			+ " reads no more messages. To stop one, use cancel_subagent.";
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
	 * Talking to a subagent one started is part of the same primitive as starting it.
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
		String message = args.get("message") instanceof String s ? s.strip() : "";
		if (message.isEmpty()) {
			return ToolResult.of("ERROR: 'message' is required and must not be empty.");
		}
		SubagentJob job = registry.get(id);
		if (job == null) {
			return ToolResult.of("ERROR: No such subagent: " + id + ". subagent_status lists the ones you started.");
		}
		if (job.status().isTerminal()
			|| !job.inbox().post(SubagentInbox.Message.of(SubagentInbox.Sender.LEAD, message))) {
			String ended = job.status().isTerminal() ? "has already ended (" + job.status() + ")"
				: "has already written its final answer";
			return ToolResult.of("Not sent: subagent " + id + " " + ended + " and reads no more messages."
				+ " Its result reaches you on its own, or through wait_for_subagent.");
		}
		return ToolResult.of("Sent to subagent " + id + ". It reads the message at the start of its next"
			+ " turn - if it is inside a tool call now, after that call returns. It does not answer the"
			+ " message; you see its effect in subagent_status and in its result.");
	}
}
