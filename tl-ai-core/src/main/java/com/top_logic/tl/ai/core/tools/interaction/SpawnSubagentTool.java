/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.interaction;

import java.util.Map;

import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobRegistry;
import com.top_logic.tl.ai.core.agent.subagent.SubagentRegistryBinding;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.tl.ai.tool.api.ToolSchema;

/**
 * Generic tool for spawning any predefined agent as an asynchronous subagent at runtime.
 *
 * <p>Unlike {@link DelegationTool} (which is pre-wired to a fixed target agent by name),
 * this tool lets the orchestrating agent choose the target at call time. Call
 * {@link ListAgentsTool} first to discover available agents.</p>
 */
public class SpawnSubagentTool implements Tool {

	private static final String INPUT_SCHEMA_JSON = """
		{
			"type": "object",
			"properties": {
				"agent": {
					"type": "string",
					"description": "The name of the predefined agent to spawn. Use list_agents to see available agents."
				},
				"task": {
					"type": "string",
					"description": "What the subagent should do. Be specific."
				},
				"context": {
					"type": "string",
					"description": "Relevant background from the conversation that the subagent needs."
				}
			},
			"required": ["agent", "task"]
		}
		""";

	private final DelegationTool.AgentLoopFactory _loopFactory;

	/**
	 * Creates a spawn-subagent tool.
	 *
	 * @param loopFactory
	 *        Factory for building the target agent's loop.
	 */
	public SpawnSubagentTool(DelegationTool.AgentLoopFactory loopFactory) {
		_loopFactory = loopFactory;
	}

	@Override
	public String name() {
		return "spawn_subagent";
	}

	@Override
	public String description() {
		return "Spawn a background subagent of a chosen predefined agent type to work on a task. "
			+ "Call list_agents first to see available agents. "
			+ "This is FIRE-AND-FORGET: it returns a subagent id immediately and the subagent runs "
			+ "in the background - do NOT wait for it afterwards. End your turn so the user can keep "
			+ "chatting; the subagent's result arrives on its own in a later turn. To answer a status "
			+ "question use subagent_status (non-blocking). Only call wait_for_subagent if the user "
			+ "explicitly asks you to wait for the result.";
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
	 * Like delegation, a subagent is bounded by the child agent's tool list and can never widen the
	 * caller's authority. It is in fact bounded more tightly: the spawn is fire-and-forget, so the
	 * child is an unattended run and the policy's {@code <background-allowlist>} caps it - see
	 * {@link com.top_logic.tl.ai.core.agent.ToolContext#forDetachedSubagent}.
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
		if (!(context instanceof ToolContext toolContext)) {
			return ToolResult.of("ERROR: Unsupported tool execution context for spawn_subagent.");
		}
		if (toolContext.delegationDepth() >= DelegationTool.MAX_DELEGATION_DEPTH) {
			return ToolResult.of("ERROR: Maximum delegation depth (" + DelegationTool.MAX_DELEGATION_DEPTH
				+ ") reached. Complete the task with your own tools.");
		}

		String agentName = (String) args.get("agent");
		if (agentName == null || agentName.isBlank()) {
			return ToolResult.of("ERROR: 'agent' parameter is required. Use list_agents to see available agents.");
		}
		String task = (String) args.get("task");
		if (task == null || task.isBlank()) {
			return ToolResult.of("ERROR: 'task' parameter is required.");
		}
		String contextStr = (String) args.get("context");

		SubagentJobRegistry registry = SubagentRegistryBinding.getRegistry();
		if (registry == null) {
			return ToolResult.of("ERROR: spawn_subagent needs a run recorded under a conversation, and this run has"
				+ " none. Do the task yourself, or with a delegate_to_* tool.");
		}

		return SubagentSpawner.spawnAsync(agentName, task, contextStr, toolContext, _loopFactory, registry);
	}
}
