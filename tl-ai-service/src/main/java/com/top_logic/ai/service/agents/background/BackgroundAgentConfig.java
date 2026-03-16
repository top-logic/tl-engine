/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents.background;

import java.util.List;

import com.top_logic.ai.service.agents.AgentConfig;
import com.top_logic.ai.service.agents.background.input.ChatContextInputNotAllowedForApplicationConstraint;
import com.top_logic.ai.service.agents.background.input.ChatContextInputNotAllowedForPreTurnConstraint;
import com.top_logic.ai.service.agents.background.input.InputConfig;
import com.top_logic.ai.service.agents.background.input.InputOptionsForTrigger;
import com.top_logic.ai.service.agents.background.output.OutputHandler;
import com.top_logic.ai.service.agents.background.output.ScriptOutputHandler;
import com.top_logic.ai.service.agents.background.trigger.ChatTriggerNotAllowedForApplicationConstraint;
import com.top_logic.ai.service.agents.background.trigger.TriggerConfig;
import com.top_logic.ai.service.agents.background.trigger.TriggerOptionsForScope;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.layout.form.values.edit.annotation.Options;

/**
 * Configuration for a background agent that runs automatically in response to certain triggers.
 *
 * <p>
 * A background agent is defined by its {@link #getScope()}, a {@link #getTrigger()} that determines
 * when it runs, an {@link #getInput()} that determines what data it processes, and
 * {@link #getOutputHandlers()} that process the result.
 * </p>
 *
 * <p>
 * A background agent cannot be an entry point or a delegation target. It is triggered independently
 * and runs outside the orchestration chain.
 * </p>
 */
@DisplayOrder({
	AgentConfig.NAME_ATTRIBUTE,
	AgentConfig.MODEL,
	AgentConfig.DESCRIPTION,
	AgentConfig.SYSTEM_PROMPT,
	BackgroundAgentConfig.SCOPE,
	BackgroundAgentConfig.TRIGGER,
	BackgroundAgentConfig.INPUT,
	BackgroundAgentConfig.OUTPUT_HANDLERS,
	AgentConfig.THINKING_TYPE,
	AgentConfig.THINKING_BUDGET_TOKENS,
	AgentConfig.RETURN_THINKING,
	AgentConfig.SEND_THINKING,
	AgentConfig.REASONING_EFFORT,
	AgentConfig.MAX_TOKENS,
	AgentConfig.TEMPERATURE,
	AgentConfig.TOP_P,
	AgentConfig.STOP_SEQUENCES,
	AgentConfig.RESPONSE_FORMAT,
	AgentConfig.TOP_K,
	AgentConfig.DISABLE_PARALLEL_TOOL_USE,
	AgentConfig.PRESENCE_PENALTY,
	AgentConfig.FREQUENCY_PENALTY,
	AgentConfig.SEED,
	AgentConfig.TIMEOUT_SECONDS,
	AgentConfig.MAX_RETRIES,
	AgentConfig.TOOLS,
	AgentConfig.SKILLS,
})
public interface BackgroundAgentConfig extends AgentConfig {

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
	@Label("Scope")
	enum Scope {

		/**
		 * Agent operates at the application level, independent of any chat session.
		 */
		@Label("Application")
		APPLICATION,

		/**
		 * Agent operates within the context of a specific chat session.
		 */
		@Label("Chat")
		CHAT
	}

	/**
	 * The operational scope of this background agent.
	 *
	 * <p>
	 * Defaults to {@link Scope#APPLICATION}.
	 * </p>
	 *
	 * @return The scope, never {@code null}.
	 */
	@Name(SCOPE)
	@Label("Scope")
	Scope getScope();

	/**
	 * The trigger configuration that determines when this background agent is activated.
	 *
	 * <p>
	 * When {@code null}, the background agent is effectively disabled and will not be triggered.
	 * This allows quickly disabling a background agent without removing its configuration.
	 * </p>
	 *
	 * @return The trigger configuration, or {@code null} if the agent is disabled.
	 */
	@Name(TRIGGER)
	@Nullable
	@Label("Trigger")
	@Options(fun = TriggerOptionsForScope.class, args = @Ref(SCOPE))
	@Constraint(value = ChatTriggerNotAllowedForApplicationConstraint.class, args = @Ref(SCOPE))
	TriggerConfig getTrigger();

	/**
	 * Optional additional input configuration for this background agent.
	 *
	 * <p>
	 * For pre-turn triggers, the pending user message is always wrapped in
	 * {@code &lt;user-message&gt;} tags. When an additional input source is configured here, it is
	 * appended in an {@code &lt;additional-context&gt;} block.
	 * </p>
	 *
	 * <p>
	 * For other trigger types, the input configuration is the sole data source for the agent.
	 * <i>Chat context</i> is not available for pre-turn triggers or application-scoped agents.
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
	@Label("Output handlers")
	List<OutputHandler> getOutputHandlers();
}
