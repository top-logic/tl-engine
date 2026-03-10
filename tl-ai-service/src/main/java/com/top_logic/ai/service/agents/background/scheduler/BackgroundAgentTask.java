/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents.background.scheduler;

import com.top_logic.ai.service.agents.AgentRegistry;
import com.top_logic.ai.service.agents.background.AgentResult;
import com.top_logic.ai.service.agents.background.BackgroundAgentExecutor;
import com.top_logic.ai.service.agents.background.context.ApplicationContext;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.util.ResKey;
import com.top_logic.util.sched.task.impl.StateHandlingTask;
import com.top_logic.util.sched.task.result.TaskResult.ResultType;

/**
 * TopLogic Scheduler task that bridges scheduled background agents to the
 * {@link BackgroundAgentExecutor}.
 *
 * <p>
 * Configured with an agent name. When the scheduler fires according to the configured schedule,
 * this task creates an {@link ApplicationContext} and executes the background agent pipeline.
 * </p>
 *
 * <p>
 * The result of the agent execution is mapped to a scheduler {@link ResultType}:
 * </p>
 * <ul>
 * <li>{@link AgentResult.Status#PASS PASS} results in {@link ResultType#SUCCESS}</li>
 * <li>{@link AgentResult.Status#ISSUE_FOUND ISSUE_FOUND} results in {@link ResultType#WARNING}</li>
 * <li>{@link AgentResult.Status#BLOCK BLOCK} results in {@link ResultType#WARNING}</li>
 * </ul>
 */
public class BackgroundAgentTask extends StateHandlingTask<BackgroundAgentTask.Config> {

	/**
	 * Configuration for {@link BackgroundAgentTask}.
	 */
	public interface Config extends StateHandlingTask.Config<BackgroundAgentTask> {

		/** @see #getAgentName() */
		String AGENT_NAME = "agent-name";

		/**
		 * The name of the background agent to execute.
		 */
		@Name(AGENT_NAME)
		@Mandatory
		String getAgentName();
	}

	/**
	 * Creates a new {@link BackgroundAgentTask}.
	 */
	@CalledByReflection
	public BackgroundAgentTask(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected void runHook() {
		String agentName = getConfig().getAgentName();
		try {
			AgentRegistry registry = AgentRegistry.getInstance();
			if (registry == null) {
				getLog().taskEnded(ResultType.ERROR,
					ResKey.text("AgentRegistry is not available, cannot execute agent '" + agentName + "'"));
				return;
			}

			BackgroundAgentExecutor executor = new BackgroundAgentExecutor(registry);
			ApplicationContext context = ApplicationContext.scheduled(agentName);
			AgentResult result = executor.execute(agentName, context);

			switch (result.status()) {
				case PASS:
					getLog().taskEnded(ResultType.SUCCESS,
						ResKey.text("Background agent '" + agentName + "' completed: no issues found"));
					break;
				case ISSUE_FOUND:
					getLog().taskEnded(ResultType.WARNING,
						ResKey.text("Background agent '" + agentName + "' found issues: " + result.message()));
					break;
				case BLOCK:
					getLog().taskEnded(ResultType.WARNING,
						ResKey.text("Background agent '" + agentName + "' blocked: " + result.reason()));
					break;
			}
		} catch (Exception ex) {
			Logger.error("Scheduled background agent '" + agentName + "' failed",
				ex, BackgroundAgentTask.class);
			getLog().taskEnded(ResultType.ERROR,
				ResKey.text("Background agent '" + agentName + "' failed: " + ex.getMessage()), ex);
		}
	}
}
