/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents.background;

import java.util.List;

import com.top_logic.ai.service.agents.background.input.InputConfig;
import com.top_logic.ai.service.agents.background.output.OutputHandler;
import com.top_logic.ai.service.agents.background.output.ScriptOutputHandler;
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

	/** @see #getOutputHandlers() */
	String OUTPUT_HANDLERS = "output-handlers";

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

	/**
	 * The output handlers that process the results of this background agent.
	 *
	 * <p>
	 * Handlers receive the raw LLM response and can perform arbitrary processing. Two handler types
	 * are available:
	 * {@link com.top_logic.ai.service.agents.background.output.OutputHandlerImplementation.Config}
	 * for custom Java classes and {@link ScriptOutputHandler} for inline TL-Script.
	 * </p>
	 *
	 * @return The list of output handler configurations, may be empty.
	 */
	@Name(OUTPUT_HANDLERS)
	List<OutputHandler> getOutputHandlers();
}
