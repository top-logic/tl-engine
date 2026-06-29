/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.registry;

import java.util.List;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.tl.ai.core.agent.AgentFactory;
import com.top_logic.tl.ai.core.agent.AgentLoop;
import com.top_logic.tl.ai.core.tools.interaction.DelegationTool;
import com.top_logic.tl.ai.core.tools.interaction.ListAgentsTool;
import com.top_logic.tl.ai.core.tools.interaction.SpawnSubagentTool;
import com.top_logic.tl.ai.core.tools.interaction.WaitForSubagentTool;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.registry.ToolFactory;

/**
 * A {@link ToolFactory} that provides the generic asynchronous-subagent orchestration tools
 * ({@link SpawnSubagentTool spawn_subagent}, {@link ListAgentsTool list_agents} and
 * {@link WaitForSubagentTool wait_for_subagent}, which also reports each subagent's status).
 *
 * <p>
 * These tools are independent of the pre-wired {@link DelegationTool delegation} flow: any agent
 * can select them by name to spawn an arbitrary predefined agent chosen at call time. The target
 * {@link AgentFactory} is resolved lazily per call so the factory can be created before the agent
 * services have started.
 * </p>
 */
public class SubagentToolFactory implements ToolFactory {

	/**
	 * Configuration for {@link SubagentToolFactory}.
	 */
	public interface Config extends PolymorphicConfiguration<SubagentToolFactory> {
		// No configuration properties: the produced tools are fixed.
	}

	/**
	 * Resolves the agent-loop factory lazily, avoiding a hard startup-order dependency on
	 * {@link AgentFactory} when the tool registry initializes.
	 */
	private static final DelegationTool.AgentLoopFactory LOOP_FACTORY = new DelegationTool.AgentLoopFactory() {

		@Override
		public AgentLoop buildAgentLoop(String agentName) {
			return AgentFactory.getInstance().buildAgentLoop(agentName);
		}

		@Override
		public AgentLoop buildSubagentLoop(String agentName) {
			return AgentFactory.getInstance().buildSubagentLoop(agentName);
		}
	};

	/**
	 * Creates a new {@link SubagentToolFactory} from the given configuration.
	 *
	 * @param context
	 *        The instantiation context for error reporting.
	 * @param config
	 *        The configuration.
	 */
	public SubagentToolFactory(InstantiationContext context, Config config) {
		// Nothing to configure.
	}

	@Override
	public List<Tool> createTools() {
		return List.of(
			new SpawnSubagentTool(LOOP_FACTORY),
			new ListAgentsTool(),
			new WaitForSubagentTool());
	}

}
