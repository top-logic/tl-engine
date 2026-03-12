/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents.background;

import com.top_logic.ai.service.agents.background.input.InputConfig;
import com.top_logic.ai.service.agents.background.trigger.TriggerConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;

/**
 * Configuration for a background agent that operates outside of interactive chat sessions.
 *
 * <p>
 * A background agent is defined by its {@link #getScope()}, a {@link #getTrigger()} that determines
 * when it runs, and an {@link #getInput()} that determines what data it processes.
 * </p>
 */
public interface BackgroundConfig extends ConfigurationItem {

	/** @see #getScope() */
	String SCOPE = "scope";

	/** @see #getTrigger() */
	String TRIGGER = "trigger";

	/** @see #getInput() */
	String INPUT = "input";

	/**
	 * Defines the operational scope of the background agent.
	 */
	enum Scope {

		/**
		 * Agent operates within the context of a specific chat session.
		 *
		 * <p>
		 * This is the default scope.
		 * </p>
		 */
		CHAT,

		/**
		 * Agent operates at the application level, independent of any chat session.
		 */
		APPLICATION
	}

	/**
	 * The operational scope of this background agent.
	 *
	 * <p>
	 * Defaults to {@link Scope#CHAT}.
	 * </p>
	 *
	 * @return The scope, never {@code null}.
	 */
	@Name(SCOPE)
	Scope getScope();

	/**
	 * The trigger configuration that determines when this background agent is activated.
	 *
	 * @return The trigger configuration, never {@code null}.
	 */
	@Name(TRIGGER)
	@Mandatory
	TriggerConfig getTrigger();

	/**
	 * The input configuration that determines what data this background agent processes.
	 *
	 * @return The input configuration, never {@code null}.
	 */
	@Name(INPUT)
	@Mandatory
	InputConfig getInput();
}
