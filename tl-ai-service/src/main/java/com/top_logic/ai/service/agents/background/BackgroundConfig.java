/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents.background;

import java.util.List;

import com.top_logic.ai.service.agents.background.input.InputConfig;
import com.top_logic.ai.service.agents.background.output.OutputHandler;
import com.top_logic.ai.service.agents.background.trigger.TriggerConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;

/**
 * Configuration for a background agent that operates outside of interactive chat sessions.
 *
 * <p>
 * A background agent is defined by its {@link #getScope()}, {@link #getAuthority()}, a
 * {@link #getTrigger()} that determines when it runs, an {@link #getInput()} that determines what
 * data it processes, and zero or more {@link #getOutputHandlers()} that determine what happens with
 * its results.
 * </p>
 */
public interface BackgroundConfig extends ConfigurationItem {

	/** @see #getScope() */
	String SCOPE = "scope";

	/** @see #getAuthority() */
	String AUTHORITY = "authority";

	/** @see #getNotifyOnBlock() */
	String NOTIFY_ON_BLOCK = "notify-on-block";

	/** @see #getBlockMessage() */
	String BLOCK_MESSAGE = "block-message";

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
	 * Defines how the background agent's decisions should be treated.
	 */
	enum Authority {

		/**
		 * Agent provides recommendations but does not enforce them.
		 *
		 * <p>
		 * This is the default authority level.
		 * </p>
		 */
		ADVISORY,

		/**
		 * Agent's decisions are binding and enforced by the system.
		 */
		AUTHORITATIVE
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
	 * The authority level of this background agent's decisions.
	 *
	 * <p>
	 * Defaults to {@link Authority#ADVISORY}.
	 * </p>
	 *
	 * @return The authority level, never {@code null}.
	 */
	@Name(AUTHORITY)
	Authority getAuthority();

	/**
	 * Whether to notify the user when the agent blocks an action.
	 *
	 * <p>
	 * Only relevant when {@link #getAuthority()} is {@link Authority#AUTHORITATIVE}. Defaults to
	 * {@code true}.
	 * </p>
	 *
	 * @return {@code true} if the user should be notified on block.
	 */
	@Name(NOTIFY_ON_BLOCK)
	@BooleanDefault(true)
	boolean getNotifyOnBlock();

	/**
	 * Optional message to display when the agent blocks an action.
	 *
	 * <p>
	 * Only relevant when {@link #getAuthority()} is {@link Authority#AUTHORITATIVE} and
	 * {@link #getNotifyOnBlock()} is {@code true}. Defaults to an empty string.
	 * </p>
	 *
	 * @return The block message, never {@code null}.
	 */
	@Name(BLOCK_MESSAGE)
	@StringDefault("")
	String getBlockMessage();

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
	 * Multiple output handlers can be configured to write results to different destinations (e.g.,
	 * audit log, scheduler log, notifications).
	 * </p>
	 *
	 * @return The list of output handler configurations, may be empty.
	 */
	@Name(OUTPUT_HANDLERS)
	List<OutputHandler> getOutputHandlers();
}
