/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents;

import java.util.List;

import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.layout.form.values.edit.annotation.Options;

/**
 * Configuration for an agent that participates in the chat orchestration chain.
 *
 * <p>
 * A chat agent can serve as the {@link #isEntryPoint() entry point} that receives user messages, or
 * as a specialist that other agents {@link #getDelegations() delegate} to.
 * </p>
 */
@DisplayOrder({
	AgentConfig.NAME_ATTRIBUTE,
	ChatAgentConfig.ENTRY_POINT,
	AgentConfig.MODEL,
	AgentConfig.DESCRIPTION,
	AgentConfig.SYSTEM_PROMPT,
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
	ChatAgentConfig.DELEGATIONS,
	AgentConfig.SKILLS,
})
public interface ChatAgentConfig extends AgentConfig {

	/** @see #isEntryPoint() */
	String ENTRY_POINT = "entry-point";

	/** @see #getDelegations() */
	String DELEGATIONS = "delegations";

	/**
	 * Whether this agent is the chat entry point.
	 *
	 * <p>
	 * Exactly one agent in the {@link AgentRegistry} must be marked as the entry point. The chat
	 * system uses this agent to handle incoming user messages.
	 * </p>
	 */
	@Name(ENTRY_POINT)
	@BooleanDefault(false)
	boolean isEntryPoint();

	/**
	 * Other agents this agent can delegate to, selected by name.
	 *
	 * <p>
	 * For each target agent name listed here, a dedicated {@code delegate_to_<name>} tool with a
	 * self-describing {@link com.top_logic.tl.ai.core.provider.ToolSchema} is created at runtime by
	 * the {@link AgentRegistry}.
	 * </p>
	 */
	@Name(DELEGATIONS)
	@Format(CommaSeparatedStrings.class)
	@Options(fun = AllChatAgentNames.class)
	List<String> getDelegations();
}
