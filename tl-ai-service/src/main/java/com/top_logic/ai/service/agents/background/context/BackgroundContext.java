/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents.background.context;

import java.time.Instant;

/**
 * Base context for background agent execution.
 *
 * <p>
 * Carries the agent name, trigger type, and timestamp for every background agent invocation.
 * </p>
 */
public class BackgroundContext {

	private final String _agentName;

	private final String _triggerType;

	private final Instant _timestamp;

	/**
	 * Creates a new {@link BackgroundContext}.
	 *
	 * @param agentName
	 *        the name of the background agent
	 * @param triggerType
	 *        the type of trigger that fired (e.g. "post-turn", "scheduled")
	 */
	public BackgroundContext(String agentName, String triggerType) {
		_agentName = agentName;
		_triggerType = triggerType;
		_timestamp = Instant.now();
	}

	/** The name of the background agent being executed. */
	public String getAgentName() {
		return _agentName;
	}

	/** The type of trigger that started this execution. */
	public String getTriggerType() {
		return _triggerType;
	}

	/** When this execution started. */
	public Instant getTimestamp() {
		return _timestamp;
	}
}
