/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents.background.context;

/**
 * Context for application-scoped background agent execution (scheduled, on-demand).
 */
public class ApplicationContext extends BackgroundContext {

	private final String _triggerSource;

	private ApplicationContext(String agentName, String triggerType, String triggerSource) {
		super(agentName, triggerType);
		_triggerSource = triggerSource;
	}

	/**
	 * Creates a context for scheduled execution.
	 */
	public static ApplicationContext scheduled(String agentName) {
		return new ApplicationContext(agentName, "scheduled", "scheduler");
	}

	/**
	 * Creates a context for manual/on-demand execution.
	 *
	 * @param userName
	 *        the user who triggered the execution
	 */
	public static ApplicationContext manual(String agentName, String userName) {
		return new ApplicationContext(agentName, "on-demand", "user:" + userName);
	}

	/** The source that triggered execution (e.g. "scheduler", "user:admin"). */
	public String getTriggerSource() {
		return _triggerSource;
	}
}
