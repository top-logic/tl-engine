/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents.background.context;

/**
 * Context for application-scoped background agent execution (scheduled).
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

	/** The source that triggered execution (e.g. "scheduler"). */
	public String getTriggerSource() {
		return _triggerSource;
	}
}
