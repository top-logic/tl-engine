/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.core.security;

import java.util.Objects;

import com.top_logic.knowledge.wrap.person.Person;

/**
 * Who a run acts as, and under what circumstances.
 *
 * <p>
 * Everything the entry point knows at the moment the run's
 * {@link com.top_logic.ai.core.agent.SecurityContext} is built, which is before the run has an id
 * or an agent: those are settled afterwards, so a factory that needed them would be asked too early.
 * What a factory does get is the account, how the run was started, whether anybody approves a tool
 * call of the run, and - for the entry points that have one - which conversation the run belongs to.
 * </p>
 *
 * @param account
 *        The authenticated account the run acts as. Never <code>null</code>: a run that cannot name
 *        its principal is refused where its context is built.
 * @param entryPoint
 *        How the run was started. Never <code>null</code>.
 * @param background
 *        Whether nobody approves a tool call of the run. Such a run is additionally bounded by the
 *        tool access policy's {@code <background-allowlist>}. It says nothing about questions:
 *        whether somebody answers them is {@link com.top_logic.ai.core.agent.ToolContext#attended()}.
 *        An A2A run is both: the calling agent answers its questions but approves no tool call.
 * @param conversationId
 *        The chat session the run belongs to, or <code>null</code> for a run that is a conversation
 *        of its own. Lets an application remember a decision for the conversation the user made it
 *        in.
 * @see SecurityContextFactory
 */
public record RunPrincipal(Person account, RunEntryPoint entryPoint, boolean background,
		String conversationId) {

	/** */
	public RunPrincipal {
		Objects.requireNonNull(entryPoint, "The entry point of a run must be stated.");
	}

	/**
	 * A run that belongs to no conversation.
	 *
	 * @param account
	 *        The account the run acts as.
	 * @param entryPoint
	 *        How the run was started.
	 * @param background
	 *        Whether nobody approves a tool call of the run.
	 */
	public RunPrincipal(Person account, RunEntryPoint entryPoint, boolean background) {
		this(account, entryPoint, background, null);
	}
}
