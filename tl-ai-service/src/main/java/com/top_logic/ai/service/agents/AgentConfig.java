/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents;

import java.util.List;

import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;

/**
 * Configuration for a single agent in the {@link AgentRegistry}.
 *
 * <p>
 * Each agent is identified by a unique name (provided by {@link NamedConfigMandatory}) and
 * defines the LLM model, optional thinking parameters, system prompt, and available tools
 * and skills.
 * </p>
 */
public interface AgentConfig extends NamedConfigMandatory {

	/** @see #getModel() */
	String MODEL = "model";

	/** @see #getThinkingType() */
	String THINKING_TYPE = "thinking-type";

	/** @see #getThinkingBudgetTokens() */
	String THINKING_BUDGET_TOKENS = "thinking-budget-tokens";

	/** @see #getMaxTokens() */
	String MAX_TOKENS = "max-tokens";

	/** @see #getTemperature() */
	String TEMPERATURE = "temperature";

	/** @see #getReturnThinking() */
	String RETURN_THINKING = "return-thinking";

	/** @see #getSendThinking() */
	String SEND_THINKING = "send-thinking";

	/** @see #getSystemPrompt() */
	String SYSTEM_PROMPT = "system-prompt";

	/** @see #getTools() */
	String TOOLS = "tools";

	/** @see #getSkills() */
	String SKILLS = "skills";

	/**
	 * The LLM model name to use for this agent (e.g. {@code "claude-sonnet-4-20250514"}).
	 */
	@Name(MODEL)
	@Mandatory
	String getModel();

	/**
	 * The thinking type for extended thinking support.
	 *
	 * <p>
	 * Supported values are {@code "adaptive"} (model decides when to think) and
	 * {@code "enabled"} (always think, requires {@link #getThinkingBudgetTokens()}).
	 * </p>
	 *
	 * @return The thinking type, or {@code null} if extended thinking is not used.
	 */
	@Name(THINKING_TYPE)
	@Nullable
	String getThinkingType();

	/**
	 * The maximum number of tokens the model may use for its internal thinking.
	 *
	 * <p>
	 * Required when {@link #getThinkingType()} is {@code "enabled"}.
	 * </p>
	 *
	 * @return The thinking budget in tokens, or {@code null} if not set.
	 */
	@Name(THINKING_BUDGET_TOKENS)
	@Nullable
	Integer getThinkingBudgetTokens();

	/**
	 * The maximum number of output tokens for the model response.
	 *
	 * @return The max output tokens, or {@code null} to use the model default.
	 */
	@Name(MAX_TOKENS)
	@Nullable
	Integer getMaxTokens();

	/**
	 * The sampling temperature for the model (0.0 to 1.0).
	 *
	 * <p>
	 * Lower values make the output more deterministic, higher values make it more creative.
	 * </p>
	 *
	 * @return The temperature, or {@code null} to use the model default.
	 */
	@Name(TEMPERATURE)
	@Nullable
	Double getTemperature();

	/**
	 * Whether to include the model's thinking in the response.
	 *
	 * <p>
	 * When {@code true}, the thinking content is included alongside the response text.
	 * </p>
	 */
	@Name(RETURN_THINKING)
	@BooleanDefault(false)
	boolean getReturnThinking();

	/**
	 * Whether to send previous thinking blocks in follow-up messages.
	 *
	 * <p>
	 * When {@code true}, the thinking content from previous turns is included in the
	 * conversation history sent to the model.
	 * </p>
	 */
	@Name(SEND_THINKING)
	@BooleanDefault(true)
	boolean getSendThinking();

	/**
	 * The system prompt that defines this agent's behavior and instructions.
	 *
	 * <p>
	 * Typically provided as inline CDATA in the XML configuration.
	 * </p>
	 */
	@Name(SYSTEM_PROMPT)
	@Mandatory
	String getSystemPrompt();

	/**
	 * The tool configurations available to this agent.
	 *
	 * <p>
	 * Each entry is a polymorphic configuration that defines a tool class to be instantiated
	 * and made available to the agent during conversation.
	 * </p>
	 */
	@Name(TOOLS)
	List<PolymorphicConfiguration<?>> getTools();

	/**
	 * Comma-separated list of skill IDs available to this agent.
	 *
	 * <p>
	 * Skills are referenced by their registered name in the skill registry.
	 * </p>
	 */
	@Name(SKILLS)
	@Format(CommaSeparatedStrings.class)
	List<String> getSkills();

}
