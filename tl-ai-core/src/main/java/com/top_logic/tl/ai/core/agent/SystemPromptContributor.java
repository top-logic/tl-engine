/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import com.top_logic.tl.ai.core.provider.AgentModelAssignment;

/**
 * Adds a section to the system prompt of every agent run.
 *
 * <p>
 * The seam for a standing instruction that belongs to the installation rather than to one agent -
 * house rules per client, per project or per account. Without it, an application has to write the
 * text into every agent record, which makes the records multiply with the clients, or to put it in
 * front of every user message, which pays for the same text on every single turn.
 * </p>
 *
 * <p>
 * The contribution is part of the system prompt, so the providers' prompt caches carry it: it is
 * charged once for the conversation, not once per turn. That is also its one rule - <b>what a
 * contributor returns may not change within a conversation</b>. The prompt sits in front of every
 * request, and one changed character in it misses the cache on every following turn. A value that
 * does change with the next click belongs in the
 * {@link com.top_logic.tl.ai.core.agent.environment.TurnContext per-turn block}, which travels in the
 * turn; a named value that holds for the conversation is better stated as an
 * {@link com.top_logic.tl.ai.core.agent.environment.EnvironmentFact environment fact}, which is
 * rendered where the model already reads the situation. This interface is for the case neither
 * covers: prose the application wants the model to follow.
 * </p>
 *
 * <p>
 * Configured on {@code LLMService}; an installation that configures none is unchanged. Every
 * contributor is asked once per run, in configuration order, and its text is appended after the
 * generated skill and delegate sections and before the {@code <environment>} block is composed - so a
 * {@code {{...}}} placeholder written into the contributed text is filled like one in the agent's own
 * prompt.
 * </p>
 *
 * @see AgentFactory#buildConfig
 */
public interface SystemPromptContributor {

	/**
	 * The text to add to this run's system prompt.
	 *
	 * <p>
	 * Called on the thread of the account the run acts for, so an implementation reads that account
	 * from {@code TLContext} as any other application code does.
	 * </p>
	 *
	 * @param agent
	 *        The agent this run is. Never <code>null</code>.
	 * @param assignment
	 *        The model the run talks to and what it was configured with. Never <code>null</code>.
	 * @return The text, or <code>null</code> or blank to contribute nothing to this run.
	 */
	String contribute(AgentDefinition agent, AgentModelAssignment assignment);

}
