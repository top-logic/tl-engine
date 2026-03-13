/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents.background;

import java.util.List;

import com.top_logic.ai.service.agents.background.input.ChatContextInputNotAllowedForApplicationConstraint;
import com.top_logic.ai.service.agents.background.input.ChatContextInputNotAllowedForPreTurnConstraint;
import com.top_logic.ai.service.agents.background.input.InputConfig;
import com.top_logic.ai.service.agents.background.input.InputOptionsForTrigger;
import com.top_logic.ai.service.agents.background.output.OutputHandler;
import com.top_logic.ai.service.agents.background.output.ScriptOutputHandler;
import com.top_logic.ai.service.agents.background.trigger.ChatTriggerNotAllowedForApplicationConstraint;
import com.top_logic.ai.service.agents.background.trigger.TriggerConfig;
import com.top_logic.ai.service.agents.background.trigger.TriggerOptionsForScope;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.layout.form.values.edit.annotation.Options;

/**
 * Configuration for a background agent that operates outside of interactive chat sessions.
 *
 * <p>
 * A background agent is defined by its {@link #getScope()}, a {@link #getTrigger()} that determines
 * when it runs, and an {@link #getInput()} that determines what data it processes.
 * </p>
 */
@DisplayOrder({
	BackgroundConfig.SCOPE,
	BackgroundConfig.TRIGGER,
	BackgroundConfig.INPUT,
	BackgroundConfig.OUTPUT_HANDLERS
})
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
	@Options(fun = TriggerOptionsForScope.class, args = @Ref(SCOPE))
	@Constraint(value = ChatTriggerNotAllowedForApplicationConstraint.class, args = @Ref(SCOPE))
	TriggerConfig getTrigger();

	/**
	 * Optional additional input configuration for this background agent.
	 *
	 * <p>
	 * For pre-turn triggers, the pending user message is always included. When an additional input
	 * source is configured here, the agent receives both parts wrapped in XML tags
	 * ({@code <user-message>} and {@code <additional-context>}). When no input is configured, the
	 * agent receives the raw user message without tags.
	 * </p>
	 *
	 * <p>
	 * For other trigger types, the input configuration is the sole data source for the agent.
	 * {@link com.top_logic.ai.service.agents.background.input.ChatContextInput ChatContextInput}
	 * is not available for pre-turn triggers or application-scoped agents.
	 * </p>
	 *
	 * @return The input configuration, or {@code null} to use the default for the trigger type.
	 */
	@Name(INPUT)
	@Nullable
	@Options(fun = InputOptionsForTrigger.class, args = { @Ref(TRIGGER), @Ref(SCOPE) })
	@Constraint(value = ChatContextInputNotAllowedForPreTurnConstraint.class, args = @Ref(TRIGGER))
	@Constraint(value = ChatContextInputNotAllowedForApplicationConstraint.class, args = @Ref(SCOPE))
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
