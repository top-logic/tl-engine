/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents.background.input;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;

/**
 * Input configuration that uses a fixed trigger message.
 *
 * <p>
 * The background agent is invoked with a static message defined at configuration time. This is
 * useful for autonomous agents that perform a predefined task without requiring dynamic context.
 * </p>
 */
public interface AutonomousInput extends InputConfig {

	/** @see #getTriggerMessage() */
	String TRIGGER_MESSAGE = "trigger-message";

	/**
	 * The fixed message that is passed to the background agent as input.
	 *
	 * @return The trigger message, never {@code null}.
	 */
	@Name(TRIGGER_MESSAGE)
	@Mandatory
	String getTriggerMessage();
}
